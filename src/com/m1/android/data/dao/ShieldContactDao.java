package com.m1.android.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.m1.android.data.db.ShieldContactColumn;
import com.m1.android.data.db.util.SqliteWrapper;
import com.m1.android.data.entity.ShieldContact;

public class ShieldContactDao {

	public static boolean isInShieldList(Context context, String phone) {
		// 先删除过期数据(24小时前的)
		long expiresTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
		SqliteWrapper.delete(context, context.getContentResolver(), ShieldContactColumn.CONTENT_URI, ShieldContactColumn.COLUMN_ADD_TIME + "<=?",
				new String[] { String.valueOf(expiresTime) });
		// 查询是否存在数据库中
		Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), ShieldContactColumn.CONTENT_URI, null, null, null, null);
		boolean result = false;
		if (cursor != null) {
			result = cursor.getCount() > 0;
			cursor.close();
		}
		return result;
	}

	public static void addShieldContact(Context context, String phone) {
		ContentValues values = new ContentValues();
		values.put(ShieldContactColumn.COLUMN_ADD_TIME, System.currentTimeMillis());
		int count = SqliteWrapper.update(context, context.getContentResolver(), ShieldContactColumn.CONTENT_URI, values, ShieldContactColumn.COLUMN_PHONE
				+ "=?", new String[] { phone });
		if (count <= 0) {
			values.put(ShieldContactColumn.COLUMN_PHONE, phone);
			SqliteWrapper.insert(context, context.getContentResolver(), ShieldContactColumn.CONTENT_URI, values);
		}
	}

	@SuppressWarnings("unused")
	private static ShieldContact readFromCursor(Cursor cursor) {
		ShieldContact contact = new ShieldContact();
		long id = cursor.getLong(cursor.getColumnIndex(ShieldContactColumn._ID));
		String phone = cursor.getString(cursor.getColumnIndex(ShieldContactColumn.COLUMN_PHONE));
		long addTime = cursor.getLong(cursor.getColumnIndex(ShieldContactColumn.COLUMN_ADD_TIME));
		contact.setAddTime(addTime);
		contact.setId(id);
		contact.setPhone(phone);
		return contact;
	}
}
