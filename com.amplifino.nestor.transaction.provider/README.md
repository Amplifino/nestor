# com.amplifino.nestor.transaction.provider #

Provides an implementation of the OSGI Transaction Service, the OSGI adaptation of JTA. 

This implementation delegates transaction commit decision logging to providers 
of the com.amplifino.nestor.transaction.provider.spi.TransactionLog interface.

See com.amplifino.nestor.transaction.spi for a sample implementation.

Users of this service can influence the order in with resources participates 
in the two phase commit protocol by having their XAResources implement com.amplifino.transaction.provider.xa.spi.XAResourceKind.




