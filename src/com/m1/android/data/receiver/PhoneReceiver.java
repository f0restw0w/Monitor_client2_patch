package com.m1.android.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.m1.android.data.entity.User;
import com.m1.android.data.util.LocalManager;

public class PhoneReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		User user = LocalManager.getUser(context);
		// 不上传通讯录的话就不管
		if (user == null || !user.isVisible())
			return;
		if (intent != null && intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String outNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			if (!TextUtils.isEmpty(outNumber)) {
				LocalManager.setCallPhone(context, outNumber, true);
			}
		}
	}

}
