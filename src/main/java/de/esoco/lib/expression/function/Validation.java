//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.function.Validation.ValidationResult;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A functional interface that produces instances of {@link ValidationResult}
 * upon the validation of input values.
 *
 * @author eso
 */
@FunctionalInterface
public interface Validation<T> extends Function<T, ValidationResult> {

	/**
	 * Returns a new validation that checks a value with the given predicate
	 * and
	 * returns the corresponding {@link ValidationResult}.
	 *
	 * @param isValid        The predicate that checks a value for validity
	 * @param invalidMessage The message to be displayed if the validation
	 *                       fails
	 * @return A new validation instance
	 */
	static <T> Validation<T> ensure(Predicate<T> isValid,
		String invalidMessage) {
		return v -> isValid.test(v) ?
		            ValidationResult.valid() :
		            ValidationResult.invalid(invalidMessage);
	}

	/**
	 * Default implementation that invokes {@link #validate(Object)}.
	 *
	 * @see Function#apply(Object)
	 */
	@Override
	default ValidationResult apply(T value) {
		return validate(value);
	}

	/**
	 * Validates the input value and returns a corresponding {@link
	 * ValidationResult}.
	 *
	 * @param value The value to validate
	 * @return The validation result
	 */
	ValidationResult validate(T value);

	/**
	 * A class that represents the result of some validation. If the validation
	 * has failed the method {@link #isValid()} will return FALSE and the
	 * method
	 * {@link #getMessage()} will return the corresponding error message.
	 *
	 * @author eso
	 */
	class ValidationResult {

		private final String message;

		/**
		 * Creates a new instance.
		 *
		 * @param message The error message
		 */
		protected ValidationResult(String message) {
			this.message = message;
		}

		/**
		 * Returns a valid result with a certain error message.
		 *
		 * @param message The error message
		 * @return A validation result that is invalid
		 */
		public static ValidationResult invalid(String message) {
			Objects.requireNonNull(message);

			return new ValidationResult(message);
		}

		/**
		 * Returns a valid result.
		 *
		 * @return A validation result that is valid
		 */
		public static ValidationResult valid() {
			return new ValidationResult(null);
		}

		/**
		 * Returns the message is the result is invalid (see {@link
		 * #isValid()}).
		 *
		 * @return The message or NULL if the validation was successful
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * Returns the validation result.
		 *
		 * @return TRUE if the validation was successful, FALSE if it failed
		 */
		public boolean isValid() {
			return message == null;
		}
	}
}
