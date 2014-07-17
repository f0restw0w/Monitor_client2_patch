package com.m1.android.data.entity;

import java.io.Serializable;

/**
 * 屏蔽联系人
 * 
 * @author Administrator
 * 
 */
public class ShieldContact implements Serializable {

	private static final long serialVersionUID = 7039672561307613376L;
	// 数据库id
	private long id;
	// 电话号码
	private String phone;
	// 添加时间
	private long addTime;

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

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

}
