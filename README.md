# Book Recommendation Engine

This is a book-recommendation-engine that gives personalized book recommendations given previously read books. This engine is built upon data scraped from Goodreads.

## Prerequisites
To execute this search engine you will need to download Docker, Maven and Elasticssearch. 

## Project structure
```
book_recommendation_engine/
├── .gitignore
├── README.md
├── book-app/
│   ├── .mvn/
│   ├── README.md
│   ├── pom.xml
│   ├── target/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/
│   │   │           └── book_engine/
│   │   │               └── app/
│   │   │                   ├── App.java (application entry point)
│   │   │                   ├── Book.java
│   │   │                   ├── Bookrecommender.java
│   │   │                   ├── Database.java
│   │   │                   ├── GUI.java
│   │   │                   ├── Tokenizer.java (currently not used)
│   │   │                   └── User.java
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── book_engine/
│   │                   └── app/
│   │                        ├── AppTest.java
│   │                        ├── UserTest.java
├── books/
├── example_books/
├── scraper/
└── users/
```


## Running the program
Start docker with: 

```sh
docker run -d --name elasticsearch   -p 9200:9200 -p 9300:9300   -e discovery.type=single-node   -e "xpack.security.enabled=false"   -e "xpack.security.http.ssl.enabled=false"   docker.elastic.co/elasticsearch/elasticsearch:8.11.2

```

Compile and run the program with:
```sh
mvn clean package && java -jar target/book-app-1.0-SNAPSHOT.jar
```

## Index
Note that indexing will be run the first time you run the program and that this may take a while, 10-30 minutes depending on your machine. The program will then reuse your index the next time you run the program. To view all previously created indices visit http://localhost:9200/_cat/indices?v . To delete old indices use
```sh
 curl -X DELETE http://localhost:9200/indexname 
 ```