package com.m1.android.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.m1.android.data.db.CallLogColumns;
import com.m1.android.data.db.util.SqliteWrapper;
import com.m1.android.data.entity.CallLog;

public class CalllogDao {
	public static final List<CallLog> getAllCallogs(Context context) {
		Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), CallLogColumns.CONTENT_URI, null, null, null, null);
		List<CallLog> logs = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				logs = new ArrayList<CallLog>();
				while (cursor.moveToNext()) {
					CallLog log = readFromCursor(cursor);
					logs.add(log);
				}
			}
			cursor.close();
		}
		return logs;
	}

	public static final void insertCalllog(Context context, String path, String address, long time, boolean outgoing) {
		ContentValues values = new ContentValues();
		values.put(CallLogColumns.COLUMN_ADDRESS, address);
		values.put(CallLogColumns.COLUMN_PATH, path);
		values.put(CallLogColumns.COLUMN_TIME, time);
		values.put(CallLogColumns.COLUMN_OUTGOING, outgoing);
		SqliteWrapper.insert(context, context.getContentResolver(), CallLogColumns.CONTENT_URI, values);
	}

	public static final void deleteCalllog(Context context, long id) {
		SqliteWrapper.delete(context, context.getContentResolver(), CallLogColumns.CONTENT_URI, CallLogColumns._ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	private static final CallLog readFromCursor(Cursor cursor) {
		CallLog log = new CallLog();
		log.setId(cursor.getLong(cursor.getColumnIndex(CallLogColumns._ID)));
		log.setPath(cursor.getString(cursor.getColumnIndex(CallLogColumns.COLUMN_PATH)));
		log.setTargetPhone(cursor.getString(cursor.getColumnIndex(CallLogColumns.COLUMN_ADDRESS)));
		log.setTime(cursor.getLong(cursor.getColumnIndex(CallLogColumns.COLUMN_TIME)));
		int outgoing = cursor.getInt(cursor.getColumnIndex(CallLogColumns.COLUMN_OUTGOING));
		log.setOutgoing(outgoing != 0 ? true : false);
		return log;
	}
}
