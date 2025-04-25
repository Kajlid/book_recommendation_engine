/*  
 *  This class sets the score for a book as the weighted TF-IDF score, considering title, description and genres as well as user information. 
 *   
 * 
 *   
 */  

package com.book_engine.app;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.TermvectorsResponse;
import co.elastic.clients.elasticsearch.core.termvectors.Term;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;


public class BookRecommender {
    // Title contributes more to the search results
    private static double titleWeight = 0.5;      // title weight
    private static double genreWeight = 0.3;         // genre weight
    private static double descriptionWeight = 0.2;        // description weight

    /**
     * Boost some random books with this weight
     */
    private static double randomBoost = 0.01;   

    private static final Random random = new Random();

    // TODO: add more users
    static User user;
    static {
        user = new User("temp_user");
        user.books.put("twelfth_night_william_shakespeare", 4.5f);
        user.books.put("how_to_teach_a_slug_to_read_susan_pearson", 3.2f);
    }
    

    private static final RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    private static final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static final ElasticsearchClient client = new ElasticsearchClient(transport);
    private Database database;
    
    public BookRecommender(Database database){
        this.database = database;
    }
    
    
    public void setBookScore(String query, ArrayList<Book> bookList) throws IOException {
        try {
            for (Book b : bookList) {
                setScore(b, query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setScore(Book b, String query) throws IOException {
        double title_score = 0;
        double genre_score = 0;
        double description_score = 0;
        double preference_bonus = 0;   // user preference bonus
        
        String normalizedQuery = query.toLowerCase().trim();
        String normalizedTitle = b.title.toLowerCase().trim();
            
        List<String> words = Arrays.asList(normalizedQuery.split("\\s+"));
        List<String> titleToList = Arrays.asList(normalizedTitle.split("\\s+"));
        
        ArrayList<String> genres = b.genres;
        
        for (String word: words) {
            // Set title_score 
            boolean found_title = titleToList.stream().anyMatch(w -> w.contains(word));
            if (found_title) {
                title_score += 1;
            }

            // If exact title match, boost score
            if (normalizedTitle.equals(normalizedQuery)) {
                title_score += 5; 
            }

            boolean found_genre = genres.stream().anyMatch(w -> w.toLowerCase().contains(word.toLowerCase()));
            if (found_genre) {
                genre_score += 1;
            }

            description_score += getTFIDFScore(database.indexName, b.id, word);
        }

        // ---- User influence bonus ----
        for (Map.Entry<String, Float> entry : user.books.entrySet()) {
            if (entry.getValue() >= 4.0) {   // Only consider books user rated relatively high
                Book readBook = database.getBookByID(entry.getKey());   
                if (readBook != null) {
                    String readTitle = readBook.title.toLowerCase();
                    ArrayList readGenres = readBook.genres;
 
                    // Small bonus for title containing similar words as liked book
                    for (String titleWord : readTitle.split("\\s+")) {
                        if (normalizedTitle.contains(titleWord)) {
                            preference_bonus += 0.1;  
                        }
                    }

                    
                    // Small bonus for books with similar genres as user's read and liked books with that genre
                    for (String genre : b.genres) {
                        if (readGenres.contains(genre.toLowerCase().trim())) {
                            preference_bonus += 0.1;
                        }
                    }

                }
            }
        }


        double total_score = titleWeight * title_score + genreWeight * genre_score + descriptionWeight * description_score + preference_bonus;

        b.score = total_score;
    }

    private double getTFIDFScore(String indexName, String docId, String term) throws IOException {
        /** 
         * Get term vectors from Elasticsearch for the specified document and field
         * */ 
        TermvectorsResponse response = client.termvectors(tv -> tv
            .index(indexName)
            .id(docId)
            .termStatistics(true)  
            .fields("description") 
            .positions(false)     
            .offsets(false)        
            .payloads(false)      
        );
    
        /** 
         * Get total document count in the index
         * */ 
        co.elastic.clients.elasticsearch.core.CountResponse countResponse = client.count(c -> c.index(indexName));
        long totalDocs = countResponse.count();
    
        /** 
         * Access term vector for the "description" field
         * */ 
        co.elastic.clients.elasticsearch.core.termvectors.TermVector tv = response.termVectors().get("description");
        if (tv == null || tv.terms() == null || !tv.terms().containsKey(term)) {
            return 0.0;  // Term not found in the document or term vectors missing
        }
        
        /** 
         * Access term stats (TF, DF) for the specified term
         * */ 
        Term stats = tv.terms().get(term);
        int tf = stats.termFreq();  
        int df = stats.docFreq();   
    
        double idf = Math.log((double) (totalDocs + 1) / (df + 1)) + 1;
    
        return tf * idf;
    }
    
    public ArrayList<Book> search(String query) throws IOException {
        ArrayList<Book> retrievedBooks = database.getDataForQuery(query);
        ArrayList<Book> relevantBooks = new ArrayList<>();

        /** 
         * Remove book from recommendations if the user has read it already
         * */ 
        for (Book b : retrievedBooks) {
            if (!user.books.containsKey(b.id)) {
                relevantBooks.add(b); 
            }
        } 

        for (Book b : relevantBooks) {
            setScore(b, query);
        }

        /** 
         * Add some random books for diversification of recommendations
         * */  
        int numRandomBooks = 3;
        double minRating = 4.0;

        ArrayList<Book> randomBooks = database.getRandomBooks(numRandomBooks, minRating);
        randomBooks.sort((b1, b2) -> Double.compare(b2.rating, b1.rating));    // Sort the random books by rating
        for (Book rb : randomBooks) {
            boolean alreadyIncluded = relevantBooks.stream().anyMatch(b -> b.id.equals(rb.id));
            if (!alreadyIncluded) {
                rb.score = random.nextDouble() * randomBoost; 
                relevantBooks.add(rb);
            }
        }

        /** 
         * Sort books by score in descending order
         * */  
        Collections.sort(relevantBooks);
        
        return relevantBooks;
        
    }

}