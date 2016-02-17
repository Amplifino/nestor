package com.amplifino.nestor.bundles.rest;

import java.util.Objects;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWire;

class WireAdapter {
	
	private final BundleWire wire;

	private WireAdapter(BundleWire wire) {
		this.wire = wire;
	}
	
	static WireAdapter of(BundleWire wire) {
		return new WireAdapter(wire);
	}
	
	Bundle from() {
		return wire.getRequirerWiring().getBundle();
	}
	
	Bundle to() {
		return wire.getProviderWiring().getBundle();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof WireAdapter) {
			WireAdapter other = (WireAdapter) o;
			return from().equals(other.from()) && to().equals(other.to());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(from(),to());
	}
}
