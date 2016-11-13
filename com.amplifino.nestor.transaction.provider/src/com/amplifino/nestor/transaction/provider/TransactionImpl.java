package com.amplifino.nestor.transaction.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.transaction.HeuristicMixedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.amplifino.nestor.transaction.provider.spi.AbortException;
import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog;
import com.amplifino.nestor.transaction.provider.xa.spi.XAResourceKind;

class TransactionImpl implements Transaction {
	
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.transaction.provider");
	private final TransactionLog log;
	private final GlobalTransaction globalTransaction;
	private int lastBranch = 0;
	private int status;
	private List<TransactionBranch> branches = new ArrayList<>(); 
	private final List<Synchronization> synchronizers = new ArrayList<>();
	private final List<Synchronization> interposedSynchronizers = new ArrayList<>();
	private final Map<Object, Object> resources = new HashMap<>();
	
	TransactionImpl(TransactionLog log) {
		this.log = log;
		globalTransaction = GlobalTransaction.random();
		status = Status.STATUS_ACTIVE;
	}
	
	@Override 
	public void commit() throws RollbackException, HeuristicMixedException {
		checkActiveOrMarked();
		if (status == Status.STATUS_MARKED_ROLLBACK) {
			rollbackAndThrow("Transaction was marked for rollback");
		}
		CompositeException<?> beforeExceptions = beforeCompletion();
		beforeExceptions.exceptions().forEach(this::report);
		if (!beforeExceptions.isEmpty()) {
			rollbackAndThrow("Exception in before completion" , beforeExceptions);
		}
		try {
			doCommit();
		} finally {
			afterCompletion();
		}
	}
	
	private void doCommit() throws RollbackException, HeuristicMixedException {
		switch (branches.size()) {
			case 0:
				status = Status.STATUS_COMMITTED;
				return;
			case 1:
				onePhaseCommit();
				return;
			default:
				twoPhaseCommit();
		}
	}
	
	private void onePhaseCommit() throws RollbackException {
		try {
			status = Status.STATUS_COMMITTING;
			branches.get(0).commitOnePhase();
			status = Status.STATUS_COMMITTED;
		} catch (Throwable e) {
			report(e);
			status = Status.STATUS_ROLLEDBACK;				
			throw (RollbackException) new RollbackException(e.toString()).initCause(e);
		}
	}
	
	private void twoPhaseCommit() throws RollbackException, HeuristicMixedException {
		sortBranches();
		if (xaResourceKind(branches.get(branches.size() - 2)).compareTo(XAResourceKind.Kind.EXCLUSIVE_LAST) >= 0) {
			rollback();
			throw new RollbackException("Too many non compliant resources");
		}
		try {
			branches = phaseOne(); 
			if (branches.isEmpty()) {
				log.commitComplete(globalTransaction);
				return;
			} else {
				log.committing(globalTransaction, xids());
			}
		} catch (AbortException e) {
			throw (HeuristicMixedException) new HeuristicMixedException("Failure in transaction log").initCause(e);
		} catch (Throwable e) {
			twoPhaseRollback("Exception in phase one: " + e, e);			
		}
		phaseTwo();
	}
	
	private List<TransactionBranch> phaseOne() throws XAException {
		List<TransactionBranch> phase2Branches = new ArrayList<>();
		status = Status.STATUS_PREPARING;
		log.preparing(globalTransaction, xids());
		for (int i = 0 ; i < branches.size() - 1; i++) {
			TransactionBranch branch = branches.get(i);
			branch.prepare(log);
			if (!branch.isReadOnly()) {
				phase2Branches.add(branch);
			}
		}
		TransactionBranch lastBranch = branches.get(branches.size() - 1);
		if (phase2Branches.isEmpty()) {
			lastBranch.commitOnePhase();
			status = Status.STATUS_COMMITTED;
			return Collections.emptyList();
		} else {
			lastBranch.prepare(log);
			if (!lastBranch.isReadOnly()) {
				phase2Branches.add(lastBranch);
			}
		}
		if (phase2Branches.size() == 1) {
			phase2Branches.get(0).commitTwoPhase(log);
			status = Status.STATUS_COMMITTED;
			return Collections.emptyList();
		} 
		status = Status.STATUS_PREPARED;
		return phase2Branches;
	}
	
	private void phaseTwo() throws HeuristicMixedException {
		status = Status.STATUS_COMMITTING;
		CompositeException<TransactionBranch> exceptions = commit(branches);
		exceptions.exceptions().forEach(this::report);
		status = Status.STATUS_COMMITTED;
		if (exceptions.isEmpty()) {
			log.commitComplete(globalTransaction);
		} else {
			log.commitInComplete(globalTransaction, exceptions.failed().map(Map.Entry::getKey).map(TransactionBranch::xid));
		}
	}
	
	private void twoPhaseRollback(String message, Throwable cause) throws RollbackException {
		report(cause);
		log.rollingback(globalTransaction, xids());
		CompositeException<TransactionBranch>  exceptions = rollback(branches);
		exceptions.exceptions().forEach(this::report);
		if (exceptions.isEmpty()) {
			log.rollbackComplete(globalTransaction);
		} else {
			log.rollbackInComplete(globalTransaction, exceptions.failed().map(Map.Entry::getKey).map(TransactionBranch::xid));
		}
		throw (RollbackException) new RollbackException(message).initCause(cause);
	}
		
