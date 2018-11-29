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

package jhi.buntata.test;

import java.io.*;

import okhttp3.*;

public class AuthenticationInterceptor implements Interceptor
{
	private String method;
	private String authToken;

	public AuthenticationInterceptor(String authToken)
	{
		String[] split = authToken.split(" ");
		this.method = split[0];
		this.authToken = split[1];
	}

	public AuthenticationInterceptor(String method, String token)
	{
		this.method = method;
		this.authToken = token;
	}

	@Override
	public Response intercept(Chain chain)
		throws IOException
	{
		if (authToken != null)
		{
			Request original = chain.request();

			Request.Builder builder = original.newBuilder()
											  .header("Authorization", method + " " + authToken);

			builder.addHeader("Cookie", "token=" + authToken);

			Request request = builder.build();
			return chain.proceed(request);
		}
		else
		{
			return chain.proceed(chain.request());
		}
	}


	public AuthenticationInterceptor setAuthToken(String method, String authToken)
	{
		this.method = method;
		this.authToken = authToken;
		return this;
	}
}