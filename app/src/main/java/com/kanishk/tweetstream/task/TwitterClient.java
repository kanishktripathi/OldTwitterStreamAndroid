package com.kanishk.tweetstream.task;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;

import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.task.TwitterTask.TweetUpdateListener;

/**
 * The Class TwitterClient. The client to manage connections to twitter stream
 * API. It takes requests for new tasks only if there's no pending stream tasks.
 * If a task is downloading tweets in background, it will not take new requests.
 */
public class TwitterClient {

	/** The Constant CONSUMER_KEY. */
	private static final String CONSUMER_KEY = "qgEXz3EfSmoOS2ncBZXcO4Av4";

	/** The Constant CONSUMER_SECRET. */
	private static final String CONSUMER_SECRET = "yzQKTfBJRjqyZA2jcBFfPEKX3QRW6HNQjnGd277Dmr5Kst7eZO";

	/** The Constant ACCESS_TOKEN. */
	private static final String ACCESS_TOKEN = "3074346977-ODVBIFTYTreSt993aOWAuffB5guY7ZRfLtCNDnK";

	/** The Constant ACCESS_TOKEN_SECRET. */
	private static final String ACCESS_TOKEN_SECRET = "2k2iHhvui6lFSpTMF4uCydtUl4BUfHgkUKAe1syp5XHdM";

	/** The Constant STREAM_END. */
	private static final String STREAM_END = "https://stream.twitter.com/1.1/statuses/sample.json?" +
			"filter_level=low";

	/** The Constant STREAM_FILTER. */
	private static final String STREAM_FILTER = "https://stream.twitter.com/1.1/statuses/" +
			"filter.json?filter_level=low&track=";
	
	private static final String HTTP_METHOD = "POST";
	
	private static final int CONNECTION_TIMEOUT = 10000;

	/** The tweet listener. */
	private TweetUpdateListener tweetListener;

	/** The authorize sign. */
	private OAuthConsumer authorizeSign;

	/** The response. */
	private Response response;

	/** The task. */
	private TwitterTask task;

	/**
	 * Instantiates a new twitter client.
	 * 
	 * @param tweetListener
	 *            the tweet listener
	 */
	public TwitterClient(TweetUpdateListener tweetListener) {
		this.tweetListener = tweetListener;
		authorizeSign = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		authorizeSign.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
	}

	/**
	 * Gets the response after connecting with the stream. Connects with
	 *  the sample twitter stream API. Do not call this method from a
	 * UI thread.
	 * @return the response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Response getResponse() throws IOException {
		return createConnection(STREAM_END);
	}

	/**
	 * Gets the response after connecting with the stream. Do not call this method from a
	 * UI thread. Connects with the filter stream API
	 * @param filterText
	 *            the filter text
	 * @return the response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Response getResponse(String filterText) throws IOException {
		filterText  = URLEncoder.encode(filterText, TweetDataConstants.UTF8);
		String url = STREAM_FILTER.concat(filterText);
		return createConnection(url);
	}

	/**
	 * Creates HTTP connection based on authorization. Uses an existing connection if
	 * querying to the same stream.
	 * @param urlText
	 *            the url endpoint of the stream API.
	 * @return the response the wrapper class of the response containing connection object,
	 *  response stream, status.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Response createConnection(String urlText) throws IOException {
		if (this.response == null || !this.response.isValid()) {
			try {
				URL url = new URL(urlText);
				HttpsURLConnection connection = (HttpsURLConnection) url
						.openConnection();
				connection.setConnectTimeout(CONNECTION_TIMEOUT);
				connection.setRequestMethod(HTTP_METHOD);
				authorizeSign.sign(connection);
                this.response = new Response(connection);
			} catch (OAuthException e) {
				throw new IOException(e);
			}
		}
		return this.response;
	}

	/**
	 * Gets the tweets. The tasks call stream  
	 * <a href="https://stream.twitter.com/1.1/statuses/filter.json">Filter API</a>
	 * asynchronously. The results are passed to {@link TweetUpdateListener} hooked with the async
	 * task. Ensures that only one task is running at a time
	 * 
	 * @param searchText
	 *            the search text
	 * @param isNewSearch
	 *            Whether its's a new search or not. If true the existing
	 *            running search(if any) is stopped and new task is started for
	 *            search.
	 */
	public void downloadTweets(String searchText, boolean isNewSearch) {
		if (isNewSearch && this.task != null) {
			task.closeAndRelease();
			task = null;
		}
		if (task == null || !task.isRunning()) {
			task = new TwitterTask(tweetListener, this);
			task.execute(searchText);
		}
	}

	/**
	 * Download tweets. Runs the task to connect to 
	 * <a href="https://stream.twitter.com/1.1/statuses/filter.json">stream API</a>
	 * and download tweets. Ensures that only one task is running at a time
	 */
	public void downloadTweets() {
		if (task == null || !task.isRunning()) {
			task = new TwitterTask(tweetListener, this);
			task.execute();
		}
	}
	
	/**
	 * Closes all open connections, cancels running tasks and release the resources.
	 */
	public void closeAndRelease() {
		if(task != null && task.isRunning()) {
			task.closeAndRelease();			
		}
	}

}
