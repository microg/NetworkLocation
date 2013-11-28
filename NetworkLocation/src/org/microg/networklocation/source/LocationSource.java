package org.microg.networklocation.source;

import org.microg.networklocation.v2.LocationSpec;
import org.microg.networklocation.v2.PropSpec;

import java.util.Collection;

public interface LocationSource<T extends PropSpec> extends DataSource {
	Collection<LocationSpec<T>> retrieveLocation(Collection<T> specs);
}
