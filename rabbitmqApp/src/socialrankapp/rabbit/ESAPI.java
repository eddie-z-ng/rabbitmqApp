package socialrankapp.rabbit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class ESAPI {
    private static final String API_KEY = "Qbb0ptd0HUmBETHOc4SdwQzuQhRZjl0q";
    private static final String HOST = "srtweet.east-us.azr.facetflow.io";
    private static final String BASE_URL = "https://" + API_KEY + ":@"
	    + HOST;

    public static String constructTargetURL(String index, String type, String id) {
	StringBuilder tmpURL = new StringBuilder();
	tmpURL.append(BASE_URL);
	tmpURL.append('/');
	tmpURL.append(index);
	tmpURL.append('/');
	tmpURL.append(type);
	tmpURL.append('/');
	tmpURL.append(id);
	return tmpURL.toString();
    }

    public static void postToIndex(String index, String type, String id,
	    String json) {
	String targetURL = constructTargetURL(index, type, id);

	CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .build();

	try {
	    HttpPost request = new HttpPost(targetURL);
	    StringEntity params = new StringEntity(json);
	    request.addHeader("content-type",
		    "application/x-www-form-urlencoded");
	    request.setEntity(params);
	    CloseableHttpResponse response = httpClient.execute(request);
	    
//	    System.out.println("Request URL: " + targetURL);
//	    System.out.println("POST Data: " + params);
	    System.out.println("Request Status: " + targetURL + " " + response.getStatusLine().toString());

	} catch (Exception ex) {
	    ex.printStackTrace();
	} 
    }
}

// curl -XPOST
// 'https://Qbb0ptd0HUmBETHOc4SdwQzuQhRZjl0q:@srtweet.east-us.azr.facetflow.io/second_index/posts'
// -d '{ "user": "FOOD", "post_date": "2014-08-31T21:21:08.920Z", "message":
// "PLEASE WORK"}'