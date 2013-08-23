package org.microg.netlocation.source;

import org.microg.netlocation.database.CellMap;

public interface CellLocationSource extends DataSource {
	public void requestCellLocation(final int mcc, final int mnc, final int cid, final CellMap cellMap);
}
