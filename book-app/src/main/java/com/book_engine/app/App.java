package com.book_engine.app;
import com.book_engine.app.User;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Database database = new Database();
        System.out.println("Hello World!");
        database.readData();
        User testUser = new User();
        String id = "Harry_Potter_and_the_Half-Blood_Prince_J.K._Rowling".toLowerCase();
        testUser.addBook(id, database);

    }
}
