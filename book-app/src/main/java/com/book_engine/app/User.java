package com.book_engine.app;
import java.util.ArrayList;
import com.book_engine.app.Database;
import java.util.HashMap;

public class User{
    public String username;
    public HashMap<Integer, Float> books = new HashMap<Integer, Float>(); // bookid to personal rating

    public User(String name){
        this.username = name;
    }

    /**
     * Add the book to books 
     */
    public void addBook(String bookid, Database db, Float rating){
        // get book object
        Integer intid = db.title2id.get(bookid);
        if (intid == null){
            return;
        }
        Book book = db.getBookByID(intid); // get same book object as in index
        if (book == null){
            return;
        }
        books.put(intid, rating);
        

    }

    
}