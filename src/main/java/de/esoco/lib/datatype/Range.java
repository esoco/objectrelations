//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.datatype;

import de.esoco.lib.collection.CollectionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.esoco.lib.datatype.Pair.t;

/**
 * A generic implementation for iterable ranges of comparable numeric values.
 * Ranges consist of a start and an end value with a certain step size to
 * increment or decrement values by during iteration. The current implementation
 * supports all standard number types (Integer, Long, Short, Byte, Float,
 * Double, BigInteger, BigDecimal) as well as {@link Character} values.
 *
 * <p>
 * Ranges are defined with a simple builder pattern: a new instance is created
 * with the static factory methods called <code>from(value)</code> with a start
 * value which also defines the range datatype. Before the range can be used an
 * end value must be defined by invoking either {@link #to(Comparable)} or
 * {@link #toBefore(Comparable)} for inclusive or exclusive end values,
 * respectively. The default step size is 1 (one) but a different step size can
 * be set through {@link #step(Comparable)}. Trying to use a range that has no
 * end value will cause a runtime exception to be thrown.
 * </p>
 *
 * <p>
 * To support the builder pattern ranges are "effectively" immutable.
 * Effectively means that the range end and step size can only be defined once
 * by calling the respective methods. Invoking these methods again causes an
 * exception to be thrown. There's one limitation: because the step size has a
 * default value of 1 a range on which no step size has been set explicitly
 * still has a mutable step size. Therefore, when defining range constants it is
 * advised to explicitly set the step size even if it is the default value of
 * one.
 * </p>
 *
 * <p>
 * Decimal or floating-point datatypes should be used cautiously, especially if
 * fractional steps are used because these may cause non-terminating divisions
 * or rounding errors. Especially binary floating-point types (float, double)
 * may behave unexpected because of the typical binary-to-decimal conversion
 * inaccuracies of these types.
 * </p>
 *
 * @author eso
 */
public class Range<T extends Comparable<T>> implements Iterable<T> {
	private static final long END_EXCLUSIVE = Long.MIN_VALUE + 1;

	private static final Map<Class<?>, Comparable<?>> DEFAULT_STEPS =
		CollectionUtil.fixedMapOf(t(Long.class, Long.valueOf(1)),
			t(Integer.class, Integer.valueOf(1)),
			t(Short.class, Short.valueOf((short) 1)),
			t(Byte.class, Byte.valueOf((byte) 1)),
			t(BigInteger.class, BigInteger.ONE),
			t(BigDecimal.class, BigDecimal.ONE),
			t(Double.class, Double.valueOf(1)),
			t(Float.class, Float.valueOf(1)),
			t(Character.class, Character.valueOf('\u0001')));

	private static final Map<Class<?>, Comparable<?>> ZERO_VALUES =
		CollectionUtil.fixedMapOf(t(Long.class, Long.valueOf(0)),
			t(Integer.class, Integer.valueOf(0)),
			t(Short.class, Short.valueOf((short) 0)),
			t(Byte.class, Byte.valueOf((byte) 0)),
			t(BigInteger.class, BigInteger.ZERO),
			t(BigDecimal.class, BigDecimal.ZERO),
			t(Double.class, Double.valueOf(0)),
			t(Float.class, Float.valueOf(0)),
			t(Character.class, Character.valueOf('\u0000')));

	private final T start;

	private final Function<RangeIterator, T> getNextValue;

	private T end = null;

	private T step = null;

	private long size = Long.MIN_VALUE;

	private boolean ascending;

	/**
	 * Internal constructor to creates a new instance. Use one of the factory
	 * methods like {@link #from(int)} to create new ranges.
	 *
	 * @param start        The starting value of this range (inclusive)
	 * @param getNextValue The function that generates the next value of this
	 *                     range
	 */
	private Range(T start, Function<RangeIterator, T> getNextValue) {
		Objects.requireNonNull(start, "Start value must not be NULL");

		this.start = start;
		this.getNextValue = getNextValue;
	}

	/**
	 * Creates a new range with a long datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Long> from(long start) {
		return new Range<Long>(start, i -> i.next + i.range().step);
	}

	/**
	 * Creates a new range with an integer datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Integer> from(int start) {
		return new Range<Integer>(start, i -> i.next + i.range().step);
	}

	/**
	 * Creates a new range with a float datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Float> from(float start) {
		return new Range<Float>(start, i -> i.next + i.range().step);
	}

	/**
	 * Creates a new range with a double datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Double> from(double start) {
		return new Range<Double>(start, i -> i.next + i.range().step);
	}

	/**
	 * Creates a new range with a short datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Short> from(short start) {
		return new Range<Short>(start, i -> (short) (i.next + i.range().step));
	}

	/**
	 * Creates a new range with a byte datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Byte> from(byte start) {
		return new Range<Byte>(start, i -> (byte) (i.next + i.range().step));
	}

	/**
	 * Creates a new range with a {@link BigInteger} datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<BigInteger> from(BigInteger start) {
		return new Range<BigInteger>(start, i -> i.next.add(i.range().step));
	}

	/**
	 * Creates a new range with a {@link BigDecimal} datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<BigDecimal> from(BigDecimal start) {
		return new Range<BigDecimal>(start, i -> i.next.add(i.range().step));
	}

	/**
	 * Creates a new range with a character datatype.
	 *
	 * @param start The start of the range
	 * @return The new range
	 */
	public static Range<Character> from(char start) {
		return new Range<Character>(start, i -> Character.valueOf(
			(char) (i.next +
				(i.range().ascending ? i.range().step : -i.range().step))));
	}

