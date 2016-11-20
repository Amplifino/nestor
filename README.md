# nestor #
Project nestor builds a modular enterprise application server based on OSGI and Declarative Services. 

See also: [http://www.amplifino.be/html/nestor.html](http://www.amplifino.be/html/nestor.html).

Development environment is based on Eclipse+BndTools. 

Main nestor components are:

- a generic, configurable object pool
- a jdbc database connection pool, configurable with OSGI Config Admin and using an OSGi DataSourceFactory.
- a transaction manager implementation
- a transactional jdbc database connection pool.
- a whiteboard for JAX-RS applications using Jersey and Jackson
- a whiteboard for SOAP web services using the JAX-WS implementation included in Java SE.

This repo contains the following Bundles (BndTools projects)  

- **cnf**: BndTools bundle repository
- **com.amplifino.counters**: thread safe counters
- **com.amplifino.nestor.associations**: lazy references to map rdbms foreign keys.
- **com.amplifino.nestor.bndrun.template**: template for bndrun files containing essential bundles for interactive testing.
- **com.amplifino.nestor.bundles.rest:** sample rest whiteboard application showing graphical view of bundle wiring
- **com.amplifino.nestor.dot**: java interface to external GraphViz dot program
- **com.amplifino.nestor.jaxrs**: OSGI RFC217 rest whiteboard using Jersey 
- **com.amplifino.nestor.jdbc.api**: fluent api on top of JDBC
- **com.amplifino.nestor.jdbc.pools**: JDBC connection pool using OSGI JDBC service (DataSourceFactory)
- **com.amplifino.nestor.logging**: logging bridge from java.util.logging to OSGI Log Service
- **com.amplifino.nestor.logging.test**: test bundle for com.amplifino.nestor.logging
- **com.amplifino.nestor.rdbms.schema**: database schema
- **com.amplifino.nestor.rdbms.schema.test**: database schema test bundle
- **com.amplifino.nestor.rest**: rest whiteboard using Jersey and Jackson
- **com.amplifino.nestor.security.http**: servlet filter using OSGI useradmin service 
- **com.amplifino.nestor.soap**: soap whiteboard using JAX-WS Reference Implementation included in Java 8
- **com.amplifino.nestor.soap.sample**: extremely simple sample webservice for testing soap whiteboard
- **com.amplifino.nestor.soap.test**: soap whiteboard test bundle
- **com.amplifino.nestor.transaction**: safe transaction API on top of javax.transaction.UserTransaction
- **com.amplifino.nestor.transaction.control**: OSGI RFC221 transaction control implementation
- **com.amplifino.nestor.transaction.control.jdbc**: OSGI RFC221 transaction control jdbc specifics implementation
- **com.amplifino.nestor.transaction.control.jdbc.test**: test bundle fro com.amplifino.nestor.transaction.control.jdbc
- **com.amplifino.nestor.transaction.datasources**: JTA Transactional JDBC connection pool
- **com.amplifino.nestor.transaction.provider**: implementation of OSGI JTA Transaction Service
- **com.amplifino.nestor.transaction.spi**: TransactionLog implementation for use in com.amplifino.nestor.transaction.provider
- **com.amplifino.nestor.transaction.test**: transaction test bundle
- **com.amplifino.nestor.useradmin**: implementation of OSGI UserAdmin Service with pluggable persistence provider
- **com.amplifino.nestor.useradmin.rest**: rest interface on OSGI UserAdmin Service with sample UI using Angular.js
- **com.amplifino.nestor.useradmin.spi.obelix**: persistence provider for nestor.useradmin using Obelix project
- **com.amplifino.nestor.useradmin.spi.rdbms**: persistence provider for nestor.useradmin using a relational database 
- **com.amplifino.nestor.useradmin.test**: test bundle for nestor.useradmin
- **com.amplifino.nestor.webconsole.security**: secures webconsole password with PBKDF2 hash
- **com.amplifino.pools**: generic object pool

Internal Bundle dependencies: ( dotted line means either bundle can be replaced with alternative implementation of the whiteboard or standard specification).

![](https://rawgit.com/Amplifino/nestor/master/nestor.svg)


