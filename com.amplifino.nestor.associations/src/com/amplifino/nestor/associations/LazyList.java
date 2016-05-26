package com.amplifino.nestor.associations;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * List that postpones realization until first access
 * LazyList is not thread safe
 */
public final class LazyList<T> implements List<T> {
	
	private List<T> list;
	private final Supplier<List<T>> supplier; 
	
	private LazyList(Supplier<List<T>> supplier) {
		this.supplier = supplier;
	}
	
	/**
	 * creates a lazy list with the given list suplier
	 * @param supplier
	 * @return 
	 */
	public static <T> List<T> supplier(Supplier<List<T>> supplier) {
		return new LazyList<>(supplier);
	}

	private List<T> list() {
		if (list == null) {
			list = Objects.requireNonNull(supplier.get());
		}
		return list;
	}
	
	@Override
	public int size() {
		return list().size();
	}

	@Override
	public boolean isEmpty() {
		return list().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list().contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return list().iterator();
	}

	@Override
	public Object[] toArray() {
		return list().toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		return list().toArray(a);
	}

	@Override
	public boolean add(T e) {
		return list().add(e);
	}

	@Override
	public boolean remove(Object o) {
		return list().remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return list().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return list().addAll(index,c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list().retainAll(c);
	}

	@Override
	public void clear() {
		list().clear();
	}

	@Override
	public T get(int index) {
		return list().get(index);
	}

	@Override
	public T set(int index, T element) {
		return  list().set(index, element);
	}

	@Override
	public void add(int index, T element) {
		list().add(index, element);	
	}

	@Override
	public T remove(int index) {
		return list().remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return list().listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list().subList(fromIndex, toIndex);
	}
	
	@Override
	public Spliterator<T> spliterator() {
		return list().spliterator();
	}

	@Override
	public Stream<T> stream() {
        return list().stream();
    }
	
	@Override
	public Stream<T> parallelStream() {
		return list().parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super T> action) {
        list().forEach(action);
    }
	
	@Override
	public boolean equals(Object other) {
		if( other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other instanceof List) {
			return list().equals(other);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return list().hashCode();
	}
	
	@Override
	public String toString() {
		return list().toString();		
	}
}
