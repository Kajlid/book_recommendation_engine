package com.book_engine.app;

import java.util.ArrayList;

/**
 * Class Book that stores book information
 * - Is comparable for sorting
 */
public class Book implements Comparable<Book> {
    public String title;
    public String author;
    public String description;
    public String average_rating;
    public ArrayList<String> genres;
    public Float rating = null;
    public double score = 0;
    public String id;

    @Override
    public String toString() {
        return "Book{title='" + this.title + "', genres=" + this.genres + ", rating=" + this.rating + ", id=" + this.id + "}";

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
    public void setRating() {
        try {
            this.rating = Float.parseFloat(this.average_rating);
        } catch (Exception e){
            this.rating = null;
            System.out.println("Was unable to find rating for book: " + this.title);
        }
    }

    /**
     * Compare books based on score in descending order
     */
    @Override
    public int compareTo(Book other) {
        int scoreComparison = Double.compare(other.score, this.score); // Descending order
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        // If scores are equal, return book with highest rating
        if (this.rating == null && other.rating == null) return 0;
        if (this.rating == null) return 1; // Put books without rating lower
        if (other.rating == null) return -1;

        return Float.compare(other.rating, this.rating);

    }
}
