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
import org.restlet.data.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.security.*;
import org.restlet.service.*;

import java.util.*;

import jhi.buntata.server.auth.*;

/**
 * {@link Buntata} is the main Restlet {@link Application}. It handles the routing of the incoming requests.
 *
 * @author Sebastian Raubach
 */
public class Buntata extends Application
{
	private ChallengeAuthenticator authenticator;
	private MethodAuthorizer       authorizer;

	public Buntata()
	{
		// Set information about API
		setName("Buntata Server");
		setDescription("This is the server implementation for Buntata");
		setOwner("The James Hutton Institute");
		setAuthor("Sebastian Raubach, Information & Computational Sciences");
	}

	private void setUpAuthentication(Context context)
	{
		authorizer = new MethodAuthorizer();
		authorizer.getAnonymousMethods().add(Method.GET);
		authorizer.getAnonymousMethods().add(Method.POST);
		authorizer.getAuthenticatedMethods().add(Method.PUT);
		authorizer.getAuthenticatedMethods().add(Method.DELETE);

		authenticator = new ChallengeAuthenticator(context, true, ChallengeScheme.HTTP_BASIC, "Buntata", new CustomVerifier());
	}

	@Override
	public Restlet createInboundRoot()
	{
		Context context = getContext();

		// Create new router
		Router router = new Router(context);

		setUpAuthentication(context);

		// Set the encoder
		Filter encoder = new Encoder(context, false, true, new EncoderService(true));
		encoder.setNext(router);
		// Set the Cors filter
		CorsFilter corsFilter = new CorsFilter(context, encoder);
		corsFilter.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
		corsFilter.setAllowedCredentials(true);
		corsFilter.setSkippingResourceForCorsOptions(false);

		// Attach the url handlers
		attachToRouter(router, "/datasource", Datasource.class);
		attachToRouter(router, "/datasource/{id}", Datasource.class);
		attachToRouter(router, "/datasource/{id}/icon", DatasourceIcon.class);
		attachToRouter(router, "/datasource/{id}/download", DatasourceDownload.class);

		// Not currently used by the Buntata app
		attachToRouter(router, "/datasource/{id}/nodes", DatasourceNodeList.class);
		attachToRouter(router, "/media/{id}", Media.class);
		attachToRouter(router, "/node", Node.class);
		attachToRouter(router, "/node/{id}", Node.class);
		attachToRouter(router, "/node/{id}/media", NodeMedia.class);

		authenticator.setNext(authorizer);
		authorizer.setNext(corsFilter);

		return authenticator;
	}

	private static void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}
}
