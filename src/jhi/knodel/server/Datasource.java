package jhi.knodel.server;

import org.restlet.resource.*;

import jhi.knodel.data.*;
import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */
public class Datasource extends ServerResource
{
	private DatasourceDAO dao = new DatasourceDAO();

	@Override
	public void doInit()
	{
		super.doInit();

		// Parse parameters here
	}

	@Get("json")
	public KnodelDatasourceList getJson()
	{
		return dao.getAll();
	}
}
