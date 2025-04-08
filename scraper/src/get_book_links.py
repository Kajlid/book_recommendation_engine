import os
import time

import requests
from bs4 import BeautifulSoup

# Constants
BASE_URL = "https://www.goodreads.com"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}
DELAY = 1.0  
MAX_PAGES_PER_LIST = 100

# Load file with book list URLs
list_file_path = "lists.txt"
if not os.path.exists(list_file_path):
    raise FileNotFoundError("lists.txt not found.")

# Read saved links to book lists
with open(list_file_path, "r", encoding="utf-8") as f:
    list_urls = [line.strip() for line in f if line.strip()]

total_lists = len(list_urls)
all_links = set()

# Load existing book links from file
book_links_path = "book_links.txt"
if os.path.exists(book_links_path):
    with open(book_links_path, "r", encoding="utf-8") as f:
        all_links = set(line.strip() for line in f)

# Start processing each list
for list_index, list_url in enumerate(list_urls, start=1):
    print(f"\nProcessing list {list_index}/{total_lists}: {list_url}")
    page = 1
    while page <= MAX_PAGES_PER_LIST:
        url = f"{list_url}?page={page}"
        print(f"Fetching page {page}...")

        try:
            response = requests.get(url, headers=HEADERS, timeout=10)
            if response.status_code != 200:
                print(f"Failed to fetch page {page}. Status code: {response.status_code}")
                break
        except requests.exceptions.RequestException as e:
            print(f"Connection error on page {page}: {e}")
            break

        soup = BeautifulSoup(response.text, "html.parser")
        book_tags = soup.find_all("a", class_="bookTitle")

        if not book_tags:
            print("✅ No more books found. Moving to next list.")
            break

        new_links = set(BASE_URL + tag["href"] for tag in book_tags)
        unique_new_links = new_links - all_links
        all_links.update(unique_new_links)

        print(f"Page {page}: Found {len(unique_new_links)} new books (Total unique: {len(all_links)})")

        # Save after each page to be safe
        with open(book_links_path, "w", encoding="utf-8") as f:
            for link in sorted(all_links):
                f.write(link + "\n")

        page += 1
        time.sleep(DELAY)

print(f"\n✅ Done! Processed {total_lists} lists and saved {len(all_links)} unique book links to '{book_links_path}'.")
