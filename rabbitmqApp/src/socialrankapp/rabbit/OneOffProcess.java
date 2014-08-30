package socialrankapp.rabbit;

import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class OneOffProcess {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
	String uri = System.getenv("CLOUDAMQP_URL");
	if (uri == null) {
	    uri = "amqp://kpuecxfp:jHNzbA3XYB8VeKLdMpXpbVjAerwFQ6so@lemur.cloudamqp.com/kpuecxfp";
	}
	ConnectionFactory factory = new ConnectionFactory();
	factory.setUri(uri);
	Connection connection = factory.newConnection();
	Channel channel = connection.createChannel();

	channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	String message = "Hello CloudAMQP! at " + new Date(); ;
	channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
	System.out.println(" [x] Sent '" + message + "'");

	channel.close();
	connection.close();
    }
}