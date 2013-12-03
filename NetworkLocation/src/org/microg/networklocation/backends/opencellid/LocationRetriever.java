package org.microg.networklocation.backends.opencellid;

import org.microg.networklocation.helper.Networking;
import org.microg.networklocation.source.OnlineCellLocationRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class LocationRetriever implements OnlineCellLocationRetriever {
	private static final String SERVICE_URL = "http://www.opencellid.org/cell/get?fmt=txt";

	LocationRetriever() {

	}

	public Response retrieveCellLocation(int mcc, int mnc, int lac, int cid) {
		try {
			URL url = new URL(SERVICE_URL + "&mcc=" + mcc + "&mnc=" + mnc + "&lac=" + lac + "&cellid=" + cid);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput(true);
			InputStream inputStream = urlConnection.getInputStream();
			String result = new String(Networking.readStreamToEnd(inputStream));
			String[] split = result.split(",");
			return new Response(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Float.parseFloat(split[2]));
		} catch (IOException e) {
			return null;
		}
	}
}
