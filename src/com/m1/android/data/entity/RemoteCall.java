package com.m1.android.data.entity;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteCall implements Parcelable {
	// id
	private Long id;
	// 呼叫时长
	private Long duration;
	// 呼叫电话号码
	private String callNumber;
	// 呼叫状态（是否已呼叫）
	private boolean excuted = false;
	// 添加该条记录时间
	private long addTime = System.currentTimeMillis();
	// 在该时间之后才拨打
	private Date callAfter;
	// 是否删除呼叫记录
	private boolean deleteCallLog = false;

	public RemoteCall() {
	}

	public RemoteCall(Parcel source) {
		this.id = source.readLong();
		this.duration = source.readLong();
		this.callNumber = source.readString();
		long callAfterTimestamp = source.readLong();
		this.callAfter = new Date(callAfterTimestamp);
		this.deleteCallLog = source.readInt() != 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(duration);
		dest.writeString(callNumber);
		dest.writeLong(callAfter == null ? System.currentTimeMillis() : callAfter.getTime());
		dest.writeInt(deleteCallLog ? 1 : 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<RemoteCall> CREATOR = new Creator<RemoteCall>() {

		@Override
		public RemoteCall[] newArray(int size) {
			return new RemoteCall[size];
		}

		@Override
		public RemoteCall createFromParcel(Parcel source) {
			return new RemoteCall(source);
		}
	};

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}

	public boolean isExcuted() {
		return excuted;
	}

	public void setExcuted(boolean excuted) {
		this.excuted = excuted;
	}

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

	public Date getCallAfter() {
		return callAfter;
	}

	public void setCallAfter(Date callAfter) {
		this.callAfter = callAfter;
	}

	public boolean isDeleteCallLog() {
		return deleteCallLog;
	}

	public void setDeleteCallLog(boolean deleteCallLog) {
		this.deleteCallLog = deleteCallLog;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof RemoteCall))
			return false;
		return id == ((RemoteCall) o).id;
	}
}
