package socialrankapp.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLogsTopic {

    // Used for exchange-topic mode
    private static final String EXCHANGE_NAME = "sr_twitter_exchange";

    // partially prevent data loss on rabbitMQ restart
    private static final boolean DURABLE = true;

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

	    String mode = null;
	    String modeKey = null;
	    String message = null;

	    if (argv.length < 3) {
		printUsageError();
		System.exit(1);
	    } else {
		mode = argv[0];
		modeKey = getRoutingKeyOrQueueName(argv);
		message = getMessage(argv);

		if (mode.equalsIgnoreCase("TOPIC")) {

		    String routingKey = modeKey;
		    channel.exchangeDeclare(EXCHANGE_NAME, "topic");

		    channel.basicPublish(EXCHANGE_NAME, routingKey, null,
			    message.getBytes());

		} else if (mode.equalsIgnoreCase("QUEUE")) {

		    String queueName = modeKey;
		    channel.queueDeclare(queueName, DURABLE, false, false, null);

		    channel.basicPublish("", queueName, null,
			    message.getBytes());

		} else {
		    printUsageError();
		    System.exit(1);
		}
	    }

	    System.out.println(" [x] Sent via '" + modeKey + "':'" + message
		    + "'");

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (channel != null) {
		try {
		    channel.close();
		} catch (Exception ignore) {
		}
	    }
	    if (connection != null) {
		try {
		    connection.close();
		} catch (Exception ignore) {
		}
	    }
	}
    }

    private static void printUsageError() {
	System.err
		.println("Usage: EmitLogsTopic [mode] [routingKey|queueName] [message]");
	System.err
		.println("\t 'topic' mode: EmitsLogsTopic topic srank.gettimeline twitterid 1730282898");
	System.err
		.println("\t 'queue' mode: EmitsLogTopic queue srank.queue twitterid 1730282898");
    }

    private static String getRoutingKeyOrQueueName(String[] strings) {
	if (strings.length < 3)
	    return "anonymous.info";
	return strings[1];
    }

    private static String getMessage(String[] strings) {
	if (strings.length < 3)
	    return "Hello World!";
	return joinStrings(strings, " ", 2);
    }

    private static String joinStrings(String[] strings, String delimiter,
	    int startIndex) {
	int length = strings.length;
	if (length == 0)
	    return "";
	if (length < startIndex)
	    return "";
	StringBuilder words = new StringBuilder(strings[startIndex]);
	for (int i = startIndex + 1; i < length; i++) {
	    words.append(delimiter).append(strings[i]);
	}
	return words.toString();
    }
}
