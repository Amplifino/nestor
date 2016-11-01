package com.amplifino.nestor.transaction.provider.spi;

import javax.transaction.xa.XAResource;

public interface RecoveryAgent {

	void recover(XAResource xaResource);
}
