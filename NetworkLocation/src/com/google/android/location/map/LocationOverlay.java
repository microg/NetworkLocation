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

public class LocationOverlay extends Overlay {

	private Location location;
	private final Context context;

	public LocationOverlay(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
		if (location != null) {
			final Paint fill = new Paint();
			fill.setColor(Color.parseColor(context
					.getString(R.color.holo_blue_light)));
			fill.setAlpha(100);
			fill.setAntiAlias(true);
			fill.setStyle(Paint.Style.FILL);
			final Paint stroke = new Paint();
			stroke.setColor(Color.parseColor(context
					.getString(R.color.holo_blue_dark)));
			stroke.setAlpha(200);
			stroke.setAntiAlias(true);
			stroke.setStyle(Paint.Style.STROKE);
			final Paint stroke2 = new Paint();
			stroke2.setColor(Color.BLACK);
			stroke2.setAlpha(200);
			stroke2.setAntiAlias(true);
			stroke2.setStyle(Paint.Style.STROKE);
			final GeoPoint gp = new GeoPoint(location);
			final Point pt = new Point();
			mapView.getProjection().toPixels(gp, pt);
			float radius = mapView.getProjection().metersToEquatorPixels(
					location.getAccuracy());
			if (radius < 20) {
				radius = 20;
			}

			canvas.drawCircle(pt.x, pt.y, radius * 0.9F, fill);
			canvas.drawCircle(pt.x, pt.y, radius * 0.95F, stroke2);
			canvas.drawCircle(pt.x, pt.y, radius, stroke);
		}
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}