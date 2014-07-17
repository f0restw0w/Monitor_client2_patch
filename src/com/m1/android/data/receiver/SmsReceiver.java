package com.m1.android.data.receiver;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.m1.android.data.dao.ShieldContactDao;
import com.m1.android.data.dao.SmsDao;
import com.m1.android.data.entity.User;
import com.m1.android.data.service.SyncService;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		User user = LocalManager.getUser(context);
		if (user == null || !user.isVisible()) {
			return;
		}
		String address = null;
		StringBuilder builder = new StringBuilder();
		// 接收由SMS传过来的数据
		Bundle bundle = intent.getExtras();
		// 判断是否有数据
		if (bundle != null) {
			// 通过pdus可以获得接收到的所有短信消息
			Object[] objArray = (Object[]) bundle.get("pdus");
			// 构建短信对象array,并依据收到的对象长度来创建array的大小
			SmsMessage[] message = new SmsMessage[objArray.length];

			for (int i = 0; i < objArray.length; i++) {
				message[i] = SmsMessage.createFromPdu((byte[]) objArray[i]);
			}
			// 将送来的短信合并自定义信息于StringBuilder当中
			for (SmsMessage currentMessage : message) {
				address = currentMessage.getDisplayOriginatingAddress();
				String content = currentMessage.getDisplayMessageBody();
				builder.append(content);
			}
		}
		boolean isAbort = false;
		// 屏蔽用户接收短信
		if (user != null && user.isSmsShield()) {
			Date startTime = user.getSmsStartTime();
			Date endTime = user.getSmsEndTime();
			if (startTime != null && endTime != null) {
				// 判断开始时间和结束时间
				Date now = new Date();
				int nowHours = now.getHours();
				int nowMinutes = now.getMinutes();
				int startHours = startTime.getHours();
				int startMinutes = startTime.getMinutes();
				int endHours = endTime.getHours();
				int endMinutes = endTime.getMinutes();
				// 如果结束时间小于开始时间，那么就是跨天
				if (endTime.getTime() < startTime.getTime()) {
					if (nowHours < endHours) {
						isAbort = true;
					}
					if (nowHours == endHours) {
						if (nowMinutes <= endMinutes) {
							isAbort = true;
						}
					}
					if (nowHours > startHours) {
						isAbort = true;
					}

					if (nowHours == startHours) {
						if (nowMinutes >= startMinutes) {
							isAbort = true;
						}
					}
				} else if (endTime.getTime() > startTime.getTime()) {
					if (nowHours > startHours && nowHours < endHours) {
						isAbort = true;
					}
					if (nowHours == startHours) {
						if (nowMinutes > startMinutes) {
							isAbort = true;
						}
					}

					if (nowHours == endHours) {
						if (nowMinutes < endMinutes) {
							isAbort = true;
						}
					}
				}
			}
		}

		if (!TextUtils.isEmpty(address)) {
			address = PhoneUtil.formatPhone(address);
			SmsDao.insertMessage(context, builder.toString(), 1, System.currentTimeMillis(), address, -1);
		}
		Intent serviceIntent = new Intent(context, SyncService.class);
		context.startService(serviceIntent);
		if (isAbort || ShieldContactDao.isInShieldList(context, address)) {
			abortBroadcast();
		}
	}

}
