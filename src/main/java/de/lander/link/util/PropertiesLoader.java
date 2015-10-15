package de.lander.link.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Helper for reading properties from classpath
 * 
 * @author mvogel
 *
 */
public final class PropertiesLoader {

	private static final String baseDirProperty = "linklander.base.directory";

	{
		String baseDirectoryString = getBaseDirectory();
		File baseDirectory = FileUtils.getFile(baseDirectoryString);
		try {
			FileUtils.forceMkdir(baseDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
//	/**
//	 * Reads the given property from the file from the classpath
//	 * 
//	 * @param propertyFile
//	 *            the property file
//	 * @param propertyToRead
//	 *            the property to read
//	 * @return the value of the property
//	 * @throws IllegalArgumentException
//	 *             if the property does not exist in the file
//	 */
//	public static String readProperty(final String propertyFile, final String propertyToRead) {
//
//		final Properties properties = new Properties();
//		try {
//			properties.load(PropertiesLoader.class.getClassLoader().getResourceAsStream(propertyFile));
//		} catch (IOException e) {
//			throw new RuntimeException("propertyFile={" + propertyFile + "} could not be read");
//		}
//
//		if (!properties.containsKey(propertyToRead)) {
//			throw new IllegalArgumentException("property={" + propertyToRead + "} does not exist in file={"
//					+ propertyFile + "}");
//		}
//
//		return properties.getProperty(propertyToRead);
//	}

	public static final String getBaseDirectory() {
		return System.getProperty(baseDirProperty, "~/.linkLander/");
	}

	/**
	 * hide
	 */
	private PropertiesLoader() {
		throw new AssertionError("do not instantiate");
	}

}
