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

import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link BuntataDatasource} icon requests.
 *
 * @author Sebastian Raubach
 */
public class DatasourceIcon extends ServerResource
{
	private final DatasourceDAO dao = new DatasourceDAO();
	private       int           id  = -1;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.id = Integer.parseInt(getRequestAttributes().get("id").toString());
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}
	}

	@Get
	public FileRepresentation getImage()
	{
		BuntataDatasource datasource = dao.get(id);

		FileRepresentation representation = null;

		if (datasource != null)
		{
			// Get the icon
			String icon = datasource.getIcon();

			if (icon != null)
			{
				File file = new File(icon);

				// Check if the icon exists
				if (file.exists() && file.isFile())
				{
					MediaType mt = MediaType.IMAGE_ALL;
					representation = new FileRepresentation(file, mt);

					Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
					disp.setFilename(file.getName());
					disp.setSize(file.length());
					representation.setDisposition(disp);
				}
			}
		}

		// Return the result
		return representation;
	}
}
