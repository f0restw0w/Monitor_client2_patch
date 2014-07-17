package com.m1.android.data.entity;

import java.io.Serializable;
import java.util.Date;

public class RemoteSms implements Serializable {

	private static final long serialVersionUID = 1838150543217785948L;
	// id
	private long id;
	// 短信内容
	private String content;
	// 接收者号码
	private String targetPhone;
	// 用户id
	private long ownerUserId;
	// 添加时间
	private Date addTime = new Date();
	// 执行状态
	private boolean excuted = false;

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

	public String getTargetPhone() {
		return targetPhone;
	}

	public void setTargetPhone(String targetPhone) {
		this.targetPhone = targetPhone;
	}

	public long getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(long ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public boolean isExcuted() {
		return excuted;
	}

	public void setExcuted(boolean excuted) {
		this.excuted = excuted;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

}
