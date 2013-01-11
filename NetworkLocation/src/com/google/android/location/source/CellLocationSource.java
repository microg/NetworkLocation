package com.google.android.location.source;

import com.google.android.location.database.CellMap;

public interface CellLocationSource {
	public void requestCellLocation(final int mcc, final int mnc,
			final int cid, final CellMap cellMap);
}
