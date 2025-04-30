package com.book_engine.app;
import java.io.IOException;

/**
 * Search engine
 */
public class App {

    public static void main(String[] args) throws IOException {
        // Set system properties for higher font resolution
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");


        System.out.println("Search engine is starting indexing...");
        Database database = new Database();
        BookRecommender bookRec = new BookRecommender(database);
        GUI gui = new GUI(bookRec);
    }
}
