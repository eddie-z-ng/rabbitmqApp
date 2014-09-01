package socialrankapp.rabbit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class ESAPI {
    private static final String FACETFLOW_API_KEY = System
	    .getenv("FACETFLOW_API_KEY");
    private static final String FACETFLOW_HOST = System
	    .getenv("FACETFLOW_HOST");
    private static final String FACETFLOW_BASE_URL = "https://"
	    + FACETFLOW_API_KEY + ":@" + FACETFLOW_HOST;

    public static String constructTargetURL(String index, String type, String id) throws Exception {
	if (FACETFLOW_API_KEY == null || FACETFLOW_HOST == null) {
	    throw new Exception("No FACETFLOW_API_KEY or FACETFLOW_HOST set");
	}
	
	String tmpPath = constructTargetPath(index, type, id);
	StringBuilder tmpURL = new StringBuilder();
	tmpURL.append(FACETFLOW_BASE_URL);
	tmpURL.append(tmpPath);
	return tmpURL.toString();
    }

    public static String constructTargetPath(String index, String type,
	    String id) {
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

	CloseableHttpClient httpClient = HttpClientBuilder.create().build();

	try {
	    String targetURL = constructTargetURL(index, type, id);
	    HttpPost request = new HttpPost(targetURL);

	    // Important -- Set expected encoding type
	    StringEntity params = new StringEntity(json, "UTF-8");

	    request.addHeader("content-type",
		    "application/x-www-form-urlencoded");
	    request.setEntity(params);
	    CloseableHttpResponse response = httpClient.execute(request);

	    // System.out.println("Request URL: " + targetURL);
	    // System.out.println("POST Data: " + params);
	    // System.out.println("Request Status: " + targetURL + " " +
	    // response.getStatusLine().toString());

	    if (response.getStatusLine().getStatusCode() >= 400) {
		System.out.println("\t" + response.getStatusLine().toString() + "\n" + json);
	    } else {
		System.out.println("\t" + response.getStatusLine().toString());
	    }

	} catch (Exception ex) {
	    System.err.println("Failed to post to ElasticSearch instance: "
		    + ex.getMessage());
	    ex.printStackTrace();
	}
    }
}