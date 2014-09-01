package socialrankapp.rabbit;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPIRequester {

    private static final String TWITTER_CONSUMER_KEY = System
	    .getenv("TWITTER_CONSUMER_KEY");
    private static final String TWITTER_SECRET_KEY = System
	    .getenv("TWITTER_SECRET_KEY");
    private static final String TWITTER_ACCESS_TOKEN = System
	    .getenv("TWITTER_ACCESS_TOKEN");
    private static final String TWITTER_ACCESS_TOKEN_SECRET = System
	    .getenv("TWITTER_ACCESS_TOKEN_SECRET");

    public static Twitter getTwitter() throws Exception {
	if (TWITTER_CONSUMER_KEY == null || TWITTER_SECRET_KEY == null || TWITTER_ACCESS_TOKEN == null || TWITTER_ACCESS_TOKEN_SECRET == null) {
	    throw new Exception("No TWITTER_CONSUMER_KEY, TWITTER_SECRET_KEY, TWITTER_ACCESS_TOKEN, or TWITTER_ACCESS_TOKEN_SECRET set");
	}
	
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
