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

/**
 * A binary function that casts an input value to another datatype.
 *
 * @author eso
 */
public class Cast<I, O> extends AbstractBinaryFunction<I, Class<O>, O> {

	/**
	 * Creates a new instance.
	 *
	 * @param castType The class of the datatype to cast to
	 */
	public Cast(Class<O> castType) {
		super(castType, Cast.class.getSimpleName());
	}

	/**
	 * Casts the target object to a certain class.
	 *
	 * @param target The target object
	 * @param type   The class to cast the target to
	 * @return The target object, casted to the class
	 */
	@Override
	public O evaluate(I target, Class<O> type) {
		return type.cast(target);
	}
}
