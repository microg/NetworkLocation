package org.microg.networklocation.backends.mozilla;

import org.microg.networklocation.source.OnlineCellLocationRetriever;

public class LocationRetriever implements OnlineCellLocationRetriever {
	private static final String SEARCH_URL = "https://location.services.mozilla.com/v1/search";

	LocationRetriever() {

	}

	@Override
	public Response retrieveCellLocation(int mcc, int mnc, int lac, int cid) {
		//TODO: Implement
		return null;
	}
}
