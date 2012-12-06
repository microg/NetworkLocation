package com.google.android.location.map;

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

public class GsmOverlay extends Overlay {

	private final Context context;
	private Location location;

	public GsmOverlay(final Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void draw(final Canvas canvas, final MapView mapView,
			final boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
		if (location != null) {
			final Paint fill = new Paint();
			fill.setColor(Color.parseColor(context
					.getString(R.color.holo_purple_light)));
			fill.setAlpha(60);
			fill.setAntiAlias(true);
			fill.setStyle(Paint.Style.FILL);
			final Paint stroke = new Paint();
			stroke.setColor(Color.parseColor(context
					.getString(R.color.holo_purple_dark)));
			stroke.setAlpha(120);
			stroke.setAntiAlias(true);
			stroke.setStyle(Paint.Style.STROKE);
			final GeoPoint gp = new GeoPoint(location);
			final Point pt = new Point();
			mapView.getProjection().toPixels(gp, pt);
			final float radius = mapView.getProjection().metersToEquatorPixels(
					location.getAccuracy());

			canvas.drawCircle(pt.x, pt.y, radius * 0.9F, fill);
			canvas.drawCircle(pt.x, pt.y, radius, stroke);
		} else {
		}
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}