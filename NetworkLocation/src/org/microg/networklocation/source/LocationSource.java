package org.microg.networklocation.source;

import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.data.PropSpec;

import java.util.Collection;

public interface LocationSource<T extends PropSpec> extends DataSource {
	Collection<LocationSpec<T>> retrieveLocation(Collection<T> specs);
}
