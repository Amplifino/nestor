package com.amplifino.nestor.transaction.provider.spi;

import javax.transaction.xa.XAResource;

public interface RecoveryService {

	void recover(XAResource xaResource);
}
