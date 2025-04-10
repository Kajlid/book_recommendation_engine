package com.book_engine.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class Database {
    
    /**
     * Reads data from Json-file
     */
    public static void readData() {
        try{
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader("../example_books/1.json"));
            Book book = gson.fromJson(reader, Book.class);
            System.out.println(book.title);
            writeData(book);  
        } catch (FileNotFoundException e){
            System.out.println("File not found: " + e);
        }
    }

    public static void writeData(Book book) {
       // Connect to Elasticsearch
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // Establish client connection to elastic
        ElasticsearchClient client = new ElasticsearchClient(transport);

        try {
            // Add a index to elastic
            IndexResponse response = client.index(IndexRequest.of(i -> i
                .index("test-index")
                .document(book)
            ));

            // Get book ID in elastic
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
}
