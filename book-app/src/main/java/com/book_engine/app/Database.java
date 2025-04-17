package com.book_engine.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class Database {
    private static int currentID = 0;
    public static HashMap<String, Integer> title2id = new HashMap<String, Integer>();
    /**
     * Reads data from Json-file
     */
    public static void readData() {
        try {
            Gson gson = new Gson();
            for (int i=0; i<100; i++) {
                JsonReader reader = new JsonReader(new FileReader(String.format("../example_books/%d.json", i)));
                Book book = gson.fromJson(reader, Book.class);

                if ((book.title).equals("Title not found")) {    // if book has no title or already exists, skip to next, title2id.get(book.title) == null
                    continue; 
                }
                book.setRating();
                book.setCombinedString();

                System.out.println(book);

                // writeData(book);  
            }
        } catch (FileNotFoundException e){
            System.out.println("File not found: " + e);
        }

        
    }

    /**
     * Put book object into index 
     */
    public static void writeData(Book book) {
       // Connect to Elasticsearch
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        ElasticsearchClient client = new ElasticsearchClient(transport);

        try {
            String customID = String.valueOf(currentID++);
            IndexResponse response = client.index(IndexRequest.of(i -> i
                .index("test-index")
                .id(customID)
                .document(book)
            ));
            title2id.put(book.title, Integer.parseInt(customID));
            System.out.println("Indexed with ID: " + response.id());
        }
        catch(Exception e) {
            System.out.println("Error occurred while indexing: " + e.getMessage());
            e.printStackTrace();
        }   

        // Close client
        try {
            restClient.close();
        } catch (IOException e) {
            System.err.println("Error closing RestClient: " + e.getMessage());
        }


    }

    /* This function should return all book objects written to ElasticSearch */
    // TODO: make it work to only return hits from specific user query from ElasticSearch
    public static ArrayList<Book> getData() {
        ArrayList<Book> bookList = new ArrayList<>();

        // Connect to Elasticsearch and retrieve all book titles
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        try {
            CountRequest countRequest = new CountRequest.Builder()
            .index("test-index")  
            .build();

            CountResponse countResponse = client.count(countRequest);
            long totalDocs = countResponse.count();      // Count number of documents
            
             SearchRequest searchRequest = new SearchRequest.Builder()
                .index("test-index")  
                .query(q -> q
                    .matchAll(m -> m)  // Match all documents
                )
                .size((int) totalDocs)  // Get all documents
                .build();
            
            SearchResponse<Book> searchResponse = client.search(searchRequest, Book.class);

            // Process all search hits
            for (Hit<Book> hit : searchResponse.hits().hits()) {
                Book book = hit.source();
                System.out.println("Found book: " + book.title);
                bookList.add(book);
            }

            return bookList;
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        // Close client
        try {
            restClient.close();
        } catch (IOException e) {
            System.err.println("Error closing RestClient: " + e.getMessage());
        }
        return new ArrayList<>();
        
    }


    
}
