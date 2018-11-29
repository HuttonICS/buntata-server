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
import org.restlet.util.*;

import java.util.*;

import jhi.buntata.server.auth.*;

/**
 * {@link Buntata} is the main Restlet {@link Application}. It handles the routing of the incoming requests.
 *
 * @author Sebastian Raubach
 */
public class Buntata extends Application
{
	private        ChallengeAuthenticator authenticator;
	private        MethodAuthorizer       authorizer;
	private static CustomVerifier         verifier = new CustomVerifier();

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
		authorizer.getAuthenticatedMethods().add(Method.OPTIONS);
		authorizer.getAuthenticatedMethods().add(Method.POST);
		authorizer.getAuthenticatedMethods().add(Method.PUT);
		authorizer.getAuthenticatedMethods().add(Method.DELETE);

		authenticator = new ChallengeAuthenticator(context, true, ChallengeScheme.HTTP_OAUTH_BEARER, "Buntata", verifier);
	}

	@Override
	public Restlet createInboundRoot()
	{
		Context context = getContext();

		setUpAuthentication(context);

		// Set the encoder
		Filter encoder = new Encoder(context, false, true, new EncoderService(true));

		// Set the Cors filter
		CorsFilter corsFilter = new CorsFilter(context, encoder)
		{
			@Override
			protected int beforeHandle(Request request, Response response)
			{
				if (getCorsResponseHelper().isCorsRequest(request))
				{
					Series<Header> headers = request.getHeaders();

					for (Header header : headers)
					{
						if (header.getName().equalsIgnoreCase("origin"))
						{
							response.setAccessControlAllowOrigin(header.getValue());
						}
					}
				}
				return super.beforeHandle(request, response);
			}
		};
		corsFilter.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
		corsFilter.setSkippingResourceForCorsOptions(true);
		corsFilter.setAllowingAllRequestedHeaders(true);
		corsFilter.setDefaultAllowedMethods(new HashSet<>(Arrays.asList(Method.POST, Method.GET, Method.PUT, Method.DELETE, Method.OPTIONS)));
		corsFilter.setAllowedCredentials(true);

		// Create new router
		Router routerAuth = new Router(context);
		Router routerUnauth = new Router(context);
		// Attach the url handlers
		attachToRouter(routerAuth, "/datasource", Datasource.class);
		attachToRouter(routerAuth, "/datasource/{id}", Datasource.class);
		attachToRouter(routerAuth, "/datasource/{id}/icon", DatasourceIcon.class);
		attachToRouter(routerAuth, "/datasource/{id}/download", DatasourceDownload.class);

		// Not currently used by the Buntata app
		attachToRouter(routerAuth, "/datasource/{id}/nodes", DatasourceNodeList.class);
		attachToRouter(routerAuth, "/media/{id}", Media.class);
		attachToRouter(routerAuth, "/node", Node.class);
		attachToRouter(routerAuth, "/node/{id}", Node.class);
		attachToRouter(routerAuth, "/node/{id}/media", NodeMedia.class);
		attachToRouter(routerAuth, "/relationship", Relationship.class);
		attachToRouter(routerAuth, "/similarity", Similarity.class);
		attachToRouter(routerAuth, "/attributevalue", AttributeValue.class);
		attachToRouter(routerAuth, "/attributevalue/{id}", AttributeValue.class);

		attachToRouter(routerUnauth, "/token", Token.class);

		// CORS first, then encoder
		corsFilter.setNext(encoder);
		// After that the unauthorized paths
		encoder.setNext(routerUnauth);
		// Set everything that isn't covered to go through the authenticator
		routerUnauth.attachDefault(authenticator);
		authenticator.setNext(authorizer);
		// And finally it ends up at the authenticated router
		authorizer.setNext(routerAuth);

		return corsFilter;
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}

	public static CustomVerifier getVerifier()
	{
		return verifier;
	}
}