	/**
	 * Checks whether a certain value is contained in this range.
	 *
	 * @param value The value to check
	 * @return TRUE if the value is contained in this range
	 */
	public boolean contains(T value) {
		checkInitialized();

		return ascending ?
		       value.compareTo(start) >= 0 && value.compareTo(end) <= 0 :
		       value.compareTo(end) >= 0 && value.compareTo(start) <= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		checkInitialized();

		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Range<?> other = (Range<?>) obj;

		other.checkInitialized();

		return start.equals(other.start) && Objects.equals(end, other.end) &&
			Objects.equals(step, other.step);
	}

	/**
	 * Returns the last value of this range. If the range has been defined with
	 * {@link #toBefore(Comparable)} the value returned by this method will be
	 * last value before the exclusive end based on the step size.
	 *
	 * @return The last value
	 */
	public T getEnd() {
		checkInitialized();

		return end;
	}

	/**
	 * Returns the start value of this range.
	 *
	 * @return The first value
	 */
	public T getStart() {
		return start;
	}

	/**
	 * Returns the step size for iterating over this range. The sign of the
	 * returned value will reflect the iteration direction (positive if
	 * iterating from lower to higher values, negative for the the other way).
	 *
	 * @return The step size
	 */
	public T getStep() {
		checkInitialized();

		return step;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		checkInitialized();

		final int prime = 31;
		int hash = 1;

		hash = prime * hash + start.hashCode();
		hash = prime * hash + end.hashCode();
		hash = prime * hash + step.hashCode();

		return hash;
	}

