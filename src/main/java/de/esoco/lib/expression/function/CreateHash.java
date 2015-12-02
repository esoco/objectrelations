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


/********************************************************************
 * A function that creates a cryptographic hash value from it's input byte
 * array.
 *
 * @author eso
 */
public class CreateHash extends AbstractFunction<byte[], byte[]>
{
	//~ Instance fields --------------------------------------------------------

	private final MessageDigest aDigest;
	private final String	    sSalt;
	private final int		    nRounds;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that uses a certain hash algorithm. The given
	 * algorithm name must be a valid input value for the method {@link
	 * MessageDigest#getInstance(String)}.
	 *
	 * @param  sAlgorithm The name of the hash algorithm
	 * @param  sSalt      A salt to append to input data or NULL for none
	 * @param  nRounds    The number of rounds to apply the hash algorithm to
	 *                    the input
	 *
	 * @throws NoSuchAlgorithmException If the given algorithm is not available
	 */
	public CreateHash(String sAlgorithm, String sSalt, int nRounds)
		throws NoSuchAlgorithmException
	{
		super(String.format(CreateHash.class.getSimpleName() + "[%s]",
							sAlgorithm));

		aDigest		 = MessageDigest.getInstance(sAlgorithm);
		this.sSalt   = sSalt;
		this.nRounds = nRounds;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public byte[] evaluate(byte[] rInput)
	{
		byte[] aResult = rInput;

		aDigest.reset();

		if (sSalt != null)
		{
			aDigest.update(sSalt.getBytes());
		}

		for (int i = 0; i < nRounds; i++)
		{
			aResult = aDigest.digest(aResult);
		}

		return aResult;
	}

	/***************************************
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		CreateHash rOtherHash = (CreateHash) rOther;

		return nRounds == rOtherHash.nRounds &&
			   sSalt.equals(rOtherHash.sSalt) &&
			   aDigest.getAlgorithm().equals(rOtherHash.aDigest.getAlgorithm());
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return nRounds +
			   (37 * sSalt.hashCode() +
				(37 * aDigest.getAlgorithm().hashCode()));
	}
}
