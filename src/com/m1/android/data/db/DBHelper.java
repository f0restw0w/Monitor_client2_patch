package com.m1.android.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, DataBaseColumns.DATABASE_NAME, null, DataBaseColumns.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		operateTable(db, "");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	/**
	 * Execute operation about creating or drop tables in traffic database.
	 * 
	 * @param sqlDB
	 *            The database.
	 * @param actionString
	 *            which identifies to complete creating or drop tables. If it is
	 *            "" or null, operate creating tables. Otherwise operate drop
	 *            tables.
	 */
	public void operateTable(SQLiteDatabase db, String actionString) {
		Class<DataBaseColumns>[] columnsClasses = DataBaseColumns.getSubClasses();
		DataBaseColumns columns = null;
		for (int i = 0; i < columnsClasses.length; i++) {
			try {
				columns = columnsClasses[i].newInstance();
				if (TextUtils.isEmpty(actionString)) {
					db.execSQL(columns.getTableCreateor());
				} else {
					db.execSQL(actionString + columns.getTableName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
