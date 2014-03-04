package com.therabbitmage.android.beacon.network;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;

import com.therabbitmage.android.beacon.R;

public final class TwitterBeacon {

	private static Twitter sTwitter;
 
	/**
	 * Generates a Twitter object from the Twitter4J library. The Singleton Pattern prevents the consumer key and secret
	 * from being placed into the twitter object twice on creation.
	 * @param ctx any context that will use the twitter object
	 * @return Twitter4J object
	 */
	public static Twitter getTwitter(Context ctx) {

		if (sTwitter == null) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(
					ctx.getString(R.string.twitter_consumer_key))
					.setOAuthConsumerSecret(
							ctx.getString(R.string.twitter_consumer_secret));

			sTwitter = new TwitterFactory(builder.build()).getInstance();
		}

		return sTwitter;
	}
	/**
	 * Clears the Twitter object from the Twitter4J library and setups it null
	 */
	public static void clearTwitter(){
		sTwitter.shutdown();
		sTwitter = null;
	}

	private TwitterBeacon() {
	}

}
