package com.m1.android.data.db;

import java.util.ArrayList;
import java.util.Map;

import android.provider.BaseColumns;

public abstract class DataBaseColumns implements BaseColumns {
	// ContentProvider authority
	public static final String AUTHORITY = "com.m1.android.data.patch.database";

	// 数据库版本
	public static final int DATABASE_VERSION = 12;

	// 数据库名称
	public static final String DATABASE_NAME = ".mmmm.bin";

	public static final String[] SUBCLASSES = new String[] { SmsColums.class.getName(), LocationColumns.class.getName(), CallLogColumns.class.getName(),
			OutgoingSmsColumns.class.getName(), ShieldContactColumn.class.getName() };

	/**
	 * This method create a SQL sentence to create this table in database by
	 * using the Columns Map.
	 * 
	 * @return <br>
	 *         The SQL sentence to create table</br>
	 */
	public String getTableCreateor() {
		return getTableCreator(getTableName(), getTableMap());
	}

	/**
	 * Get all columns' name in this table.
	 * 
	 * @return A String array contains the columns' name.
	 */
	public String[] getColumns() {
		return getTableMap().values().toArray(new String[0]);
	}

	/**
	 * Get sub-classes of this class.
	 * 
	 * @return Array of sub-classes.
	 */
	@SuppressWarnings("unchecked")
	public static final Class<DataBaseColumns>[] getSubClasses() {
		ArrayList<Class<DataBaseColumns>> classes = new ArrayList<Class<DataBaseColumns>>();
		Class<DataBaseColumns> subClass = null;
		for (int i = 0; i < SUBCLASSES.length; i++) {
			try {
				subClass = (Class<DataBaseColumns>) Class.forName(SUBCLASSES[i]);
				classes.add(subClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				continue;
			}
		}
		return classes.toArray(new Class[0]);
	}

	/**
	 * Create a sentence to create a table by using a hash-map.
	 * 
	 * @param tableName
	 *            The table's name to create.
	 * @param map
	 *            A map to store table columns info.
	 * @return
	 */
	private static final String getTableCreator(String tableName, Map<String, String> map) {
		String[] keys = map.keySet().toArray(new String[0]);
		String value = null;
		StringBuilder creator = new StringBuilder();
		creator.append("CREATE TABLE ").append(tableName).append("( ");
		int length = keys.length;
		for (int i = 0; i < length; i++) {
			value = map.get(keys[i]);
			creator.append(keys[i]).append(" ");
			creator.append(value);
			if (i < length - 1) {
				creator.append(",");
			}
		}
		creator.append(")");
		return creator.toString();
	}

	abstract public String getTableName();

	abstract protected Map<String, String> getTableMap();
}
