package com.m1.android.data.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class PhoneUtil {
	public static final String getPhone(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String phone = tm.getLine1Number();
		if (TextUtils.isEmpty(phone)) {
			phone = LocalManager.getPhone(context);
		}
		return formatPhone(phone);
	}

	public static final String formatPhone(String phone) {
		if (!TextUtils.isEmpty(phone)) {
			String regEx = "[^\\d]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(phone);
			// 替换与模式匹配的所有字符（即非数字的字符将被""替换）
			phone = m.replaceAll("").trim();
		}
		return phone;
	}

	public static final String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		if (TextUtils.isEmpty(imei)) {
			imei = LocalManager.getImei(context);
		}
		return imei;
	}

	public static final String getModel() {
		return Build.MODEL;
	}

	public static final String getVersion() {
		return Build.VERSION.RELEASE;
	}
}
