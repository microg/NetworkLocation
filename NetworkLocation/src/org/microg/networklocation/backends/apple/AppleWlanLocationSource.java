package org.microg.networklocation.backends.apple;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.data.MacAddress;
import org.microg.networklocation.data.WlanSpec;
import org.microg.networklocation.source.LocationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class AppleWlanLocationSource implements LocationSource<WlanSpec> {

	public static final float LATLON_WIRE = 1E8F;
	private static final String TAG = "AppleWlanLocationSource";
	private static final String NAME = "Apple Location Service";
	private static final String DESCRIPTION = "Retrieve WLAN locations from Apple";
	private final ConnectivityManager connectivityManager;
	private final LocationRetriever locationRetriever = new LocationRetriever();

	public AppleWlanLocationSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	public AppleWlanLocationSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	public static String niceMac(String mac) {
		mac = mac.toLowerCase(Locale.getDefault());
		final StringBuilder builder = new StringBuilder();
		final String[] arr = mac.split(":");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].length() == 1) {
				builder.append("0");
			}
			builder.append(arr[i]);
			if (i < arr.length - 1) {
				builder.append(":");
			}
		}
		return builder.toString();
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isSourceAvailable() {
		return (connectivityManager.getActiveNetworkInfo() != null) &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public Collection<LocationSpec<WlanSpec>> retrieveLocation(Collection<WlanSpec> specs) {
		Collection<LocationSpec<WlanSpec>> locationSpecs = new ArrayList<LocationSpec<WlanSpec>>();
		Collection<String> macs = new ArrayList<String>();
		for (WlanSpec spec : specs) {
			macs.add(niceMac(spec.getMac().toString()));
		}

		try {
			Response response = locationRetriever.retrieveLocations(macs);
			int locsGet = 0;
			for (Response.ResponseWLAN responseWLAN : response.wlan) {
				try {
					WlanSpec wlanSpec = new WlanSpec(MacAddress.parse(responseWLAN.mac), responseWLAN.channel);
					if ((responseWLAN.location.altitude != null) && (responseWLAN.location.altitude > -500)) {
						locationSpecs
								.add(new LocationSpec<WlanSpec>(wlanSpec, responseWLAN.location.latitude / LATLON_WIRE,
																responseWLAN.location.longitude / LATLON_WIRE,
																responseWLAN.location.accuracy,
																responseWLAN.location.altitude));
					} else {
						locationSpecs
								.add(new LocationSpec<WlanSpec>(wlanSpec, responseWLAN.location.latitude / LATLON_WIRE,
																responseWLAN.location.longitude / LATLON_WIRE,
																responseWLAN.location.accuracy));
					}
					locsGet++;
				} catch (Exception e) {
					if (MainService.DEBUG) {
						Log.w(TAG, e);
					}
				}
			}
			if (MainService.DEBUG) {
				Log.d(TAG, "got " + locsGet + " usable locations from server");
			}

		} catch (IOException e) {
			if (MainService.DEBUG) {
				Log.w(TAG, e);
			}
		}
		return locationSpecs;
	}
}
