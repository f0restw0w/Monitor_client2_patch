package com.m1.android.data.entity;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.Expose;

public class Message implements Serializable {

	private static final long serialVersionUID = -6417213508858670877L;
	@Expose(serialize = false, deserialize = false)
	// id
	private long id;
	// 消息内容
	@Expose(serialize = true, deserialize = true)
	private String content;
	// 机主号码
	@Expose(serialize = true, deserialize = true)
	private String phone;
	// 发送者电话号码
	@Expose(serialize = true, deserialize = true)
	private String senderPhone;
	// 接收者电话号码
	@Expose(serialize = true, deserialize = true)
	private String receiverPhone;
	// 短信发送时间
	@Expose(serialize = true, deserialize = true)
	private Date sendTime;
	// 短信在系统数据库中的ID
	@Expose(serialize = true, deserialize = true)
	private long clientId = 0;

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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSenderPhone() {
		return senderPhone;
	}

	public void setSenderPhone(String senderPhone) {
		this.senderPhone = senderPhone;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

}
