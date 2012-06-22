package com.google.android.location.map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private final ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	private final Context context;

	public MyItemizedOverlay(Context context, Drawable marker) {
		super(boundCenterBottom(marker));
		this.context = context;
		// TODO Auto-generated constructor stub

		populate();
	}

	public void addItem(GeoPoint p, String title, String snippet) {
		final OverlayItem newItem = new OverlayItem(p, title, snippet);
		overlayItemList.add(newItem);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return overlayItemList.get(i);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
		// boundCenterBottom(marker);
	}

	public void removeAll() {
		overlayItemList.clear();
		populate();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlayItemList.size();
	}

}