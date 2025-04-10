package com.book_engine.app;

import java.util.ArrayList;

public class Book{
    private String content;
    String title;
    String author;
    String description;
    String  average_rating;
    ArrayList<String> genres;

    public Book() {}

    public Book(String title, String author, String description, String average_rating, String genres){
        this.title = title;
        this.author = author;
        this.description = description;
        this.average_rating = average_rating;
        this.genres = makeGenreList(genres);
    }

    private ArrayList<String> makeGenreList(String genres){
        if (genres == null || genres.trim().isEmpty()) return new ArrayList<>();
        return new ArrayList<String>();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format(
            "Title: %s\nAuthor: %s\nRating: %s\nGenres: %s\nDescription: %s\n",
            title,
            author,
            average_rating != null ? average_rating : "N/A",
            genres != null ? genres.toString() : "[]",
            description
        );
    }
}