package com.kanishk.tweetstream.operations;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.Toast;

import com.kanishk.tweetstream.data.DBUpdateManager;
import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.model.Tweet;
import com.kanishk.tweetstream.operations.DisplayFragment.OnDisplayRefreshListener;
import com.kanishk.tweetstream.operations.TweetTaskFragment.TaskFragmentListener;

import java.util.List;

/**
 * The Class TweetActivity. The activity for displaying tweets without filter.
 */
public class TweetActivity extends FragmentActivity implements TaskFragmentListener, OnDisplayRefreshListener {

	protected TweetTaskFragment taskFragment;
	
	protected DisplayFragment displayFragment;

    private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		FragmentManager manager = getSupportFragmentManager();
		taskFragment = (TweetTaskFragment) getSupportFragmentManager()
				.findFragmentByTag(TweetDataConstants.TASK_FRAGMENT);
		if (taskFragment == null) {
			taskFragment = new TweetTaskFragment();
			manager.beginTransaction().add(taskFragment, 
					TweetDataConstants.TASK_FRAGMENT).commit();
		}
		if(savedInstanceState != null) {
			displayFragment = (DisplayFragment) manager.findFragmentById(R.id.container);
            setUpDisplayFragment();
		}
		if(displayFragment == null) {
			displayFragment = new DisplayFragment();
            setUpDisplayFragment();
            manager.beginTransaction().add(R.id.container, displayFragment).commit();
        }
    }

    private void  setUpDisplayFragment() {
        displayFragment.setDataUri(getDataUri());
        if (taskFragment.isLoading()) {
            displayFragment.setIsLoading(true);
        }
    }
	
	protected Uri getDataUri() {
		return TweetDataConstants.CONTENT_URI;
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
        this.searchView = searchView;
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
                searchView.clearFocus();
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
	public void onUpdateTweets(List<Tweet> tweetList) {
		displayFragment.removeRefresh();
		if(tweetList != null && !tweetList.isEmpty()) {
			DBUpdateManager dbManager = DBUpdateManager.getInstance();
			dbManager.insertRows(tweetList, getContentResolver());			
		}
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
	public void loadTweets() {
		if (hasNetAccess()) {
			taskFragment.refresh();
		} else {
			displayMessage(getString(R.string.net_connect_error));
			displayFragment.removeRefresh();
		}		
	}
}
