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

Access UI on Kibana:

1. Start docker for Kibana with:
```
docker run -d --name kibana \
  --link elasticsearch:elasticsearch \
  -p 5601:5601 \
  -e ELASTICSEARCH_HOSTS=http://elasticsearch:9200 \
  docker.elastic.co/kibana/kibana:8.11.2
```

2. Go to Discover in the left panel
3. Set 'Name' and change index pattern, then click save