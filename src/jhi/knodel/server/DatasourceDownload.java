package jhi.knodel.server;

import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import org.zeroturnaround.zip.*;

import java.io.*;
import java.nio.file.*;

import javax.activation.*;
import javax.servlet.*;

import jhi.knodel.sqlite.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceDownload extends ServerResource
{
	private int id = -1;

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
			e.printStackTrace();
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
			File folder = Files.createTempDirectory("knodel-datasource-" + id + "-").toFile();
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
					Integer.toString(id), sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(), database, username, password);

			// Redirect output
			processBuilder = processBuilder.inheritIO();

			// Start it
			Process process = processBuilder.start();

			// Wait for it
			System.out.println("Exit value: " + process.waitFor());

			// Zip it
			File zipFile = File.createTempFile("knodel-datasource-" + id + "-", ".zip");
			ZipUtil.pack(folder, zipFile);

			return zipFile;
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}