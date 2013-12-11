package org.microg.networklocation.provider;

import android.os.IBinder;
import org.microg.networklocation.source.GeocodeSource;

import java.util.List;

public interface GeocodeProvider {
	IBinder getBinder();

	void setSources(List<GeocodeSource> geocodeSources);
}
