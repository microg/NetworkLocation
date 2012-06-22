package com.google.android.location.map;

import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.location.Database;
import com.google.android.location.LocationData;
import com.google.android.location.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class Activity extends android.app.Activity {

	class GetLocationTask extends AsyncTask<Void, Void, Location> {

		@Override
		protected Location doInBackground(Void... params) {
			final Location loc = data.getCurrentLocation();
			return loc;
		}

		@Override
		protected void onPostExecute(Location loc) {
			super.onPostExecute(loc);
			if (loc != null) {
				final GeoPoint pt = new GeoPoint(loc.getLatitude(),
						loc.getLongitude());
				pos_overlay.removeAll();
				no_overlay.removeAll();
				yes_overlay.removeAll();
				pos_overlay.addItem(pt, "me", "me");
				mc.setCenter(pt);
				final Collection<String> macs = data.getWLANs();
				final Map<String, Location> cache = data.getNextFromCache(loc.getLatitude(), loc.getLongitude(), 200);
				for (final String mac : cache.keySet()) {
					final Location l = cache.get(mac);
					final GeoPoint p = new GeoPoint(l.getLatitude(),
							l.getLongitude());
					if (macs.contains(mac)) {
						yes_overlay.addItem(p, mac, mac);
					} else {
						no_overlay.addItem(p, mac, mac);
					}
				}
				Log.d("Activity", "locked at " + pt.toDoubleString()
						+ ". Refresh in 10sec...");
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						final GetLocationTask task = new GetLocationTask();
						task.execute();
					}
				}, 10000);

			} else {
				pos_overlay.removeAll();
				no_overlay.removeAll();
				yes_overlay.removeAll();
				final Map<String, Location> cache = data.getCache();
				for (final String mac : cache.keySet()) {
					final Location l = cache.get(mac);
					final GeoPoint p = new GeoPoint(l.getLatitude(),
							l.getLongitude());
					no_overlay.addItem(p, mac, mac);
				}
				Log.d("Activity", "failed. Refresh in 10sec...");
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						final GetLocationTask task = new GetLocationTask();
						task.execute();
					}
				}, 10000);
			}
		}

	}

	LocationData data;

	Timer timer;

	MapView mapView;
	MapController mc;
	MyItemizedOverlay no_overlay;
	MyItemizedOverlay yes_overlay;
	MyItemizedOverlay pos_overlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mapView = (MapView) findViewById(R.id.mapView);
		mc = mapView.getController();
		no_overlay = new MyItemizedOverlay(this, getResources().getDrawable(
				R.drawable.btn_radio_on_disabled_holo_light));
		yes_overlay = new MyItemizedOverlay(this, getResources().getDrawable(
				R.drawable.btn_radio_on_holo));
		pos_overlay = new MyItemizedOverlay(this, getResources().getDrawable(
				R.drawable.btn_radio_on_focused_holo_light));
		mapView.getOverlays().add(no_overlay);
		mapView.getOverlays().add(yes_overlay);
		mapView.getOverlays().add(pos_overlay);
		mc.setZoom(17);
		timer = new Timer();
		Database.init(this);
		LocationData.init(this);
		data = LocationData.getInstance();
		final GetLocationTask task = new GetLocationTask();
		task.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		timer.cancel();
		timer.purge();
	}

}
