package com.kanishk.tweetstream.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.AsyncTask;
import android.util.Log;

import com.kanishk.tweetstream.model.Tweet;

/**
 * The Class ConnectionTask. The async task to connect to the twitter API, parse
 * the response and notify the UI thread.
 */
public class ConnectionTask extends AsyncTask<String, Tweet, List<Tweet>> {

	/**
	 * The Constant MAX_TWEETS. The delay time(milliseconds) to read a single
	 * line. A delay of more than 10 seconds to read a single line will cause thread
	 * to return whatever results retrieved till now. 
	 */
	private static final int MAX_DELAY = 10;

	/** The twitter client. */
	private TwitterClient twitterClient;

	/** The client response. */
	private Response clientResponse;

	/** The tweet listener. */
	private TweetUpdateListener tweetListener;

	/** The twitter task. */
	private TwitterTask twitterTask;

	/**
	 * Instantiates a new twitter task.
	 * 
	 * @param tweetListener
	 *            the tweet listener
	 * @param client
	 *            the client
	 */
	public ConnectionTask(TweetUpdateListener tweetListener,
			TwitterClient client) {
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
			twitterClient = null;
			if (clientResponse != null && clientResponse.isSuccess() && !isCancelled()) {
                twitterTask = new TwitterTask();
                twitterTask.executeOnExecutor(THREAD_POOL_EXECUTOR, clientResponse);
                result = twitterTask.get(MAX_DELAY, TimeUnit.SECONDS);
			} else if(clientResponse != null && !clientResponse.isSuccess()) {
				String response = clientResponse.streamReader().readLine();
				Log.e(ConnectionTask.class.toString(), response);
			}
		} catch (IOException | ExecutionException | InterruptedException
				| TimeoutException e) {
			Log.e(e.toString(), e.toString());
            twitterTask.cancel(true);
			result = new ArrayList<>(0);
		}
		if (!isRunning()) {
			result = null;
		}
		return result;
	}

	@Override
	protected void onPostExecute(List<Tweet> result) {
		tweetListener.updateUI(result);
	}

	/**
	 * Checks if the task is in running state.
	 * 
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return Status.RUNNING.equals(this.getStatus());
	}

    /**
     *  Cancels and interrupts this current task.
     */
    private void cancelTask() {
        if(twitterTask != null) {
            twitterTask.cancel(true);
        }
        this.cancel(true);
    }
	/**
	 * Close and release. Closes and releases the resources of this task.
	 */
	public void closeAndRelease() {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(new HelperThread(this));
	}

	/**
	 * The listener interface for receiving tweetUpdate events. The class that
	 * is interested in processing a tweetUpdate event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addTweetUpdateListener<code> method. When
	 * the tweetUpdate event occurs, that object's appropriate
	 * method is invoked.
	 */
	public static interface TweetUpdateListener {

		/**
		 * Update ui. Sends the downloaded tweets to its implemented class
		 * object. It should preferably be an object on UI thread.
		 * 
		 * @param tweet
		 *            the tweetList
		 */
		void updateUI(List<Tweet> tweet);
	}

	/**
	 * The Class HelperThread. A helper thread called from Connection thread
	 * to clear the resources used by this thread.
	 */
	private static class HelperThread implements Runnable {

		/** The task. */
		private ConnectionTask task;

		/**
		 * Instantiates a new helper thread.
		 *
		 * @param task the task
		 */
		public HelperThread(ConnectionTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				if (task.isRunning()) {
					task.cancelTask();
				}
			} finally {
				try {
                    if(task.clientResponse != null) {
                        task.clientResponse.releaseResources();
                    }
				} catch (IOException e) {
					Log.e(HelperThread.class.toString(), e.getMessage());
				}
			}
		}
	}
}
