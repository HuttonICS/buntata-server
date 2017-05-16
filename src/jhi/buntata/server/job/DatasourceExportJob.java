/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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

package jhi.buntata.server.job;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;

import jhi.buntata.data.*;
import jhi.buntata.resource.*;
import jhi.buntata.server.*;
import jhi.buntata.sqlite.*;

/**
 * This {@link Runnable} updates the data size information of all {@link BuntataDatasource} objects by checking their {@link BuntataMedia} objects and
 * summing over their size.
 */
public class DatasourceExportJob implements Runnable
{
	private static final SimpleDateFormat SDF             = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static final String           TEMP_SUB_FOLDER = "buntata-datasources";

	private static File TARGET_FOLDER;

	private final DatasourceDAO datasourceDAO = new DatasourceDAO();

	private ServletContext servlet;

	public DatasourceExportJob(ServletContext servlet)
	{
		this.servlet = servlet;

		String version = servlet.getInitParameter("version");

		TARGET_FOLDER = new File(System.getProperty("java.io.tmpdir"), TEMP_SUB_FOLDER + "-" + version);
		TARGET_FOLDER.mkdirs();
	}

	@Override
	public void run()
	{
		datasourceDAO.getAll(true)
					 .parallelStream()
					 .forEach(ds -> makeSureExists(servlet, ds));
	}

	public static File[] makeSureExists(ServletContext servlet, BuntataDatasource datasource)
	{
		Date date = datasource.getUpdatedOn();
		if (date == null)
			date = datasource.getCreatedOn();

		File targetFileTrue;
		File targetFileFalse;

		/* Make sure to synchronize this as it'd cause race conditions otherwise */
		synchronized (SDF)
		{
			targetFileTrue = new File(TARGET_FOLDER, datasource.getId() + "-" + SDF.format(date) + "-true.zip");
			targetFileFalse = new File(TARGET_FOLDER, datasource.getId() + "-" + SDF.format(date) + "-false.zip");
		}

		// If the file doesn't exist, we need to create it.
		if (!targetFileTrue.exists() || !targetFileFalse.exists())
		{
			exportFile(servlet, datasource, targetFileTrue, true);
			exportFile(servlet, datasource, targetFileFalse, false);

			// Delete potentially existing old files
			Arrays.stream(getOldFiles(datasource, targetFileTrue, targetFileFalse))
				  .forEach(File::delete);
		}

		return new File[]{targetFileTrue, targetFileFalse};
	}

	private static void exportFile(ServletContext servlet, BuntataDatasource datasource, File target, boolean includeVideos)
	{
		try
		{
			final int id = datasource.getId();

			// Get some information from the servlet
			File sourceFile = new File(servlet.getRealPath("/WEB-INF/database.db"));

			// Create a temporary directory and new sqlite file
			File folder = Files.createTempDirectory("buntata-datasource-" + id + "-").toFile();
			File targetFile = new File(folder, id + ".sqlite");

			// Get the classpath elements
			File libFolder = new File(servlet.getRealPath("/WEB-INF/lib"));
			File jdbcClient = new File(servlet.getRealPath("/WEB-INF/sqlite/sqlite-client.jar"));
			File jdbcJar = new File(servlet.getRealPath("/WEB-INF/sqlite/sqlite-jdbc.jar"));

			// Get the database connection information
			String database = servlet.getInitParameter("database");
			String username = servlet.getInitParameter("username");
			String password = servlet.getInitParameter("password");

			if (password == null || password.equals(""))
				password = "\"\"";

			// Define the external process
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", jdbcJar.getAbsolutePath() + File.pathSeparator + jdbcClient.getAbsolutePath() + File.pathSeparator + libFolder.getAbsolutePath() + "/*", MySqlToSqLiteConverter.class.getCanonicalName(),
					Integer.toString(id), Boolean.toString(includeVideos), sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(), database, username, password);

			// Redirect output
			processBuilder = processBuilder.inheritIO();

			// Start it
			processBuilder.start()
						  .waitFor();

			// Zip it
			zipIt(folder, target);

			// Delete temp files
			Utils.deleteDirectory(folder);
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Zips the given source folder into the given target file
	 *
	 * @param sourceFolder The source {@link File} (folder)
	 * @param targetFile   The target {@link File}
	 */
	private static void zipIt(File sourceFolder, File targetFile)
	{
		if (targetFile.exists())
			targetFile.delete();

		byte[] buffer = new byte[1024];

		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile)))
		{
			List<File> fileList = new ArrayList<>();

			generateFileList(fileList, sourceFolder);

			// Add all the files to the zip file
			for (File file : fileList)
			{
				ZipEntry ze = new ZipEntry(file.getName());
				zos.putNextEntry(ze);
				try (FileInputStream fis = new FileInputStream(file))
				{
					int len;
					while ((len = fis.read(buffer)) > 0)
					{
						zos.write(buffer, 0, len);
					}
				}
			}

			zos.closeEntry();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void generateFileList(List<File> result, File parent)
	{
		// add file only
		if (parent.isFile())
		{
			result.add(parent);
		}

		// Recursively add files
		if (parent.isDirectory())
		{
			File[] children = parent.listFiles();
			if (children != null)
			{
				for (File child : children)
				{
					generateFileList(result, child);
				}
			}
		}
	}

	private static File[] getOldFiles(BuntataDatasource datasource, File t, File f)
	{
		return TARGET_FOLDER.listFiles(file -> !file.equals(t) && !file.equals(f) && file.getName().startsWith(datasource.getId() + "-"));
	}
}