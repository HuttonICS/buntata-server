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

import java.util.*;

import jhi.buntata.resource.*;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * @author Sebastian Raubach
 */
public interface ApiProvider
{
	@GET("datasource")
	Call<List<BuntataDatasource>> getDatasources();

	@POST("datasource")
	Call<ResponseBody> postDatasource(@Body BuntataDatasource datasource);

	@PUT("datasource/{id}")
	Call<ResponseBody> putDatasource(@Path("id") String id, @Body BuntataDatasource datasource);

	@GET("node")
	Call<ResponseBody> getNodes();

	@POST("node")
	Call<ResponseBody> postNode(@Body BuntataNode node);

	@PUT("node/{id}")
	Call<ResponseBody> putNode(@Path("id") String id, @Body BuntataNode node);

	@GET("node/{id}/media")
	Call<List<BuntataMedia>> getMediaForNode(@Path("id") String nodeId);

	@POST("media")
	Call<ResponseBody> postMedia(@Body BuntataMedia media);

	@PUT("media/{id}")
	Call<ResponseBody> putMedia(@Path("id") String id, @Body BuntataMedia media);
}
