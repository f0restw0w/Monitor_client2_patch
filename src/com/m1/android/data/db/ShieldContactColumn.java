package com.m1.android.data.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

public class ShieldContactColumn extends DataBaseColumns {
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_ADD_TIME = "addTime";
	public static final String TABLE_NAME = "_sheildContact";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap() {
		HashMap<String, String> tableCreator = new HashMap<String, String>();
		tableCreator.put(_ID, "integer primary key autoincrement  not null");
		tableCreator.put(COLUMN_PHONE, "text");
		tableCreator.put(COLUMN_ADD_TIME, "integer");
		return tableCreator;
	}

}
