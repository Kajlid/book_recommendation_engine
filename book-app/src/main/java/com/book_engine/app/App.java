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
        // database.books = Database.getData();

        // BookRecommender.setBookScore("Potter is waiting nervously fantasy");
        // for (Book b: Database.getData()) {
        //     System.out.println(b.score);
        // }
        BookRecommender.search("romance");
    }
}
