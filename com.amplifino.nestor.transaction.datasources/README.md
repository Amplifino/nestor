# com.amplifino.nestor.transaction.datasources

Provides a JTA transaction aware JDBC Connection Pool.

A transactional connection pool takes care of enlisting an XAResource
with the transaction manager for each lease started during the execution of the transaction.

If no transaction is in progress at the time of the connection lease, it behaves
as a non transactional connection pool. 


