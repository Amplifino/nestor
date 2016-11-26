package com.amplifino.nestor.jms;
import javax.jms.JMSException;

public final class UncheckedJMSException  extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public UncheckedJMSException(JMSException e) {
		super(e);
	}

}
