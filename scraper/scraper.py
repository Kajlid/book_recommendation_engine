
import requests
from bs4 import BeautifulSoup

# Constants for testing
#URL = 'https://www.goodreads.com/book/show/223536115-all-the-skills-5'
#URL = 'https://www.goodreads.com/book/show/9648068-the-first-days'
URL = 'https://www.goodreads.com/book/show/210678415-how-to-feed-the-world'

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                  "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}


def getBook(url: str):
    """Retrieves the title of a book

    Args:
        url (str): The url to the book page on goodreads

    Returns:
        str: The title of the book
    
    Raises:
        Exception: Invalid url
    """
    
    # Handle invalid input
    if url == None:
        raise Exception
    
    # Request website
    response = requests.get(url, headers=HEADERS)
    
    # Handle response
    if response.status_code == 200:
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Find the book title
        title_tag = soup.find('h1', {'class': 'Text Text__title1', 'data-testid': 'bookTitle'})
        title = title_tag.text.strip() if title_tag else 'Title not found'
        
        # Find the book description
        desc_container = soup.find('div', class_='DetailsLayoutRightParagraph')
        if desc_container:
            formatted_span = desc_container.find('span', class_='Formatted')
            description = formatted_span.text.strip() if formatted_span else 'Description not found'
        else:
            description = 'Description not found'
            
            
        # Print information
        print(f"Title: {title}\n")
        print(f"Description: {description}\n")
        
        return title
    
    else:
        print("Failed to fetch the page. Status code:", response.status_code)
        return None

def main():
    """Run a simple scraper test on a single book site"""
    getBook(URL)
    

if __name__ == "__main__":
    main()