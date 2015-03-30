package com.kanishk.tweetstream.operations;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * The listener interface for receiving customScroll events. The listener
 * checks whether the user has scrolled to the bottom of the list or not.
 * If user scrolls to the bottom of the list, the listener issues loadData
 * call to fetch more items.
 */
public abstract class CustomScrollListener implements OnScrollListener {

	/** The Constant ITEMS_PER_PAGE. The minimum items per page*/
	public static final int ITEMS_PER_PAGE = 20;

	/** The Constant ITEMS_TOTAL_LIMIT. The upper limit for maximum items to be shown.*/
	public static final int ITEMS_TOTAL_LIMIT = 600;

	/** The Constant PAGE_ITEMS_COUNT. */
	public static final String PAGE_ITEMS_COUNT = "page_items_count";

	/** The Constant PAGE_PREVIOUS_COUNT. */
	public static final String PAGE_PREVIOUS_COUNT = "page_previous_count";

	/** The scroll threshold. The number of items below the current position.
	 * If user scrolls below this position, the listener will fetch more items.*/
	private static int SCROLL_THRESHOLD = 3;

	/** The current scroll count. The current number of items shown on listview */
	private int currentScrollCount;

	/** The loading. The check whether currently there's any data loading or not*/
	private boolean loading;

	/** The previous total count. */
	private int previousTotalCount;

	/**
	 * Instantiates a new custom scroll listener.
	 */
	public CustomScrollListener() {
		currentScrollCount = ITEMS_PER_PAGE;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (currentScrollCount >= ITEMS_TOTAL_LIMIT
				|| totalItemCount < ITEMS_PER_PAGE) {
			return;
		}
		if (totalItemCount < previousTotalCount) {
			currentScrollCount = ITEMS_PER_PAGE;
			previousTotalCount = totalItemCount;
		}
		if (loading && previousTotalCount < totalItemCount) {
			previousTotalCount = totalItemCount;
			currentScrollCount += ITEMS_PER_PAGE;
			loading = false;
			return;
		}
		if (!loading
				&& totalItemCount - visibleItemCount - firstVisibleItem <= SCROLL_THRESHOLD
				&& visibleItemCount < totalItemCount) {
			loading = true;
			loadData(currentScrollCount + ITEMS_PER_PAGE);
		}
	}

	/**
	 * Reset scroll.
	 */
	public void resetScroll() {
		currentScrollCount = ITEMS_PER_PAGE;
		previousTotalCount = 0;
		loading = false;
	}

	/**
	 * Gets the current scroll count.
	 *
	 * @return the current scroll count
	 */
	public int getCurrentScrollCount() {
		return currentScrollCount;
	}

	/**
	 * Sets the current scroll count.
	 *
	 * @param currentScrollCount the new current scroll count
	 */
	public void setCurrentScrollCount(int currentScrollCount) {
		this.currentScrollCount = currentScrollCount;
	}

	/**
	 * Gets the previous total count.
	 *
	 * @return the previous total count
	 */
	public int getPreviousTotalCount() {
		return previousTotalCount;
	}

	/**
	 * Sets the previous total count.
	 *
	 * @param previousTotalCount the new previous total count
	 */
	public void setPreviousTotalCount(int previousTotalCount) {
		this.previousTotalCount = previousTotalCount;
	}

	/**
	 * Load data. The method for reloading data after scroll.
	 * This method must be implemented to fetch more data items.
	 * @param dataSize the number of items to fetch
	 */
	public abstract void loadData(int dataSize);

}
