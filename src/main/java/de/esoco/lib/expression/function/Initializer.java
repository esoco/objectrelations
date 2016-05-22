package de.esoco.lib.expression.function;

/********************************************************************
 * An interface for the initialization of objects.
 *
 * @author eso
 */
public interface Initializer<T>
{
	//~ Methods ------------------------------------------------------------

	/***************************************
	 * Initializes the given object.
	 *
	 * @param rInitObject The object to initialize
	 */
	public void init(T rInitObject);
}