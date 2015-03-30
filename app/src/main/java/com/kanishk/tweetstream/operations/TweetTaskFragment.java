package com.kanishk.tweetstream.operations;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kanishk.tweetstream.model.Tweet;
import com.kanishk.tweetstream.task.ConnectionTask.TweetUpdateListener;
import com.kanishk.tweetstream.task.TwitterClient;

/**
 * The Class TweetTaskFragment. The headless fragment for maintaining
 * the connection client and background tasks. The fragment retains it's instance
 * on configuration change.
 */
public class TweetTaskFragment extends Fragment implements TweetUpdateListener {
	
	/** The temp list. */
	private List<Tweet> tempList;
	
	/** The is loading. */
	private boolean isLoading;
	
	/** The twitter client. */
	private TwitterClient  twitterClient;
	
	/** The listener. */
	private TaskFragmentListener listener;
	
	/** The is new search. */
	private boolean isNewSearch;
	
	/** The search text. */
	private String searchText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.twitterClient = new TwitterClient(this);
		setRetainInstance(true);
	}

	@Override
	public void updateUI(List<Tweet> tweetList) {
		isLoading = false;
		if(isAdded()) {
			listener.onUpdateTweets(tweetList);
			this.tempList = null;
		} else {
			this.tempList = tweetList;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof TaskFragmentListener) {
			this.listener = (TaskFragmentListener) activity;
		} else {
			throw new IllegalArgumentException("Expecting an implementation of TaskFragmentListener");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(this.tempList != null) {
			listener.onUpdateTweets(tempList);
		} else if(isNewSearch && searchText != null) {
			refresh(searchText, isNewSearch);
			isNewSearch = false;
			searchText = null;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}
	
	/**
	 * Checks if a task is running in the background.
	 *
	 * @return true, if is running
	 */
	public boolean isLoading() {
		return this.isLoading;
	}
	
	/**
	 * Refresh.
	 */
	public void refresh() {
		isLoading = true;
		twitterClient.downloadTweets();
	}
	
	/**
	 * Refresh. Loads the tweet data.
	 *
	 * @param searchText the search text
	 * @param isNewSearch the is new search
	 */
	public void refresh(String searchText, boolean isNewSearch) {
		isLoading = true;
		twitterClient.downloadTweets(searchText, isNewSearch);
	}
	
	/**
	 * Sets the up init search. The method should called when the calling activity
	 * wants to sets the search string for filter API. The search API will be called
	 * if there's a search text and the fragement is attached to an activity.
	 * @param searchText the new up init search
	 */
	public void setupInitSearch(String searchText) {
		this.searchText = searchText;
		this.isNewSearch = true;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        twitterClient.closeAndRelease();
    }

    /**
	 * The listener interface for receiving taskFragment events.
	 * The class that is interested in processing a taskFragment
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTaskFragmentListener<code> method. When
	 * the taskFragment event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TaskFragmentEvent
	 */
	public static interface TaskFragmentListener {
		
		/**
		 * On udate tweets. Sends the updated tweets to the activity
		 * for further processing.
         * @param tweetList the tweet list
         */
		void onUpdateTweets(List<Tweet> tweetList);
	}
	
}
