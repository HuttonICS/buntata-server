/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jhi.buntata.server.auth;

import org.restlet.security.*;

/**
 * @author Sebastian Raubach
 */
public class CustomVerifier extends SecretVerifier
{
	private static String masterUsername;
	private static String masterPassword;

	@Override
	public int verify(String s, char[] chars)
	{
		boolean canAccess = true;

		if (masterUsername == null || masterPassword == null || "".equals(masterUsername) || "".equals(masterPassword))
			canAccess = false;
		else if (!masterUsername.equals(s) || !masterPassword.equals(new String(chars)))
			canAccess = false;

		return canAccess ? RESULT_VALID : RESULT_INVALID;
	}

	public static void setMasterUsername(String masterUsername)
	{
		CustomVerifier.masterUsername = masterUsername;
	}

	public static void setMasterPassword(String masterPassword)
	{
		CustomVerifier.masterPassword = masterPassword;
	}
}
