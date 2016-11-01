/*
 * Copyright (c) 2016 Information & Computational Sciences, The James Hutton Institute
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
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import javax.activation.*;
import javax.servlet.*;

import jhi.buntata.sqlite.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceDownload extends ServerResource
{
	private int id = -1;
	private boolean includeVideos = true;

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
			String queryValue = getQueryValue("includevideos");
			if(queryValue != null)
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
		if (id != -1)
		{
			File file = createFile();

			if (file != null)
			{
				MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
				String mimeTypeStr = mimeTypesMap.getContentType(file);
				MediaType mt = new MediaType(mimeTypeStr);
				representation = new FileRepresentation(file, mt);

				Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
				disp.setFilename("datasource-" + id + ".zip");
				disp.setSize(file.length());
				representation.setDisposition(disp);
			}
		}

		return representation;
	}

	/**
	 * Extracts the relevant information from MySQL to Sqlite. This has been moved to a Tomcat-external jar so that the Sqlite JDBC driver doesn't
	 * conflict with the way Tomcat works. To make it work without this workaround, one would need to move the Sqlite JDBC jar to Tomcat's own lib
	 * folder rather than the apps lib folder.
	 */
	private File createFile()
	{
		try
		{
			// Get some information from the servlet
			ServletContext servlet = (ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
			File sourceFile = new File(servlet.getRealPath("/WEB-INF/database.db"));

			// Create a temporary directory and new sqlite file
			File folder = Files.createTempDirectory("buntata-datasource-" + id + "-").toFile();
			File targetFile = new File(folder, id + ".sqlite");

			// Get the classpath elements
			File libFolder = new File(servlet.getRealPath("/WEB-INF/lib"));
			File jdbcClient = new File(servlet.getRealPath("/WEB-INF/sqlite/sqlite-client.jar"));
			File jdbcJar = new File(servlet.getRealPath("/WEB-INF/sqlite/sqlite-jdbc.jar"));

			// Get the database connection information
			String database = getContext().getParameters().getFirstValue("database");
			String username = getContext().getParameters().getFirstValue("username");
			String password = getContext().getParameters().getFirstValue("password");

			// Define the external process
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", jdbcJar.getAbsolutePath() + File.pathSeparator + jdbcClient.getAbsolutePath() + File.pathSeparator + libFolder.getAbsolutePath() + "/*", MySqlToSqLiteConverter.class.getCanonicalName(),
					Integer.toString(id), Boolean.toString(includeVideos), sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(), database, username, password);

			// Redirect output
			processBuilder = processBuilder.inheritIO();

			// Start it
			Process process = processBuilder.start();

			// Wait for it
			System.out.println("Exit value: " + process.waitFor());

			// Zip it
			File zipFile = File.createTempFile("buntata-datasource-" + id + "-", ".zip");
			zipIt(folder, zipFile);

			folder.delete();

			return zipFile;
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public void zipIt(File sourceFolder, File targetFile)
	{
		byte[] buffer = new byte[1024];

		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile)))
		{
//			System.out.println("Output to Zip : " + targetFile);

			List<File> fileList = new ArrayList<>();

			generateFileList(fileList, sourceFolder);

			for (File file : fileList)
			{
//				System.out.println("File Added : " + file);
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
//			System.out.println("Folder successfully compressed");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void generateFileList(List<File> result, File parent)
	{
		// add file only
		if (parent.isFile())
		{
			result.add(parent);
		}

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
}
