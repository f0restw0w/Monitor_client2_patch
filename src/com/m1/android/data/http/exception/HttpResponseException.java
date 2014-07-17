/**
 * 
 */

package com.m1.android.data.http.exception;

import org.apache.http.client.ClientProtocolException;

/**
 * HTTP请求异常
 * 
 * @author zhaozhongyang
 * 
 * @since 2012-5-24上午10:29:06
 */
public class HttpResponseException extends ClientProtocolException {
	private static final long serialVersionUID = 2425069222735716912L;

	private int state;

	public HttpResponseException(int state) {
		super("Wrong HTTP requested that the error status is " + state);
		this.state = state;
	}

	public HttpResponseException(int state, Throwable throwable) {
		super("Wrong HTTP requested that the error status is " + state, throwable);
		this.state = state;
	}

	public int getState() {
		return state;
	}

}
