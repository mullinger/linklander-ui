package de.lander.link.util;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 *
 * @author mvogel
 *
 */
public class DatabaseFactoryForTests {

	@Inject
	public static transient Logger LOGGER;

	@Produces
	public GraphDatabaseService createTestDatabase() {
		LOGGER.debug("Testdatabase!");
		return new TestGraphDatabaseFactory().newImpermanentDatabase();
	}
}
