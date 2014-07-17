package com.m1.android.data.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

/**
 * 记录发送消息的时间
 * 
 * @author Administrator
 * 
 */
public class OutgoingSmsColumns extends DataBaseColumns {
	public static final String TABLE_NAME = "_outgoingsms";
	public static final String COLUMN_TIME = "sendTime";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap() {
		HashMap<String, String> tableCreator = new HashMap<String, String>();
		tableCreator.put(_ID, "integer primary key autoincrement  not null");
		tableCreator.put(COLUMN_TIME, "integer");
		return tableCreator;
	}

}
