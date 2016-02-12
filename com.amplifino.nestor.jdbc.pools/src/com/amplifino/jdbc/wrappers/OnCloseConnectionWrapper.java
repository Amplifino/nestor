package com.amplifino.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;

public final class OnCloseConnectionWrapper extends ConnectionWrapper {

	private final Consumer<Connection> onClose;
	
	public OnCloseConnectionWrapper (Connection connection, Consumer<Connection> onClose) {
		super(connection);
		this.onClose = Objects.requireNonNull(onClose);
	}
	
	@Override
	public void close() throws SQLException {
		onClose.accept(wrapped());
	}
}
