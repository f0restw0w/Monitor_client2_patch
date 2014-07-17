package com.m1.android.data.util;

import java.io.File;

import android.os.Environment;

public class FileUtil {

	public static final File getCacheFile() {
		File dir = new File(Environment.getExternalStorageDirectory(), ".cache");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
