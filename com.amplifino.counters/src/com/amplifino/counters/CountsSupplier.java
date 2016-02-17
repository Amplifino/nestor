package com.amplifino.counters;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Interface indicating that the implementor tracks event counts 
 *
 */
@ConsumerType
public interface CountsSupplier {
	/**
	 * return a snapshot of the current counts
	 * @return
	 */
	Counts counts();
}
