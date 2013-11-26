package org.microg.networklocation.opencellid;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.source.CellLocationSource;
import org.microg.networklocation.source.OnlineCellLocationSource;

public class OpenCellIdLocationSource extends OnlineCellLocationSource {

	protected OpenCellIdLocationSource(Context context) {
		super(context, new LocationRetriever());
	}

	protected OpenCellIdLocationSource(ConnectivityManager connectivityManager) {
		super(connectivityManager, new LocationRetriever());
	}
}
