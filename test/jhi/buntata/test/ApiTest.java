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

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import jhi.buntata.resource.*;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.*;
import retrofit2.converter.jackson.*;

/**
 * @author Sebastian Raubach
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiTest extends DatabaseTest
{
	private ApiProvider provider;

	public void prepareProvider(OkHttpClient client)
	{
		Retrofit.Builder builder = new Retrofit.Builder()
			.baseUrl("http://localhost:8080/" + tomcatName + "/v1.1/")
			.addConverterFactory(JacksonConverterFactory.create());

		if (client != null)
			builder.client(client);

		provider = builder
			.build()
			.create(ApiProvider.class);
	}

	@Test
	public void putDatasourceWithoutAuth()
		throws InterruptedException
	{
		prepareProvider(null);

		BuntataDatasource datasource = new BuntataDatasource(1L, new Date(), new Date())
			.setName("Datasource 1")
			.setContact("Sebastian Raubach")
			.setDataProvider("The James Hutton Institute")
			.setShowKeyName(true)
			.setShowSingleChild(false)
			.setVisibility(true)
			.setIcon("myicon.png");
		boolean[] wait = {true, true};
		provider.putDatasource("1", datasource).enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
			{
				try
				{
					wait[0] = false;
					assert response.raw().code() == 403;
				}
				catch (AssertionError e)
				{
					wait[1] = false;
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable throwable)
			{
				wait[0] = false;
				wait[1] = false;
				throw new AssertionError(throwable);
			}
		});

		// I know it's bad style to wait for async requests in tests, but this seems to be the best way to test it in this case
		while (wait[0])
			Thread.sleep(500);

		assert wait[1];
	}

	@Test
	public void putDatasourceWithAuth()
		throws InterruptedException
	{
		String token = Credentials.basic(masterUsername, masterPassword);

		AuthenticationInterceptor interceptor = new AuthenticationInterceptor(token);
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		httpClient.addInterceptor(interceptor);

		prepareProvider(httpClient.build());

		BuntataDatasource datasource = new BuntataDatasource(1L, new Date(), new Date())
			.setName("Datasource 1")
			.setContact("Sebastian Raubach")
			.setDataProvider("The James Hutton Institute")
			.setShowKeyName(true)
			.setShowSingleChild(false)
			.setVisibility(true)
			.setIcon("myicon.png");
		boolean[] wait = {true, true};
		provider.putDatasource("1", datasource).enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
			{
				try
				{
					wait[0] = false;
					assert response.raw().code() == 200;
					assert response.body() != null;
					try
					{
						assert response.body().string().equals("true");
					}
					catch (IOException e)
					{
						throw new AssertionError(e);
					}
				}
				catch (AssertionError e)
				{
					wait[1] = false;
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable throwable)
			{
				wait[0] = false;
				wait[1] = false;
				throw new AssertionError(throwable);
			}
		});

		// I know it's bad style to wait for async requests in tests, but this seems to be the best way to test it in this case
		while (wait[0])
			Thread.sleep(500);

		assert wait[1];
	}

	@Test
	public void putNodeWithAuth()
		throws InterruptedException
	{
		String token = Credentials.basic(masterUsername, masterPassword);

		AuthenticationInterceptor interceptor = new AuthenticationInterceptor(token);
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		httpClient.addInterceptor(interceptor);

		prepareProvider(httpClient.build());

		BuntataNode node = new BuntataNode(1L, new Date(), new Date())
			.setName("Node 1")
			.setDescription("Some node description")
			.setDatasourceId(1L);

		boolean[] wait = {true, true};
		provider.putNode("1", node).enqueue(new Callback<ResponseBody>()
		{
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
			{
				try
				{
					wait[0] = false;
					assert response.raw().code() == 200;
					assert response.body() != null;
					try
					{
						assert response.body().string().equals("true");
					}
					catch (IOException e)
					{
						throw new AssertionError(e);
					}
				}
				catch (AssertionError e)
				{
					wait[1] = false;
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable throwable)
			{
				wait[0] = false;
				wait[1] = false;
				throw new AssertionError(throwable);
			}
		});

		// I know it's bad style to wait for async requests in tests, but this seems to be the best way to test it in this case
		while (wait[0])
			Thread.sleep(500);

		assert wait[1];
	}

	@Test
	public void checkDatasources()
		throws InterruptedException
	{
		prepareProvider(null);

		boolean[] wait = {true, true};
		provider.getAllDatasources().enqueue(new Callback<List<BuntataDatasource>>()
		{
			@Override
			public void onResponse(Call<List<BuntataDatasource>> call, Response<List<BuntataDatasource>> response)
			{
				try
				{
					wait[0] = false;
					assert response.isSuccessful();
					assert response.body() != null;
					assert response.body().size() == 1;
				}
				catch (AssertionError e)
				{
					wait[1] = false;
				}
			}

			@Override
			public void onFailure(Call<List<BuntataDatasource>> call, Throwable throwable)
			{
				wait[0] = false;
				wait[1] = false;
				throw new AssertionError(throwable);
			}
		});

		// I know it's bad style to wait for async requests in tests, but this seems to be the best way to test it in this case
		while (wait[0])
			Thread.sleep(500);

		assert wait[1];
	}
}
