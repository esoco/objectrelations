package de.esoco.lib.collection;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/********************************************************************
 * An implementation of an immutable collection that allows subclassing. All
 * modifying methods of the Collection interface will throw an {@link
 * UnsupportedOperationException} if they are accessed.
 *
 * @author eso
 */
public class ImmutableCollection<E> extends AbstractCollection<E>
{
	//~ Instance fields ----------------------------------------------------

	private final Collection<E> rWrappedCollection;

	//~ Constructors -------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rWrappedCollection The collection to read the data from
	 */
	public ImmutableCollection(Collection<E> rWrappedCollection)
	{
		this.rWrappedCollection = rWrappedCollection;
	}

	//~ Methods ------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			private final Iterator<E> aWrappedIterator =
				rWrappedCollection.iterator();

			@Override
			public boolean hasNext()
			{
				return aWrappedIterator.hasNext();
			}

			@Override
			public E next()
			{
				return aWrappedIterator.next();
			}
		};
	}

	/***************************************
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size()
	{
		return rWrappedCollection.size();
	}
}