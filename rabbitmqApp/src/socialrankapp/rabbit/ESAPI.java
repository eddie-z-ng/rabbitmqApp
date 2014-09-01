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
	String tmpPath = constructTargetPath(index, type, id);
	StringBuilder tmpURL = new StringBuilder();
	tmpURL.append(BASE_URL);
	tmpURL.append(tmpPath);
	return tmpURL.toString();
    }
    
    public static String constructTargetPath(String index, String type, String id) {
	StringBuilder tmpURL = new StringBuilder();
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
	    
	    // Important -- Set expected encoding type
	    StringEntity params = new StringEntity(json, "UTF-8");
	    
	    request.addHeader("content-type",
		    "application/x-www-form-urlencoded");
	    request.setEntity(params);
	    CloseableHttpResponse response = httpClient.execute(request);
	    
//	    System.out.println("Request URL: " + targetURL);
//	    System.out.println("POST Data: " + params);
//	    System.out.println("Request Status: " + targetURL + " " + response.getStatusLine().toString());

	    if (response.getStatusLine().getStatusCode() >= 400) {
		System.out.println("\tBAD REQUEST: " + targetURL + json);
	    } else {
		System.out.println("\tSUCCESS");
	    }
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	} 
    }
}