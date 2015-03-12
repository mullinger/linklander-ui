package de.lander.link.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Helper for reading properties from classpath
 * 
 * @author mvogel
 *
 */
public final class PropertiesLoader {

	/**
	 * Reads the given property from the file from the classpath
	 * 
	 * @param propertyFile the property file
	 * @param propertyToRead the property to read
	 * @return the value of the property
	 * @throws IllegalArgumentException if the property does not exist in the file
	 */
	public static String readProperty(final String propertyFile,
			final String propertyToRead) {
		final Properties properties = new Properties();
		try {
			properties.load(PropertiesLoader.class.getClassLoader()
					.getResourceAsStream(propertyFile));
		} catch (IOException e) {
			throw new RuntimeException("propertyFile={" + propertyFile + "} could not be read");
		}

		if (!properties.containsKey(propertyToRead)) {
			throw new IllegalArgumentException("property={" + propertyToRead
					+ "} does not exist in file={" + propertyFile + "}");
		}

		return properties.getProperty(propertyToRead);
	}

	/**
	 * hide
	 */
	private PropertiesLoader() {
		throw new AssertionError("do not instantiate");
	}

}
