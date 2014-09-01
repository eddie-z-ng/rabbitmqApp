package socialrankapp.rabbit;

import java.util.Date;

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

    // Used for exchange-topic mode
    private static final String EXCHANGE_NAME = "sr_twitter_exchange";

    private static final String ES_TYPE = "tweet";

    // remove messages only when fully processed
    private static final boolean NO_ACK = false;

    // partially prevent data loss on rabbitmq restart
    private static final boolean DURABLE = true;

    private static Twitter twitterInstance;

    public static void main(String[] argv) {
	Connection connection = null;
	Channel channel = null;
	try {
	    String uri = System.getenv("CLOUDAMQP_URL");
	    if (uri == null) {
		throw new Exception("No CLOUDAMQP_URL specified");
	    }
	    
	    twitterInstance = TwitterAPIRequester.getTwitter();
	    
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri(uri);
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    String queueName = null;
	    QueueingConsumer consumer = null;
	    String mode = null;
	    String modeKey = null;

	    if (argv.length < 2) {
		printUsageError();
		System.exit(1);
	    } else {
		mode = argv[0];
		modeKey = getBindingKeyOrQueueName(argv);

		if (mode.equalsIgnoreCase("TOPIC")) {
		    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		    queueName = channel.queueDeclare().getQueue();

		    String bindingKey = modeKey;
		    channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);

		} else if (mode.equalsIgnoreCase("QUEUE")) {
		    queueName = modeKey;
		    channel.queueDeclare(queueName, DURABLE, false, false, null);

		} else {
		    printUsageError();
		    System.exit(1);
		}
	    }

	    channel.basicQos(1);
	    consumer = new QueueingConsumer(channel);
	    channel.basicConsume(queueName, NO_ACK, consumer);

	    System.out
		    .println(" [*] Waiting for messages. To exit press CTRL+C. MODE: "
			    + mode + " KEY: " + modeKey);

	    while (true) {
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		String message = new String(delivery.getBody());
		String routingKey = delivery.getEnvelope().getRoutingKey();

		System.out.println(new Date() + " [x] Received '" + routingKey
			+ "':'" + message + "'");

		processMessage(message);

		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// finally {
	// if (connection != null) {
	// try {
	// connection.close();
	// } catch (Exception ignore) {
	// }
	// }
	// }
    }

    private static String getBindingKeyOrQueueName(String[] strings) {
	if (strings.length < 2) {
	    return "anonymous.info";
	}
	return strings[1];
    }

    private static void printUsageError() {
	System.err
		.println("Usage: ReceiveLogsTopic [mode] [bindingKey|queueName]");
	System.err.println("\t 'topic' mode: ReceiveLogsTopic topic srank.#");
	System.err
		.println("\t 'queue' mode: ReceiveLogsTopic queue srank.queue");
    }

    private static void processMessage(String message) throws JSONException {
	String[] parts = message.split(" ");
	if (parts.length > 0 && parts[0].equals("twitterid")) {
	    String twitterid = parts[1];
	    long userId = Long.parseLong(twitterid);

	    try {
		ResponseList<Status> tweets = twitterInstance
			.getUserTimeline(userId);

		for (Status tweet : tweets) {
		    String json = TwitterObjectFactory.getRawJSON(tweet);

		    // ElasticSearch scheme: /<screen_name>/tweet/<tweet_id>
		    // NOTE: screen names should be normalized to lower case
		    String es_index = tweet.getUser().getScreenName()
			    .toLowerCase();
		    String es_type = ES_TYPE;
		    String es_id = String.valueOf(tweet.getId());

		    String es_path = ESAPI.constructTargetPath(es_index,
			    es_type, es_id);
		    System.out.println("\tPost to ES path: " + es_path);

		    ESAPI.postToIndex(es_index, es_type, es_id, json);
		}

	    } catch (TwitterException te) {
		System.err.println("Failed to search tweets for user ID: "
			+ userId + " " + te.getMessage());
		te.printStackTrace();
	    }

	}
    }
}