package org.microg.networklocation.source;

public interface DataSource {
	String getName();
	String getDescription();
	String getCopyright();
	boolean isSourceAvailable();
}
