package com.amplifino.nestor.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;

public final class OnCloseConnectionWrapper extends ConnectionWrapper {

	private final Consumer<Connection> onClose;
	
	private OnCloseConnectionWrapper (Connection connection, Consumer<Connection> onClose) {
		super(connection);
		this.onClose = Objects.requireNonNull(onClose);
	}
	
	@Override
	public void close() throws SQLException {
		onClose.accept(wrapped());
	}
	
	public static Connection on (Connection connection, Consumer<Connection> onClose) {
		return new OnCloseConnectionWrapper(connection, onClose);
	}
}
