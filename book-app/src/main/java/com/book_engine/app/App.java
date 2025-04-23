package com.book_engine.app;
import java.io.IOException;
import java.util.ArrayList;

import com.book_engine.app.User;

/**
 * Search engine
 */
public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Search engine is starting indexing...");
        Database database = new Database();
        BookRecommender.search("romance");    // hardcoded for now
    }
}
