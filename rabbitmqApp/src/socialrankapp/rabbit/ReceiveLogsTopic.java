package socialrankapp.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "sr_twitter_scrape";

    public static void main(String[] argv) {
	Connection connection = null;
	Channel channel = null;
	try {
	    String uri = System.getenv("CLOUDAMQP_URL");
	    if (uri == null) {
		uri = "amqp://kpuecxfp:jHNzbA3XYB8VeKLdMpXpbVjAerwFQ6so@lemur.cloudamqp.com/kpuecxfp";
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
	    channel.basicConsume(queueName, true, consumer);

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
	    System.out.println("Twitter ID received is: " + twitterid);
	}
    }
}