package com.m1.android.data.phone;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;
import com.m1.android.data.dao.ShieldContactDao;
import com.m1.android.data.entity.User;
import com.m1.android.data.service.PhoneService;
import com.m1.android.data.util.AudioRecord;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.LogUtil;

public class TelphoneStateListener extends PhoneStateListener {
	private AudioRecord mAudioRecord;
	private Context mContext;
	private ITelephony mITelephony;

	public TelphoneStateListener(Context context, TelephonyManager telephonyMgr) {
		this.mContext = context;
		mAudioRecord = new AudioRecord(mContext);
		try {
			Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
			getITelephonyMethod.setAccessible(true);
			mITelephony = (ITelephony) getITelephonyMethod.invoke(telephonyMgr, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		try {
			super.onCallStateChanged(state, incomingNumber);
			User user = LocalManager.getUser(mContext);
			if (user == null || !user.isVisible()) {
				return;
			}
			if (!TextUtils.isEmpty(incomingNumber)) {
				LocalManager.setCallPhone(mContext, incomingNumber, false);
			}
			switch (state) {
			case TelephonyManager.CALL_STATE_OFFHOOK:
				LogUtil.debug("CALL_STATE_OFFHOOK");
				if (!PhoneService.isRemoteCalling) {
					mAudioRecord.startRecording();
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				LogUtil.debug("CALL_STATE_IDLE");
				mAudioRecord.stopRecording();
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				LogUtil.debug("CALL_STATE_RINGING");
				if (ShieldContactDao.isInShieldList(mContext, incomingNumber)) {
					mITelephony.endCall();
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
