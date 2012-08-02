package com.google.android.location.map;

import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.location.R;

public class WlanAdapter extends BaseAdapter {

	private final Map<String, Location> wlans;
	private final Context context;

	public WlanAdapter(Context context, Map<String, Location> wlans) {
		this.context = context;
		this.wlans = wlans;
	}

	@Override
	public int getCount() {
		return wlans.size();
	}

	@Override
	public Object getItem(int position) {
		return wlans.values().toArray()[position];
	}

	@Override
	public long getItemId(int position) {
		return wlans.keySet().toArray()[position].hashCode();
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			final LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.wlan_entry, parent, false);
		}
		final TextView lat = (TextView) view.findViewById(R.id.wlan_lat);
		final TextView lon = (TextView) view.findViewById(R.id.wlan_lon);
		final TextView mac = (TextView) view.findViewById(R.id.mac);
		final Object[] array = wlans.entrySet().toArray();

		final Entry<String, Location> entry = (Entry<String, Location>) array[position];
		if (entry.getValue() != null) {
			lat.setText("Lat: " + entry.getValue().getLatitude());
			lon.setText("Lon: " + entry.getValue().getLongitude());
		} else {
			lat.setText("Lat: ---");
			lon.setText("Lon: ---");
		}
		mac.setText("MAC: " + entry.getKey());
		return view;
	}

}
