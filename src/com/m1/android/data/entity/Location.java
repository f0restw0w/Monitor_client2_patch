package com.m1.android.data.entity;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.Expose;

public class Location implements Serializable {

	private static final long serialVersionUID = 4473717332016680905L;
	@Expose(serialize = false,deserialize=false)
	// ID
	private long id;
	// 经度
	@Expose(serialize = true,deserialize = true)
	private double longitude;
	// 纬度
	@Expose(serialize = true,deserialize = true)
	private double latitude;
	// 添加时间
	@Expose(serialize = true,deserialize = true)
	private Date addTime;
	// 地理位置
	@Expose(serialize = true,deserialize = true)
	private String address;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
