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
    
    // Title contributes more to the search results
    private static double alpha = 0.5;      // title weight
    private static double beta = 0.4;         // genre weight
    private static double gamma = 0.1;        // description weight

    private static final RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    private static final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static final ElasticsearchClient client = new ElasticsearchClient(transport);

    public static void setBookScore(String query, ArrayList<Book> bookList) throws IOException {
        try {
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
        
        // TODO: replace with tokenization method
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
                title_score += 3; // bonus points for perfect title match
            }

            // Set genre_score
            boolean found_genre = genres.stream().anyMatch(w -> w.toLowerCase().contains(word.toLowerCase()));
            if (found_genre) {
                genre_score += 1;
            }

            // Set description_score
            description_score += getTFIDFScore(Database.indexName, b.id, word);
        }

        double total_score = alpha * title_score + beta * genre_score + gamma * description_score;
        b.score = total_score;
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
    
    public static void search(String query) throws IOException {
        ArrayList<Book> relevantBooks = Database.getDataForQuery(query);
        for (Book b : relevantBooks) {
            setScore(b, query);
        }

        // Sort relevantBooks
        Collections.sort(relevantBooks);

        for (Book b : relevantBooks) {
            System.out.println(b.title + ": " + b.score + " " + b.rating + " , Genre: " + b.genres);
        }
    }

}