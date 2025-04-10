import time

import requests
from bs4 import BeautifulSoup

# Constants
BASE_URL = "https://www.goodreads.com"
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36'
}

START_PAGE = 1
END_PAGE = 100  # Maximum page number, this seems to be a hard limit set by goodreads
DELAY_SECONDS = 2.0

# Create a set for all list URLs
all_links = set()

# Iterate over all pages on goodreads that contain links to lists
for page in range(START_PAGE, END_PAGE + 1):
    # Go to the next page
    url = f"{BASE_URL}/list/popular_lists?page={page}"
    print(f"Scraping page {page}...")

    # Handle response
    try:
        response = requests.get(url, headers=HEADERS)
        if response.status_code != 200:
            print(f"Failed to fetch page {page}, status: {response.status_code}")
            continue

        # Find all <a>-tags containing URLs to lists
        soup = BeautifulSoup(response.text, 'html.parser')
        list_anchors = soup.find_all('a', class_='listTitle')

        for anchor in list_anchors:
            href = anchor.get('href')
            if href and href.startswith("/list/show/"):
                all_links.add(BASE_URL + href)

        # Don't overload goodreads
        time.sleep(DELAY_SECONDS)

    except Exception as e:
        print(f"Error on page {page}: {e}")

# Save all links to file
with open("lists.txt", "w", encoding="utf-8") as f:
    for link in sorted(all_links):
        f.write(link + "\n")

print(f"\n✅ Done! Saved {len(all_links)} unique list URLs to lists.txt.")
