package org.microg.networklocation.backends.mozilla;

import android.content.Context;
import android.net.ConnectivityManager;
import org.microg.networklocation.source.OnlineCellLocationRetriever;
import org.microg.networklocation.source.OnlineCellLocationSource;

public class IchnaeaCellLocationSource extends OnlineCellLocationSource {
	private static final String NAME = "Mozilla Location Service";
	private static final String DESCRIPTION = "Retrieve cell locations from Mozilla while online";
	private static final String COPYRIGHT = "© Mozilla\nLicense: unknown";

	public IchnaeaCellLocationSource(Context context) {
		super(context, new LocationRetriever());
	}

	protected IchnaeaCellLocationSource(ConnectivityManager connectivityManager,
										OnlineCellLocationRetriever locationRetriever) {
		super(connectivityManager, locationRetriever);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
