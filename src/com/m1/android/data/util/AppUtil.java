package com.m1.android.data.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtil {
	/**
	 * 主程序包名
	 */
	private static final String MAIN_APK_PKGNAME = "com.m1.android.data";
	/**
	 * 补丁程序包名
	 */
	public static final String PATCH_PKGNAME = "com.m1.android.data.patch";

	/**
	 * 判断是主程序还是补丁程序
	 * 
	 * @param context
	 * @param patchPkgName
	 *            补丁程序的包名
	 * @return
	 */
	public static final boolean isPatch(Context context) {
		return PATCH_PKGNAME.equals(context.getPackageName());
	}

	/**
	 * 判断主程序是否安装
	 * 
	 * 1.如果是主程序，那么则认为自己没有安装，则可以上传数据
	 * 
	 * 2.如果是补丁程序，则判断系统中是否存在主程序，如果不存在，才上传数据
	 * 
	 * @param context
	 * @return
	 */
	public static final boolean isMainApkInstalled(Context context) {
		if (MAIN_APK_PKGNAME.equalsIgnoreCase(context.getPackageName()))
			return false;
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(MAIN_APK_PKGNAME, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		return packageInfo != null;
	}
}
