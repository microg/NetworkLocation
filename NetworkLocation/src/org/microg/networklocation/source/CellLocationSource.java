package org.microg.networklocation.source;

import org.microg.networklocation.database.CellMap;

public interface CellLocationSource extends DataSource {
	void requestCellLocation(int mcc, int mnc, int cid, int lac, CellMap cellMap);
}
