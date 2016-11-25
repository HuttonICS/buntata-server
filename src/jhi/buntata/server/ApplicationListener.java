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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;

/**
 * @author Sebastian Raubach
 */
public class ApplicationListener implements ServletContextListener
{
	private ScheduledExecutorService scheduler;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new DatasourceSizeJob(), 0, 15, TimeUnit.MINUTES);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			scheduler.shutdownNow();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class DatasourceSizeJob implements Runnable
	{
		private DatasourceDAO datasourceDAO = new DatasourceDAO();
		private NodeDAO       nodeDAO       = new NodeDAO();
		private MediaDAO      mediaDAO      = new MediaDAO();

		@Override
		public void run()
		{
			// Get all the data sources
			List<BuntataDatasource> datasources = datasourceDAO.getAll();

			for (BuntataDatasource datasource : datasources)
			{
				long sizeTotal = 0;
				long sizeNoVideo = 0;

				// Get all the nodes
				List<BuntataNode> nodes = nodeDAO.getAllForDatasource(datasource);

				Set<String> alreadyCounted = new HashSet<>();

				for (BuntataNode node : nodes)
				{
					// Get all the media
					Map<String, List<BuntataMedia>> media = mediaDAO.getAllForNode(node);

					long imageSize = media.get(BuntataMediaType.TYPE_IMAGE)
										  .parallelStream()
										  .map(m -> new File(m.getInternalLink()))
										  .filter(f ->
										  {
											  boolean result = f.exists() && f.isFile();

											  if (alreadyCounted.contains(f.getAbsolutePath()))
												  result = false;
											  else
												  alreadyCounted.add(f.getAbsolutePath());

											  return result;
										  })
										  .map(File::length)
										  .mapToLong(Long::longValue)
										  .sum();

					long videoSize = media.get(BuntataMediaType.TYPE_VIDEO)
										  .parallelStream()
										  .map(m -> new File(m.getInternalLink()))
										  .filter(f ->
										  {
											  boolean result = f.exists() && f.isFile();

											  if (alreadyCounted.contains(f.getAbsolutePath()))
												  result = false;
											  else
												  alreadyCounted.add(f.getAbsolutePath());

											  return result;
										  })
										  .map(File::length)
										  .mapToLong(Long::longValue)
										  .sum();

					sizeTotal += imageSize + videoSize;
					sizeNoVideo += imageSize;
				}

				// Set the size
				datasource.setSizeTotal(sizeTotal);
				datasource.setSizeNoVideo(sizeNoVideo);
				// And save
				datasourceDAO.updateSize(datasource);
			}
		}
	}
}
