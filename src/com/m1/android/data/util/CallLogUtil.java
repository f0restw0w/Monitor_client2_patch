package com.m1.android.data.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.text.TextUtils;

public class CallLogUtil {
	public static synchronized void deleteRecentLog(Context context, String number) {
		if (TextUtils.isEmpty(number))
			return;
		try {
			Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, Calls.NUMBER + "=?", new String[] { number }, Calls.DATE + " desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					long time = cursor.getLong(cursor.getColumnIndex(Calls.DATE));
					if (System.currentTimeMillis() - time < 10 * 1000) {
						long _id = cursor.getLong(cursor.getColumnIndex(Calls._ID));
						context.getContentResolver().delete(Calls.CONTENT_URI, Calls._ID + "=?", new String[] { String.valueOf(_id) });
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void deleteCalllogByPhone(Context context, String number) {
		if (TextUtils.isEmpty(number))
			return;
		try {
			Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, Calls.NUMBER + "=?", new String[] { number },
					Calls.DATE + " desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					long _id = cursor.getLong(cursor.getColumnIndex(Calls._ID));
					context.getContentResolver().delete(Calls.CONTENT_URI, Calls._ID + "=?", new String[] { String.valueOf(_id) });
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
