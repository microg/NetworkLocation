package com.google.android.location.map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;

import com.google.android.location.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class WlanOverlay extends Overlay {

	public class Entry {
		private final String mac;
		private final String name;
		private final Location location;
		private final boolean active;

		public Entry(Location location) {
			this(location, false, null, null);
		}

		public Entry(Location location, boolean active) {
			this(location, active, null, null);
		}

		public Entry(Location location, boolean active, String mac) {
			this(location, active, mac, null);
		}

		public Entry(Location location, boolean active, String mac, String name) {
			this.location = location;
			this.mac = mac;
			this.name = name;
			this.active = active;
		}

		public Location getLocation() {
			return location;
		}

		public String getMac() {
			return mac;
		}

		public String getName() {
			return name;
		}

		public boolean isActive() {
			return active;
		}
	}

	private final ArrayList<Entry> entryList;
	private final Context context;

	public WlanOverlay(Context context) {
		super(context);
		this.context = context;
		entryList = new ArrayList<Entry>();
	}

	public void addItem(Entry entry) {
		entryList.add(entry);
	}

	public void addItem(Location location, boolean active, String mac,
			String name) {
		addItem(getEntry(location, active, mac, name));
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		final Point pt = new Point();
		final Paint fillActive = new Paint();
		fillActive.setColor(Color.parseColor(context
				.getString(R.color.holo_green_light)));
		fillActive.setAlpha(75);
		fillActive.setAntiAlias(true);
		fillActive.setStyle(Paint.Style.FILL);
		final Paint strokeActive = new Paint();
		strokeActive.setColor(Color.parseColor(context
				.getString(R.color.holo_green_dark)));
		strokeActive.setAlpha(150);
		strokeActive.setAntiAlias(true);
		strokeActive.setStyle(Paint.Style.STROKE);
		final Paint fillInactive = new Paint();
		fillInactive.setColor(Color.BLACK);
		fillInactive.setAlpha(5);
		fillInactive.setAntiAlias(true);
		fillInactive.setStyle(Paint.Style.FILL);
		final Paint strokeInactive = new Paint();
		strokeInactive.setColor(Color.BLACK);
		strokeInactive.setAlpha(25);
		strokeInactive.setAntiAlias(true);
		strokeInactive.setStyle(Paint.Style.STROKE);
		for (final Entry entry : entryList) {
			final GeoPoint p = new GeoPoint(entry.getLocation());
			mapView.getProjection().toPixels(p, pt);
			final float radius = mapView.getProjection().metersToEquatorPixels(
					entry.getLocation().getAccuracy());
			if (entry.isActive()) {
				canvas.drawCircle(pt.x, pt.y, radius * 0.9F, fillActive);
				canvas.drawCircle(pt.x, pt.y, radius, strokeActive);
			} else {
				canvas.drawCircle(pt.x, pt.y, radius * 0.9F, fillInactive);
				canvas.drawCircle(pt.x, pt.y, radius, strokeInactive);
			}
		}
	}

	public Entry getEntry(Location location, boolean active, String mac,
			String name) {
		return new Entry(location, active, mac, name);
	}

	public void removeAll() {
		entryList.clear();
	}

}