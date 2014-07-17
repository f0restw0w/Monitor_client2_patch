/**
 * 
 */

package com.m1.android.data.http;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 手机接入点，常用工具，可以获取当前手机通过哪种方式接入到网络
 * 
 * @author zhaozhongyang
 * 
 * @since 2012-5-24上午10:10:33
 */
public class ApnUtil {
	/**
	 * APN数据访问地址
	 */
	private static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

	/**
	 * 判断是否为3G网络，只有网络接入点是cmnet、ctnet和3gnet才是3G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean is3G(Context context) {
		String type = getApnType(context);

		return type.equals(ApnNet.CMNET) || type.equals(ApnNet.CTNET) || type.equals(ApnNet.GNET_3);
	}

	/**
	 * 获取手机网络接入点类型
	 * 
	 * @param context
	 * @return
	 */
	public static String getApnType(Context context) {
		String type = "unknown";
		try {
			Cursor c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					String apn = c.getString(c.getColumnIndex("apn"));
					if (TextUtils.isEmpty(apn)) {
						type = "unknown";
					} else if (apn.startsWith(ApnNet.CTNET)) {
						type = ApnNet.CTNET;
					} else if (apn.startsWith(ApnNet.CTWAP)) {
						type = ApnNet.CTWAP;
					} else if (apn.startsWith(ApnNet.CMWAP)) {
						type = ApnNet.CMWAP;
					} else if (apn.startsWith(ApnNet.CMNET)) {
						type = ApnNet.CMNET;
					} else if (apn.startsWith(ApnNet.GWAP_3)) {
						type = ApnNet.GWAP_3;
					} else if (apn.startsWith(ApnNet.GNET_3)) {
						type = ApnNet.GNET_3;
					} else if (apn.startsWith(ApnNet.UNIWAP)) {
						type = ApnNet.UNIWAP;
					} else if (apn.startsWith(ApnNet.UNINET)) {
						type = ApnNet.UNINET;
					}
				}
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			type = "unknown";
		}

		return type;
	}
}
