package com.amplifino.nestor.jdbc.wrappers;

import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.Objects;

public class WrapperWrapper implements Wrapper{

	private final Wrapper wrapper;
	
	protected WrapperWrapper(Wrapper wrapper) {
		this.wrapper = Objects.requireNonNull(wrapper);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) || wrapper.isWrapperFor(iface);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.isInstance(this) ? (T) this : wrapper.unwrap(iface);
	}

}
