package socialrankapp.rabbit;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "sr_twitter_scrape";
    // remove messages only when fully processed
    private static final boolean NO_ACK = false;

    // partially prevent data loss on rabbitmq restart
    // private static final boolean DURABLE = true;

    private static Twitter twitterInstance;

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

	    twitterInstance = TwitterAPIRequester.getTwitter();

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

    public static void processMessage(String message) {
	String[] parts = message.split(" ");
	if (parts.length > 0 && parts[0].equals("twitterid")) {
	    String twitterid = parts[1];
	    long userId = Long.parseLong(twitterid);
	    System.out.println("Twitter ID received is: " + twitterid);
	    try {
		ResponseList<Status> tweets = twitterInstance
			.getUserTimeline(userId);
		for (Status tweet : tweets) {
		    System.out.println("@" + tweet.getUser().getScreenName()
			    + " - " + tweet.getText());
		}
	    } catch (TwitterException te) {
		te.printStackTrace();
		System.out.println("Failed to search tweets for the given ID: "
			+ userId);
	    }

	}
    }
}