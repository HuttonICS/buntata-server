/*
 * Copyright (c) 2016 Information & Computational Sciences, The James Hutton Institute
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

import org.restlet.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.service.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class Buntata extends Application
{
	public Buntata()
	{
		setName("Buntàta Server");
		setDescription("This is the server implementation for Buntàta");
		setOwner("The James Hutton Institute");
		setAuthor("Sebastian Raubach, Information & Computational Sciences");
	}

	@Override
	public Restlet createInboundRoot()
	{
		Router router = new Router(getContext());

		Filter encoder = new Encoder(getContext(), false, true, new EncoderService(true));
		encoder.setNext(router);
		CorsFilter corsFilter = new CorsFilter(getContext(), encoder);
		corsFilter.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
		corsFilter.setAllowedCredentials(true);
		corsFilter.setSkippingResourceForCorsOptions(false);

		attachToRouter(router, "/datasource", Datasource.class);
		attachToRouter(router, "/datasource/{id}", Datasource.class);
		attachToRouter(router, "/datasource/{id}/icon", DatasourceIcon.class);
		attachToRouter(router, "/datasource/{id}/download", DatasourceDownload.class);

		return corsFilter;
	}

	private static void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}
}
