package org.microg.networklocation.opencellid;

import android.content.Context;
import android.net.ConnectivityManager;
import org.microg.networklocation.source.OnlineCellLocationSource;

public class OpenCellIdLocationSource extends OnlineCellLocationSource {

	private static final String NAME = "opencellid.org";
	private static final String DESCRIPTION = "Retrieve cell locations from opencellid.org when online";

	protected OpenCellIdLocationSource(Context context) {
		super(context, new LocationRetriever());
	}

	protected OpenCellIdLocationSource(ConnectivityManager connectivityManager) {
		super(connectivityManager, new LocationRetriever());
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
