package com.amplifino.nestor.jdbc.wrappers;

import java.sql.Connection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Changes the close behavior of the wrapped connection
 *
 */
public final class OnCloseConnectionWrapper extends ConnectionWrapper {

	private final Consumer<Connection> onClose;
	
	private OnCloseConnectionWrapper (Connection connection, Consumer<Connection> onClose) {
		super(connection);
		this.onClose = Objects.requireNonNull(onClose);
	}
	
	@Override
	public void close() {
		onClose.accept(wrapped());
	}
	
	/**
	 * creates a ConnectionWrapper with a different close behavior
	 * @param connection
	 * @param onClose
	 * @return
	 */
	public static Connection on (Connection connection, Consumer<Connection> onClose) {
		return new OnCloseConnectionWrapper(connection, onClose);
	}
}
