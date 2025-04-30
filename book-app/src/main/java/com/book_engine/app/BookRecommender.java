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
import java.util.HashMap;
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
    private static double titleWeight = 0.4;      // title weight
    private static double genreWeight = 0.25;         // genre weight
    private static double descriptionWeight = 0.2;        // description weight
    private static double authorWeight = 0.15;

    // TODO: add more users
    static User user;
    static {
        user = new User("harry_fan");
        user.books.put("Last Mission_Jack_Everett", 3.2f);
    }

    static User user2;
    static {
        user2 = new User("anna932");
        user2.books.put("twelfth_night_william_shakespeare", 4.5f);
        user2.books.put("how_to_teach_a_slug_to_read_susan_pearson", 3.2f);
    }

    static User user3;
    static {
        user3 = new User("bla");
        user3.books.put("twelfth_night_william_shakespeare", 4.5f);
        user3.books.put("The_Ant-Man_of_Malfen_Derek_Prior", 3.2f);
    }

    static User user4;
    static {
        user4 = new User("yer_a_wizard");
        user4.books.put("Father_Unknown_Lesley_Pearse", 3.2f);
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
        double author_score = 0; 
        double preference_bonus = 0;   // user preference bonus

        double rating_boost = 0;
        if (b.average_rating != null) {
            try {
                rating_boost = Math.abs(Double.parseDouble(b.average_rating)- 1.0);     // highly rated books should get a higher score
            }
            catch (Exception e) {
                rating_boost = 0;
            }
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        String normalizedTitle = b.title.toLowerCase().trim();
        String normalizedAuthor = b.author.toLowerCase().trim();
            
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

            if (normalizedAuthor.contains(word)) {
                author_score += 1;
            }
            
            // If exact author match, boost score
            if (normalizedAuthor.equals(normalizedQuery)) {
                author_score += 5;
            }

            description_score += getTFIDFScore(database.indexName, b.id, word);
        }

        // User influence bonus (based on personal preferences)
        for (Map.Entry<String, Float> entry : user.books.entrySet()) {
            if (entry.getValue() >= 4.0) {   // Only consider books user rated relatively high
                Book readBook = database.getBookByID(entry.getKey());   
                if (readBook != null) {
                    String readTitle = readBook.title.toLowerCase();
                    ArrayList readGenres = readBook.genres;
                    String readAuthor = readBook.author.toLowerCase();
 
                    // Small bonus for title containing similar words as liked book
                    for (String titleWord : readTitle.split("\\s+")) {
                        if (normalizedTitle.contains(titleWord)) {
                            preference_bonus += 0.05;  
                        }
                    }

                    // Small bonus for books with the same author as the read and liked book
                    if (normalizedAuthor.equals(readAuthor)) {
                        preference_bonus += 0.1;
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

        // Collaborative filtering preference bonus (find similar users and their read books)
        List<User> similarUsers = findSimilarUsers(user, Arrays.asList(user2, user3, user4), 3);
        for (User similarUser : similarUsers) {
            for (Map.Entry<String, Float> entry : similarUser.books.entrySet()) {
                if (entry.getValue() >= 4.0) {
                    Book likedBook = database.getBookByID(entry.getKey());
                    if (likedBook != null) {
                        // If a similar user has read and liked books of specific genres or authors, boost books of those genres and authors
                        for (String genre : b.genres) {
                            if (likedBook.genres.contains(genre.toLowerCase().trim())) {
                                preference_bonus += 0.1;
                            }
                        }

                        if (likedBook.author.equals(normalizedAuthor)) {
                            preference_bonus += 0.1;
                        }
                    }
                }
            }
        }

        double baseScore = titleWeight * title_score 
                           + genreWeight * genre_score 
                           + descriptionWeight * description_score 
                           + authorWeight * author_score;

        double total_score = (baseScore + preference_bonus) * rating_boost;

        b.score = total_score;
    }

    private double getTFIDFScore(String indexName, String docId, String term) throws IOException { 
        //Get term vectors from Elasticsearch for the specified document and field
        TermvectorsResponse response = client.termvectors(tv -> tv
            .index(indexName)
            .id(docId)
            .termStatistics(true)  
            .fields("description") 
            .positions(false)     
            .offsets(false)        
            .payloads(false)      
        );
    
        // Get total document count in the index
        co.elastic.clients.elasticsearch.core.CountResponse countResponse = client.count(c -> c.index(indexName));
        long totalDocs = countResponse.count();
    
        // Access term vector for the "description" field
        co.elastic.clients.elasticsearch.core.termvectors.TermVector tv = response.termVectors().get("description");
        if (tv == null || tv.terms() == null || !tv.terms().containsKey(term)) {
            return 0.0;  // Term not found in the document or term vectors missing
        }
        
        // Access term stats (TF, DF) for the specified term
        Term stats = tv.terms().get(term);
        int tf = stats.termFreq();
        double tfScaled = 0;
        if (tf > 0) {
            tfScaled = 1 + Math.log(tf);     // sublinear TF scaling to reduce bias of long descriptions
        }
        int df = stats.docFreq();   
    
        double idf = Math.log((double) (totalDocs + 1) / (df + 1)) + 1;
    
        return tfScaled * idf;
    }

    public double computeCosineSimilarity(User u1, User u2) {
        /*
         * Compute and return the cosine similarity between two users based on which books they have read and how they rated them. 
         * If users have no read books in common, the cosine similarity will be 0.
         * If the users have rated many book similarly, the cosine similarity score will be high.
         */
        double dot = 0;
        double normU1 = 0;
        double normU2 = 0;
    
        for (Map.Entry<String, Float> entry : u1.books.entrySet()) {
            String bookId = entry.getKey();
            Float rating1 = entry.getValue();
    
            if (u2.books.containsKey(bookId)) {
                Float rating2 = u2.books.get(bookId);
                dot += rating1 * rating2;
            }
            normU1 += rating1 * rating1;
        }
    
        for (Float rating2 : u2.books.values()) {
            normU2 += rating2 * rating2;
        }
    
        if (normU1 == 0 || normU2 == 0) return 0;
        return dot / (Math.sqrt(normU1) * Math.sqrt(normU2));
    }

    public List<User> findSimilarUsers(User targetUser, List<User> allUsers, int topN) {
        List<User> similarUsers = new ArrayList<>();
        List<Double> similarities = new ArrayList<>();
    
        for (User other : allUsers) {
            if (other.username.equals(targetUser.username)) continue;  // skip self
            double sim = computeCosineSimilarity(targetUser, other);
            if (sim > 0.5) {  // threshold for similarity (adding users to neighborhood)
                similarUsers.add(other);
                similarities.add(sim);
            }
        }
    
        // Sort users by similarity descending
        for (int i = 0; i < similarUsers.size() - 1; i++) {
            for (int j = i + 1; j < similarUsers.size(); j++) {
                if (similarities.get(j) > similarities.get(i)) {
                    Collections.swap(similarUsers, i, j);
                    Collections.swap(similarities, i, j);
                }
            }
        }
    
        if (similarUsers.size() > topN) {
            // Return only n most similar users
            return similarUsers.subList(0, topN);
        }

        return similarUsers;
    }

    public ArrayList<Book> initialRecommendations(User user) throws IOException {
        HashMap<String, Float> read_books = user.books;
        ArrayList<Book> recommendedBooks = new ArrayList<>();
        String query = "";

        for (Map.Entry<String, Float> entry : read_books.entrySet()) {
            // Only if the user rated the book high does it contribute to recommendations
            if (entry.getValue() >= 4.0) {
                Book readBook = database.getBookByID(entry.getKey());   
                if (readBook != null && !user.books.containsKey(readBook.id)) {
                    String readTitle = readBook.title.toLowerCase();
                    ArrayList readGenres = readBook.genres;
                    String readAuthor = readBook.author.toLowerCase();

                    // Make a big initial query string with all titles, genres and authors of user's read and liked books
                    query += readTitle + " " + String.join(" ", readGenres) + " " + readAuthor + " ";

                }

            }
                
        }

        recommendedBooks = database.getDataForQuery(query);

        return recommendedBooks;

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
         * Sort books by score in descending order
         * */  
        Collections.sort(relevantBooks);
        
        return relevantBooks;
        
    }

}