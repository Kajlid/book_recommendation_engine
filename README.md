# book_recommendation_engine

This is a book-recommendation-engine that 

Start docker with: 

```sh
docker run -d --name elasticsearch   -p 9200:9200 -p 9300:9300   -e discovery.type=single-node   -e "xpack.security.enabled=false"   -e "xpack.security.http.ssl.enabled=false"   docker.elastic.co/elasticsearch/elasticsearch:8.11.2

```

Compile and run with:
```sh
mvn clean package && java -jar target/book-app-1.0-SNAPSHOT.jar
```