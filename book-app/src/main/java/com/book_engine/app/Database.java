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
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class Database {
    private static final String indexName = "index4";
    private static RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    private static RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static ElasticsearchClient client = new ElasticsearchClient(transport);
    private HashMap<String, Integer> title2id = new HashMap<>();

    // Constructor
    public Database() throws ElasticsearchException, IOException {
        boolean indexExists = client.indices().exists(e -> e.index(indexName)).value();
        if (!indexExists) {
            this.readData(); // If index doesn't exist, load data from JSON files
        }
    }

    // Generate a unique ID based on the book title and author
    public static String generateId(Book book) {
        String title = book.title != null ? book.title.toLowerCase().replaceAll("\\s+", "_") : "untitled";
        String author = book.author != null ? book.author.toLowerCase().replaceAll("\\s+", "_") : "unknown";
        return title + "_" + author;
    }

    /**
     * Reads data from Json-file and adds books to Elasticsearch
     */
    public void readData() {
        try {
            Gson gson = new Gson();
            for (int i = 0; i < 100; i++) {
                JsonReader reader = new JsonReader(new FileReader(String.format("../example_books/%d.json", i)));
                Book book = gson.fromJson(reader, Book.class);

                // Skip if title is not found or already exists in title2id
                if (book.title.equals("Title not found") || title2id.get(book.title) != null) {
                    continue;
                }

                book.setRating();
                writeData(book); // Index the book
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e);
        }
    }

    /**
     * Index a book object into Elasticsearch
     */
    public void writeData(Book book) {
        try {
            boolean indexExists = client.indices().exists(e -> e.index(indexName)).value();
            if (!indexExists) {
                client.indices().create(c -> c.index(indexName)); // Create the index if it doesn't exist
            }

            IndexResponse response = client.index(IndexRequest.of(i -> i
                .index(indexName)
                .document(book)
            ));
        } catch (Exception e) {
            System.out.println("Error occurred while indexing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieve all book data from Elasticsearch
     */
    public static ArrayList<Book> getData() {
        ArrayList<Book> bookList = new ArrayList<>();
        try {
            // Count total number of documents in the index
            CountRequest countRequest = new CountRequest.Builder().index(indexName).build();
            CountResponse countResponse = client.count(countRequest);
            long totalDocs = countResponse.count();

            // Search for all documents
            SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q.matchAll(m -> m)) // Match all documents
                .size((int) totalDocs)  // Retrieve all documents
                .build();

            SearchResponse<Book> searchResponse = client.search(searchRequest, Book.class);

            // Process the search hits
            for (Hit<Book> hit : searchResponse.hits().hits()) {
                Book book = hit.source();
                book.id = hit.id();   // Set the book ID
                book.score = hit.score(); // Set the score
                bookList.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    /**
     * Retrieve a book by its ID from Elasticsearch
     */
    public Book getBookByID(int bookid) {
        Book book = null;
        try {
            // Get book from Elastic by its ID
            GetResponse<Book> response = client.get(g -> g
                .index(indexName)
                .id(Integer.toString(bookid)), Book.class);

            if (response.found()) {
                book = response.source();
            } else {
                System.out.println("Book not found");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return book;
    }

    /**
     * Retrieve books filtered by a query from Elasticsearch
     */
    public static ArrayList<Book> getDataForQuery(String query) {
        ArrayList<Book> bookList = new ArrayList<>();
        try {
            // Perform a multi_match search query on relevant fields
            SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q.multiMatch(m -> m.query(query).fields("title", "description", "genres")))
                .size(500) // Limit the number of results
                .build();

            SearchResponse<Book> searchResponse = client.search(searchRequest, Book.class);

            // Add relevant hits to the list
            for (co.elastic.clients.elasticsearch.core.search.Hit<Book> hit : searchResponse.hits().hits()) {
                Book book = hit.source();
                if (book != null) {
                    book.id = hit.id(); // Set ID
                    book.score = hit.score();   // Set score
                    bookList.add(book);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookList;
    }
}
