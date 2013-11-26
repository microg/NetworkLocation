package org.microg.networklocation.source;

import org.microg.networklocation.database.CellMap;

public interface CellLocationSource extends DataSource {
	String getName();
	String getDescription();
	void requestCellLocation(int mcc, int mnc, int cid, int lac, CellMap cellMap);
}
