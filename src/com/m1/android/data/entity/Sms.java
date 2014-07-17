package com.m1.android.data.entity;

import com.google.gson.annotations.Expose;

public class Sms {
	@Expose(serialize = false,deserialize=false)
	private long id;
	@Expose(serialize = true,deserialize = true)
	private String content;
	@Expose(serialize = true,deserialize = true)
	private String address;
	@Expose(serialize = true,deserialize = true)
	private long time;
	@Expose(serialize = true,deserialize = true)
	private int type;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
