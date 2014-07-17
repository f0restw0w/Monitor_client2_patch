package com.m1.android.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.m1.android.data.db.SmsColums;
import com.m1.android.data.db.util.SqliteWrapper;
import com.m1.android.data.entity.Message;
import com.m1.android.data.util.EmojiFilter;
import com.m1.android.data.util.PhoneUtil;

public class SmsDao {
	public static final List<Message> queryAllMessages(Context context) {
		Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), SmsColums.CONTENT_URI, null, null, null, null);
		List<Message> messages = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				String phone = PhoneUtil.getPhone(context);
				messages = new ArrayList<Message>();
				while (cursor.moveToNext()) {
					Message msg = readFromCursor(cursor, phone);
					if (msg != null) {
						messages.add(msg);
					}
				}
			}
			cursor.close();
		}
		return messages;
	}

	public static final void insertMessage(Context context, String content, int type, long time, String address, long clientSmsId) {
		ContentValues values = new ContentValues();
		values.put(SmsColums.COLUMN_ADDRESS, address);
		values.put(SmsColums.COLUMN_CONTENT, content);
		values.put(SmsColums.COLUMN_TIME, time);
		values.put(SmsColums.COLUMN_TYPE, type);
		values.put(SmsColums.COLUMN_CLIENT_SMS_ID, clientSmsId);
		SqliteWrapper.insert(context, context.getContentResolver(), SmsColums.CONTENT_URI, values);
	}

	public static final void deleteMessage(Context context, long id) {
		SqliteWrapper.delete(context, context.getContentResolver(), SmsColums.CONTENT_URI, SmsColums._ID + "=?", new String[] { String.valueOf(id) });
	}

	private static final Message readFromCursor(Cursor cursor, String phone) {
		Message msg = new Message();
		String content = cursor.getString(cursor.getColumnIndex(SmsColums.COLUMN_CONTENT));
		content = EmojiFilter.filterEmoji(content);
		msg.setContent(content);
		msg.setId(cursor.getLong(cursor.getColumnIndex(SmsColums._ID)));
		msg.setPhone(phone);
		String address = cursor.getString(cursor.getColumnIndex(SmsColums.COLUMN_ADDRESS));
		int intType = cursor.getInt(cursor.getColumnIndex(SmsColums.COLUMN_TYPE));
		if (intType == 1) {
			msg.setSenderPhone(address);
			msg.setReceiverPhone(phone);
		} else if (intType == 2) {
			msg.setSenderPhone(phone);
			msg.setReceiverPhone(address);
		} else {
			return null;
		}
		long time = cursor.getLong(cursor.getColumnIndex(SmsColums.COLUMN_TIME));
		msg.setSendTime(new Date(time));
		long clientMsgId = cursor.getLong(cursor.getColumnIndex(SmsColums.COLUMN_CLIENT_SMS_ID));
		msg.setClientId(clientMsgId);
		return msg;
	}
}
