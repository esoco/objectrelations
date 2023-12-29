package de.esoco.lib.collection;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * An implementation of an immutable collection that allows subclassing. All
 * modifying methods of the Collection interface will throw an {@link
 * UnsupportedOperationException} if they are accessed.
 *
 * @author eso
 */
public class ImmutableCollection<E> extends AbstractCollection<E> {

	private final Collection<E> wrappedCollection;

	/**
	 * Creates a new instance.
	 *
	 * @param wrappedCollection The collection to read the data from
	 */
	public ImmutableCollection(Collection<E> wrappedCollection) {
		this.wrappedCollection = wrappedCollection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private final Iterator<E> wrappedIterator =
				wrappedCollection.iterator();

			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public E next() {
				return wrappedIterator.next();
			}
		};
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return wrappedCollection.size();
	}
}