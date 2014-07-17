
package com.m1.android.data.locate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 地理位置
 * 
 * @author zhaozhongyang
 * @2011-12-12 下午08:00:28
 */
public class LocationEntity implements Parcelable {
    // 国家
    private String country;

    // 省级
    private String province;

    // 市级
    private String city;

    // 县级
    private String county;

    // 经度
    private double longitude;

    // 纬度
    private double latitude;

    // 详细地址
    private String address;

    public LocationEntity() {
    }

    public LocationEntity(Parcel source) {
        this.country = source.readString();
        this.province = source.readString();
        this.city = source.readString();
        this.county = source.readString();
        this.longitude = source.readDouble();
        this.latitude = source.readDouble();
        this.address = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(county);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(address);
    }

    public static final Parcelable.Creator<LocationEntity> CREATOR = new Creator<LocationEntity>() {

        @Override
        public LocationEntity[] newArray(int size) {
            return new LocationEntity[size];
        }

        @Override
        public LocationEntity createFromParcel(Parcel source) {
            return new LocationEntity(source);
        }
    };

    /**
     * @return the province
     */
    public String getProvince() {
        return province;
    }

    /**
     * @param province the province to set
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the county
     */
    public String getCounty() {
        return county;
    }

    /**
     * @param county the county to set
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

}
