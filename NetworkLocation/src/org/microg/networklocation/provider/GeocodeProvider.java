package org.microg.networklocation.provider;

import android.os.IBinder;
import org.microg.networklocation.database.GeocodeDatabase;
import org.microg.networklocation.source.GeocodeSource;

import java.util.List;

public interface GeocodeProvider {
	IBinder getBinder();

	void setGeocodeDatabase(GeocodeDatabase geocodeDatabase);

	void setSources(List<GeocodeSource> geocodeSources);
}
