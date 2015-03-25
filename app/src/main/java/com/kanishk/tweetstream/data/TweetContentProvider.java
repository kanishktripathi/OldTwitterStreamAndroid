package com.kanishk.tweetstream.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class TweetContentProvider extends ContentProvider {

	public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS tweets (_id INTEGER PRIMARY KEY "
			+ "autoincrement, screen_name TEXT, name TEXT, image_url TEXT, tweet TEXT)";
	public static final String CREATE_SEARCH_QUERY = "CREATE TABLE IF NOT EXISTS TweetSearch (_id INTEGER "
			+ "PRIMARY KEY autoincrement, screen_name TEXT, name TEXT, image_url TEXT, tweet TEXT)";
	public static final int TWEETS = 1;
	public static final int TWEETS_SEARCH = 2;
	private static final UriMatcher matcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private SQLiteOpenHelper helper;
	static {
		matcher.addURI(TweetDataConstants.AUTHORITY,
				TweetDataConstants.TWEET_TABLE, TWEETS);
		matcher.addURI(TweetDataConstants.AUTHORITY,
				TweetDataConstants.SEARCH_TABLE, TWEETS_SEARCH);
	}

	@Override
	public boolean onCreate() {
		helper = new DBHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor readCursor = null;
		switch (matcher.match(uri)) {
		case TWEETS:
			readCursor = db.query(TweetDataConstants.TWEET_TABLE, projection,
					selection, selectionArgs, null, null, sortOrder);
			break;
		case TWEETS_SEARCH:
			readCursor = db.query(TweetDataConstants.SEARCH_TABLE, projection,
					selection, selectionArgs, null, null, sortOrder);
			break;
		}
		if(readCursor != null) {
			readCursor.setNotificationUri(getContext().getContentResolver(), uri);			
		}
		return readCursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long retVal = 0;
		SQLiteDatabase db;
		switch (matcher.match(uri)) {
		case TWEETS:
			db = helper.getWritableDatabase();
			retVal = db.insert(TweetDataConstants.TWEET_TABLE, null, values);
			break;
		case TWEETS_SEARCH:
			db = helper.getWritableDatabase();
			retVal = db.insert(TweetDataConstants.SEARCH_TABLE, null, values);
			break;
		}
		if (retVal != -1) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int retVal = 0;
		SQLiteDatabase db;
		switch (matcher.match(uri)) {
		case TWEETS:
			db = helper.getWritableDatabase();
			retVal = insertData(db, TweetDataConstants.TWEET_TABLE, values);
			break;
		case TWEETS_SEARCH:
			db = helper.getWritableDatabase();
			retVal = insertData(db, TweetDataConstants.SEARCH_TABLE, values);
			break;
		}
		if (retVal > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return retVal;
	}

	private int insertData(SQLiteDatabase database, String tableName, ContentValues... values) {
		database.beginTransaction();
		int retVal = 0;
		try {
			int length = values.length;
			for (int i = 0; i < length; i++) {
				database.insert(tableName, null, values[i]);
				retVal++;
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		return retVal;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = matcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case TWEETS:
			rowsDeleted = sqlDB.delete(TweetDataConstants.TWEET_TABLE,
					selection, selectionArgs);
			break;
		case TWEETS_SEARCH:
			rowsDeleted = sqlDB.delete(TweetDataConstants.SEARCH_TABLE,
					selection, selectionArgs);
			break;
		}
		if(rowsDeleted > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	public void close() {
		helper.close();
	}

	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, TweetDataConstants.DB_NAME, null,
					TweetDataConstants.VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_QUERY);
			db.execSQL(CREATE_SEARCH_QUERY);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TweetDataConstants.TWEET_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "
					+ TweetDataConstants.SEARCH_TABLE);
			onCreate(db);
		}

	}

}
