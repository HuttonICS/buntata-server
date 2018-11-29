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

package jhi.buntata.resource;

import java.util.*;

import jhi.database.shared.util.*;

/**
 * @author Sebastian Raubach
 */
public class BuntataUser extends DatabaseObject
{
	public static final String TABLE_NAME     = "users";
	public static final String FIELD_USERNAME = "username";
	public static final String FIELD_PASSWORD = "password";

	private String username;
	private String password;

	public BuntataUser()
	{
	}

	public BuntataUser(Long id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public String getUsername()
	{
		return username;
	}

	public BuntataUser setUsername(String username)
	{
		this.username = username;
		return this;
	}

	public String getPassword()
	{
		return password;
	}

	public BuntataUser setPassword(String password)
	{
		this.password = password;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataUser{" +
			"username='" + username + '\'' +
			"} " + super.toString();
	}
}
