package jhi.knodel.server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.*;

import jhi.knodel.data.*;
import jhi.knodel.resource.*;

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
		scheduler.scheduleAtFixedRate(new DatasourceSizeJob(), 0, 1, TimeUnit.HOURS);
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
			List<KnodelDatasource> datasources = datasourceDAO.getAll().getList();

			for (KnodelDatasource datasource : datasources)
			{
				long size = 0;

				// Get all the nodes
				List<KnodelNode> nodes = nodeDAO.getAllForDatasource(datasource);

				for (KnodelNode node : nodes)
				{
					// Get all the media
					List<KnodelMedia> media = mediaDAO.getAllForNode(node);

					// Sum up their data sizes
					size += media.stream()
								 .map(m -> new File(m.getInternalLink()))
								 .filter(f -> f.exists() && f.isFile())
								 .map(File::length)
								 .mapToLong(Long::longValue)
								 .sum();
				}

				// Set the size
				datasource.setSize(size);
				// And save
				datasourceDAO.updateSize(datasource);
			}
		}
	}
}
