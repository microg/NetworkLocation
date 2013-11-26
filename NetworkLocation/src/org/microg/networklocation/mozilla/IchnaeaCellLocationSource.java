package org.microg.networklocation.mozilla;

import android.content.Context;
import android.net.ConnectivityManager;
import org.microg.networklocation.source.OnlineCellLocationRetriever;
import org.microg.networklocation.source.OnlineCellLocationSource;

public class IchnaeaCellLocationSource extends OnlineCellLocationSource {
	protected IchnaeaCellLocationSource(Context context) {
		super(context, new LocationRetriever());
	}

	protected IchnaeaCellLocationSource(ConnectivityManager connectivityManager,
										OnlineCellLocationRetriever locationRetriever) {
		super(connectivityManager, locationRetriever);
	}
}
