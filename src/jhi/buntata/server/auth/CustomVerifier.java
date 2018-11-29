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

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.security.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * @author Sebastian Raubach
 */
public class CustomVerifier implements Verifier
{
	public static final long AGE = 1800000;

	private static String masterUsername;
	private static String masterPassword;

	private static Map<String, Long> tokenToTimestamp = new ConcurrentHashMap<>();

	public CustomVerifier()
	{
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				tokenToTimestamp.entrySet().removeIf(token -> token.getValue() < (System.currentTimeMillis() - AGE));
			}
		}, 0, AGE);
	}

	public static boolean removeToken(String password)
	{
		return tokenToTimestamp.remove(password) != null;
	}

	@Override
	public int verify(Request request, Response response)
	{
		ChallengeResponse cr = request.getChallengeResponse();
		if (cr != null)
		{
			String token = cr.getRawValue();

			boolean canAccess = false;

			// Check if it's the master username and password
			Long timestamp = tokenToTimestamp.get(token);

			if (timestamp != null)
			{
				// First, check the bearer token and see if we have it in the cache
				if ((System.currentTimeMillis() - AGE) < timestamp)
				{
					// If we do, validate it against the cookie
					List<Cookie> cookies = request.getCookies()
												  .stream()
												  .filter(c -> c.getName().equals("token"))
												  .collect(Collectors.toList());

					if (cookies.size() > 0)
					{
						canAccess = Objects.equals(token, cookies.get(0).getValue());
						// Extend the cookie
						tokenToTimestamp.put(token, System.currentTimeMillis());
						setCookie(response, token);
					}
					else
					{
						canAccess = false;
					}
				}
				else
				{
					return RESULT_STALE;
				}
			}

			return canAccess ? RESULT_VALID : RESULT_INVALID;
		}
		else
		{
			return RESULT_MISSING;
		}
	}

	public static void addToken(Response response, String token)
	{
		setCookie(response, token);
		tokenToTimestamp.put(token, System.currentTimeMillis());
	}

	private static void setCookie(Response response, String token)
	{
		CookieSetting cookie = new CookieSetting(0, "token", token);
		cookie.setAccessRestricted(true);
		cookie.setMaxAge((int) (AGE / 1000));
		cookie.setPath("/");
		response.getCookieSettings().add(cookie);
	}

	public static void setMasterUsername(String masterUsername)
	{
		CustomVerifier.masterUsername = masterUsername;
	}

	public static void setMasterPassword(String masterPassword)
	{
		CustomVerifier.masterPassword = masterPassword;
	}

	public static String getMasterUsername()
	{
		return masterUsername;
	}

	public static String getMasterPassword()
	{
		return masterPassword;
	}
}
