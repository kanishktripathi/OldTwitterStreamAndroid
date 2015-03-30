package com.kanishk.tweetstream.operations;

import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import com.kanishk.tweetstream.operations.TweetTaskFragment.TaskFragmentListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * The Class TweetActivity. The activity for displaying tweets without filter.
 */
public class TweetActivity extends FragmentActivity implements
		OnRefreshListener, TaskFragmentListener,
		LoaderCallbacks<Cursor> {

	/** The cursor adapter for list view. */
	protected TweetCursorAdapter adapter;

	/** The task fragment. */
	protected TweetTaskFragment taskFragment;

	/** The Constant TWEET_LOADER. */
	protected static final int TWEET_LOADER = 0;

	/**  The static load options for async image loading. */
	protected static ImageLoaderConfiguration IMG_LOAD_OPTIONS;

	/** The image loader object. */
	protected ImageLoader imageLoader;

	/** The swipe layout. */
	protected SwipeRefreshLayout swipeLayout;

	/** The handler. */
	private Handler handler;
	
	/** The scroll listener. */
	protected CustomScrollListener scrollListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		initImageOptions();
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swripe_refresh);
		ListView listView = (ListView) findViewById(R.id.list);
		swipeLayout.setOnRefreshListener(this);
		adapter = new TweetCursorAdapter(this, null, ImageLoader.getInstance());
		listView.setAdapter(adapter);
		taskFragment = (TweetTaskFragment) getSupportFragmentManager()
				.findFragmentByTag(TweetDataConstants.TASK_FRAGMENT);
		if (taskFragment == null) {
			taskFragment = new TweetTaskFragment();
			getSupportFragmentManager().beginTransaction()
					.add(taskFragment, TweetDataConstants.TASK_FRAGMENT)
					.commit();
		}
		scrollListener = initScrollListener(savedInstanceState);
		listView.setOnScrollListener(scrollListener);
		getSupportLoaderManager().initLoader(TWEET_LOADER, null, this);
		this.handler = new Handler();
		if (taskFragment.isLoading()) {
			displayRefresh();
		}
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
	 * 
	 * @param intent
	 *            the intent parameter
	 */
	protected void checkIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (hasNetAccess()) {
				String searchText = intent.getStringExtra(SearchManager.QUERY);
				Intent searchIntent = new Intent(getApplicationContext(),
						SearchActivity.class);
				searchIntent.putExtra(SearchManager.QUERY, searchText);
				startActivity(searchIntent);
			} else {
				displayMessage(getString(R.string.net_connect_error));
			}
		}
	}
	
	/**
	 * Initialize the scroll listener.
	 * @param savedInstance the saved instance to obtain current item count
	 * on configuration change
	 * @return the  scroll listener for the list view
	 */
	private CustomScrollListener initScrollListener(Bundle savedInstance) {
		final LoaderCallbacks<Cursor> callBack = this;
		CustomScrollListener listener = new CustomScrollListener() {
			@Override
			public void loadData(int dataSize) {
				getSupportLoaderManager().restartLoader(TWEET_LOADER, null, callBack);
			}
		};
		if(savedInstance != null) {
			listener.setCurrentScrollCount(savedInstance.getInt(CustomScrollListener.PAGE_ITEMS_COUNT));
			listener.setPreviousTotalCount(savedInstance.getInt(CustomScrollListener.PAGE_PREVIOUS_COUNT));
		}
		return listener;
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
			taskFragment.refresh();
		} else {
			swipeLayout.setRefreshing(false);
			displayMessage(getString(R.string.net_connect_error));
		}
	}

	/**
	 * Checks for internet access.
	 * 
	 * @return true, if successful
	 */
	protected boolean hasNetAccess() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		return connManager.getActiveNetworkInfo() != null
				&& connManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void onUpdateTweets(List<Tweet> tweetList) {
		swipeLayout.setRefreshing(false);
		DBUpdateManager dbManager = DBUpdateManager.getInstance();
		if (tweetList != null && !tweetList.isEmpty()) {
			dbManager.insertRows(tweetList, getContentResolver());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CustomScrollListener.PAGE_ITEMS_COUNT, scrollListener.getCurrentScrollCount());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		if (arg0 == TWEET_LOADER) {
			String orderBy = TweetDataConstants.ORDER_BY + scrollListener.getCurrentScrollCount();
			loader = new CursorLoader(this, TweetDataConstants.CONTENT_URI,
					TweetDataConstants.COLUMNS, null, null,
					orderBy);
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
	 * @param text
	 *            the text
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

	/**
	 * Initializes the image options.
	 */
	protected void initImageOptions() {
		if (IMG_LOAD_OPTIONS == null) {
			Context context = getApplicationContext();
			IMG_LOAD_OPTIONS = new ImageLoaderConfiguration.Builder(context)
					.diskCacheSize(TweetDataConstants.MAX_MEMORY_SIZE)
					.memoryCacheSizePercentage(
							TweetDataConstants.IMAGE_CACHE_PERCENT)
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

	/**
	 * Display refresh.
	 */
	protected void displayRefresh() {
		if (!swipeLayout.isRefreshing()) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					swipeLayout.setRefreshing(true);
				}
			}, 100);
		}
	}
}
