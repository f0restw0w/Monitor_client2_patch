package com.m1.android.data.util;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.m1.android.data.entity.Contact;
import com.m1.android.data.entity.User;

public class LocalManager {
	private static final String FILE_NAME = "cache";
	private static final String USER_INFO = "user_info";
	private static final String SMS_ID = "_id";
	private static final String PHONE = "phone";
	private static final String FIRST_IN = "first_in";
	private static final String CONTACT_LIST = "contact_list";
	private static final String IMEI = "imei_code";
	private static final String ALBUM_ID = "album_id";
	private static final String CALLLOG_NUMBER = "calllog_number";
	private static final String CALLLOG_OUTGOING = "calllog_outgoing";

	public static final long getSmsId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getLong(SMS_ID, 0);
	}

	public static final void setSmsId(Context context, long id) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putLong(SMS_ID, id);
		edit.commit();
	}

	public static final User getUser(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		String userStr = preferences.getString(USER_INFO, null);
		Gson gson = new Gson();
		return gson.fromJson(userStr, User.class);
	}

	public static final void setUser(Context context, User user) {
		if (user == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		Gson gson = new Gson();
		edit.putString(USER_INFO, gson.toJson(user));
		edit.commit();
	}

	public static final String getPhone(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getString(PHONE, "");
	}

	public static final void setPhone(Context context, String phone) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putString(PHONE, phone);
		edit.commit();
	}

	public static final boolean isFirst(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		boolean first = preferences.getBoolean(FIRST_IN, false);
		if (!first) {
			Editor editor = preferences.edit();
			editor.putBoolean(FIRST_IN, true);
			editor.commit();
		}
		return first;
	}

	public static final void updateContactList(Context context,
			List<Contact> contacts) {
		if (contacts != null && !contacts.isEmpty()) {
			Gson gson = new Gson();
			SharedPreferences preferences = context.getSharedPreferences(
					FILE_NAME, Context.MODE_PRIVATE);
			List<Contact> uploadedContacts = getContactList(context);
			if (uploadedContacts != null && uploadedContacts.isEmpty()) {
				contacts.addAll(uploadedContacts);
			}
			Editor edit = preferences.edit();
			edit.putString(CONTACT_LIST, gson.toJson(contacts));
			edit.commit();
		}

	}

	public static final List<Contact> getContactList(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		String contactStr = preferences.getString(CONTACT_LIST, null);
		List<Contact> contacts = null;
		if (!TextUtils.isEmpty(contactStr)) {
			Gson gson = new Gson();
			try {
				java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Contact>>() {
				}.getType();
				contacts = gson.fromJson(contactStr, type);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			}
		}
		return contacts;
	}

	public static final void setImei(Context context, String imei) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(IMEI, imei);
		editor.commit();
	}

	public static final String getImei(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getString(IMEI, null);
	}

	public static final long getAlbumId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getLong(ALBUM_ID, 0);
	}

	public static final void setAlbumId(Context context, long id) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(ALBUM_ID, id);
		editor.commit();
	}

	public static final void setCallPhone(Context context, String phone,
			boolean outgoing) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(CALLLOG_NUMBER, phone);
		editor.putBoolean(CALLLOG_OUTGOING, outgoing);
		editor.commit();
	}

	public static final void clearCallPhone(Context context) {
		setCallPhone(context, null, false);
	}

	public static final String getCallPhone(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getString(CALLLOG_NUMBER, null);
	}

	public static final boolean isOutGoing(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getBoolean(CALLLOG_OUTGOING, false);
	}

	public static final long getActionLastExcuteTime(Context context,
			String actionName) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return preferences.getLong(actionName, 0);
	}

	public static final void setActionLastExcuteTime(Context context,
			String actionName, long time) {
		SharedPreferences preferences = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(actionName, time);
		editor.commit();
	}

}
