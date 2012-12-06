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
		private final boolean active;
		private final Location location;
		private final String mac;
		private final String name;

		public Entry(final Location location) {
			this(location, false, null, null);
		}

		public Entry(final Location location, final boolean active) {
			this(location, active, null, null);
		}

		public Entry(final Location location, final boolean active,
				final String mac) {
			this(location, active, mac, null);
		}

		public Entry(final Location location, final boolean active,
				final String mac, final String name) {
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

	private final Context context;
	private final ArrayList<Entry> entryList;

	public WlanOverlay(final Context context) {
		super(context);
		this.context = context;
		entryList = new ArrayList<Entry>();
	}

	public void addItem(final Entry entry) {
		entryList.add(entry);
	}

	public void addItem(final Location location, final boolean active,
			final String mac, final String name) {
		addItem(getEntry(location, active, mac, name));
	}

	@Override
	public void draw(final Canvas canvas, final MapView mapView,
			final boolean shadow) {
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

	public Entry getEntry(final Location location, final boolean active,
			final String mac, final String name) {
		return new Entry(location, active, mac, name);
	}

	public void removeAll() {
		entryList.clear();
	}

}