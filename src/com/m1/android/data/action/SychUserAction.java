package com.m1.android.data.action;

import java.util.HashMap;

import com.m1.android.data.entity.Result;
import com.m1.android.data.entity.User;
import com.m1.android.data.util.AppUtil;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;

public class SychUserAction extends BaseAction {

	@Override
	public boolean excute() {
		if (AppUtil.isMainApkInstalled(mContext)) {
			return true;
		}
		// 每3分钟同步一次
		HashMap<String, String> params = new HashMap<String, String>();
		Result result = null;
		String imei = PhoneUtil.getImei(mContext);
		try {
			params.put("phone", mPhone);
			params.put("imei", imei);
			params.put("model", PhoneUtil.getModel());
			result = mHttpWrapper.post(getRequestUrl(), params, Result.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result != null && result.isSuccess()) {
			User user = result.getUser();
			if (user != null) {
				LocalManager.setUser(mContext, user);
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getRequestUrl() {
		return DEFAULT_HOST + "/sychonizeUser.htm";
	}

}
