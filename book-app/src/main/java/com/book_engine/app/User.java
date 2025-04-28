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
    public void addBook(Book book, Database db, Float rating){ //TODO: might be able to delete this function and only use addBookById
        // get book object id
        String stringid = db.generateId(book);

        // Handle invalid id
        if (stringid == null){
            return;
        }
        Book bookdb = db.getBookByID(stringid); // get same book object as in index
        if (bookdb == null){
            return;
        }
        books.put(stringid, rating);
    }

    public void addBookById(String bookid, Database db, Float rating){
        Book bookdb = db.getBookByID(bookid); // get same book object as in index
        if (bookdb == null){
            return;
        }
        books.put(bookid, rating);
    }
}
