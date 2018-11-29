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

import com.fasterxml.jackson.annotation.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuntataToken
{
	private String token;
	private Long   lifetime;
	private Long   createdOn;

	public BuntataToken()
	{
	}

	public BuntataToken(String token, Long lifetime, Long createdOn)
	{
		this.token = token;
		this.lifetime = lifetime;
		this.createdOn = createdOn;
	}

	public String getToken()
	{
		return token;
	}

	public BuntataToken setToken(String token)
	{
		this.token = token;
		return this;
	}

	public Long getLifetime()
	{
		return lifetime;
	}

	public BuntataToken setLifetime(Long lifetime)
	{
		this.lifetime = lifetime;
		return this;
	}

	public Long getCreatedOn()
	{
		return createdOn;
	}

	public BuntataToken setCreatedOn(Long createdOn)
	{
		this.createdOn = createdOn;
		return this;
	}
}
