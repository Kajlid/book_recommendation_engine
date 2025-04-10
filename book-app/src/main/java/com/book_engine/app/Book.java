package com.book_engine.app;

import java.util.ArrayList;

public class Book{
    private String content;
    public String title;
    public String author;
    public String description;
    public String  average_rating;
    public ArrayList<String> genres;

    public Book() {}

    public Book(String title, String author, String description, String average_rating, String genres){
        this.title = title;
        this.author = author;
        this.description = description;
        this.average_rating = average_rating;
        this.genres = makeGenreList(genres);
    }

    //region Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAverage_rating() { return average_rating; }
    public void setAverage_rating(String average_rating) { this.average_rating = average_rating; }

    public ArrayList<String> getGenres() { return genres; }
    public void setGenres(ArrayList<String> genres) { this.genres = genres; }
    //endregion

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