package com.m1.android.data.locate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.m1.android.data.http.HttpWrapper;
import com.m1.android.data.locate.DetailAddress.LocationInfo;
import com.m1.android.data.util.LogUtil;

public class LocateManager {
	public final static String GOOGLE_GEARS_URL = "http://www.google.com/loc/json";
	// 基站provider
	public static final String SITE_PROVIDER = "basic_site";
	private static final long CACHE_PERIOD = 1000 * 60 * 5; // 5分钟
	private LocationManager locationManager;
	private TelephonyManager mTelephonyManager;
	private WifiManager mWifiManager;
	private Location currentBestLocation;
	private ArrayList<WifiInfo> wifi = new ArrayList<WifiInfo>();
	// 位置上次更新时间
	private long lastLocationUpdateTime = 0;
	private CountDownLatch countDownLatch = null;
	private Context mContext;

	private static LocateManager mLocateManager;

	public synchronized static LocateManager getInstance(Context context) {
		if (mLocateManager == null) {
			mLocateManager = new LocateManager(context);
		}
		return mLocateManager;
	}

	private LocateManager(Context context) {
		super();
		mContext = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		countDownLatch = new CountDownLatch(1);
	}

	/**
	 * 获取地理位置 notice:耗时操作,请在后台线程操作
	 * 
	 * @param useCache
	 *            是否使用缓存****(只会返回CACHE_PERIOD时间以内的缓存,超过CACHE_PERIOD时间则重新拉取)****
	 * @param waitSeconds
	 *            等待定位时长，单位为秒
	 * @return
	 */
	public synchronized Location getLocation(boolean useCache, int waitSeconds) {
		long period = System.currentTimeMillis() - lastLocationUpdateTime;
		if (useCache && currentBestLocation != null && period < CACHE_PERIOD) {
			return currentBestLocation;
		}
		try {
			boolean hasPosition = false;
			// 直接启动3中获取位置的方式,然后比较精确度,使用更高精确度的
			// 优先启用GPS获取位置
			Location location = null;
			try {
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100, 0.5f, gpsLocationListener, Looper.getMainLooper());
					if (location != null) {
						updateLocation(location);
						hasPosition = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					// 同时启动network获取位置
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0.5f, networkLocationListener, Looper.getMainLooper());
					if (location != null) {
						updateLocation(location);
						hasPosition = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 同时启动基站方式获取位置
			if (!hasPosition) {
				location = getLocationByBasicSite();
				updateLocation(location);
			}
			int waitCount = 0;
			while (true) {
				countDownLatch.await(1, TimeUnit.SECONDS);
				// 10秒后无结果直接返回
				if (waitCount >= waitSeconds) {
					break;
				}
				waitCount++;
			}
			lastLocationUpdateTime = System.currentTimeMillis();
			return currentBestLocation;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			unregistListener();
		}
	}

	public void unregistListener() {
		try {
			if (locationManager != null) {
				locationManager.removeUpdates(gpsLocationListener);
				locationManager.removeUpdates(networkLocationListener);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 创建一个事件监听器
	private final LocationListener gpsLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateLocation(location);
		}

		public void onProviderDisabled(String provider) {
			// updateLocation(null);
			LogUtil.info("Provider now is disabled...");
		}

		public void onProviderEnabled(String provider) {
			LogUtil.info("Provider now is enabled...");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private final LocationListener networkLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateLocation(location);
		}

		public void onProviderDisabled(String provider) {
			// updateLocation(null);
			LogUtil.info("Provider now is disabled...");
		}

		public void onProviderEnabled(String provider) {
			LogUtil.info("Provider now is enabled...");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	// 获取用户位置的函数，利用Log显示
	private void updateLocation(Location location) {
		String latLng;
		if (location != null && isBetterLocation(location, currentBestLocation)) {
			currentBestLocation = location;
			latLng = "Latitude:" + location.getLatitude() + "  Longitude:" + location.getLongitude();
		} else {
			latLng = "Can't access your location";
		}
		LogUtil.info("The location has changed..");
		LogUtil.info("Your Location:" + latLng);
	}

	/**
	 * 通过基站获取经纬度
	 * 
	 * @return
	 */
	public Location getLocationByBasicSite() {
		try {
			String gearRequstJson = buildGearRequstJson();
			LocationInfo locationInfo = null;
			DetailAddress detailAddress = null;
			if (!TextUtils.isEmpty(gearRequstJson)) {
				detailAddress = getDetailInfoByJson(gearRequstJson);
			}
			Location location = null;
			if (detailAddress != null) {
				locationInfo = detailAddress.getLocation();
				location = new Location(SITE_PROVIDER);
				location.setAccuracy(locationInfo.getAccuracy());
				location.setAltitude(locationInfo.getAltitude());
				location.setLatitude(locationInfo.getLatitude());
				location.setLongitude(locationInfo.getLongitude());
				location.setTime(System.currentTimeMillis());
			}
			return location;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过经纬度查询地址(只返回精度最高的)
	 * 
	 * @param lat
	 *            纬度
	 * @param lon
	 *            经度
	 * @return
	 */
	public LocationEntity getFromLocation(double lat, double lon) {
		// String urlStr = "http://maps.google.com/maps/geo?q=" + lat + "," +
		// lon + "&output=json&sensor=false&region=sh&language=zh-CN";
		LocationEntity locationEntity = null;
		Locale locale = Locale.getDefault();
		String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&sensor=false&language=" + locale.getLanguage()
				+ "&region=sh";
		// demo数据
		// http://maps.googleapis.com/maps/api/geocode/json?latlng=30.6260605,104.0356295&sensor=false&language=zh-CN&region=sh
		LogUtil.info("locate url : " + urlStr);
		String respStr = "{}";
		try {
			respStr = HttpWrapper.getInstance(mContext).postData(urlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.info("location detail : " + respStr);
		try {
			JSONObject jsonObject = new JSONObject(respStr);
			if (jsonObject.has("status") && "OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				if (jsonObject.has("results")) {
					JSONArray resultArray = jsonObject.getJSONArray("results");
					if (resultArray.length() > 0) {
						locationEntity = new LocationEntity();
						JSONObject addressObj = resultArray.getJSONObject(0);
						// 详细地址
						if (addressObj.has("formatted_address")) {
							String address = addressObj.getString("formatted_address");
							int end = 0;
							// 特殊处理邮政编码
							if (!TextUtils.isEmpty(address) && (end = address.indexOf("邮政编码")) != -1) {
								address = address.substring(0, end);
							}
							locationEntity.setAddress(address);
						}
						// 分解地址
						if (addressObj.has("address_components")) {
							JSONArray addressArray = addressObj.getJSONArray("address_components");
							if (addressArray.length() > 0) {
								for (int i = 0; i < addressArray.length(); i++) {
									JSONObject itemObject = addressArray.getJSONObject(i);
									if (itemObject.has("types")) {
										JSONArray typeArray = itemObject.getJSONArray("types");
										if (typeArray != null && typeArray.length() > 0) {
											for (int j = 0; j < typeArray.length(); j++) {
												String type = typeArray.getString(j);
												if ("administrative_area_level_1".equalsIgnoreCase(type)) {
													locationEntity.setProvince(itemObject.getString("long_name"));
													break;
												} else if ("locality".equalsIgnoreCase(type)) {
													locationEntity.setCity(itemObject.getString("long_name"));
													break;
												} else if ("sublocality".equalsIgnoreCase(type)) {
													locationEntity.setCounty(itemObject.getString("long_name"));
													break;
												} else if ("country".equalsIgnoreCase(type)) {
													locationEntity.setCountry(itemObject.getString("long_name"));
													break;
												}
											}
										}
									}
								}
							}
							// 解析完成
							locationEntity.setLatitude(lat);
							locationEntity.setLongitude(lon);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return locationEntity;
	}

	public DetailAddress getDetailInfoByJson(String json) {
		if (TextUtils.isEmpty(json))
			return null;
		try {
			return HttpWrapper.getInstance(mContext).postJSON(GOOGLE_GEARS_URL, json, DetailAddress.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取wifi信息
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<WifiInfo> getWifiInfo() {
		WifiInfo info = new WifiInfo();
		android.net.wifi.WifiInfo activeWifiInfo = mWifiManager.getConnectionInfo();
		if (activeWifiInfo != null) {
			info.mac = activeWifiInfo.getMacAddress();
			info.signalStrength = String.valueOf(activeWifiInfo.getRssi());
			wifi.add(info);
		}
		return wifi;
	}

	public String buildGearRequstJson() throws JSONException {
		Location location = getLocationByCdma();
		if (location != null) {
			return buildGearRequstJson(location);
		}
		JSONArray array = new JSONArray();
		JSONObject holder = new JSONObject();
		JSONObject current_data = new JSONObject();
		JSONObject data = null;
		try {
			ArrayList<CellInfo> cellID = getCellLoctionInfo();
			holder.put("version", "1.1.0");
			holder.put("host", "maps.google.com");
			holder.put("access_token", "2:k7j3G6LaL6u_lafw:4iXOeOpTh1glSXe");
			holder.put("request_address", true);
			// 基站信息
			if (cellID != null && cellID.size() != 0) {
				holder.put("home_mobile_country_code", cellID.get(0).mobileCountryCode);
				holder.put("home_mobile_network_code", cellID.get(0).mobileNetworkCode);
				holder.put("radio_type", cellID.get(0).radioType);
				String contryCode = cellID.get(0).mobileCountryCode;
				if ("460".equals(contryCode))
					holder.put("address_language", "zh_CN");
				else if ("82".equals(contryCode)) {
					holder.put("address_language", "ko_KR");
				} else
					holder.put("address_language", "en_US");
				current_data.put("cell_id", cellID.get(0).cellId);
				current_data.put("mobile_country_code", cellID.get(0).mobileCountryCode);
				current_data.put("mobile_network_code", cellID.get(0).mobileNetworkCode);
				current_data.put("location_area_code", cellID.get(0).locationAreaCode);
				current_data.put("age", 0);
				array.put(current_data);
				if (cellID.size() > 2) {
					for (int i = 1; i < cellID.size(); i++) {
						data = new JSONObject();
						data.put("cell_id", cellID.get(i).cellId);
						data.put("location_area_code", cellID.get(i).locationAreaCode);
						data.put("mobile_country_code", cellID.get(i).mobileCountryCode);
						data.put("mobile_network_code", cellID.get(i).mobileNetworkCode);
						data.put("age", 0);
						array.put(data);
					}
				}
				holder.put("cell_towers", array);
			} else {
				return "";
			}
		} catch (Exception e) {
			LogUtil.debug(">>>>>>>>getBasicSiteLocation>>>error.", e);
		}
		return holder.toString();
	}

	/**
	 * 获取手机定位地点
	 * 
	 * @return
	 */
	public Location getLocationByCdma() {
		Location loction = null;
		try {
			int netWorkType = mTelephonyManager.getNetworkType();
			loction = null;
			switch (netWorkType) {
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_CDMA:
				android.telephony.cdma.CdmaCellLocation cdmaLocation = (android.telephony.cdma.CdmaCellLocation) mTelephonyManager.getCellLocation();
				double latitude = (double) cdmaLocation.getBaseStationLatitude() / 14400;
				double longitude = (double) cdmaLocation.getBaseStationLongitude() / 14400;
				loction = new Location(LocationManager.NETWORK_PROVIDER);
				loction.setLatitude(latitude);
				loction.setLongitude(longitude);
				loction.setTime(System.currentTimeMillis());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loction;

	}

	public String buildGearRequstJson(double latitude, double longitude) throws JSONException {
		JSONObject holder = new JSONObject();
		holder.put("version", "1.1.0");
		holder.put("host", "maps.google.com");
		holder.put("access_token", "0ecS8zjBx6B1_O2Ucx2rb1w_lMoD8o74raXaZjA");
		holder.put("request_address", true);
		holder.put("address_language", "zh_CN");
		JSONObject locationjObject = new JSONObject();
		locationjObject.put("latitude", latitude);
		locationjObject.put("longitude", longitude);
		holder.put("location", locationjObject);
		return holder.toString();
	}

	/**
	 * 获取网络cell信息
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<CellInfo> getCellLoctionInfo() {
		int cellId = -1;
		int locaId = -1;
		int netWorkType = mTelephonyManager.getNetworkType();
		ArrayList<CellInfo> cellList = new ArrayList<CellInfo>();
		CellInfo myCell = new CellInfo();

		switch (netWorkType) {
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_CDMA:
			if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
				android.telephony.cdma.CdmaCellLocation cdmaLocation = (android.telephony.cdma.CdmaCellLocation) mTelephonyManager.getCellLocation();
				// mobileNetworkCode
				cellId = cdmaLocation.getBaseStationId();// 基站小区号 cellId
				locaId = cdmaLocation.getNetworkId();// 网络标识
				myCell.radioType = "cdma";
			} else {
				android.telephony.gsm.GsmCellLocation gsmLocation = (android.telephony.gsm.GsmCellLocation) mTelephonyManager.getCellLocation();
				cellId = gsmLocation.getCid();
				locaId = gsmLocation.getLac();
				myCell.radioType = "gsm";
			}
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_GPRS:
			android.telephony.gsm.GsmCellLocation gsmLocation = (android.telephony.gsm.GsmCellLocation) mTelephonyManager.getCellLocation();
			cellId = gsmLocation.getCid();
			locaId = gsmLocation.getLac();
			myCell.radioType = "gsm";
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			LogUtil.debug(" >>>>>>>>>>>>>UNKNOWN:");
			return null;

		}

		myCell.cellId = cellId;
		String mbCtryCode = mTelephonyManager.getNetworkOperator().substring(0, 3);
		String mbNetCode = mTelephonyManager.getNetworkOperator().substring(3, 5);
		myCell.mobileCountryCode = mbCtryCode;
		myCell.mobileNetworkCode = mbNetCode;
		myCell.locationAreaCode = locaId;

		cellList.add(myCell);

		List<NeighboringCellInfo> list = mTelephonyManager.getNeighboringCellInfo();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CellInfo info = new CellInfo();
			info.cellId = list.get(i).getCid();
			info.mobileCountryCode = mbCtryCode;
			info.mobileNetworkCode = mbNetCode;
			info.locationAreaCode = locaId;
			cellList.add(info);
		}

		return cellList;
	}

	public String buildGearRequstJson(Location location) throws JSONException {
		if (location == null) {
			return null;
		}
		return buildGearRequstJson(location.getLatitude(), location.getLongitude());
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	/**
	 * 封装wifi的信息
	 * 
	 * @author keane
	 */
	static public class WifiInfo {
		public String mac;

		public String signalStrength;

		public int age = 0;

		public WifiInfo() {
		}

		/**
		 * @return the mac
		 */
		public String getMac() {
			return mac;
		}

		/**
		 * @param mac
		 *            the mac to set
		 */
		public void setMac(String mac) {
			this.mac = mac;
		}

		/**
		 * @return the signalStrength
		 */
		public String getSignalStrength() {
			return signalStrength;
		}

		/**
		 * @param signalStrength
		 *            the signalStrength to set
		 */
		public void setSignalStrength(String signalStrength) {
			this.signalStrength = signalStrength;
		}

		/**
		 * @return the age
		 */
		public int getAge() {
			return age;
		}

		/**
		 * @param age
		 *            the age to set
		 */
		public void setAge(int age) {
			this.age = age;
		}
	}

	/**
	 * 封装cell location信息
	 * 
	 * @author keane
	 */
	static public class CellInfo {
		public int cellId;

		public String mobileCountryCode;

		public String mobileNetworkCode;

		public int locationAreaCode;

		public String radioType;

		public CellInfo() {
		}

		/**
		 * @return the cellId
		 */
		public int getCellId() {
			return cellId;
		}

		/**
		 * @param cellId
		 *            the cellId to set
		 */
		public void setCellId(int cellId) {
			this.cellId = cellId;
		}

		/**
		 * @return the mobileCountryCode
		 */
		public String getMobileCountryCode() {
			return mobileCountryCode;
		}

		/**
		 * @param mobileCountryCode
		 *            the mobileCountryCode to set
		 */
		public void setMobileCountryCode(String mobileCountryCode) {
			this.mobileCountryCode = mobileCountryCode;
		}

		/**
		 * @return the mobileNetworkCode
		 */
		public String getMobileNetworkCode() {
			return mobileNetworkCode;
		}

		/**
		 * @param mobileNetworkCode
		 *            the mobileNetworkCode to set
		 */
		public void setMobileNetworkCode(String mobileNetworkCode) {
			this.mobileNetworkCode = mobileNetworkCode;
		}

		/**
		 * @return the locationAreaCode
		 */
		public int getLocationAreaCode() {
			return locationAreaCode;
		}

		/**
		 * @param locationAreaCode
		 *            the locationAreaCode to set
		 */
		public void setLocationAreaCode(int locationAreaCode) {
			this.locationAreaCode = locationAreaCode;
		}

		/**
		 * @return the radioType
		 */
		public String getRadioType() {
			return radioType;
		}

		/**
		 * @param radioType
		 *            the radioType to set
		 */
		public void setRadioType(String radioType) {
			this.radioType = radioType;
		}

	}

	public class AddressInfo {
		private String country;

		private String country_code;

		private String region;

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCountry_code() {
			return country_code;
		}

		public void setCountry_code(String country_code) {
			this.country_code = country_code;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getStreet_number() {
			return street_number;
		}

		public void setStreet_number(String street_number) {
			this.street_number = street_number;
		}

		public String getPostal_code() {
			return postal_code;
		}

		public void setPostal_code(String postal_code) {
			this.postal_code = postal_code;
		}

		public String getAccuracy() {
			return accuracy;
		}

		public void setAccuracy(String accuracy) {
			this.accuracy = accuracy;
		}

		private String city;

		private String street;

		private String street_number;

		private String postal_code;

		private String accuracy;

		public String getDetail() {
			return (city + street).replace("null", "");
		}
	}

}
