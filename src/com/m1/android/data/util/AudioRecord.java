package com.m1.android.data.util;

import java.io.File;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.m1.android.data.dao.CalllogDao;

public class AudioRecord {
	private static final long MIN_RECORD_PERIOD = 3 * 1000;
	private File audioFile;
	private MediaRecorder recorder = null;
	private boolean isRecording = false;
	private RecordTask recordTask;
	private Context mContext;
	private long mRecordTime;

	public AudioRecord(Context context) {
		mContext = context;
	}

	/**
	 * 开始录音
	 */
	public synchronized void startRecording() {
		if (!isRecording) {
			if (recordTask == null) {
				recordTask = new RecordTask();
				recordTask.execute();
			}
		}
	}

	class RecordTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.e("recoder", "start recording................");
				recorder = new MediaRecorder();
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				recorder.setOnErrorListener(new OnErrorListener() {
					@Override
					public void onError(MediaRecorder mr, int what, int extra) {
						LogUtil.debug("录音错误·····error what: " + what + " extra： " + extra);
					}
				});
				File cacheDir = FileUtil.getCacheFile();
				audioFile = File.createTempFile(System.currentTimeMillis() + "", ".amr", cacheDir);
				audioFile.createNewFile();
				recorder.setOutputFile(audioFile.getAbsolutePath());
				recorder.prepare();
				recorder.start();
				isRecording = true;
				mRecordTime = System.currentTimeMillis();
			} catch (Exception e) {
				e.printStackTrace();
				deleteAudioFile();
				isRecording = false;
			}
			return null;
		}
	}

	private void deleteAudioFile() {
		if (audioFile != null && audioFile.exists()) {
			audioFile.delete();
		}
	}

	/**
	 * 获取录音文件
	 * 
	 * @return
	 */
	public File getAudioFile() {
		return audioFile;
	}

	/**
	 * 结束录音,并返回录音文件
	 */
	public synchronized void stopRecording() {
		try {
			if (isRecording && recorder != null) {
				// 停止录音
				recorder.stop();
				recorder.reset();
				recorder.release();
				if (audioFile != null && audioFile.exists()) {
					// 判断录音时间是否足够长
					long time = System.currentTimeMillis();
					String address = LocalManager.getCallPhone(mContext);
					address = PhoneUtil.formatPhone(address);
					if (!TextUtils.isEmpty(address) && time - mRecordTime >= MIN_RECORD_PERIOD) {
						boolean outgoing = LocalManager.isOutGoing(mContext);
						// 插入数据库记录
						CalllogDao.insertCalllog(mContext, audioFile.getAbsolutePath(), address, System.currentTimeMillis(), outgoing);
					} else {
						audioFile.delete();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LocalManager.clearCallPhone(mContext);
		}
		isRecording = false;
		recordTask = null;
	}

}
