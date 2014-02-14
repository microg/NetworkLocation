package org.microg.networklocation.backends.opencellid;

import android.content.Context;
import android.util.Log;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.helper.Networking;
import org.microg.networklocation.source.LocationSource;
import org.microg.networklocation.source.OnlineDataSource;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

public class OpenCellIdLocationSource extends OnlineDataSource implements LocationSource<CellSpec> {

	private static final String TAG = "nlp.OpenCellIdLocationSource";
	private static final String NAME = "opencellid.org";
	private static final String DESCRIPTION = "Retrieve cell locations from opencellid.org when online";
	private static final String COPYRIGHT = "© OpenCellID.org\nLicense: CC BY-SA 3.0";
	private static final String SERVICE_URL =
			"http://www.opencellid.org/cell/get?fmt=txt&mcc=%d&mnc=%d&lac=%d&cellid=%d";
	private final Context context;

	public OpenCellIdLocationSource(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public boolean isSourceAvailable() {
		// FIXME: Temporary disabled until usage is cleared up
		return false;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> cellSpecs) {
		Collection<LocationSpec<CellSpec>> locationSpecs = new ArrayList<LocationSpec<CellSpec>>();
		for (CellSpec cellSpec : cellSpecs) {
			try {
				URL url = new URL(String.format(SERVICE_URL, cellSpec.getMcc(), cellSpec.getMnc(), cellSpec.getLac(),
												cellSpec.getCid()));
				URLConnection connection = url.openConnection();
				Networking.setUserAgentOnConnection(connection, context);
				connection.setDoInput(true);
				InputStream inputStream = connection.getInputStream();
				String result = new String(Networking.readStreamToEnd(inputStream));
				if ((result == null) || !result.isEmpty()) {
					String[] split = result.split(",");
					if (split.length >= 3) {
						locationSpecs.add(new LocationSpec<CellSpec>(cellSpec, Double.parseDouble(split[0]),
																	 Double.parseDouble(split[1]),
																	 Double.parseDouble(split[2])));
					}
				}
			} catch (Throwable t) {
				Log.w(TAG, t);
			}
		}
		return locationSpecs;
	}
}
