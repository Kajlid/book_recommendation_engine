import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class ElasticsearchTest {
    public static void main(String[] args) {
        try (RestClient client = RestClient.builder(new HttpHost("localhost", 9200, "http")).build()) {
            Response response = client.performRequest(new Request("GET", "/"));
            System.out.println("Elasticsearch is running: " + response.getStatusLine());
        } catch (Exception e) {
            System.out.println("Failed to connect to Elasticsearch: " + e.getMessage());
        }
    }
}
