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

import java.util.concurrent.*;

import javax.servlet.*;

import jhi.buntata.server.job.*;

/**
 * {@link ApplicationListener} is the main {@link ServletContextListener}. It schedules {@link DatasourceSizeJob}s set given intervals to update the
 * {@link Datasource} information.
 *
 * @author Sebastian Raubach
 */
public class ApplicationListener implements ServletContextListener
{
	private ScheduledExecutorService scheduler;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		// Start the scheduler
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new DatasourceSizeJob(), 0, 15, TimeUnit.MINUTES);
		scheduler.scheduleAtFixedRate(new DatasourceExportJob(sce.getServletContext()), 0, 15, TimeUnit.MINUTES);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			// Stop the scheduler
			scheduler.shutdownNow();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
