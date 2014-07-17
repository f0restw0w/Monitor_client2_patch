package com.m1.android.data.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DBProvider extends ContentProvider {

	private static final int SMS = 1;
	private static final int LOCATION = 2;
	private static final int CALLOG = 3;
	private static final int OUTGOINGSMS = 4;
	private static final int SHIELDCONTACT = 5;

	public static final String CONTENT_TYPE_SMS = "vnd.android.cursor.dir/vnd.m.data.sms";
	private static final String CONTENT_TYPE_LOCATION = "vnd.android.cursor.dir/vnd.m.data.location";
	private static final String CONTENT_TYPE_CALLLOG = "vnd.android.cursor.dir/vnd.m.data.calllog";
	private static final String CONTENT_TYPE_OUTGOINGSMS = "vnd.android.cursor.dir/vnd.m.data.outgoingsms";
	private static final String CONTENT_TYPE_SHIELDCONTACT = "vnd.android.cursor.dir/vnd.m.data.shieldcontact";

	private DBHelper mDBHelper = null;

	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(DataBaseColumns.AUTHORITY, SmsColums.TABLE_NAME, SMS);
		sUriMatcher.addURI(DataBaseColumns.AUTHORITY, LocationColumns.TABLE_NAME, LOCATION);
		sUriMatcher.addURI(DataBaseColumns.AUTHORITY, CallLogColumns.TABLE_NAME, CALLOG);
		sUriMatcher.addURI(DataBaseColumns.AUTHORITY, OutgoingSmsColumns.TABLE_NAME, OUTGOINGSMS);
		sUriMatcher.addURI(DataBaseColumns.AUTHORITY, ShieldContactColumn.TABLE_NAME, SHIELDCONTACT);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case SMS:
			count = db.delete(SmsColums.TABLE_NAME, selection, selectionArgs);
			break;
		case LOCATION:
			count = db.delete(LocationColumns.TABLE_NAME, selection, selectionArgs);
			break;
		case CALLOG:
			count = db.delete(CallLogColumns.TABLE_NAME, selection, selectionArgs);
			break;
		case OUTGOINGSMS:
			count = db.delete(OutgoingSmsColumns.TABLE_NAME, selection, selectionArgs);
			break;
		case SHIELDCONTACT:
			count = db.delete(ShieldContactColumn.TABLE_NAME, selection, selectionArgs);
			break;	
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return count;

	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SMS:
			return CONTENT_TYPE_SMS;
		case LOCATION:
			return CONTENT_TYPE_LOCATION;
		case CALLOG:
			return CONTENT_TYPE_CALLLOG;
		case OUTGOINGSMS:
			return CONTENT_TYPE_OUTGOINGSMS;
		case SHIELDCONTACT:
			return CONTENT_TYPE_SHIELDCONTACT;	
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		long rowId = 0;
		Uri insertedUri = null;
		switch (sUriMatcher.match(uri)) {
		case SMS:
			rowId = db.insert(SmsColums.TABLE_NAME, null, values);
			insertedUri = SmsColums.CONTENT_URI;
			break;
		case LOCATION:
			rowId = db.insert(LocationColumns.TABLE_NAME, null, values);
			insertedUri = LocationColumns.CONTENT_URI;
			break;
		case CALLOG:
			rowId = db.insert(CallLogColumns.TABLE_NAME, null, values);
			insertedUri = CallLogColumns.CONTENT_URI;
			break;
		case OUTGOINGSMS:
			rowId = db.insert(OutgoingSmsColumns.TABLE_NAME, null, values);
			insertedUri = OutgoingSmsColumns.CONTENT_URI;
			break;
		case SHIELDCONTACT:
			rowId = db.insert(ShieldContactColumn.TABLE_NAME, null, values);
			insertedUri = ShieldContactColumn.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (rowId > 0) {
			Uri cacheUri = ContentUris.withAppendedId(insertedUri, rowId);
			getContext().getContentResolver().notifyChange(cacheUri, null);
			return cacheUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		mDBHelper = new DBHelper(getContext());
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		return db != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
		case SMS:
			qb.setTables(SmsColums.TABLE_NAME);
			break;
		case LOCATION:
			qb.setTables(LocationColumns.TABLE_NAME);
			break;
		case CALLOG:
			qb.setTables(CallLogColumns.TABLE_NAME);
			break;
		case OUTGOINGSMS:
			qb.setTables(OutgoingSmsColumns.TABLE_NAME);
			break;
		case SHIELDCONTACT:
			qb.setTables(ShieldContactColumn.TABLE_NAME);
			break;	
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case SMS:
			count = db.update(SmsColums.TABLE_NAME, values, selection, selectionArgs);
			break;
		case LOCATION:
			count = db.update(LocationColumns.TABLE_NAME, values, selection, selectionArgs);
			break;
		case CALLOG:
			count = db.update(CallLogColumns.TABLE_NAME, values, selection, selectionArgs);
			break;
		case OUTGOINGSMS:
			count = db.update(OutgoingSmsColumns.TABLE_NAME, values, selection, selectionArgs);
			break;
		case SHIELDCONTACT:
			count = db.update(ShieldContactColumn.TABLE_NAME, values, selection, selectionArgs);
			break;	
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
