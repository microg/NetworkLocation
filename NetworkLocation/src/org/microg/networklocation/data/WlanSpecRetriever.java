package org.microg.networklocation.data;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.microg.networklocation.MainService;

import java.util.ArrayList;
import java.util.Collection;

public class WlanSpecRetriever {
	private static final String TAG = "WlanSpecRetriever";
	private final WifiManager wifiManager;

	public WlanSpecRetriever(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	public WlanSpecRetriever(Context context) {
		this((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
	}

	public Collection<WlanSpec> retrieveWlanSpecs() {
		Collection<WlanSpec> wlanSpecs = new ArrayList<WlanSpec>();
		if (wifiManager == null) {
			return wlanSpecs;
		}
		Collection<ScanResult> scanResults = wifiManager.getScanResults();
		if (scanResults == null) {
			return wlanSpecs;
		}
		for (ScanResult scanResult : scanResults) {
			MacAddress macAddress = MacAddress.parse(scanResult.BSSID);
			int frequency = scanResult.frequency;
			int level = scanResult.level;
			wlanSpecs.add(new WlanSpec(macAddress, frequency, level));
		}
		if (MainService.DEBUG) {
			Log.d(TAG, "Found "+wlanSpecs.size()+" WLANs");
		}
		return wlanSpecs;
	}
}
