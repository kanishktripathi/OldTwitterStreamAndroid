package com.kanishk.tweetstream.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.kanishk.tweetstream.model.Tweet;

/**
 * The Class TwitterTask. The async task to connect to the twitter API, parse the response
 * and notify the UI thread.
 */
public class TwitterTask extends AsyncTask<String, Tweet, List<Tweet>> {

	/** The Constant MAX_TWEETS. Maximum number of tweets to fetch in a single task. */
	private static final int MAX_TWEETS = 50;
	
	/** The Constant MAX_TWEETS. The delay time(milliseconds) to read a single line. A delay
	 * of more than 3 seconds to read a single line will cause the */
	private static final int MAX_DELAY = 2000;

	/** The twitter client. */
	private TwitterClient twitterClient;
	
	private Response clientResponse;

	/** The tweet listener. */
	private TweetUpdateListener tweetListener;

	/** The gson. */
	private static Gson GSON = new Gson();

	/** The sys time. */
	private long sysTime;

	/**
	 * Instantiates a new twitter task.
	 *
	 * @param tweetListener the tweet listener
	 * @param client the client
	 */
	public TwitterTask(TweetUpdateListener tweetListener, TwitterClient client) {
		this.twitterClient = client;
		this.tweetListener = tweetListener;
	}

	@Override
	protected List<Tweet> doInBackground(String... params) {
		List<Tweet> result = null;
		try {
			if (params.length == 0) {
				clientResponse = twitterClient.getResponse();
			} else {
				clientResponse = twitterClient.getResponse(params[0]);
			}
			if (clientResponse.isSuccess()) {
				result = getTweets(clientResponse);
			} else {
				String response = clientResponse.streamReader().readLine();
				Log.e(TwitterTask.class.toString(), response);
			}
		} catch (IOException e) {
			Log.e(e.toString(), e.getMessage());
		}
		if(isCancelled()) {
			result = null;
		}
		return result;
	}

	@Override
	protected void onPostExecute(List<Tweet> result) {
		if (result != null && result.size() > 0) {
			tweetListener.updateUI(result);
		}
	}

	/**
	 * Gets the tweets.
	 *
	 * @param response the response
	 * @return the tweets
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private List<Tweet> getTweets(Response response) throws IOException {
		BufferedReader reader = response.streamReader();
		String json = null;
		int count = 0;
		List<Tweet> tweetList = new ArrayList<Tweet>(MAX_TWEETS);
		sysTime = SystemClock.elapsedRealtime();
		do {
			try {
                // Stop running if cancelled
                if(isCancelled()) {
                    return null;
                }
                json = reader.readLine();
                if(isCancelled()) {
                    return null;
                }
                //Returns the list if taking a long time to read from stream.
                if (duration(sysTime) > MAX_DELAY) {
                    return tweetList;
                }
                Tweet tweet = getParsedTweet(json);
                if (tweet != null) {
                    tweetList.add(tweet);
                }
			} catch (IOException e) {
				response.releaseResources();
				return tweetList;
			} catch (JSONException e) {
				Log.e(e.toString(), e.getMessage());
			}
			count++;
		} while (count < MAX_TWEETS && json != null);
		return tweetList;
	}

	/**
	 * Gets the parsed tweet from the JSON string.
	 * @param json the json
	 * @return the parsed tweet object
	 * @throws JSONException the JSON exception
	 */
	private Tweet getParsedTweet(String json) throws JSONException {
		Tweet tweet = null;
		if (json != null && !json.isEmpty()) {
			tweet = GSON.fromJson(json, Tweet.class);
			if (tweet.getText() == null)
				return null;
		}
		return tweet;
	}

	/**
	 * Duration. Measures the time interval(in seconds) from the last Systime.
	 *
	 * @param time the previous time whose difference from current time
	 * will be measured.
	 * @return the duration
	 */
	private long duration(long time) {
		long current = SystemClock.elapsedRealtime();
		long duration = current - time;
		this.sysTime = current;
		return duration;
	}

	/**
	 * Checks if the task is in running state.
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return Status.RUNNING.equals(this.getStatus());
	}
	
	
	/**
	 * Close and release. Closes and releases the resources of this task.
	 */
	public void closeAndRelease() {
		Thread t = new Thread(new HelperThread(this));
		t.start();
	}

	/**
	 * The listener interface for receiving tweetUpdate events.
	 * The class that is interested in processing a tweetUpdate
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTweetUpdateListener<code> method. When
	 * the tweetUpdate event occurs, that object's appropriate
	 * method is invoked.
	 *
	 */
	public static interface TweetUpdateListener {
		
		/**
		 * Update ui. Sends the downloaded tweets to its implemented
		 * class object. It should preferably be an object on UI thread.
		 * @param tweet the tweetList
		 */
		void updateUI(List<Tweet> tweet);
	}
	
	private static class HelperThread implements Runnable {
		
		private TwitterTask task;

		public HelperThread(TwitterTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				task.cancel(true);
				task.clientResponse.releaseResources();
			} catch (IOException e) {
				Log.e(HelperThread.class.toString(), e.getMessage());
			}
		}
		
	}
}
