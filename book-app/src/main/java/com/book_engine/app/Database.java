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
            for (int i=0; i<100; i++){
                JsonReader reader = new JsonReader(new FileReader(String.format("../example_books/%d.json", i)));
                Book book = gson.fromJson(reader, Book.class);

                if ((book.title).equals("Title not found")) {    // if book has no title, skip to next
                    continue; 
                }
                book.setRating();
                System.out.println(book);
                System.out.println(book.title);
                writeData(book);
                
            }
            
        } catch (FileNotFoundException e){
            System.out.println("File not found: " + e);
        }
    }

    public static void writeData(Book book) {
        /*
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
        */
       // Connect to Elasticsearch
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        ElasticsearchClient client = new ElasticsearchClient(transport);

        try{
            IndexResponse response = client.index(IndexRequest.of(i -> i
                .index("test-index")
                .document(book)
            ));
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


        // docker run -d --name elasticsearch   -p 9200:9200 -p 9300:9300   -e discovery.type=single-node   -e "xpack.security.enabled=false"   -e "xpack.security.http.ssl.enabled=false"   docker.elastic.co/elasticsearch/elasticsearch:8.11.2

    }
}
