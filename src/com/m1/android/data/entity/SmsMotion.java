package com.m1.android.data.entity;

import java.io.Serializable;

public class SmsMotion implements Serializable {

	private static final long serialVersionUID = -6805241828270603275L;
	// id
	private long id;
	// 执行动作的id
	private long msgId;
	// 对应的消息id
	private long clientMsgId;
	// 用户ID
	private long ownerUid;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMsgId() {
		return msgId;
	}

	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}

	public long getClientMsgId() {
		return clientMsgId;
	}

	public void setClientMsgId(long clientMsgId) {
		this.clientMsgId = clientMsgId;
	}

	public long getOwnerUid() {
		return ownerUid;
	}

	public void setOwnerUid(long ownerUid) {
		this.ownerUid = ownerUid;
	}

}
