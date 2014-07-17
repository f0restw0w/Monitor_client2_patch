package com.m1.android.data.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.m1.android.data.entity.Sms;

public class SmsUtil {
	public static final String SMS_URI_ALL = "content://sms/";

	public static final List<Sms> loadSms(Context context, long id, String phone) {
		List<Sms> list = new ArrayList<Sms>();
		try {
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] projection = new String[] { "_id", "address", "body", "date", "type" };
			Cursor cur = null;
			if (id == 0) {
				cur = context.getContentResolver().query(uri, projection, null, null, " _id desc limit 5");
			} else {
				cur = context.getContentResolver().query(uri, projection, " _id>?", new String[] { String.valueOf(id) }, " _id desc");
			}

			if (cur != null) {
				if (cur.moveToFirst()) {
					do {
						String address = cur.getString(cur.getColumnIndex("address"));
						if (!TextUtils.isEmpty(address)) {
							address = PhoneUtil.formatPhone(address);
						}
						int intType = cur.getInt(cur.getColumnIndex("type"));
						Sms sms = new Sms();
						sms.setType(intType);
						sms.setId(cur.getLong(cur.getColumnIndex("_id")));
						sms.setContent(cur.getString(cur.getColumnIndex("body")));
						sms.setAddress(address);
						long dateTime = cur.getLong(cur.getColumnIndex("date"));
						sms.setTime(dateTime);
						list.add(sms);
					} while (cur.moveToNext());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static long getMaxId(List<Sms> list) {
		long id = 0;
		for (int i = 0; i < list.size(); i++) {
			Sms sms = list.get(i);
			try {
				long mid = sms.getId();
				if (mid > id) {
					id = mid;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public static final void deleteSms(Context context, long id) {
		try {
			Uri uri = Uri.parse(SMS_URI_ALL);
			context.getContentResolver().delete(uri, " _id=? ", new String[] { String.valueOf(id) });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