	private Stream<Xid> xids() {
		return branches.stream().map(TransactionBranch::xid);
	}
	
	private CompositeException<TransactionBranch> commit(List<TransactionBranch> branches) {
		return branches.stream()
			.collect(() -> CompositeException.of(branch -> branch.commitTwoPhase(log)), CompositeException::add, CompositeException::addAll);
	}

	
	@Override
	public boolean delistResource(XAResource resource, int flags) throws SystemException {
		checkActiveOrMarked();
		Optional<TransactionBranch> branch = branch(resource);
		if (!branch.isPresent()) {
			return false;
		}
		try {
			return branch.get().end(resource, flags);
		} catch (XAException e) {
			throw (SystemException) new SystemException(e.toString()).initCause(e);
		}		
	}
	
	private Optional<TransactionBranch> branch(XAResource resource) {
		return branches.stream()
			.filter(branch -> branch.resource(resource).isPresent())
			.findAny();
	}
	
	@Override
	public boolean enlistResource(XAResource resource) throws RollbackException, IllegalStateException, SystemException {
		checkActive();		
		try {
			for (TransactionBranch branch : branches) {
				if (branch.adopt(resource)) {
					return true;
				}
			}
			TransactionBranch branch = new TransactionBranch(resource, branch());
			branch.start();
			branches.add(branch);
			return true;
		} catch (XAException e) {
			throw (SystemException) new SystemException(e.getMessage()).initCause(e);
		}			
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void registerSynchronization(Synchronization synchronizer) throws RollbackException, IllegalStateException, SystemException {
		checkActive();		
		synchronizers.add(synchronizer);
	}

	void registerInterposedSynchronization(Synchronization synchronizer) {
		interposedSynchronizers.add(synchronizer);
	}
	
	private void rollbackAndThrow(String message) throws RollbackException {
		rollback();
		throw new RollbackException(message);
	}
	
	private void rollbackAndThrow(String message, CompositeException<?> exceptions) throws RollbackException {
		rollback();
		exceptions.ifNotEmptyThrow(() -> new RollbackException(message));
		throw new RollbackException(message);
	}
	
	@Override
	public void rollback()  {
		checkActiveOrMarked();
		rollback(branches);
		afterCompletion();
	}

	private CompositeException<TransactionBranch> rollback(List<TransactionBranch> branches) {
		status = Status.STATUS_ROLLING_BACK;
		CompositeException<TransactionBranch> result = branches.stream()
			.collect(() -> CompositeException.of(branch -> branch.rollback(log)), CompositeException::add, CompositeException::addAll);
		status = Status.STATUS_ROLLEDBACK;
		return result;
	}
	
	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		checkActiveOrMarked();
		status = Status.STATUS_MARKED_ROLLBACK;		
	}

	private Xid branch() {
		lastBranch++;
		return new XidImpl(log.getFormatId(), globalTransaction, ("" + lastBranch).getBytes());
	}
	
	private CompositeException<Synchronization> beforeCompletion() {
		return Stream.concat(synchronizers.stream(), interposedSynchronizers.stream())
			.collect(() -> CompositeException.of(Synchronization::beforeCompletion), CompositeException::add, CompositeException::addAll);
	}
	
	private void afterCompletion() {		
		Stream.concat(interposedSynchronizers.stream(), synchronizers.stream())
			.collect(() -> CompositeException.<Synchronization>of(s -> s.afterCompletion(status)), CompositeException::add, CompositeException::addAll)
			.exceptions()
			.forEach(this::report);
		status = Status.STATUS_NO_TRANSACTION;
	}
	
	private Throwable report(Throwable e) {
		logger.log(Level.SEVERE, "Exception in state " + status + ": " + e.getMessage() , e);
		return e;
	}
	
	Object getResource(Object key) {
		return resources.get(key);
	}
	
	void putResource(Object key, Object value) {
		resources.put(key, value);
	}
	
	private void checkActive() throws RollbackException  {
		if (status == Status.STATUS_MARKED_ROLLBACK) {
			throw new RollbackException("Transaction marked for rollback");
		}
		if (status != Status.STATUS_ACTIVE) {
			throw new IllegalStateException("Transaction not active, but in state: " + status);
		}
	}
	
	private void checkActiveOrMarked() {
		if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
			throw new IllegalStateException("Transaction not active, but in state: " + status);
		}
	}
	
	private void sortBranches() {
		branches.sort(Comparator.comparing(this::xaResourceKind));
	}
	
	private XAResourceKind.Kind xaResourceKind(TransactionBranch branch) {
		return Optional.of(branch.resource())
			.filter(XAResourceKind.class::isInstance)
			.map(XAResourceKind.class::cast)
			.map(XAResourceKind::kind)
			.orElse(XAResourceKind.Kind.COMPLIANT);		
	}
}
