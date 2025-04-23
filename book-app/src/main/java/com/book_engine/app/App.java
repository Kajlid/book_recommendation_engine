package com.book_engine.app;
import com.book_engine.app.User;

/**
 * Search engine
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Search engine is starting indexing...");
        Database database = new Database();
        database.readData();

    }
}
