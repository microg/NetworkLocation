package org.microg.networklocation.retriever;

import org.microg.networklocation.data.WifiSpec;

import java.util.Collection;

public interface WifiSpecRetriever {
	Collection<WifiSpec> retrieveWifiSpecs();
}
