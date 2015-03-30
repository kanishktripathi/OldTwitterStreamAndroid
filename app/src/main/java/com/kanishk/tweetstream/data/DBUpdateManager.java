package com.kanishk.tweetstream.data;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;

import com.kanishk.tweetstream.model.Tweet;
import com.kanishk.tweetstream.model.User;

/**
 * The Class DBUpdateManager. An update manager to perform CRUD operations on
 * the database based on the URI. Since content provider does not support
 * inserts on a separate thread, this class manages those operations.
 */
public class DBUpdateManager {

	/** The Constant manager. */
	private static final DBUpdateManager manager;

	static {
		manager = new DBUpdateManager();
	}

	/**
	 * Gets the single instance of DBUpdateManager.
	 * 
	 * @return single instance of DBUpdateManager
	 */
	public static DBUpdateManager getInstance() {
		return manager;
	}

	/**
	 * Insert rows in the database in a separate thread.
	 * 
	 * @param tweetList
	 *            the tweet list
	 * @param resolver
	 *            the resolver
	 */
	public void insertRows(List<Tweet> tweetList, ContentResolver resolver) {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(new InsertTask(TweetDataConstants.CONTENT_URI,
				tweetList, resolver, false));
	}

	/**
	 * Insert search results in the database table.
	 * 
	 * @param tweetList
	 *            the tweet list
	 * @param resolver
	 *            the content resolver used to query content provider
	 * @param refresh
	 *            . Check whether to clear previous entries or not. Set true if it's a new search
	 */
	public void insertSearchResults(List<Tweet> tweetList,
			ContentResolver resolver, boolean refresh) {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(new InsertTask(
				TweetDataConstants.CONTENT_SEARCH_URI, tweetList, resolver,
				refresh));
	}
	
	/**
	 * Clears the tweet search table.
	 * @param resolver the content resolver
	 */
	public void clearSearchTable(ContentResolver resolver) {
		resolver.delete(TweetDataConstants.CONTENT_SEARCH_URI, "1", null);
//		AsyncTask.THREAD_POOL_EXECUTOR.execute(new DeleteTask(TweetDataConstants.CONTENT_SEARCH_URI,
//				resolver));
	}

	/**
	 * The Class InsertTask. A runnable task to insert data using the content provider.
	 */
	private static class InsertTask implements Runnable {

		/** The data uri. */
		private Uri dataURI;

		/** The tweet list. */
		private List<Tweet> tweetList;

		/** The resolver. */
		private ContentResolver resolver;

		/** The clear. */
		private boolean clear;

		/**
		 * Instantiates a new insert task.
		 * 
		 * @param dataURI
		 *            the data uri
		 * @param tweetList
		 *            the tweet list
		 * @param resolver
		 *            the resolver
		 * @param clearTable
		 *            the clear table
		 */
		public InsertTask(Uri dataURI, List<Tweet> tweetList,
				ContentResolver resolver, boolean clearTable) {
			this.dataURI = dataURI;
			this.tweetList = tweetList;
			this.resolver = resolver;
			this.clear = clearTable;
		}

		@Override
		public void run() {
			if (this.clear) {
				resolver.delete(dataURI, null, null);
			}
			insertRows();
		}

		/**
		 * Insert rows.
		 */
		private void insertRows() {
			int index = 0;
			ContentValues[] values = new ContentValues[tweetList.size()];
			for (Tweet tweet : tweetList) {
				ContentValues value = new ContentValues();
				User user = tweet.getUser();
				value.put(TweetDataConstants.FIELD_TWEET_TEXT, tweet.getText());
				value.put(TweetDataConstants.FIELD_IMAGE_URL,
						user.getProfile_image_url());
				value.put(TweetDataConstants.FIELD_NAME, user.getName());
				value.put(TweetDataConstants.FIELD_SCREEN_NAME,
						user.getScreen_name());
				values[index] = value;
				index++;
			}
			resolver.bulkInsert(dataURI, values);
		}
	}
	
	/**
	 * The Class DeleteTask. A separate runnable task to delete rows from a table.
	 * Currently designed to clear the whole table.
	 */
	private class DeleteTask implements Runnable {
		
		/** The data uri. */
		private Uri dataURI;

		/** The content resolver. */
		private ContentResolver resolver;
		
		/**
		 * Instantiates a new delete task.
		 * @param dataURI the data uri
		 * @param resolver the content resolver to query content provider.
		 */
		public DeleteTask(Uri dataURI, ContentResolver resolver) {
			this.dataURI = dataURI;
			this.resolver = resolver;
		}

		@Override
		public void run() {
			resolver.delete(dataURI, "1", null);
		}
	}
}
