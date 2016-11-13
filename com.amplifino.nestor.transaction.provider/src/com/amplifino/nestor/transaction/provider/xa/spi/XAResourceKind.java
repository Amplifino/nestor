package com.amplifino.nestor.transaction.provider.xa.spi;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * An optional interface for XAResource 
 * in order to influence the order in with XAResource's are prepared and committed
 *
 */
@ConsumerType
public interface XAResourceKind {

	Kind kind();
	
	/**
	 * Kind of XAResource
	 * The transaction manager will prepare and commit XAResources in sequence of ascending Kind ordinal's.
	 */
	enum Kind {		
		
		/**
		 * A fully compliant XA Resource capable of two phase commit.
		 * Default if the XAResource does not implement XAResourceKind 
		 */
		COMPLIANT,
		
		/**
		 * A fully compliant XA Resource, but preferably last partipant in the two phase commit process
		 * Useful e.g. for JMS resources to avoid a race condition after a 2PC commit.
		 * If a JMS resource is committed first, a message consumer may want to enrich the message by querying
		 * the database. When using a multi-version db like Oracle or Postgresql the message consumer may not see
		 * the db changes because the database commit is still executing, causing a failure in the enrichment process.
		 * 
		 */
		PREFER_LAST,
		
		/**
		 * This resource should be prepared last, often because it does not support 2PC.
		 * In this case the resource should commit on prepare.
		 * Use if one wants to add multiple non XA resource to an XA transaction.
		 * The resources will be committed (actually prepared) between the prepare and the commit phase,  
		 */
		LAST,
		
		/**
		 * This resource must be prepared last. If there is more than one resource with this kind the transaction will rollback.
		 * Typically this resource will commit on prepare.
		 * Use to implement the last resource gambit. 
		 * Not fully ACID, as the transaction manager may fail between preparing (actually committing),
		 * the last resource and recording its commit decision.
		 *   
		 */
		EXCLUSIVE_LAST,
		
		/**
		 * This resource only supports one phase commit, and will throw an exception on prepare.
		 * It can only participate in a 2PC XA transaction if all other resources respond with XA_RDONLY to prepare. 
		 */
		ONEPHASE;
	}
}
