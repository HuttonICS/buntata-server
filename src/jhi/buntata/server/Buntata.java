/*
 * Copyright 2016 Information & Computational Sciences, The James Hutton Institute
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
 * {@link Buntata} is the main Restlet {@link Application}. It handles the routing of the incoming requests.
 *
 * @author Sebastian Raubach
 */
public class Buntata extends Application
{
	public Buntata()
	{
		// Set information about API
		setName("Buntata Server");
		setDescription("This is the server implementation for Buntata");
		setOwner("The James Hutton Institute");
		setAuthor("Sebastian Raubach, Information & Computational Sciences");
	}

	@Override
	public Restlet createInboundRoot()
	{
		// Create new router
		Router router = new Router(getContext());

		// Set the encoder
		Filter encoder = new Encoder(getContext(), false, true, new EncoderService(true));
		encoder.setNext(router);
		// Set the Cors filter
		CorsFilter corsFilter = new CorsFilter(getContext(), encoder);
		corsFilter.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
		corsFilter.setAllowedCredentials(true);
		corsFilter.setSkippingResourceForCorsOptions(false);

		// Attach the url handlers
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
