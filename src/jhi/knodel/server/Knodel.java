package jhi.knodel.server;

import org.restlet.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.service.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class Knodel extends Application
{
	public Knodel()
	{
		setName("Knodel Server");
		setDescription("This is the server implementation for Knodel");
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
		attachToRouter(router, "/datasource/{id}/download", DatasourceDownload.class);

		return corsFilter;
	}

	private static void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}
}
