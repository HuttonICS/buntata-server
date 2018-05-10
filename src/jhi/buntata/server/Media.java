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

package jhi.buntata.server;

import net.coobird.thumbnailator.*;

import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;
import java.util.logging.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link Media} requests.
 *
 * @author Sebastian Raubach
 */
public class Media extends ServerResource
{
	public static final String PARAM_SIZE = "small";

	private final MediaDAO dao   = new MediaDAO();
	private       int      id    = -1;
	private       boolean  small = false;

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

		try
		{
			this.small = Boolean.parseBoolean(getQueryValue(PARAM_SIZE));
		}
		catch (NullPointerException e)
		{
		}
	}

	@Get
	public Representation getImage()
	{
		BuntataMedia media = dao.get(id);

		FileRepresentation representation = null;

		if (media != null)
		{
			File file = new File(media.getInternalLink());
			boolean created = false;

			// Check if the icon exists
			if (file.exists() && file.isFile())
			{
				try
				{
					if (small)
					{
						File target = File.createTempFile(file.getName(), "-small.jpg");
						created = true;

						Thumbnails.of(file)
								  .height(400)
								  .keepAspectRatio(true)
								  .toFile(target);

						file = target;
					}

					MediaType type = MediaType.IMAGE_ALL;
					representation = new FileRepresentation(file, type);
					representation.setAutoDeleting(created);
					representation.setSize(file.length());
					representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
				}
				catch (IOException e)
				{
				}
			}
			else
			{
				Logger.getLogger("").log(Level.WARNING, "File not found: " + file.getAbsolutePath());
			}
		}
		else
		{
			Logger.getLogger("").log(Level.WARNING, "Media not found: " + id);
		}

		return representation;
	}
}
