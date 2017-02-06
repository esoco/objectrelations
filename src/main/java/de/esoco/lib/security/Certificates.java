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
package de.esoco.lib.security;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.type.StandardTypes;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import static de.esoco.lib.security.SecurityRelationTypes.CERTIFICATE_VALIDITY;
import static de.esoco.lib.security.SecurityRelationTypes.COMMON_NAME;
import static de.esoco.lib.security.SecurityRelationTypes.COUNTRY;
import static de.esoco.lib.security.SecurityRelationTypes.KEY_PASSWORD;
import static de.esoco.lib.security.SecurityRelationTypes.KEY_SIZE;
import static de.esoco.lib.security.SecurityRelationTypes.LOCALITY;
import static de.esoco.lib.security.SecurityRelationTypes.ORGANIZATION;
import static de.esoco.lib.security.SecurityRelationTypes.ORGANIZATION_UNIT;
import static de.esoco.lib.security.SecurityRelationTypes.STATE_PROVINCE_REGION;

import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.START_DATE;


/********************************************************************
 * Contains static helper methods for the handling of cryptographic
 * certificates. Relies on relation types defined in {@link
 * SecurityRelationTypes}.
 *
 * @author eso
 */
public class Certificates
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Certificates()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Generates a self-signed certificate with the given parameters and returns
	 * a Java security key store containing the certificate and it's private
	 * key. The following parameter must be provided or else an exception will
	 * be thrown:
	 *
	 * <ul>
	 *   <li>{@link StandardTypes#NAME}: The alias name under which to store the
	 *     certificate in the key store.</li>
	 *   <li>{@link SecurityRelationTypes#COMMON_NAME}: the common name of the
	 *     certificate.</li>
	 *   <li>{@link SecurityRelationTypes#KEY_SIZE}: the size of the private key
	 *     the password is generated with.</li>
	 *   <li>{@link SecurityRelationTypes#KEY_PASSWORD}: the password to protect
	 *     the private key with.</li>
	 *   <li>{@link SecurityRelationTypes#CERTIFICATE_VALIDITY}: the validity of
	 *     the certificate in days.</li>
	 * </ul>
	 *
	 * <p>The following parameter are optional and will default to standard
	 * values (typically an empty string if not stated otherwise):</p>
	 *
	 * <ul>
	 *   <li>{@link StandardTypes#START_DATE}: the date from which the
	 *     certificate will be valid (defaults to the time of invocation).</li>
	 *   <li>{@link SecurityRelationTypes#ORGANIZATION}</li>
	 *   <li>{@link SecurityRelationTypes#ORGANIZATION_UNIT}</li>
	 *   <li>{@link SecurityRelationTypes#LOCALITY}</li>
	 *   <li>{@link SecurityRelationTypes#STATE_PROVINCE_REGION}</li>
	 *   <li>{@link SecurityRelationTypes#COUNTRY}</li>
	 * </ul>
	 *
	 * @param  rParams The parameters to create the certificate with
	 *
	 * @return A new key store with the certificate and it's key
	 *
	 * @throws IllegalArgumentException If a required parameter is missing in
	 *                                  the parameters argument
	 * @throws IllegalStateException    If the certificate generation fails
	 *                                  because of unavailable algorithms
	 */
	@SuppressWarnings("boxing")
	public static KeyStore generateSelfSignedCertificate(Relatable rParams)
	{
		ObjectRelations.require(rParams,
								r -> r.getTarget() != null,
								NAME,
								COMMON_NAME,
								KEY_SIZE,
								KEY_PASSWORD,
								CERTIFICATE_VALIDITY);

		try
		{
			CertAndKeyGen aCertAndKeyGen =
				new CertAndKeyGen("RSA", "SHA1WithRSA", null);

			X500Name aX500Name =
				new X500Name(rParams.get(COMMON_NAME),
							 rParams.get(ORGANIZATION_UNIT),
							 rParams.get(ORGANIZATION),
							 rParams.get(LOCALITY),
							 rParams.get(STATE_PROVINCE_REGION),
							 rParams.get(COUNTRY));

			aCertAndKeyGen.generate(rParams.get(KEY_SIZE));

			PrivateKey aPrivateKey = aCertAndKeyGen.getPrivateKey();

			int nValiditySeconds =
				rParams.get(CERTIFICATE_VALIDITY) * 24 * 60 * 60;

			Date rStartDate = rParams.get(START_DATE);

			if (rStartDate == null)
			{
				rStartDate = new Date();
			}

			X509Certificate aCertificate =
				aCertAndKeyGen.getSelfCertificate(aX500Name,
												  rStartDate,
												  nValiditySeconds);

			X509Certificate[] aCertChain = new X509Certificate[1];

			aCertChain[0] = aCertificate;

			KeyStore aKeyStore = KeyStore.getInstance("JKS");

			aKeyStore.load(null, null);

			aKeyStore.setKeyEntry(rParams.get(NAME),
								  aPrivateKey,
								  rParams.get(KEY_PASSWORD).toCharArray(),
								  aCertChain);

			return aKeyStore;
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}
	}

	/***************************************
	 * Initializes an SSL context from a key store so that it will use the
	 * certificates from the key store for SSL/TLS connections.
	 *
	 * @param  rKeyStore         The key store to initialize the context from
	 * @param  sKeyStorePassword The key store password
	 *
	 * @return A new SSL context initialized to use the given key store
	 *
	 * @throws IllegalArgumentException If the given key store or password are
	 *                                  invalid
	 * @throws IllegalStateException    If the necessary security algorithms are
	 *                                  not available
	 */
	public static SSLContext getSslContext(
		KeyStore rKeyStore,
		String   sKeyStorePassword)
	{
		try
		{
			KeyManagerFactory aKeyManagerFactory =
				KeyManagerFactory.getInstance("SunX509");

			TrustManagerFactory aTrustManagerFactory =
				TrustManagerFactory.getInstance("SunX509");

			aKeyManagerFactory.init(rKeyStore, sKeyStorePassword.toCharArray());
			aTrustManagerFactory.init(rKeyStore);

			SSLContext     aSslContext    = SSLContext.getInstance("TLS");
			TrustManager[] rTrustManagers =
				aTrustManagerFactory.getTrustManagers();

			aSslContext.init(aKeyManagerFactory.getKeyManagers(),
							 rTrustManagers,
							 null);

			return aSslContext;
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException(e);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}
