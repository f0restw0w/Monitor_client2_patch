package com.m1.android.data.locate;

public class DetailAddress {
	private String access_token;

	private LocationInfo location;

	public LocationInfo getLocation() {
		return location;
	}

	public void setLocation(LocationInfo location) {
		this.location = location;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	static public class LocationInfo {
		private AddressInfo address;

		private double latitude;

		private double longitude;

		private float altitude;

		private float accuracy;

		private float altitude_accuracy;

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public AddressInfo getAddress() {
			return address;
		}

		public void setAddress(AddressInfo address) {
			this.address = address;
		}

		/**
		 * @return the altitude
		 */
		public float getAltitude() {
			return altitude;
		}

		/**
		 * @param altitude
		 *            the altitude to set
		 */
		public void setAltitude(float altitude) {
			this.altitude = altitude;
		}

		/**
		 * @return the accuracy
		 */
		public float getAccuracy() {
			return accuracy;
		}

		/**
		 * @param accuracy
		 *            the accuracy to set
		 */
		public void setAccuracy(float accuracy) {
			this.accuracy = accuracy;
		}

		/**
		 * @return the altitude_accuracy
		 */
		public float getAltitude_accuracy() {
			return altitude_accuracy;
		}

		/**
		 * @param altitude_accuracy
		 *            the altitude_accuracy to set
		 */
		public void setAltitude_accuracy(float altitude_accuracy) {
			this.altitude_accuracy = altitude_accuracy;
		}
	}
	
}
