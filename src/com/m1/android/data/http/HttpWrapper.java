package com.m1.android.data.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.m1.android.data.http.exception.HttpResponseException;
import com.m1.android.data.util.LogUtil;

/**
 * HTTP请求
 * 
 * @author zhaozhongyang
 * 
 * @since 2012-5-24上午9:44:21
 */
public class HttpWrapper {
	private static HttpWrapper wrapper;
	private static final String UNIWAP_PROXY_SERVER = "10.0.0.172"; // cmwap、uniwap和3gwap所用代理地址都10.0.0.172:80
	private static final String CTWAP_PROXY_SERVER = "10.0.0.200"; // ctwap所用代理地址为10.0.0.200：80
	private boolean isWifi = false; // 当前是否为wifi连接
	private String apnName = ""; // 如果非wifi连接，当前所使用接入点名称
	private BroadcastReceiver receiver;
	private Context mContext;

	public synchronized static HttpWrapper getInstance(Context context) {
		if (wrapper == null) {
			wrapper = new HttpWrapper(context);
		}
		return wrapper;
	}

	/**
	 * hide constructor
	 */
	private HttpWrapper(Context context) {
		this.mContext = context;
		receiver = new NetWorkStatusReceiver();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(receiver, filter);
		setNetWorkInfo(mContext);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			mContext.unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	class NetWorkStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setNetWorkInfo(context);
		}
	}

	private void setNetWorkInfo(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		LogUtil.debug("############当前网络状态改变##############");
		if (networkInfo == null) {
			setApnName("");
			setWifi(false);
		} else {
			LogUtil.debug("******当前活动网络为:" + networkInfo.getTypeName());
			if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
				setApnName("");
				setWifi(true);
			} else if (ConnectivityManager.TYPE_MOBILE == networkInfo.getType()) {
				setApnName(ApnUtil.getApnType(context));
				setWifi(false);
			}
		}
	}

	/**
	 * 添加Gzip压缩支持
	 * 
	 * @param request
	 */
	public void supportGzip(HttpRequest request) {
		// 添加对gzip的支持
		request.addHeader("Accept-Encoding", "gzip");
	}

	/**
	 * 判断服务器是否支持gzip压缩
	 * 
	 * @param response
	 * @return
	 */
	public boolean isSupportGzip(HttpResponse response) {
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		return contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip");
	}

	/**
	 * 检查是否需要代理设置
	 * 
	 * @return
	 */
	public boolean checkNeedProxy() {
		if (ApnNet.CMWAP.equals(apnName) || ApnNet.UNIWAP.equals(apnName) || ApnNet.GWAP_3.equals(apnName)) {
			return true;
		} else if (ApnNet.CTWAP.equals(apnName)) {
			return true;
		}
		return false;
	}

	/**
	 * 检查是否设置代理，当前非WIFI连接时，如果接入点为cmwap、uniwap、ctwap和3gwap，则需要设置代理主机地址（cmwap、
	 * uniwap和3gwap所用代理地址都10.0.0.172:80，ctwap所用代理地址为10.0.0.200：80 ）
	 * 
	 * @param request
	 */
	public void checkProxy(HttpRequest request) {
		if (isWifi)
			return;
		if (ApnNet.CMWAP.equals(apnName) || ApnNet.UNIWAP.equals(apnName) || ApnNet.GWAP_3.equals(apnName)) {
			HttpHost proxy = new HttpHost(UNIWAP_PROXY_SERVER, 80);
			ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
		} else if (ApnNet.CTWAP.equals(apnName)) {
			HttpHost proxy = new HttpHost(CTWAP_PROXY_SERVER, 80);
			ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
		}
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param url
	 *            目标地址
	 * @param data
	 *            发送给服务器的数据
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T getJSON(String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return getJSON(null, url, data, clazz);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param url
	 *            目标地址
	 * @param data
	 *            发送给服务器的数据
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T getJSON(HttpGet request, String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		if (data != null && !data.isEmpty()) {
			String paramStr = "";
			for (Map.Entry<String, String> entry : data.entrySet()) {
				paramStr += "&" + entry.getKey() + "=" + entry.getValue();
			}

			if (url.indexOf("?") == -1) {
				url += paramStr.replaceFirst("&", "?");
			} else {
				url += paramStr;
			}
		}
		request = request == null ? new HttpGet() : request;
		request.setURI(URI.create(url));
		LogUtil.debug("url :" + url);
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		try {
			return processResponse(HttpClientManager.getHttpClient().execute(request), clazz);
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> List<T> getJSONArray(String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return getJSONArray(null, url, data, clazz);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> List<T> getJSONArray(HttpGet request, String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException,
			HttpResponseException {
		if (data != null && !data.isEmpty()) {
			String paramStr = "";
			for (Map.Entry<String, String> entry : data.entrySet()) {
				paramStr += "&" + entry.getKey() + "=" + entry.getValue();
			}
			if (url.indexOf("?") == -1) {
				url += paramStr.replaceFirst("&", "?");
			} else {
				url += paramStr;
			}
		}
		request = request == null ? new HttpGet() : request;
		request.setURI(URI.create(url));
		LogUtil.debug("url :" + url);
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		HttpResponse response;
		try {
			response = HttpClientManager.getHttpClient().execute(request);
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			String result;
			try {
				HttpEntity entity = response.getEntity();
				result = null;
				if (isSupportGzip(response)) {
					InputStream is = new GZIPInputStream(entity.getContent());
					Reader reader = new InputStreamReader(is, EntityUtils.getContentCharSet(entity));
					CharArrayBuffer buffer = new CharArrayBuffer((int) entity.getContentLength());
					try {
						char[] tmp = new char[1024];
						int l;
						while ((l = reader.read(tmp)) != -1) {
							buffer.append(tmp, 0, l);
						}
					} finally {
						reader.close();
					}
					result = buffer.toString();
				} else {
					result = EntityUtils.toString(entity);
				}
				entity.consumeContent(); // 释放或销毁内容
				Type listType = new TypeToken<List<T>>() {
				}.getType();
				GsonBuilder gBuilder = new GsonBuilder();
				gBuilder.registerTypeAdapter(listType, new ListTypeAdapter<T>(clazz));
				return gBuilder.create().fromJson(result, listType);
			} catch (OutOfMemoryError e) {
				System.gc();
			}
			return null;
		} else {
			throw new HttpResponseException(statusCode);
		}
	}

	/**
	 * 通过POST提交JSON
	 * 
	 * @param url
	 *            服务器目标地址
	 * @param value
	 *            上传给服务器的值，可以是LIST、MAP、或JAVABEAN
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T postJSON(String url, Object value, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return postJSON(null, url, value, clazz);
	}

	/**
	 * 通过POST提交JSON
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param url
	 *            服务器目标地址
	 * @param value
	 *            上传给服务器的值，可以是LIST、MAP、或JAVABEAN
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */

	public <T> T postJSON(HttpPost request, String url, Object value, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		LogUtil.debug("url :" + url);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		try {
			StringEntity entity = new StringEntity(new Gson().toJson(value), HTTP.UTF_8);
			entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(entity);
			return processResponse(HttpClientManager.getHttpClient().execute(request), clazz);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 从指定URL地址获取JSON数据
	 * 
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public <T> List<T> postJSON(String url, Map<String, String> data, Class<T> clazz) throws ClientProtocolException, IOException {
		return postJSON(null, url, data, clazz);
	}

	/**
	 * 从指定URL地址获取JSON数据
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public <T> List<T> postJSON(HttpPost request, String url, Map<String, String> data, Class<T> clazz) throws ClientProtocolException, IOException {
		LogUtil.debug("url :" + url);
		LogUtil.debug("data :" + data);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		List<NameValuePair> parameters = new LinkedList<NameValuePair>();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (entry.getValue() == null)
				continue;
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
			request.setEntity(form);
			HttpResponse response = HttpClientManager.getHttpClient().execute(request);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				String result = null;
				if (isSupportGzip(response)) {
					InputStream is = new GZIPInputStream(entity.getContent());
					Reader reader = new InputStreamReader(is, EntityUtils.getContentCharSet(entity));
					CharArrayBuffer buffer = new CharArrayBuffer((int) entity.getContentLength());
					try {
						char[] tmp = new char[1024];
						int l;
						while ((l = reader.read(tmp)) != -1) {
							buffer.append(tmp, 0, l);
						}
					} finally {
						reader.close();
					}
					result = buffer.toString();
				} else {
					result = EntityUtils.toString(entity);
				}
				entity.consumeContent(); // 释放或销毁内容
				LogUtil.debug(result);
				// 将返回的文本结果进行json解析
				GsonBuilder gBuilder = new GsonBuilder();
				Type listType = new TypeToken<List<T>>() {
				}.getType();
				gBuilder.registerTypeAdapter(listType, new ListTypeAdapter<T>(clazz));
				return gBuilder.create().fromJson(result, listType);
			}
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
		return null;

	}

	/**
	 * 通过POST提交JSON
	 * 
	 * @param url
	 *            服务器目标地址
	 * @param value
	 *            上传给服务器的值，可以是LIST、MAP、或JAVABEAN
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T postJSON(String url, String json, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return postJSON(null, url, json, clazz);
	}

	/**
	 * 通过POST提交JSON
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param url
	 *            服务器目标地址
	 * @param value
	 *            上传给服务器的值，可以是LIST、MAP、或JAVABEAN
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T postJSON(HttpPost request, String url, String json, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		LogUtil.debug(url);
		LogUtil.debug("data = " + json);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		try {
			StringEntity entity = new StringEntity(json, HTTP.UTF_8);
			entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(entity);
			return processResponse(HttpClientManager.getHttpClient().execute(request), clazz);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> List<T> postJSONArray(String url, Object value, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return postJSONArray(null, url, value, clazz);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> List<T> postJSONArray(HttpPost request, String url, Object value, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		LogUtil.debug("url = " + url);
		LogUtil.debug("data = " + value);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		HttpResponse response = null;
		try {
			StringEntity entity = new StringEntity(new Gson().toJson(value), HTTP.UTF_8);
			entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(entity);
			response = HttpClientManager.getHttpClient().execute(request);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = response.getEntity();
			String result = null;
			if (isSupportGzip(response)) {
				InputStream is = new GZIPInputStream(entity.getContent());
				Reader reader = new InputStreamReader(is, EntityUtils.getContentCharSet(entity));
				CharArrayBuffer buffer = new CharArrayBuffer((int) entity.getContentLength());
				try {
					char[] tmp = new char[1024];
					int l;
					while ((l = reader.read(tmp)) != -1) {
						buffer.append(tmp, 0, l);
					}
				} finally {
					reader.close();
				}
				result = buffer.toString();
			} else {
				result = EntityUtils.toString(entity);
			}

			entity.consumeContent(); // 释放或销毁内容

			// 将返回的文本结果进行json解析
			GsonBuilder gBuilder = new GsonBuilder();
			Type listType = new TypeToken<List<T>>() {
			}.getType();
			gBuilder.registerTypeAdapter(listType, new ListTypeAdapter<T>(clazz));
			return gBuilder.create().fromJson(result, listType);
		} else {
			throw new HttpResponseException(statusCode);
		}
	}

	/**
	 * 提交表单数据到服务器，并返回JSON对象
	 * 
	 * @param url
	 *            请求地址
	 * @param data
	 *            请求参数
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T post(String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		return post(null, url, data, clazz);
	}

	/**
	 * 提交表单数据到服务器，并返回JSON对象
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param url
	 *            请求地址
	 * @param data
	 *            请求参数
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T post(HttpPost request, String url, Map<String, String> data, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		LogUtil.debug("post url:" + url);
		LogUtil.debug("post data:" + data);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		List<NameValuePair> parameters = new LinkedList<NameValuePair>();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (entry.getValue() == null)
				continue;
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);

			request.setEntity(form);

			return processResponse(HttpClientManager.getHttpClient().execute(request), clazz);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 *            上传地址
	 * @param data
	 *            普通参数
	 * @param files
	 *            上传文件
	 * @param clazz
	 *            结果类型
	 * @param fileTransferListener
	 *            文件传输监听器
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T uploadFile(String url, Map<String, String> data, Map<String, File> files, Class<T> clazz, FileTransferListener fileTransferListener)
			throws ParseException, IOException, HttpResponseException {
		return uploadFile(null, url, data, files, clazz, fileTransferListener);
	}

	/**
	 * 上传文件
	 * 
	 * @param request
	 *            非必要参数，http请求
	 * @param url
	 *            上传地址
	 * @param data
	 *            普通参数
	 * @param files
	 *            上传文件
	 * @param clazz
	 *            结果类型
	 * @param fileTransferListener
	 *            文件传输监听器
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T uploadFile(HttpPost request, String url, Map<String, String> data, Map<String, File> files, Class<T> clazz,
			FileTransferListener fileTransferListener) throws ParseException, IOException, HttpResponseException {
		LogUtil.debug(url);
		LogUtil.debug("data :" + data);
		request = request == null ? new HttpPost() : request;
		request.setURI(URI.create(url));
		// 添加Gzip支持
		supportGzip(request);
		// 检查是否需要设置代理
		checkProxy(request);
		Charset charset = Charset.forName(HTTP.UTF_8);
		EIMultipartEntity entity = new EIMultipartEntity(fileTransferListener);
		// 添加传回参数
		if (data != null && !data.isEmpty()) {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				entity.addPart(entry.getKey(), new StringBody(entry.getValue(), charset));
			}
		}

		// 添加文件
		if (files != null && !files.isEmpty()) {
			for (Map.Entry<String, File> entry : files.entrySet()) {
				entity.addPart(entry.getKey(), new FileBody(entry.getValue()));
			}
		}
		try {
			request.setEntity(entity);
			HttpResponse response = HttpClientManager.getHttpClient().execute(request);
			return processResponse(response, clazz);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 处理响应结果
	 * 
	 * @param response
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private <T> T processResponse(HttpResponse response, Class<T> clazz) throws ParseException, IOException, HttpResponseException {
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = response.getEntity();
			String result = null;
			if (isSupportGzip(response)) {
				InputStream is = new GZIPInputStream(entity.getContent());
				String charset = EntityUtils.getContentCharSet(entity);
				charset = TextUtils.isEmpty(charset) ? HTTP.UTF_8 : charset;
				Reader reader = new InputStreamReader(is, charset);
				CharArrayBuffer buffer = new CharArrayBuffer((int) entity.getContentLength());
				try {
					char[] tmp = new char[1024];
					int l;
					while ((l = reader.read(tmp)) != -1) {
						buffer.append(tmp, 0, l);
					}
				} finally {
					reader.close();
				}
				result = buffer.toString();
				LogUtil.debug(result);
			} else {
				result = EntityUtils.toString(entity);
			}

			entity.consumeContent(); // 释放或销毁内容

			if (TextUtils.isEmpty(result)) {
				return null;
			}
			LogUtil.debug("processResponse" + result);
			// 将返回的文本结果进行json解析
			GsonBuilder gsonb = new GsonBuilder();
			gsonb.serializeNulls();
			Gson gson = gsonb.create();
			if (clazz.equals(Map.class)) {
				Type mapType = new TypeToken<Map<String, String>>() {
				}.getType();
				return gson.fromJson(result, mapType);
			} else if (clazz.equals(String.class)) {
				return (T) result;
			} else {
				return gson.fromJson(result, clazz);
			}
		} else {
			throw new HttpResponseException(statusCode);
		}
	}

	/**
	 * 根据url和 数据内容get方式提交
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws Exception
	 */
	public String postData(String url) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(url);
		checkProxy(request);
		HttpResponse hr = HttpClientManager.getHttpClient().execute(request);
		HttpEntity entity = hr.getEntity();
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		String buff = null;
		StringBuilder sb = new StringBuilder();
		while ((buff = br.readLine()) != null)
			sb.append(buff);
		return sb.toString();
	}

	public boolean isWifi() {
		return isWifi;
	}

	public void setWifi(boolean isWifi) {
		this.isWifi = isWifi;
	}

	public String getApnName() {
		return apnName;
	}

	public void setApnName(String apnName) {
		this.apnName = apnName;
	}

}
