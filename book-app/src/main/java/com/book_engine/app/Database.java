package com.book_engine.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class Database {
    
    /**
     * Reads data from Json-file
     */
    public static void readData() {
        Gson gson = new Gson();

        // Read all example books
        for (int i = 0; i < 99; i++) {
            // Add each book to elastic
            try {
                String file_name = String.format("../example_books/%d.json", i);
                JsonReader reader = new JsonReader(new FileReader(file_name));
                Book book = gson.fromJson(reader, Book.class);
                System.out.println(String.format("Adding book \"%s\" to Elastic.", book.title));
                writeData(book);  
            } catch (FileNotFoundException e){
                System.out.println("File not found: " + e);
            }
        }

        // Allow user to search for books in elastic
        while (true) { 
            search();    
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

    public static void search() {
        // Take user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter search term for book title: ");
        String searchTerm = scanner.nextLine();
    
        // Connect to Elasticsearch
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
    
        try {
            // Build and send search request
            SearchResponse<Book> response = client.search(s -> s
                    .index("test-index")
                    .query(q -> q
                        .match(m -> m
                            .field("title")
                            .query(searchTerm)
                        )
                    ),
                    Book.class
            );
    
            // Print matching books
            List<Hit<Book>> hits = response.hits().hits();
            if (hits.isEmpty()) {
                System.out.println("No books found matching: " + searchTerm);
            } else {
                System.out.println("Books found:");
                for (Hit<Book> hit : hits) {
                    Book book = hit.source();
                    if (book != null) {
                        System.out.println("- " + book.title);
                    }
                }
            }
    
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                restClient.close();
            } catch (IOException e) {
                System.err.println("Error closing RestClient: " + e.getMessage());
            }
        }
    }
}
