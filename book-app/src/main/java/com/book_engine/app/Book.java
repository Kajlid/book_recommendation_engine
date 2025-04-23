package com.book_engine.app;

import java.util.ArrayList;

public class Book {
    public String title;
    public String author;
    public String description;
    public String average_rating;
    public ArrayList<String> genres;
    public Float rating = null;

    @Override
    public String toString() {
        return "Book{title='" + this.title + "', genres=" + this.genres + "rating= " + this.rating + "}";
    }

    /**
     * Displays the content of the book in a formatted way
     * @return String formatted book details
     */
    public String displayContents() {
        String title = this.title + " by " + this.author + "\n";
        String rating = "Rating: " + this.rating + "★\n";
        String line = "-----------------------------------------------------\n";
        String description = this.description + "\n";
        return title + rating + line + description;
    }

    /**
     * Function to handle null ratings 
     */
    public void setRating(){
        try {
            this.rating = Float.parseFloat(this.average_rating);
        } catch (Exception e){
            this.rating = null;
            System.out.println("Was unable to find rating for book: " + this.title);
        }
       
    }

   
}