package com.amplifino.nestor.transaction.control;

import javax.transaction.xa.XAResource;

import org.osgi.service.transaction.control.LocalResource;

import com.amplifino.nestor.transaction.provider.xa.spi.XAResourceKind;

public enum Compliance {
	ACID {
		@Override
		XAResource wrap(LocalResource resource) {
			return new AcidXAResourceAdapter(resource);
		}
	},
	LAST_RESOURCE_GAMBIT {
		@Override
		XAResource wrap(LocalResource resource) {
			return new HeuristicXAResourceAdapter(resource, XAResourceKind.Kind.EXCLUSIVE_LAST);
		}
	},
	HEURISTIC {
		@Override
		XAResource wrap(LocalResource resource) {
			return new HeuristicXAResourceAdapter(resource, XAResourceKind.Kind.ONEPHASE);
		}
	};
	
	abstract XAResource wrap(LocalResource resource);
}
