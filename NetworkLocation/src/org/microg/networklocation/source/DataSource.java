package org.microg.networklocation.source;

public interface DataSource {
	String getName();
	String getDescription();
	boolean isSourceAvailable();
}
