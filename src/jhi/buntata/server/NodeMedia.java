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

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.restlet.data.*;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * {@link ServerResource} handling {@link NodeDAO} {@link BuntataMedia} object requests.
 *
 * @author Sebastian Raubach
 */
public class NodeMedia extends ServerResource
{
	private static String dataDir;

	private final NodeDAO      nodeDAO      = new NodeDAO();
	private final MediaDAO     mediaDAO     = new MediaDAO();
	private final NodeMediaDAO nodeMediaDAO = new NodeMediaDAO();
	private       Long         id           = null;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.id = Long.parseLong(getRequestAttributes().get("id").toString());
		}
		catch (NullPointerException | NumberFormatException e)
		{
		}
	}

	@Put
	public boolean putMedia(Representation entity)
	{
		if (entity != null && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true))
		{
			try
			{
				BuntataNode node = nodeDAO.get(id);

				DiskFileItemFactory factory = new DiskFileItemFactory();
				RestletFileUpload upload = new RestletFileUpload(factory);
				FileItemIterator fileIterator = upload.getItemIterator(entity);

				if (fileIterator.hasNext())
				{
					String nodeName = node.getName().replace(" ", "-");
					File dir = new File(dataDir, nodeName);
					dir.mkdirs();

					FileItemStream fi = fileIterator.next();

					String name = fi.getName();

					File file = new File(dir, name);

					int i = 1;
					while (file.exists())
						file = new File(dir, (i++) + name);

					// Copy the file to its target location
					Files.copy(fi.openStream(), file.toPath());

					// Create the media entity
					String relativePath = new File(dataDir).toURI().relativize(file.toURI()).getPath();
					BuntataMedia media = new BuntataMedia(null, new Date(), new Date())
						.setInternalLink(relativePath)
						.setName(name)
						.setDescription(name)
						.setMediaTypeId(1L);
					mediaDAO.add(media);

					// Create the node media entity
					nodeMediaDAO.add(new BuntataNodeMedia()
						.setMediaId(media.getId())
						.setNodeId(node.getId()));

					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
		}

		return false;
	}

	@Get("json")
	public Map<String, List<BuntataMedia>> getMedia()
	{
		BuntataNode node = nodeDAO.get(id);

		Map<String, List<BuntataMedia>> result = new HashMap<>();

		if (node != null)
			result = mediaDAO.getAllForNode(node.getId(), false);

		// Restrict paths to just the filename
		for (Map.Entry<String, List<BuntataMedia>> e : result.entrySet())
			e.getValue().forEach(BuntataMedia::restrict);

		return result;
	}

	public static void setDataDir(String dataDir)
	{
		NodeMedia.dataDir = dataDir;
	}
}
