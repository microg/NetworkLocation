package org.microg.networklocation.source;

import android.content.Context;
import android.net.ConnectivityManager;

public abstract class OnlineDataSource implements DataSource {
	private static final String TAG = "nlp.OnlineDataSource";
	private final ConnectivityManager connectivityManager;

	protected OnlineDataSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	protected OnlineDataSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	@Override
	public boolean isSourceAvailable() {
		return (connectivityManager.getActiveNetworkInfo() != null) &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}
}
