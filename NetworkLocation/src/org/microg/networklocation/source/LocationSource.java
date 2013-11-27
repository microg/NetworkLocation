package org.microg.networklocation.source;

import org.microg.networklocation.v2.LocationSpec;
import org.microg.networklocation.v2.PropSpec;

import java.util.List;

public interface LocationSource<T extends PropSpec> extends DataSource {
	LocationSpec[] retrieveLocation(T... specs);
	List<LocationSpec> retrieveLocation(List<T> specs);
}
