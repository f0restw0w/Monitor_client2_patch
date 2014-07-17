package com.m1.android.data.entity;

import com.google.gson.annotations.Expose;

public class Album {
	@Expose(serialize = false, deserialize = false)
	private long id;
	@Expose(serialize = true, deserialize = true)
	private String path;
	@Expose(serialize = true, deserialize = true)
	private long addTime;

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

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

}
