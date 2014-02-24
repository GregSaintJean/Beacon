package com.therabbitmage.android.beacon.network;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;

import com.therabbitmage.android.beacon.R;

public final class TwitterBeacon {

	private static Twitter sTwitter;

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

	private TwitterBeacon() {
	}

}
