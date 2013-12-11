package org.microg.networklocation.retriever;

import org.microg.networklocation.data.CellSpec;

import java.util.Collection;

public interface CellSpecRetriever {
	Collection<CellSpec> retrieveCellSpecs();
}
