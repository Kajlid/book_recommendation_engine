import json
import os
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests
from bs4 import BeautifulSoup

# Constants
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}
BOOK_URL_FILE = "book_links.txt"
BOOKS_DIR = "books"
MAX_RETRIES = 5
SLEEP_BETWEEN_REQUESTS = 1.0  
MAX_WORKERS = 6  # Num threads

# Output dir
os.makedirs(BOOKS_DIR, exist_ok=True)

# Load book URLs
with open(BOOK_URL_FILE, "r", encoding="utf-8") as f:
    book_urls = [line.strip() for line in f if line.strip()]

total_books = len(book_urls)

def fetch_and_save_book(index_url):
    index, url = index_url
    book_path = os.path.join(BOOKS_DIR, f"{index}.json")
    
    # Skip existing
    if os.path.exists(book_path):
        return f"Skipped {index} (already exists)"

    # Try to retrieve book
    for attempt in range(MAX_RETRIES):
        try:
            response = requests.get(url, headers=HEADERS, timeout=20)
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')

                # Extract title
                title_tag = soup.find('h1', {'class': 'Text Text__title1', 'data-testid': 'bookTitle'})
                title = title_tag.text.strip() if title_tag else 'Title not found'

                # Extract container
                desc_container = soup.find('div', class_='DetailsLayoutRightParagraph')
                if desc_container:
                    formatted_span = desc_container.find('span', class_='Formatted')
                    description = formatted_span.text.strip() if formatted_span else 'Description not found'
                else:
                    description = 'Description not found'

                # Extract genres
                genre_spans = soup.find_all('span', class_='BookPageMetadataSection__genreButton')
                genres = [span.find('span', class_='Button__labelItem').text.strip()
                          for span in genre_spans if span.find('span', class_='Button__labelItem')]

                # Extract author
                author_tag = soup.find('span', class_='ContributorLink__name', attrs={'data-testid': 'name'})
                author = author_tag.text.strip() if author_tag else 'Author not found'

                # Extract rating
                rating_tag = soup.find('div', class_='RatingStatistics__rating')
                rating = rating_tag.text.strip() if rating_tag else 'Rating not found'

                # JSON format for book data
                book_data = {
                    "title": title,
                    "description": description,
                    "genres": genres,
                    "author": author,
                    "average_rating": rating,
                    "url": url
                }

                # Save to file
                with open(book_path, "w", encoding="utf-8") as f:
                    json.dump(book_data, f, ensure_ascii=False, indent=2)

                return f"✅ Saved {index}: {title}"
            else:
                print(f"Status {response.status_code} (attempt {attempt + 1}) for {url}")
        except Exception as e:
            print(f"Exception (attempt {attempt + 1}) for {url}: {e}")
        
        # Wait for a bit
        time.sleep(SLEEP_BETWEEN_REQUESTS)

    return f"Skipped {index} after {MAX_RETRIES} retries"

# Parallel execution
results = []
with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
    futures = [executor.submit(fetch_and_save_book, (i, url)) for i, url in enumerate(book_urls)]
    for i, future in enumerate(as_completed(futures), 1):
        result = future.result()
        print(f"[{i}/{total_books}] {result}")

print(f"\n✅ Done! Book data saved in '{BOOKS_DIR}/'.")
