package org.microg.networklocation.source;

public interface OnlineCellLocationRetriever {
	Response retrieveLocation(int mcc, int mnc, int lac, int cid);

	class Response {
		private double latitude;
		private double longitude;
		private float accuracy;

		public Response(double latitude, double longitude, float accuracy) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.accuracy = accuracy;
		}

		public float getAccuracy() {
			return accuracy;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}
	}
}
