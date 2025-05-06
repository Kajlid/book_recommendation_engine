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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.TermvectorsResponse;
import co.elastic.clients.elasticsearch.core.termvectors.Term;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;


public class BookRecommender {
    // Title contributes more to the search results
    private static double titleWeight = 0.1;      // title weight
    private static double genreWeight = 0.7;         // genre weight
    private static double descriptionWeight = 0.1;        // description weight
    private static double authorWeight = 0.1;
    
    private User currentUser; 
    
    private static final RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    private static final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static final ElasticsearchClient client = new ElasticsearchClient(transport);
    private Database database;
    
    public BookRecommender(Database database){
        this.database = database;
        this.currentUser = database.users.get(0);
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
                rating_boost = Math.log(Math.abs(Double.parseDouble(b.average_rating)));     // highly rated books should get a higher score
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
            title_score += getTFIDFScore(database.indexName, b.id, word, "title");

            // If almost exact title match, boost score
            LevenshteinDistance distance = new LevenshteinDistance();
            int editDistanceTitle = distance.apply(normalizedTitle, normalizedQuery);

            if (editDistanceTitle <= 3) {
                title_score += 5;  // treat as a title match
            }

            // Genre query match
            long matchingGenres = genres.stream()
            .filter(g -> g.toLowerCase().contains(word.toLowerCase()))
            .count();

            genre_score += matchingGenres * 1000;

            if (normalizedAuthor.contains(word)) {
                author_score += 1;
            }

            // If almost exact author match, boost score
            int editDistanceAuthor = distance.apply(normalizedAuthor, normalizedQuery);
            if (editDistanceAuthor <= 3) {
                author_score += 5;  // treat as an author match
            }

            description_score += getTFIDFScore(database.indexName, b.id, word, "description");
        }

        // User influence bonus (based on personal preferences)
        for (Map.Entry<String, Float> entry : currentUser.books.entrySet()) {
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
        List<User> similarUsers = findSimilarUsers(this.currentUser, database.users, 3);
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

    private double getTFIDFScore(String indexName, String docId, String term, String field) throws IOException { 
        // Get term vectors from Elasticsearch for the specified document and field
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
    
        // Access term vector for the description field
        co.elastic.clients.elasticsearch.core.termvectors.TermVector tv = response.termVectors().get(field);
        if (tv == null || tv.terms() == null || !tv.terms().containsKey(term)) {
            return 0.0;  // Term not found in the document or term vectors missing
        }
        
        // Access term stats (TF, DF) for the specified term
        Term stats = tv.terms().get(term);
        int tf = stats.termFreq();
        double tfScaled = 0;
        if (tf > 0) {
            tfScaled = 1 + Math.log(tf);     // Sublinear TF scaling to reduce bias of long descriptions
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

            if (!other.username.equals("Guest user")) {
                // Guest user should never be included as similar user
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
    
    private User getUserFromUsername(String username){
        User userChosen = null;
        for (User user: database.users){
            if ((user.username).equals(username)){
                userChosen = user;
            }
        }
        return userChosen;
    }
    
    public ArrayList<Book> initialRecommendations(String username) throws IOException {
        currentUser = getUserFromUsername(username);
        if (currentUser == null || username.equals("Guest user")) {
            return new ArrayList<>();  // Return empty if user not found
        }
        HashMap<String, Float> read_books = currentUser.books;
        ArrayList<Book> recommendedBooks = new ArrayList<>();
        String query = "";

        for (Map.Entry<String, Float> entry : read_books.entrySet()) {
            // Only if the user rated the book high does it contribute to recommendations
            if (entry.getValue() >= 4.0) {
                Book readBook = database.getBookByID(entry.getKey());   
                if (readBook != null) {
                    ArrayList readGenres = readBook.genres;

                    // Make an initial query string with all genres of user's most recently read and liked book
                    query += String.join(" ", readGenres);
                    
                    break;   // Break when the latest added book with user rating over 4.0 is found
                }

            }
                
        }

        ArrayList<Book> retrievedBooks = database.getDataForGenres(query, 0, 20);
        for (Book b : retrievedBooks) {
            if (!currentUser.books.containsKey(b.id)) {
                recommendedBooks.add(b); 
            }
        } 
        // Sort relevantBooks by average rating in descending order
        recommendedBooks.sort((b1, b2) -> {
            try {
                double r1 = b1.average_rating != null ? Double.parseDouble(b1.average_rating) : 0;
                double r2 = b2.average_rating != null ? Double.parseDouble(b2.average_rating) : 0;
                return Double.compare(r2, r1);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        User mostSimilarUser;

        try {
            mostSimilarUser = findSimilarUsers(this.currentUser, database.users, 1).get(0);
        }

        catch(Exception e) {
            mostSimilarUser = null;
        }

        if (mostSimilarUser != null) {
            System.out.println("Found most similar user: " + mostSimilarUser.username); // For demo 
            LinkedHashMap<String, Float> books = mostSimilarUser.books;
            LinkedHashMap<String, Float> sortedBooks = new LinkedHashMap<>();
            books.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .forEachOrdered(entry -> sortedBooks.put(entry.getKey(), entry.getValue()));

                // Put books recommended by similar users at predefined indices
                int i = 1;
                for (Map.Entry<String,Float> entry : sortedBooks.entrySet()) {
                    if (!currentUser.books.containsKey(entry.getKey())) {   // If the user has not already read book
                        recommendedBooks.add(i, database.getBookByID(entry.getKey()));
                        i += 2;
                    }
                    
                    if (i > 5) { // only add 3 books from similar user 
                        break;
                    }
                    
                } 

        }

        return recommendedBooks;

    }
    
    
    public ArrayList<Book> search(String query, int from, int size) throws IOException {
        ArrayList<Book> retrievedBooks = database.getDataForQuery(query, from, size);
        ArrayList<Book> relevantBooks = new ArrayList<>();

        // Remove book from recommendations if the user has read it already
        for (Book b : retrievedBooks) {
            if (!currentUser.books.containsKey(b.id)) {
                relevantBooks.add(b); 
            }
        } 

        for (Book b : relevantBooks) {
            setScore(b, query);
        }

        // Sort books by score in descending order 
        Collections.sort(relevantBooks);
        
        return relevantBooks;
        
    }

}