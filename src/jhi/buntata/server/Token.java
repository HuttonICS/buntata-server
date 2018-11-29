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

package jhi.buntata.server;

import org.restlet.resource.*;

import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;
import jhi.buntata.server.auth.*;

/**
 * {@link ServerResource} handling {@link Token} requests.
 *
 * @author Sebastian Raubach
 */
public class Token extends ServerResource
{
	private final UserDAO dao = new UserDAO();

	@Delete("json")
	public boolean deleteJson(BuntataUser request)
	{
		if (request.getPassword() != null)
		{
			try
			{
				UUID.fromString(request.getPassword());
				return CustomVerifier.removeToken(request.getPassword());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		return false;
	}

	@Post("json")
	public BuntataToken postJson(BuntataUser request)
	{
		boolean canAccess = false;
		String token = null;
		String masterUsername = CustomVerifier.getMasterUsername();
		String masterPassword = CustomVerifier.getMasterPassword();

		if (masterUsername != null && masterPassword != null && !masterUsername.equals("") && !masterPassword.equals("") && masterUsername.equals(request.getUsername()) && masterPassword.equals(request.getPassword()))
		{
			canAccess = true;
		}
		else
		{
			try
			{
				BuntataUser user = dao.getForName(request.getUsername());

				canAccess = BCrypt.checkpw(request.getPassword(), user.getPassword());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ResourceException(403);
			}
		}

		if (canAccess)
		{
			token = UUID.randomUUID().toString();
			CustomVerifier.addToken(getResponse(), token);
		}
		else
		{
			throw new ResourceException(403);
		}

		return new BuntataToken(token, CustomVerifier.AGE, System.currentTimeMillis());
	}
}
