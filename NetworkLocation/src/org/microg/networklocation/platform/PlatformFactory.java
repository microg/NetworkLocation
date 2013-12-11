package org.microg.networklocation.platform;

import android.content.Context;
import android.os.Build;

public class PlatformFactory {
	private PlatformFactory() {

	}

	public static org.microg.networklocation.provider.NetworkLocationProvider newNetworkLocationProvider() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
			return new org.microg.networklocation.platform.NetworkLocationProvider();
		} else {
			return new NetworkLocationProviderV2();
		}
	}

	public static org.microg.networklocation.retriever.CellSpecRetriever newCellSpecRetriever(Context context) {
		return new CellSpecRetriever(context);
	}

	public static org.microg.networklocation.retriever.WifiSpecRetriever newWifiSpecRetriever(Context context) {
		return new WifiSpecRetriever(context);
	}

	public static GeocodeProvider newGeocodeProvider() {
		return new GeocodeProvider();
	}
}
