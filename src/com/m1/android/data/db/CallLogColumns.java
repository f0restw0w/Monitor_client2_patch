package com.m1.android.data.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

public class CallLogColumns extends DataBaseColumns {
	public static final String COLUMN_PATH = "path";

	public static final String COLUMN_TIME = "time";

	public static final String COLUMN_ADDRESS = "address";

	public static final String COLUMN_OUTGOING = "outgoing";
	public static final String TABLE_NAME = "_calllog";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap() {
		HashMap<String, String> tableCreator = new HashMap<String, String>();
		tableCreator.put(_ID, "integer primary key autoincrement  not null");
		tableCreator.put(COLUMN_PATH, "text");
		tableCreator.put(COLUMN_ADDRESS, "text");
		tableCreator.put(COLUMN_TIME, "integer");
		tableCreator.put(COLUMN_OUTGOING, "integer");
		return tableCreator;
	}

}
