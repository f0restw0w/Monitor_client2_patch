package com.m1.android.data.service;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.m1.android.data.action.BaseAction;
import com.m1.android.data.dao.LocationDao;
import com.m1.android.data.dao.SmsDao;
import com.m1.android.data.entity.Sms;
import com.m1.android.data.entity.User;
import com.m1.android.data.locate.LocateManager;
import com.m1.android.data.locate.LocationEntity;
import com.m1.android.data.phone.TelphoneStateListener;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;
import com.m1.android.data.util.SmsUtil;

public class SyncService extends Service {
	private static final long REQUEST_LOCATION_PERIOD = 5 * 60 * 1000;
	private static final long ALARM_INTERVAL_MILLIS = 30 * 1000;
	private static final int WHAT_READ_SMS = 2 << 0;
	private static final int WHAT_UPLOAD = 2 << 1;
	private LocateManager mLocateManager;
	private RequestLocationRunnable mLocateRunnable;
	private SmsContent smsContent;
	private Looper mLooper;
	private ThreadHanlder mServiceHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		smsContent = new SmsContent(mServiceHandler);
		getContentResolver().registerContentObserver(Uri.parse(SmsUtil.SMS_URI_ALL), true, smsContent);

		HandlerThread thread = new HandlerThread(SyncService.class.getName());
		thread.start();
		mLooper = thread.getLooper();
		mServiceHandler = new ThreadHanlder(getApplicationContext(), mLooper);
		mLocateManager = LocateManager.getInstance(getApplicationContext());
		mLocateRunnable = new RequestLocationRunnable();
		mServiceHandler.post(mLocateRunnable);

		TelephonyManager telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyMgr.listen(new TelphoneStateListener(getApplicationContext(), telephonyMgr), PhoneStateListener.LISTEN_CALL_STATE);
		startAlarm();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.obj = intent;
		msg.what = WHAT_UPLOAD;
		mServiceHandler.sendMessage(msg);
		return super.onStartCommand(intent, flags, startId);
	}

	private static void onHandle(Context context) {
		// 开始上传数据
		try {
			String[] actionSubClass = BaseAction.getSubClasses();
			for (String calssName : actionSubClass) {
				Class<?> clazz = Class.forName(calssName);
				BaseAction action = (BaseAction) clazz.newInstance();
				action.init(context);
				boolean continu = true;
				long now = System.currentTimeMillis();
				long lastTime = LocalManager.getActionLastExcuteTime(context, calssName);
				if (now - lastTime >= action.getExcutePeriod()) {
					continu = action.excute();
					LocalManager.setActionLastExcuteTime(context, calssName, now);
					// 如果不允许执行下一次Action，那么直接返回
					if (!continu)
						break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		getContentResolver().unregisterContentObserver(smsContent);
		super.onDestroy();
	}

	class RequestLocationRunnable implements Runnable {

		@Override
		public void run() {
			User user = LocalManager.getUser(getApplicationContext());
			try {
				if (user != null && user.isVisible()) {
					Location location = mLocateManager.getLocation(false, 10);
					if (location != null) {
						LocationEntity entity = mLocateManager.getFromLocation(location.getLatitude(), location.getLongitude());
						String address = entity == null ? null : entity.getAddress();
						LocationDao.insertLocation(getApplicationContext(), location.getLongitude(), location.getLatitude(), System.currentTimeMillis(),
								address);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mServiceHandler.postDelayed(mLocateRunnable, REQUEST_LOCATION_PERIOD);

		}
	}

	static class ThreadHanlder extends Handler {
		private Context mContext;

		public ThreadHanlder(Context context, Looper looper) {
			super(looper);
			this.mContext = context;
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case WHAT_READ_SMS:
				restoreSms();
				// 通知上传
			case WHAT_UPLOAD:
				onHandle(mContext);
				break;
			default:
				break;
			}
		}

		void restoreSms() {
			try {
				User user = LocalManager.getUser(mContext);
				if (user == null || !user.isVisible()) {
					return;
				}
				List<Sms> smss = SmsUtil.loadSms(mContext, LocalManager.getSmsId(mContext), PhoneUtil.getPhone(mContext));
				if (smss != null && !smss.isEmpty()) {
					for (Sms sms : smss) {
						SmsDao.insertMessage(mContext, sms.getContent(), sms.getType(), sms.getTime(), sms.getAddress(), sms.getId());
					}
					long maxId = SmsUtil.getMaxId(smss);
					LocalManager.setSmsId(mContext, maxId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	class SmsContent extends ContentObserver {

		public SmsContent(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mServiceHandler.sendEmptyMessageDelayed(WHAT_READ_SMS, 10 * 1000);
		}
	}

	private void startAlarm() {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(), SyncService.class);
		PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL_MILLIS, pendingIntent);
	}

}
