package org.microg.networklocation.source;

import org.microg.networklocation.database.CellMap;

public interface CellLocationSource extends DataSource {
	public void requestCellLocation(final int mcc, final int mnc, final int cid, final CellMap cellMap);
}
