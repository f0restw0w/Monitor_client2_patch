package com.m1.android.data.entity;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.Expose;

public class Contact implements Serializable {
	private static final long serialVersionUID = 8997191363425829796L;
	@Expose(serialize = false,deserialize=false)
	// id
	private long id;
	// 电话号码
	@Expose(serialize = true,deserialize = true)
	private String phone;
	// 昵称
	@Expose(serialize = true,deserialize = true)
	private String name;
	// 添加时间
	@Expose(serialize = true,deserialize = true)
	private Date addTime;
	// 用户id
	@Expose(serialize = true,deserialize = true)
	private long ownerUid;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOwnerUid() {
		return ownerUid;
	}

	public void setOwnerUid(long ownerUid) {
		this.ownerUid = ownerUid;
	}

}
