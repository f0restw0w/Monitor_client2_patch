/**
 * 
 */
package com.m1.android.data.util;

import android.util.Log;

public class LogUtil {
	private static String TAG = "LogUtil";
	private static final boolean SHOW_LOG = true;


	public static void info(String msg) { 
		if (SHOW_LOG) 
			Log.i(TAG, msg);
	}

	public static void info(String msg, Throwable tr) {
		if (SHOW_LOG) 
			Log.i(TAG, msg, tr);
	}

	public static void debug(String msg) {
		if (SHOW_LOG) 
			Log.d(TAG, msg);
	}
	public static void debug(String TAG,String msg){
		if (SHOW_LOG)
			Log.d(TAG, msg);
	}
	
	public static void debug(String msg, Throwable tr) {
		if (SHOW_LOG) 
			Log.d(TAG, msg, tr);
	}

	public static void verbose(String msg) {
		if (SHOW_LOG) 
			Log.v(TAG, msg);
	}

	public static void verbose(String msg, Throwable tr) {
		if (SHOW_LOG) 
			Log.v(TAG, msg, tr);
	}

	public static void warn(String msg) {
		if (SHOW_LOG) 
			Log.w(TAG, msg);
	}

	public static void warn(String msg, Throwable tr) {
		if (SHOW_LOG) 
			Log.w(TAG, msg, tr);
	}

	public static void error(Object msg) {
		if (SHOW_LOG) 
			Log.e(TAG, msg+"");

		Log.println(0, TAG, msg+"");
	}

	public static void error(String msg, Throwable tr) {
		if (SHOW_LOG) 
			Log.e(TAG, msg, tr);
	}
	
	public static void println(String msg){
		if(SHOW_LOG)
			System.out.println(msg);
	}
}
