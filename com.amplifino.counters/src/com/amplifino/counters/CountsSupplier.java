package com.amplifino.counters;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface CountsSupplier {
	Counts counts();
}
