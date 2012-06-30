package com.google.android.location.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.location.Database;
import com.google.android.location.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class Activity extends android.app.Activity implements LocationListener {
	private Database data;

	private MapView mapView;
	private MapController mc;
	private LocationOverlay pos_overlay;

	private WlanOverlay wlan_overlay;

	private Collection<String> getWLANs() {
		final ArrayList<String> wlans = new ArrayList<String>();
		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		final List<ScanResult> result = wifiManager.getScanResults();
		if (result != null) {
			for (final ScanResult scanResult : result) {
				final String mac = Database.niceMac(scanResult.BSSID);
				wlans.add(mac);
			}
		}
		return wlans;
	}

	private void makeLocationVisible(Location loc) {
		final GeoPoint pt = new GeoPoint(loc);
		wlan_overlay.removeAll();
		pos_overlay.setLocation(loc);
		mc.setCenter(pt);
		final Collection<String> macs = getWLANs();
		final Map<String, Location> cache = data.getNext(loc.getLatitude(),
				loc.getLongitude(), 200);
		for (final String mac : cache.keySet()) {
			final Location l = cache.get(mac);
			wlan_overlay.addItem(l, macs.contains(mac), mac, null);
		}
		Log.d("LocationActivity", "locked at " + pt.toDoubleString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mapView = (MapView) findViewById(R.id.mapView);
		mc = mapView.getController();
		wlan_overlay = new WlanOverlay(this);
		pos_overlay = new LocationOverlay(this);
		mapView.getOverlays().add(wlan_overlay);
		mapView.getOverlays().add(pos_overlay);
		mc.setZoom(17);
		Database.init(this);
		data = Database.getInstance();
		final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 2000, 10, this);
	}

	@Override
	protected void onDestroy() {
		final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(this);
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			makeLocationVisible(location);
		} else {
			Log.d("LocationActivity", "could not lock!");
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
