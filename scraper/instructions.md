# Goodreads Scraper – Instructions

This toolset helps you scrape book data from Goodreads in three stages.

## 1. Overview of Files

### `get_book_lists.py`
Scrapes popular book list URLs from Goodreads and saves them to `lists.txt`.

- Output: `lists.txt` (contains Goodreads list URLs)

### `get_book_links.py`
Goes through each list in `lists.txt` and extracts individual book URLs.

- Input: `lists.txt`
- Output: `book_links.txt` (contains Goodreads book URLs)

### `get_book_info.py`
Fetches and extracts detailed info (title, description, author, genres, rating) from each book in `book_links.txt`.

- Input: `book_links.txt`
- Output: One `.json` file per book in the `books/` directory


## 2. Setup Instructions

Install dependencies:
   ```bash
   pip3 install -r requirements.txt
