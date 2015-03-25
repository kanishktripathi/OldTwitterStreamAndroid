package com.kanishk.tweetstream.task;

import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.kanishk.tweetstream.adapter.TweetCursorAdapter;
import com.kanishk.tweetstream.data.DBUpdateManager;
import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.model.Tweet;
import com.kanishk.tweetstream.task.TwitterTask.TweetUpdateListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * The Class TweetActivity.
 */
public class TweetActivity extends FragmentActivity implements OnRefreshListener, 
		TweetUpdateListener, LoaderManager.LoaderCallbacks<Cursor> {

	/** The client to make authenticate and make API calls. */
	protected TwitterClient twitterClient;

	/** The cursor adapter for list view. */
	protected TweetCursorAdapter adapter;

	/** The Constant TWEET_LOADER. */
	protected static final int TWEET_LOADER = 0;

	/** The static load options for async image loading */
	protected static ImageLoaderConfiguration IMG_LOAD_OPTIONS;
	
	/** The db update manager. */
	protected DBUpdateManager dbManager;
	
	/** The image loader object. */
	protected ImageLoader imageLoader;
	
	protected SwipeRefreshLayout swipeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		initImageOptions();
		this.twitterClient = new TwitterClient(this);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swripe_refresh);
		ListView listView = (ListView) findViewById(R.id.list);
		swipeLayout.setOnRefreshListener(this);
		dbManager = DBUpdateManager.getInstance();
		adapter = new TweetCursorAdapter(this, null, ImageLoader.getInstance());
		listView.setAdapter(adapter);
		getSupportLoaderManager().initLoader(TWEET_LOADER, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Checks intent. Checks whether the intent is of search from the action bar
	 * @param intent the intent parameter
	 */
	protected void checkIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if(hasNetAccess()) {
				String searchText = intent.getStringExtra(SearchManager.QUERY);
				Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
				searchIntent.putExtra(SearchManager.QUERY, searchText);
				startActivity(searchIntent);				
			} else {
				displayMessage(getString(R.string.net_connect_error));
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		checkIntent(intent);
	}

	@Override
	public void onRefresh() {
		if (hasNetAccess()) {
			twitterClient.downloadTweets();
		} else {
			swipeLayout.setRefreshing(false);
			displayMessage(getString(R.string.net_connect_error));
		}
	}

	/**
	 * Checks for net access.
	 *
	 * @return true, if successful
	 */
	protected boolean hasNetAccess() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		return connManager.getActiveNetworkInfo() != null
				&& connManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void updateUI(List<Tweet> tweet) {
		swipeLayout.setRefreshing(false);
		dbManager.insertRows(tweet, getContentResolver());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		if (arg0 == TWEET_LOADER) {
			loader = new CursorLoader(this, TweetDataConstants.CONTENT_URI,
					TweetDataConstants.COLUMNS, null, null,
					TweetDataConstants.ORDER_BY_ID);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.changeCursor(arg1);

	}

	/**
	 * Display a message using {@link android.widget.Toast} library. 
	 *
	 * @param text the text
	 */
	protected void displayMessage(String text) {
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		imageLoader.stop();		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adapter.changeCursor(null);
		twitterClient.closeAndRelease();
	}
	
	/**
	 * Initializes the image options.
	 */
	protected void initImageOptions() {
		if (IMG_LOAD_OPTIONS == null) {
			Context context = getApplicationContext();
			IMG_LOAD_OPTIONS = new ImageLoaderConfiguration.Builder(context)
					.diskCacheSize(TweetDataConstants.MAX_MEMORY_SIZE)
					.memoryCacheSizePercentage(TweetDataConstants.IMAGE_CACHE_PERCENT)
					.threadPoolSize(TweetDataConstants.IMAGE_THREAD_POOL)
					.build();
			imageLoader = ImageLoader.getInstance();
			imageLoader.init(IMG_LOAD_OPTIONS);
		} else {
			imageLoader = ImageLoader.getInstance();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(null);
	}
}
