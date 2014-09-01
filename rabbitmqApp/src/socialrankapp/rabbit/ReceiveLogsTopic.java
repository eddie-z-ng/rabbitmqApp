package socialrankapp.rabbit;

import twitter4j.JSONException;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "sr_twitter_scrape";
    private static final String ES_TYPE = "tweet";

    // remove messages only when fully processed
    private static final boolean NO_ACK = false;

    // partially prevent data loss on rabbitmq restart
    // private static final boolean DURABLE = true;

    private static Twitter twitterInstance = TwitterAPIRequester.getTwitter();

    public static void main(String[] argv) {
	Connection connection = null;
	Channel channel = null;
	try {
	    String uri = System.getenv("CLOUDAMQP_URL");
	    if (uri == null) {
		throw new Exception("No CLOUDAMQP_URL specified");
	    }
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri(uri);
	    connection = factory.newConnection();
	    channel = connection.createChannel();

	    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
	    String queueName = channel.queueDeclare().getQueue();

	    if (argv.length < 1) {
		System.err.println("Usage: ReceiveLogsTopic [binding_key]...");
		System.exit(1);
	    }

	    for (String bindingKey : argv) {
		channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
	    }

	    System.out
		    .println(" [*] Waiting for messages. To exit press CTRL+C");

	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(queueName, NO_ACK, consumer);

	    while (true) {
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		String message = new String(delivery.getBody());
		String routingKey = delivery.getEnvelope().getRoutingKey();

		System.out.println(" [x] Received '" + routingKey + "':'"
			+ message + "'");

		processMessage(message);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (connection != null) {
		try {
		    connection.close();
		} catch (Exception ignore) {
		}
	    }
	}
    }

    public static void processMessage(String message) throws JSONException {
	String[] parts = message.split(" ");
	if (parts.length > 0 && parts[0].equals("twitterid")) {
	    String twitterid = parts[1];
	    long userId = Long.parseLong(twitterid);

	    try {
		ResponseList<Status> tweets = twitterInstance
			.getUserTimeline(userId);
		String userIdString = String.valueOf(userId);
		for (Status tweet : tweets) {
		    String json = TwitterObjectFactory.getRawJSON(tweet);

		    // ElasticSearch scheme: /<screen_name>/tweet/<tweet_id>
		    // NOTE: screen names should be normalized to lower case
		    String es_index = tweet.getUser().getScreenName()
			    .toLowerCase();
		    String es_type = ES_TYPE;
		    String es_id = String.valueOf(tweet.getId());
		    
		    String es_path = ESAPI.constructTargetPath(es_index, es_type, es_id);
		    System.out.println("Post to ES path: " + es_path);
		    
		    ESAPI.postToIndex(es_index, es_type, es_id, json);
		}
		
		System.out.println("\t Tweets for " + twitterid + " saved to ES");
		
	    } catch (TwitterException te) {
		System.err.print("Failed to search tweets for user ID: "
			+ userId + " " + te.getMessage());
		te.printStackTrace();
	    }

	}
    }
}