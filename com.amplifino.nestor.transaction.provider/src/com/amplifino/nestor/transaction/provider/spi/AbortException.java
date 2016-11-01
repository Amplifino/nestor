package com.amplifino.nestor.transaction.provider.spi;

public class AbortException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public AbortException(String message) {
		super(message);
	}

	public AbortException(Throwable e) {
		super(e);
	}
	
	public AbortException(String message, Throwable e) {
		super(message, e);
	}
	
	
}
