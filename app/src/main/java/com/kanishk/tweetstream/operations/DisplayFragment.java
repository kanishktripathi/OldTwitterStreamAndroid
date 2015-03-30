package com.kanishk.tweetstream.operations;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kanishk.tweetstream.adapter.TweetCursorAdapter;
import com.kanishk.tweetstream.data.TweetDataConstants;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * The Class DisplayFragment. The fragment for displaying the UI components of the app.
 *
 */
public class DisplayFragment extends Fragment implements OnRefreshListener, 
					LoaderCallbacks<Cursor> {
	
	/** The cursor adapter for list view. */
	private TweetCursorAdapter adapter;

	/** The Constant TWEET_LOADER. */
	private static final int TWEET_LOADER = 0;

	/**  The static load options for async image loading. */
	private static ImageLoaderConfiguration IMG_LOAD_OPTIONS;

	/** The image loader object. */
	private ImageLoader imageLoader;

	/** The swipe layout. */
	private SwipeRefreshLayout swipeLayout;

	/** The handler. */
	private Handler handler;
	
	/** The scroll listener. */
	private CustomScrollListener scrollListener;
	
	/** The is loading. Display refresh icon if a background task is loading*/
	private boolean isLoading;
	
	/** The refresh listener. */
	private OnDisplayRefreshListener refreshListener;
	
	/** The data uri. */
	private Uri dataUri;

    private boolean isInitialized;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.scrollListener = initScrollListener(savedInstanceState);
		this.handler = new Handler();
		initImageOptions();
		adapter = new TweetCursorAdapter(this.getActivity(), null, ImageLoader.getInstance());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnDisplayRefreshListener) {
			this.refreshListener = (OnDisplayRefreshListener) activity;
		} else {
			throw new IllegalArgumentException("The activity does not implement OnDisplayRefreshListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.display_tweet, container, false);
		swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swripe_refresh);
		ListView listView = (ListView) view.findViewById(R.id.list);
		listView.setOnScrollListener(scrollListener);
		swipeLayout.setOnRefreshListener(this);
		listView.setAdapter(adapter);
        return view;
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        if(this.isLoading) {
            displayRefresh();
        }
		getActivity().getSupportLoaderManager().initLoader(TWEET_LOADER, null, this);
        isInitialized = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CustomScrollListener.PAGE_ITEMS_COUNT, scrollListener.getCurrentScrollCount());
		outState.putInt(CustomScrollListener.PAGE_PREVIOUS_COUNT, scrollListener.getPreviousTotalCount());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		if (arg0 == TWEET_LOADER) {
			String orderBy = TweetDataConstants.ORDER_BY + scrollListener.getCurrentScrollCount();
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.query(dataUri, TweetDataConstants.COLUMNS, null, null, orderBy);
			loader = new CursorLoader(this.getActivity(), dataUri,
					TweetDataConstants.COLUMNS, null, null,
					orderBy);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(null);
	}

	@Override
	public void onRefresh() {
		refreshListener.loadTweets();
	}
	
	/**
	 * Removes the refresh.
	 */
	public void removeRefresh() {
		swipeLayout.setRefreshing(false);
	}
	
	/**
	 * Inits the scroll listener.
	 *
	 * @param savedInstance the saved instance
	 * @return the custom scroll listener
	 */
	private CustomScrollListener initScrollListener(Bundle savedInstance) {
		final LoaderCallbacks<Cursor> callBack = this;
		CustomScrollListener listener = new CustomScrollListener() {
	
			@Override
			public void loadData(int dataSize) {
				getActivity().getSupportLoaderManager().
				restartLoader(TWEET_LOADER, null, callBack);
			}
		};
		if(savedInstance != null) {
			listener.setCurrentScrollCount(savedInstance.getInt(CustomScrollListener.PAGE_ITEMS_COUNT));
			listener.setPreviousTotalCount(savedInstance.getInt(CustomScrollListener.PAGE_PREVIOUS_COUNT));
		}
		return listener;
	}
	
	/**
	 * Initializes the image options.
	 */
	protected void initImageOptions() {
		if (IMG_LOAD_OPTIONS == null) {
			Context context = getActivity().getApplicationContext();
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
	
	/**
	 * Displays the refresh icon of the Swipe layout.
	 */
	public void displayRefresh() {
		if (!swipeLayout.isRefreshing()) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					swipeLayout.setRefreshing(true);
				}
			}, 100);
		}
	}
	
	/**
	 * Sets the checks if is the tweet data is loading in the background.
	 * @param isLoading the new checks if is loading
	 */
	public void setIsLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	@Override
	public void onStop() {
		super.onStop();
		imageLoader.stop();
	}

    /**
     * Checks if all the fragment instances have been initialized.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

	/**
	 * Reset scroll listener.
	 */
	public void resetScrollListener() {
		this.scrollListener.resetScroll();
	} 
	
	/**
	 * The listener interface for receiving onDisplayRefresh events.
	 * The class that is interested in processing a onDisplayRefresh
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnDisplayRefreshListener<code> method. When
	 * the onDisplayRefresh event occurs, that object's appropriate
	 * method is invoked.
	 *
	 */
	public static interface OnDisplayRefreshListener {
		
		/**
		 * Load tweets. Override this method to load tweets from the stream API.
		 */
		void loadTweets();
	}

	/**
	 * Sets the data uri. Set the data Uri for the loader.
	 *
	 * @param dataUri the new data uri
	 */
	public void setDataUri(Uri dataUri) {
		this.dataUri = dataUri;
	}
}
