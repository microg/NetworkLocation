package org.microg.networklocation.backends.mozilla;

import android.content.Context;
import android.net.ConnectivityManager;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.source.LocationSource;
import org.microg.networklocation.source.OnlineDataSource;

import java.util.Collection;

public class IchnaeaCellLocationSource extends OnlineDataSource implements LocationSource<CellSpec> {
	private static final String NAME = "Mozilla Location Service";
	private static final String DESCRIPTION = "Retrieve cell locations from Mozilla while online";
	private static final String COPYRIGHT = "© Mozilla\nLicense: unknown";

	public IchnaeaCellLocationSource(Context context) {
		super(context);
	}

	protected IchnaeaCellLocationSource(ConnectivityManager connectivityManager) {
		super(connectivityManager);
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
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> specs) {
		return null; //TODO: Implement
	}
}
