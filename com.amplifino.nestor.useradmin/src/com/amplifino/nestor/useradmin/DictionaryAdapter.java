package com.amplifino.nestor.useradmin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

abstract class DictionaryAdapter extends Dictionary<String, Object> {

	abstract Map<String, ?> map();
	
	@Override
	public final int size() {		
		return map().size();
	}

	@Override
	public final boolean isEmpty() {
		return map().isEmpty();
	}

	@Override
	public final Enumeration<String> keys() {
		// copy keySet to avoid concurrentmodification errors
		return new IteratorAdapter<>(new ArrayList<>(map().keySet()).iterator());
	}

	@Override
	public final Enumeration<Object> elements() {
		// copy values to avoid concurrentmodification errors
		return new IteratorAdapter<Object>(new ArrayList<Object>(map().values()).iterator());
	}

	@Override
	public final Object get(Object key) {
		return map().get(key);
	}
	
	@Override
	public String toString() {
		return map().toString();
	}
	
	private static class IteratorAdapter<T> implements Enumeration<T> {
		private final Iterator<T> iterator;
		
		IteratorAdapter(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		@Override
		public T nextElement() {
			return iterator.next();
		}
		
		
	}

}
