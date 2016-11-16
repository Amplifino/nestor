# com.amplifino.nestor.transaction.spi #

Provides a sample implementation of TransactionLog.

By default this implementation does not use a persistent log, 
and in doubt xa transactions are not auto recoverable after a system failure.

A persistent log can be provided by publishing a com.amplifino.nestor.transaction.provider.spi.PersistentLog implementation.

When multiple OSGI containers use this bundle to connect to the same XA resources, 
it is recommended to configure a unique format id for each container to avoid recovering xa transactions
that originated in an other container.





