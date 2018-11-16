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

import com.brsanthu.googleanalytics.*;

import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;

import javax.activation.*;
import javax.servlet.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;
import jhi.buntata.server.job.*;

/**
 * {@link ServerResource} handlind {@link Datasource} download.
 *
 * @author Sebastian Raubach
 */
public class DatasourceDownload extends ServerResource
{
	private Long          id            = null;
	private boolean       includeVideos = true;
	private DatasourceDAO dao           = new DatasourceDAO();

	private ServletContext  servlet;
	private GoogleAnalytics ga;

	@Override
	public void doInit()
	{
		super.doInit();

		if (servlet == null)
		{
			servlet = (ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");

			String id = servlet.getInitParameter("gatrackingid");

			if (id != null)
			{
				ga = GoogleAnalytics.builder()
									.withTrackingId(id)
									.build();
			}
		}

		// Try to parse the id
		try
		{
			this.id = Long.parseLong(getRequestAttributes().get("id").toString());
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}

		// Try to check if the parameter for "includevideos" has been set
		try
		{
			String queryValue = getQueryValue("includevideos");
			if (queryValue != null)
				this.includeVideos = Boolean.parseBoolean(queryValue);
		}
		catch (NullPointerException e)
		{
		}
	}

	@Get
	public FileRepresentation getFile()
	{
		FileRepresentation representation = null;

		// Check if the id is set
		if (id != null)
		{
			BuntataDatasource ds = dao.get(id);

			if (ds != null)
			{
				// Export the data to the SQLite file
				File[] files = DatasourceExportJob.makeSureExists((ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext"), ds);
				File file = includeVideos ? files[0] : files[1];

				if (file != null)
				{
					if (ga != null)
					{
						ga.event()
						  .eventCategory("dataset")
						  .eventAction("download")
						  .eventLabel(Long.toString(id))
						  .sendAsync();
					}

					// Prepare the result
					MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
					String mimeTypeStr = mimeTypesMap.getContentType(file);
					MediaType mt = new MediaType(mimeTypeStr);
					representation = new FileRepresentation(file, mt);

					// Give it a name and set the size
					Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
					disp.setFilename("datasource-" + id + ".zip");
					disp.setSize(file.length());
					representation.setDisposition(disp);
				}
			}
			else
			{
				throw new ResourceException(404);
			}
		}

		return representation;
	}
}
