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

        // TEST tokenization
        // System.out.println(Tokenizer.tokenize("It is the middle of the summer 11 (Rowling, 2007), but there is an unseasonal mist pressing against the windowpanes"));

    }
}
