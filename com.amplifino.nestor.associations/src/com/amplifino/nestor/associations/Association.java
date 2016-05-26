package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.Function;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Association is an immutable key value pair mainly intended to manage 
 * the relation between a foreign key and its realization as an object 
 *  
 *  Implementation note:
 *  Due to the lazy nature of some Association implementations,
 *  implementations generally do not implement equals or hashCode in a meaningful way.
 *  
 *  Unless specified, Association instances are not thread safe
 *  
 **/

@ProviderType
public interface Association<K,V> {

	/**
	 * 
	 * @return the key
	 */
	K key();
	
	/**
	 * @return the value
	 */
	V value();
	
	/**
	 * returns an association between the arguments
	 * @param key
	 * @param value
	 * @return
	 */
	static <K,V> Association<K,V> of(K key, V value) {
		return new SimpleAssociation<>(Objects.requireNonNull(key), Objects.requireNonNull(value));
	}
	
	/**
	 * returns a lazy association 
	 * @param key
	 * @param fetcher produces the initial value
	 * @return
	 */
	static <K,V> Association<K,V> of(K key, Function<K,V> fetcher) {
		return new LazyAssociation<>(Objects.requireNonNull(key), Objects.requireNonNull(fetcher));
	}
}
