//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A function that creates a cryptographic hash value from it's input byte
 * array.
 *
 * @author eso
 */
public class CreateHash extends AbstractFunction<byte[], byte[]> {

	private final MessageDigest digest;

	private final String salt;

	private final int rounds;

	/**
	 * Creates a new instance that uses a certain hash algorithm. The given
	 * algorithm name must be a valid input value for the method
	 * {@link MessageDigest#getInstance(String)}.
	 *
	 * @param algorithm The name of the hash algorithm
	 * @param salt      A salt to append to input data or NULL for none
	 * @param rounds    The number of rounds to apply the hash algorithm to the
	 *                  input
	 * @throws NoSuchAlgorithmException If the given algorithm is not available
	 */
	public CreateHash(String algorithm, String salt, int rounds)
		throws NoSuchAlgorithmException {
		super(String.format(CreateHash.class.getSimpleName() + "[%s]",
			algorithm));

		digest = MessageDigest.getInstance(algorithm);
		this.salt = salt;
		this.rounds = rounds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] evaluate(byte[] input) {
		byte[] result = input;

		digest.reset();

		if (salt != null) {
			digest.update(salt.getBytes());
		}

		for (int i = 0; i < rounds; i++) {
			result = digest.digest(result);
		}

		return result;
	}

	/**
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		CreateHash otherHash = (CreateHash) other;

		return rounds == otherHash.rounds && salt.equals(otherHash.salt) &&
			digest.getAlgorithm().equals(otherHash.digest.getAlgorithm());
	}

	/**
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		return rounds +
			(37 * salt.hashCode() + (37 * digest.getAlgorithm().hashCode()));
	}
}
