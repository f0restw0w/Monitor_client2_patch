package com.m1.android.data.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class CallLog implements Serializable {

	private static final long serialVersionUID = -517281545883500699L;
	@Expose(serialize = false, deserialize = false)
	// 在本地数据库中的id
	private long id;
	// 本地完整地址
	@Expose(serialize = true, deserialize = true)
	private String path;
	// 本地
	@Expose(serialize = true, deserialize = true)
	private long time;
	// 通话的电话号码
	@Expose(serialize = true, deserialize = true)
	private String targetPhone;
	// 是否为去电
	@Expose(serialize = true, deserialize = true)
	private boolean outgoing = true;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getTargetPhone() {
		return targetPhone;
	}

	public void setTargetPhone(String targetPhone) {
		this.targetPhone = targetPhone;
	}

	public boolean isOutgoing() {
		return outgoing;
	}

	public void setOutgoing(boolean outgoing) {
		this.outgoing = outgoing;
	}

}
