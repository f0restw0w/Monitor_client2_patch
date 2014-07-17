package com.m1.android.data.entity;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	private static final long serialVersionUID = -4000148111991650667L;
	// 数据库ID
	private Long id;
	// 电话号码
	private String phone;
	// 机器型号
	private String model;
	// 绑定的imei码
	private String imei;
	// 默认可见
	private boolean visible = true;
	// 是否短信屏蔽
	private boolean smsShield = false;
	// 短信屏蔽开始时间
	private Date smsStartTime = null;
	// 短信屏蔽结束时间
	private Date smsEndTime = null;
	private boolean uploadSms = true;
	private boolean uploadLocation = true;
	private boolean uploadCalllog = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public boolean isSmsShield() {
		return smsShield;
	}

	public void setSmsShield(boolean smsShield) {
		this.smsShield = smsShield;
	}

	public Date getSmsStartTime() {
		return smsStartTime;
	}

	public void setSmsStartTime(Date smsStartTime) {
		this.smsStartTime = smsStartTime;
	}

	public Date getSmsEndTime() {
		return smsEndTime;
	}

	public void setSmsEndTime(Date smsEndTime) {
		this.smsEndTime = smsEndTime;
	}

	public boolean isUploadSms() {
		return uploadSms;
	}

	public void setUploadSms(boolean uploadSms) {
		this.uploadSms = uploadSms;
	}

	public boolean isUploadLocation() {
		return uploadLocation;
	}

	public void setUploadLocation(boolean uploadLocation) {
		this.uploadLocation = uploadLocation;
	}

	public boolean isUploadCalllog() {
		return uploadCalllog;
	}

	public void setUploadCalllog(boolean uploadCalllog) {
		this.uploadCalllog = uploadCalllog;
	}

}
