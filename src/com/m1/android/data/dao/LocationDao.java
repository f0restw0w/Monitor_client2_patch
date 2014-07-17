package com.m1.android.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.m1.android.data.db.LocationColumns;
import com.m1.android.data.db.util.SqliteWrapper;
import com.m1.android.data.entity.Location;

public class LocationDao {
	public static final List<Location> getAllLocations(Context context) {
		Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), LocationColumns.CONTENT_URI, null, null, null, null);
		List<Location> locations = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				locations = new ArrayList<Location>();
				while (cursor.moveToNext()) {
					Location location = readFromCursor(cursor);
					locations.add(location);
				}
			}
			cursor.close();
		}
		return locations;
	}

	public static final void insertLocation(Context context, double longitude, double latitude, long time, String address) {
		ContentValues values = new ContentValues();
		values.put(LocationColumns.COLUMN_LATITUDE, latitude);
		values.put(LocationColumns.COLUMN_LONGITUDE, longitude);
		values.put(LocationColumns.COLUMN_TIME, time);
		values.put(LocationColumns.COLUMN_ADDRESS, address);
		SqliteWrapper.insert(context, context.getContentResolver(), LocationColumns.CONTENT_URI, values);
	}

	public static final void deleteLocation(Context context, long id) {
		SqliteWrapper.delete(context, context.getContentResolver(), LocationColumns.CONTENT_URI, LocationColumns._ID + "=?",
				new String[] { String.valueOf(id) });
	}

	private static final Location readFromCursor(Cursor cursor) {
		Location location = new Location();
		long time = cursor.getLong(cursor.getColumnIndex(LocationColumns.COLUMN_TIME));
		location.setAddTime(new Date(time));
		location.setId(cursor.getLong(cursor.getColumnIndex(LocationColumns._ID)));
		location.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationColumns.COLUMN_LATITUDE)));
		location.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationColumns.COLUMN_LONGITUDE)));
		location.setAddress(cursor.getString(cursor.getColumnIndex(LocationColumns.COLUMN_ADDRESS)));
		return location;
	}
}
