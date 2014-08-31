package socialrankapp.rabbit;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPIRequester {
    // private static final String TWITTER_CONSUMER_KEY =
    // "nB0Xt47mtc7Duq9V6HZkXPVSx";
    // private static final String TWITTER_SECRET_KEY =
    // "Ybjoiwc3BPslPCkkjKygzXapHH8Fj1hbOOODOP9P3VSsmCY9FZ";
    // private static final String TWITTER_ACCESS_TOKEN =
    // "1730282898-hWAE7ABiNvlGIGJbomjpliylq7lPTo7cO9rRgIh";
    // private static final String TWITTER_ACCESS_TOKEN_SECRET =
    // "807OFgYZHy18gM4YS3FmE4JgeCAlI4q8pouDhOXc9K3d3";

    private static final String TWITTER_CONSUMER_KEY = System.getenv("TWITTER_CONSUMER_KEY");
    private static final String TWITTER_SECRET_KEY = System.getenv("TWITTER_SECRET_KEY");
    private static final String TWITTER_ACCESS_TOKEN = System.getenv("TWITTER_ACCESS_TOKEN");
    private static final String TWITTER_ACCESS_TOKEN_SECRET = System.getenv("TWITTER_ACCESS_TOKEN_SECRET");

    public static Twitter getTwitter() {
	ConfigurationBuilder cb = new ConfigurationBuilder();
	cb.setDebugEnabled(true).setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
		.setOAuthConsumerSecret(TWITTER_SECRET_KEY)
		.setOAuthAccessToken(TWITTER_ACCESS_TOKEN)
		.setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);
	
	// To store JSON in memory
	cb.setJSONStoreEnabled(true);
	TwitterFactory tf = new TwitterFactory(cb.build());
	Twitter twitterInstance = tf.getInstance();
	return twitterInstance;
    }
}
