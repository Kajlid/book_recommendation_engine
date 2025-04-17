/*  
 *  This class sets the score for a book as the weighted TF-IDF score, considering title, description and genres as well as user information. 
 *   
 * 
 *   
 */  

package com.book_engine.app;
import java.io.IOException;
import java.util.*;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.mapper.TextSearchInfo.TermVector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.CountResponse;
import co.elastic.clients.elasticsearch.core.termvectors.Term;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch.core.*;


public class BookRecommender {
    
    private static double alpha = 0.5;
    private static double beta = 2;
    private static double gamma = 3;

    private static final RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    private static final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static final ElasticsearchClient client = new ElasticsearchClient(transport);

    public static void setBookScore(String query) throws IOException {
        ArrayList<Book> bookList = new ArrayList<>();
        try {
            // Database db = new Database();
            bookList = Database.books;

            for (Book b : bookList) {
                setScore(b, query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void setScore(Book b, String query) throws IOException {
        double title_score = 0;
        double genre_score = 0;
        double description_score = 0;
        
        
        List<String> words = Arrays.asList(query.split(" "));
        List<String> titleToList = Arrays.asList(b.title.split(" "));
        ArrayList<String> genres = b.genres;
        
        for (String word: words) {

            // Set title_score
            boolean found_title = titleToList.stream().anyMatch(w -> w.equalsIgnoreCase(word));
            if (found_title) {
                title_score += 1;
            }

            // Set genre_score
            boolean found_genre = genres.stream().anyMatch(w -> w.equalsIgnoreCase(word));
            if (found_genre) {
                genre_score += 1;
            }

            // Set description_score
            description_score += getTFIDFScore("index", b.id, word);
            // System.out.println("score: " + description_score);
        }

        double total_score = alpha * title_score + beta * genre_score + gamma * description_score;

        updateScore(b.id, total_score);
        
    }

    private static double getTFIDFScore(String indexName, String docId, String term) throws IOException {
        // Get term vectors from Elasticsearch for the specified document and field
        TermvectorsResponse response = client.termvectors(tv -> tv
            .index(indexName)
            .id(docId)
            .termStatistics(true)  // Fetch term statistics (TF, DF)
            .fields("description") // Specify the field to get term vectors for
            .positions(false)      // Disable positions
            .offsets(false)        // Disable offsets
            .payloads(false)       // Disable payloads
        );
    
        // Get total document count in the index
        co.elastic.clients.elasticsearch.core.CountResponse countResponse = client.count(c -> c.index(indexName));
        long totalDocs = countResponse.count();
    
        // Access term vector for the "description" field
        co.elastic.clients.elasticsearch.core.termvectors.TermVector tv = response.termVectors().get("description");
        if (tv == null || tv.terms() == null || !tv.terms().containsKey(term)) {
            return 0.0; // Term not found in the document or term vectors missing
        }
    
        // Access term stats (TF, DF) for the specified term
        Term stats = tv.terms().get(term);
        int tf = stats.termFreq();  // Term frequency in the document
        int df = stats.docFreq();   // Document frequency across all documents
    
        // Calculate IDF (Inverse Document Frequency)
        double idf = Math.log((double) (totalDocs + 1) / (df + 1)) + 1;
    
        // Calculate and return the TF-IDF score
        return tf * idf;
    }

    public static void updateScore(String bookId, double newScore) {
        Map<String, Object> updateDoc = new HashMap<>();
        updateDoc.put("score", newScore);
        // System.out.println(newScore);
        // System.out.println(updateDoc);

        try {
            UpdateResponse<Book> response = client.update(UpdateRequest.of(u -> u
                .index("index")
                .id(bookId)
                .doc(updateDoc)
            ), Book.class);

            // System.out.println("Response from ElasticSearch: " + response.result());

            // Force refresh
            // client.indices().refresh(r -> r.index("books"));
        }


        catch (IOException e) {
            e.printStackTrace();
        }

    }
    

}