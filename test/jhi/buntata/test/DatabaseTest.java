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

package jhi.buntata.test;

import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.*;

import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.util.*;

import jhi.database.server.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;

/**
 * @author Sebastian Raubach
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseTest
{
	private Properties properties = new Properties();

	protected String server;
	protected String database;
	protected String username;
	protected String password;

	protected String masterUsername;
	protected String masterPassword;

	protected String antPath;

	protected String tomcatUsername;
	protected String tomcatPassword;
	protected String tomcatName;

	private void readProperties()
		throws IOException
	{
		properties.load(new FileReader(new File("res/test.properties")));

		server = properties.getProperty("database.server");
		database = properties.getProperty("database.name");
		username = properties.getProperty("database.username");
		password = properties.getProperty("database.password");

		masterUsername = UUID.randomUUID().toString();
		masterPassword = UUID.randomUUID().toString();

		antPath = properties.getProperty("path.ant");

		tomcatUsername = properties.getProperty("tomcat.username");
		tomcatPassword = properties.getProperty("tomcat.password");
		tomcatName = properties.getProperty("tomcat.deploy.name");

		assert StringUtils.isNotBlank(server);
		assert StringUtils.isNotBlank(database);
		assert StringUtils.isNotBlank(username);
		assert StringUtils.isNotBlank(masterUsername);
		assert StringUtils.isNotBlank(tomcatUsername);
		assert StringUtils.isNotBlank(tomcatName);
		assert StringUtils.isNotBlank(antPath);
	}

	@BeforeAll
	public void initDatabase()
		throws DatabaseException, IOException, SQLException
	{
		readProperties();

		Database.init(server + "/", username, password);

		// Drop old version of db (if exists)
		new ValueQuery("DROP DATABASE IF EXISTS `" + database + "`")
			.execute();
		// Create the db
		new ValueQuery("CREATE DATABASE `" + database + "`")
			.execute();

		// Import the db creation script
		File databaseScript = new File("res/buntata_template.sql");
		assert databaseScript.exists();

		Database.init(server + "/" + database, username, password);
		Database db = Database.connect();
		ScriptRunner runner = new ScriptRunner(db.getConnection(), false, true);
		runner.setLogWriter(new PrintWriter(System.out));
		runner.setErrorLogWriter(new PrintWriter(System.err));
		runner.runScript(new BufferedReader(new InputStreamReader(new FileInputStream(databaseScript), StandardCharsets.UTF_8)));
		db.close();
	}

	@BeforeAll
	public void deployServerApi()
		throws IOException, InterruptedException
	{
		int result = runAntTarget("deploy");

		assert result == 0;
	}

	private int runAntTarget(String target)
		throws IOException, InterruptedException
	{
		/* Build up the ant command line arguments */
		List<String> arguments = new ArrayList<>();
		arguments.add(antPath);
		arguments.add("-f");
		arguments.add(new File("build.xml").getAbsolutePath());
		arguments.add("-Dproject.name=" + database);
		arguments.add("-Dtomcat.manager.url=http://localhost:8080/manager/text");
		arguments.add("-Dtomcat.manager.username=" + tomcatUsername);
		arguments.add("-Dtomcat.manager.password=" + (StringUtils.isBlank(tomcatPassword) ? "\"\"" : tomcatPassword));
		arguments.add("-Ddatasource=jdbc/database");
		arguments.add("-Dga.tracking.id=\"\"");
		arguments.add("-Ddeploy.name=" + tomcatName);
		arguments.add("-Dapi.version=1.1");
		arguments.add("-Ddatabase.username=" + username);
		arguments.add("-Ddatabase.password=" + (StringUtils.isBlank(password) ? "\"\"" : password));
		arguments.add("-Dmaster.username=" + masterUsername);
		arguments.add("-Dmaster.password=" + masterPassword);
		arguments.add("-Ddatabase.url=jdbc:mysql://" + server + ":3306/" + database);

		arguments.add(target);

		/* Create the process and listen to the output */
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.directory(new File(""));
		builder.directory(new File("build.xml").getParentFile());
		builder.redirectErrorStream(true);
		builder.inheritIO();
		Process process = builder.start();
		return process.waitFor();
	}

	@AfterAll
	public void closeDatabase()
		throws DatabaseException, IOException, InterruptedException
	{
		int result = runAntTarget("undeploy");

		assert result == 0;

		// Connect to parent
		Database.init(server + "/", username, password);

		// Drop table
		new ValueQuery("DROP DATABASE IF EXISTS `" + database + "`")
			.execute();
	}
}
