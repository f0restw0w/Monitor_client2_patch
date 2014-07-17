package com.m1.android.data.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

public class SmsColums extends DataBaseColumns {

	public static final String COLUMN_CONTENT = "content";

	public static final String COLUMN_ADDRESS = "address";

	public static final String COLUMN_TYPE = "type";

	public static final String COLUMN_TIME = "time";

	public static final String COLUMN_CLIENT_SMS_ID = "client_sms_Id";

	public static final String TABLE_NAME = "_sms";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap() {
		HashMap<String, String> tableCreator = new HashMap<String, String>();
		tableCreator.put(_ID, "integer primary key autoincrement  not null");
		tableCreator.put(COLUMN_CONTENT, "text");
		tableCreator.put(COLUMN_ADDRESS, "text");
		tableCreator.put(COLUMN_TYPE, "integer");
		tableCreator.put(COLUMN_TIME, "integer");
		tableCreator.put(COLUMN_CLIENT_SMS_ID, "integer");
		return tableCreator;
	}

}
