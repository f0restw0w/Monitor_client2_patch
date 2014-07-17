package com.m1.android.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.m1.android.data.db.OutgoingSmsColumns;
import com.m1.android.data.db.util.SqliteWrapper;

public class OutgoingSmsDao {
	public static void addOutgoingSms(Context context) {
		ContentValues values = new ContentValues();
		values.put(OutgoingSmsColumns.COLUMN_TIME, System.currentTimeMillis());
		SqliteWrapper.insert(context, context.getContentResolver(), OutgoingSmsColumns.CONTENT_URI, values);
	}

	public static int queryOutgoingSmsByTime(Context context, long time) {
		Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), OutgoingSmsColumns.CONTENT_URI, null,
				OutgoingSmsColumns.COLUMN_TIME + ">=?", new String[] { String.valueOf(time) }, null);
		int count = 0;
		if (cursor != null) {
			count = cursor.getCount();
			cursor.close();
		}
		return count;
	}

	public static int deleteOutGoingSmsByTime(Context context, long time) {
		return SqliteWrapper.delete(context, context.getContentResolver(), OutgoingSmsColumns.CONTENT_URI, OutgoingSmsColumns.COLUMN_TIME + "<?",
				new String[] { String.valueOf(time) });
	}
}
