package de.lander.link.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Producer for specific loggers
 *
 * @author mvogel
 *
 */
public class LoggerFactory {

	/**
	 * Creates a logger for the class with the injection point
	 *
	 * @param injectionPoint
	 *            the injection point to examine
	 * @return the logger for the class
	 */
	@Produces
	public Logger getLogger(final InjectionPoint injectionPoint) {
		return LogManager.getLogger(injectionPoint.getMember()
				.getDeclaringClass());
	}
}
