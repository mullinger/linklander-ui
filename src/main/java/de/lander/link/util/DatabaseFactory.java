package de.lander.link.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

/**
 * Factory for the NEO 4J Database<br>
 * Uses the store directory defined in "db.properties"
 *
 * @author mvogel
 *
 */
public class DatabaseFactory {

	@Inject
	public static transient Logger LOGGER;

	private static final String NEO4J_STORE_DIRECTORY = "de.lander.storedir";
	private static final String DB_PROPERTY_FILE = "db.properties";

	@Produces
	public GraphDatabaseService createDatabase(
			final InjectionPoint injectionPoint) {
		LOGGER.debug("annotated " + injectionPoint.getAnnotated());
		Annotated annotated = injectionPoint.getAnnotated();
		LOGGER.debug("annotated.annotations " + annotated.getAnnotations());
		LOGGER.debug("annotated.annotations " + annotated.getBaseType());
		LOGGER.debug("annotated.typeClosure " + annotated.getTypeClosure());
		LOGGER.debug("bean " + injectionPoint.getBean());
		LOGGER.debug("member " + injectionPoint.getMember());
		LOGGER.debug("qualifiers " + injectionPoint.getQualifiers());
		LOGGER.debug("type " + injectionPoint.getType());
		LOGGER.debug("isDelegate " + injectionPoint.isDelegate());
		LOGGER.debug("isTransient " + injectionPoint.isTransient());

		Bean<?> bean = injectionPoint.getBean();
		LOGGER.debug("bean.beanClass " + bean.getBeanClass());
		LOGGER.debug("bean.injectionPoints " + bean.getInjectionPoints());
		LOGGER.debug("bean.name " + bean.getName());
		LOGGER.debug("bean.qualifiers " + bean.getQualifiers());
		LOGGER.debug("bean.scope " + bean.getScope());
		LOGGER.debug("bean.stereotypes " + bean.getStereotypes());
		LOGGER.debug("bean.types " + bean.getTypes());

		String storeDir = PropertiesLoader.readProperty(DB_PROPERTY_FILE,
				NEO4J_STORE_DIRECTORY);
		return new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.nodestore_mapped_memory_size,
						"10M")
				.setConfig(GraphDatabaseSettings.string_block_size, "60")
				.setConfig(GraphDatabaseSettings.array_block_size, "300")
				.newGraphDatabase();
	}
}