	/**
	 * Checks the range direction.
	 *
	 * @return TRUE if the range values are ordered ascending, FALSE if ordered
	 * descending
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		checkInitialized();

		return new RangeIterator(start);
	}

	/**
	 * Returns the size of this range.
	 *
	 * @return The range size
	 */
	public long size() {
		checkInitialized();

		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Spliterator<T> spliterator() {
		return Spliterators.spliterator(iterator(), size(),
			Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL |
				Spliterator.ORDERED | Spliterator.SORTED | Spliterator.SIZED |
				Spliterator.SUBSIZED);
	}

	/**
	 * Sets the step size for iteration through this range. The step must
	 * always
	 * be a positive value. If the range end is lower than the start the step
	 * will be automatically applied by subtraction.
	 *
	 * <p>
	 * This method can only be invoked once and afterwards this range is
	 * effectively immutable. If no explicit value is given a default step size
	 * of 1 (one) will be used.
	 * </p>
	 *
	 * @param step The step size
	 * @return This instance for fluent invocations
	 * @throws IllegalArgumentException If the step value is invalid or has
	 *                                  already been set
	 */
	@SuppressWarnings("unchecked")
	public Range<T> step(T step) {
		Objects.requireNonNull(step, "Range step must not be NULL");

		if (this.step != null) {
			throw new IllegalArgumentException(
				"Range step already set to " + this.step);
		}

		if (step.compareTo((T) ZERO_VALUES.get(start.getClass())) <= 0) {
			throw new IllegalArgumentException(
				"Step must be a positive number");
		}

		this.step = step;

		return this;
	}

	/**
	 * Returns a stream of the values in this range.
	 *
	 * @return The stream of range values
	 */
	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Sets the inclusive end value of this range. This method can only be
	 * invoked once and afterwards this range is effectively immutable.
	 *
	 * @param end The end value (inclusive)
	 * @return This instance for fluent invocations
	 * @throws IllegalArgumentException If the end value has already been set
	 */
	public Range<T> to(T end) {
		Objects.requireNonNull(end, "End value must not be NULL");

		if (this.end != null) {
			throw new IllegalArgumentException(
				"Range end already set to " + this.end);
		}

		this.end = end;

		ascending = start.compareTo(end) <= 0;

		return this;
	}

	/**
	 * Sets the exclusive end value of this range. This method can only be
	 * invoked once and afterwards this range is effectively immutable. The
	 * value returned by {@link #getEnd()} will be the argument of this method
	 * incremented or decremented by the range step depending on the range
	 * direction.
	 *
	 * @param value The end value (exclusive)
	 * @return This instance for fluent invocations
	 * @throws IllegalArgumentException If the end value has already been set
	 */
	public Range<T> toBefore(T value) {
		Range<T> withEnd = to(value);

		// internal signal for an exclusive end value
		size = END_EXCLUSIVE;

		return withEnd;
	}

	/**
	 * Returns a list that contains all elements of this range.
	 *
	 * @return A new list containing the range elements
	 */
	public List<T> toList() {
		return stream().collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		checkInitialized();

		StringBuilder result = new StringBuilder(start.toString());

		result.append("..");
		result.append(end);

		if (step != DEFAULT_STEPS.get(start.getClass())) {
			result.append(" step ").append(step);
		}

		return result.toString();
	}

	/**
	 * Internal method to calculate the size of this range.
	 */
	private void calcSize() {
		if (end != null && step != null && size <= END_EXCLUSIVE) {
			if (!ascending) {
				step = negate(step);
			}

			if (size == END_EXCLUSIVE) {
				// calculate inclusive end
				ascending = !ascending;
				step = negate(step);
				end = getNextValue.apply(new RangeIterator(end));
				step = negate(step);
				ascending = !ascending;
			}

			@SuppressWarnings("unchecked")
			Class<T> rangeType = (Class<T>) end.getClass();

			if (rangeType == BigDecimal.class) {
				size = ((BigDecimal) end)
					.subtract((BigDecimal) start)
					.add((BigDecimal) step)
					.divide((BigDecimal) step)
					.longValue();
			} else if (rangeType == BigInteger.class) {
				size = ((BigInteger) end)
					.subtract((BigInteger) start)
					.add((BigInteger) step)
					.divide((BigInteger) step)
					.longValue();
			} else if (Number.class.isAssignableFrom(rangeType)) {
				if (rangeType == Double.class || rangeType == Float.class) {
					double stepDouble = ((Number) step).doubleValue();

					size = (long) ((((Number) end).doubleValue() -
						((Number) start).doubleValue() + stepDouble) /
						stepDouble);
				} else {
					long stepLong = ((Number) step).longValue();

					size = (((Number) end).longValue() -
						((Number) start).longValue() + stepLong) / stepLong;
				}
			} else if (rangeType == Character.class) {
				int stepChar = ((Character) step).charValue();

				if (!ascending) {
					stepChar = -stepChar;
				}

				size = (((Character) end).charValue() -
					((Character) start).charValue() + stepChar) / stepChar;
			}
		}
	}

	/**
	 * Checks whether this instance has been fully initialized or else
	 * throws an
	 * {@link IllegalStateException}.
	 */
	@SuppressWarnings("unchecked")
	private void checkInitialized() {
		if (end == null) {
			throw new IllegalStateException("Range end has not been set");
		}

		if (step == null) {
			step((T) DEFAULT_STEPS.get(start.getClass()));

			if (step == null) {
				throw new IllegalArgumentException(
					"No range mapping for type " + start.getClass());
			}
		}

		calcSize();
	}

	/**
	 * Returns the negated value of the argument.
	 *
	 * @param value The value to negate
	 * @return The negated value
	 */
	@SuppressWarnings("unchecked")
	private T negate(T value) {
		Class<T> valueType = (Class<T>) value.getClass();

		if (valueType == Integer.class) {
			value = (T) Integer.valueOf(-((Integer) value).intValue());
		} else if (valueType == Long.class) {
			value = (T) Long.valueOf(-((Long) value).longValue());
		} else if (valueType == Short.class) {
			value = (T) Short.valueOf((short) -((Short) value).shortValue());
		} else if (valueType == Byte.class) {
			value = (T) Byte.valueOf((byte) -((Byte) value).byteValue());
		} else if (valueType == BigDecimal.class) {
			value = (T) ((BigDecimal) value).negate();
		} else if (valueType == BigInteger.class) {
			value = (T) ((BigInteger) value).negate();
		} else if (valueType == Double.class) {
			value = (T) Double.valueOf(-((Double) value).doubleValue());
		} else if (valueType == Float.class) {
			value = (T) Float.valueOf(-((Float) value).floatValue());
		} else if (valueType == Character.class) {
			// characters are unsigned, therefore this must be handled by
			// the next value function based on the upwards flag
		}

		return value;
	}

	/**
	 * The iterator implementation for ranges.
	 *
	 * @author eso
	 */
	class RangeIterator implements Iterator<T> {
		private T next;

		/**
		 * Creates a new instance.
		 *
		 * @param startValue The value to start the iteration at
		 */
		RangeIterator(T startValue) {
			next = startValue;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return next != null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T next() {
			T current = next;

			next = getNextValue.apply(this);

			int nextCompared = next.compareTo(end);

			if (ascending && nextCompared > 0 ||
				!ascending && nextCompared < 0) {
				next = null;
			}

			return current;
		}

		/**
		 * Returns the range of this instance.
		 *
		 * @return The iterated range
		 */
		public final Range<T> range() {
			return Range.this;
		}
	}
}
