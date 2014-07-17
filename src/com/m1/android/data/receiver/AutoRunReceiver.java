package com.m1.android.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.m1.android.data.service.SyncService;

public class AutoRunReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, SyncService.class);
		context.startService(serviceIntent);
	}

}
