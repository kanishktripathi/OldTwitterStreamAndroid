package com.kanishk.tweetstream.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kanishk.tweetstream.data.TweetDataConstants;
import com.kanishk.tweetstream.operations.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * The Class TweetCursorAdapter.
 */
public class TweetCursorAdapter extends CursorAdapter {
	
	/** The image loader. */
	private ImageLoader imageLoader;
	
	/**
	 * Instantiates a new tweet cursor adapter.
	 *
	 * @param context the context
	 * @param c the c
	 * @param loader the loader
	 */
	public TweetCursorAdapter(Context context, Cursor c, ImageLoader loader) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		this.imageLoader = loader;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View retView = inflater.inflate(R.layout.list_layout, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.screenName = (TextView) retView
				.findViewById(R.id.user_name);
		holder.name = (TextView) retView.findViewById(R.id.user_id);
		holder.tweet = (TextView) retView.findViewById(R.id.tweet);
		holder.image = (ImageView) retView.findViewById(R.id.image);
		retView.setTag(holder);
		return retView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		cursor.getString(0);
		holder.name.setText(cursor.getString(0));
		holder.screenName.setText(cursor.getString(1));
		holder.tweet.setText(cursor.getString(3));
		imageLoader.displayImage(cursor.getString(2), holder.image, 
				TweetDataConstants.IMAGE_DISPLAY_OPTIONS);
	}

	/**
	 * The Class ViewHolder. A holder class to avoid calling findview by id
	 * in thr adapter.
	 */
	private static class ViewHolder {
		
		/** The screen name. */
		TextView screenName;
		
		/** The name. */
		TextView name;
		
		/** The tweet. */
		TextView tweet;
		
		/** The image. */
		ImageView image;
	}
}
