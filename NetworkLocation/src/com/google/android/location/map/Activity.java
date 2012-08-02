package com.google.android.location.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.location.DatabaseHelper;
import com.google.android.location.GsmCellMap;
import com.google.android.location.R;
import com.google.android.location.WlanLocationData;
import com.google.android.location.WlanMap;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class Activity extends android.app.Activity implements LocationListener {

	private static final String TAG = Activity.class.getName();
	private WlanMap wlans;
	private GsmCellMap cells;
	private Map<String, Location> currentWlans;
	private DatabaseHelper helper;

	private MapView mapView;
	private MapController mc;
	private LocationOverlay pos_overlay;

	private WlanOverlay wlan_overlay;
	private GsmOverlay gsm_overlay;
	private TextView youLat;
	private TextView youLon;
	private TextView cellMcc;
	private TextView cellMnc;
	private TextView cellCid;
	private TextView cellLat;
	private TextView cellLon;
	private ListView wlanList;
	private WlanAdapter wlanAdapter;
	private TelephonyManager telephonyManager;

	private GsmCellLocation getGsmCellLocation() {
		final CellLocation cellLocation = telephonyManager.getCellLocation();
		if (cellLocation instanceof GsmCellLocation) {
			return (GsmCellLocation) cellLocation;
		}
		return null;
	}

	private Collection<String> getWLANs() {
		return WlanLocationData.getWLANs(this);
	}

	private void makeLocationVisible(Location loc) {
		youLat.setText("Lat: " + loc.getLatitude());
		youLon.setText("Lon: " + loc.getLongitude());
		final GsmCellLocation cell = getGsmCellLocation();
		int cid = -1, mcc = -1, mnc = -1;
		if (cell == null) {
			cellCid.setText("CID: ---");
		} else {
			cid = cell.getCid();
			cellCid.setText("CID: " + cid);
		}
		final String operator = telephonyManager.getNetworkOperator();
		if (operator == null || operator.length() <= 3) {
			cellMcc.setText("MCC: ---");
			cellMnc.setText("MNC: ---");
		} else {
			mcc = Integer.parseInt(operator.substring(0, 3));
			mnc = Integer.parseInt(operator.substring(3));
			cellMcc.setText("MCC: " + mcc);
			cellMnc.setText("MNC: " + mnc);
		}
		boolean setCellLoc = false;
		if (cid != -1 && mcc != -1 && mnc != -1) {
			final Location l = cells.get(mcc, mnc, cid);
			if (l != null) {
				setCellLoc = true;
				cellLat.setText("Lat: " + l.getLatitude());
				cellLon.setText("Lon: " + l.getLongitude());
				gsm_overlay.setLocation(l);
			}
		}
		if (!setCellLoc) {
			cellLat.setText("Lat: ---");
			cellLon.setText("Lon: ---");
			gsm_overlay.setLocation(null);
		}
		final GeoPoint pt = new GeoPoint(loc);
		wlan_overlay.removeAll();
		pos_overlay.setLocation(loc);
		mc.setCenter(pt);
		final Collection<String> macs = getWLANs();
		final Map<String, Location> cache = wlans.getNext(loc.getLatitude(),
				loc.getLongitude(), 400);
		currentWlans.clear();
		for (final String mac : cache.keySet()) {
			if (!macs.contains(mac)) {
				final Location l = cache.get(mac);
				wlan_overlay.addItem(l, false, mac, null);
			}
		}
		for (final String mac : macs) {
			final Location l = wlans.get(mac);
			wlan_overlay.addItem(l, true, mac, null);
			currentWlans.put(mac, l);
		}
		wlanAdapter.notifyDataSetChanged();
		Log.d(TAG, "locked at " + pt.toDoubleString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		currentWlans = new HashMap<String, Location>();
		mapView = (MapView) findViewById(R.id.mapView);
		youLat = (TextView) findViewById(R.id.you_lat);
		youLon = (TextView) findViewById(R.id.you_lon);
		cellMcc = (TextView) findViewById(R.id.mcc);
		cellMnc = (TextView) findViewById(R.id.mnc);
		cellCid = (TextView) findViewById(R.id.cid);
		cellLat = (TextView) findViewById(R.id.cell_lat);
		cellLon = (TextView) findViewById(R.id.cell_lon);
		wlanList = (ListView) findViewById(R.id.wlan_list);
		wlanAdapter = new WlanAdapter(this, currentWlans);
		wlanList.setAdapter(wlanAdapter);
		mc = mapView.getController();
		wlan_overlay = new WlanOverlay(this);
		pos_overlay = new LocationOverlay(this);
		gsm_overlay = new GsmOverlay(this);
		mapView.getOverlays().add(wlan_overlay);
		mapView.getOverlays().add(pos_overlay);
		mc.setZoom(17);
		helper = new DatabaseHelper(this);
		wlans = new WlanMap(helper);
		cells = new GsmCellMap(helper);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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
