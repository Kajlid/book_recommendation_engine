package com.book_engine.app;
import java.util.HashMap;

public class User{
    public String username;
    public HashMap<String, Float> books = new HashMap<String, Float>(); // bookid to personal rating

    public User(String name){
        this.username = name;
    }

    /**
     * Add the book to books 
     * @param book Book object to be added
     * @param db Database object to get the book id
     * @param rating User rating of the book
     */
    public void addBookById(String bookid, Database db, Float rating){
        Book bookdb = db.getBookByID(bookid); // get same book object as in index
        if (bookdb == null){
            return;
        }
        books.put(bookid, rating);
    }
}
