package jhi.knodel.server;

import org.restlet.resource.*;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class Datasource extends ServerResource
{
	@Override
	public void doInit()
	{
		super.doInit();

		// Parse parameters here
	}

	@Get("json")
	public List<KnodelDatasource> getJson()
	{
		List<KnodelDatasource> result = new ArrayList<>();

		result.add(new KnodelDatasource()
				.setName("Name")
				.setDescription("Description"));

		return result;
	}
}
