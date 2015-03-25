package com.kanishk.tweetstream.task;

import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.model.Tweet;

public class SearchActivity extends TweetActivity {

	/** The search text. The text of the current search. */
	private String searchText;

	/**
	 * The is new search result. A variable to check the state of the current
	 * search. Whether it's a new search or not.
	 */
	private boolean isNewSearchResult;
	
	/** The activity handler. */
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		Intent launchIntent = getIntent();
		String query = launchIntent.getExtras().getString(SearchManager.QUERY);
		setUpSearch(query);
	}

	@Override
	protected void checkIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			if (!searchQuery.equals(this.searchText)) {
				setUpSearch(searchQuery);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		if (arg0 == TWEET_LOADER) {
			loader = new CursorLoader(this,
					TweetDataConstants.CONTENT_SEARCH_URI,
					TweetDataConstants.COLUMNS, null, null,
					TweetDataConstants.ORDER_BY_ID);
		}
		return loader;
	}

	@Override
	public void onRefresh() {
		if (hasNetAccess()) {
			twitterClient.downloadTweets(searchText, isNewSearchResult);
			isNewSearchResult = false;
		} else {
			swipeLayout.setRefreshing(false);
			displayMessage(getString(R.string.net_connect_error));
		}
	}

	@Override
	public void updateUI(List<Tweet> tweet) {
		swipeLayout.setRefreshing(false);
		dbManager.insertSearchResults(tweet, getContentResolver(),
				isNewSearchResult);
		isNewSearchResult = false;
	}

	@Override
	protected void onDestroy() {
		dbManager.clearSearchTable(getContentResolver());
		super.onDestroy();
	}
	
	/**
	 * Sets the up the search from the search text box.
	 * @param searchString the string on which to filter the stream
	 */
	private void setUpSearch(String searchString) {
		swipeLayout.setRefreshing(false);
		this.searchText = searchString;
		this.isNewSearchResult = true;
		dbManager.clearSearchTable(getContentResolver());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(true);
			}
		}, 100);
		onRefresh();
	}
}
