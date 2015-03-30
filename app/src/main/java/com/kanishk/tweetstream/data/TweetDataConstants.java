package com.kanishk.tweetstream.data;

import android.net.Uri;

import com.kanishk.tweetstream.operations.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * The Class TweetDataConstants.
 */
public class TweetDataConstants {
	
	/** The db name. */
	public static String DB_NAME = "com.kanishk.tweet";

	/** The table name. */
	public static String TABLE_NAME = "tweets";
	
	/** The limit on the number of rows to fetch in a query. */
	public static String LIMIT = "400";
	
	/** The field screen name. */
	public static String FIELD_SCREEN_NAME = "screen_name";
	
	/** The field id. */
	public static String FIELD_ID = "_id";
	
	/** The Constant TWEET_TABLE. */
	public static final String TWEET_TABLE = "tweets";
	
	/** The Constant SEARCH_TABLE. */
	public static final String SEARCH_TABLE = "tweetSearch";
	
	/** The field name. */
	public static String FIELD_NAME = "name";
	
	/** The field image url. */
	public static String FIELD_IMAGE_URL = "image_url";
	
	/** The field tweet text. */
	public static String FIELD_TWEET_TEXT = "tweet";
	
	/** The Constant TWEET_LIMIT. The maximum number of tweets to show in a list view*/
	public static final int TWEET_LIMIT = 400;
	
	/** The order by id. */
	public static String ORDER_BY_ID = "_id desc LIMIT " + 20;
	
	/** The order by id. */
	public static String ORDER_BY = "_id desc LIMIT ";
	
	/** The columns. */
	public static String COLUMNS[] = {FIELD_SCREEN_NAME, FIELD_NAME,
			FIELD_IMAGE_URL, FIELD_TWEET_TEXT, FIELD_ID};
	
	/** The default version of SQLite database. */
	public static int VERSION = 1;
	
	/** The Constant AUTHORITY. */
	public static final String AUTHORITY = "com.kanishk.tweetstream.data.TweetContentProvider";
	
	/** The Constant CONTENT_URI. */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TweetDataConstants.TWEET_TABLE);
	
	/** The Constant CONTENT_SEARCH_URI. */
	public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TweetDataConstants.SEARCH_TABLE);
	
	/** The Constant MAX_MEMORY_SIZE. */
	public static final int MAX_MEMORY_SIZE = 4 * 1024 * 1024;
	
	/** The Constant IMAGE_THREAD_POOL. */
	public static final int IMAGE_THREAD_POOL = Runtime.getRuntime().availableProcessors() + 2;
	
	/** The Constant IMAGE_CACHE_PERCENT. The size of memory cache as percentage
	 * of available memory*/
	public static final int IMAGE_CACHE_PERCENT = 10;

	/** The Constant SEARCH_TEXT. Key for search text on a stream*/
	public static final String SEARCH_TEXT = "search_text";
	
	/** The Constant UTF8. */
	public static final String UTF8 = "UTF-8";
	
	/** The Constant TASK_FRAGMENT. */
	public static final String TASK_FRAGMENT = "com.kanishk.tweet.taskFragment";
	
	/** The Constant DISPLAY_FRAGMENT. */
	public static final String DISPLAY_FRAGMENT = "com.kanishk.tweet.displayFragment";
	
	/** The Constant RECREATED. */
	public static final String RECREATED ="recreated";
	
	/** The Constant IS_NEW. */
	public static final String IS_NEW ="is_new";
	
	/** The Constant IMAGE_DISPLAY_OPTIONS. */
	public static final DisplayImageOptions IMAGE_DISPLAY_OPTIONS;
	static{
		IMAGE_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.no_image).showImageOnFail(R.drawable.no_image)
		.cacheOnDisk(true).cacheInMemory(true).showImageOnLoading(R.drawable.no_image).build();
	}
}
