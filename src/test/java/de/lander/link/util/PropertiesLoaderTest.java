package de.lander.link.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests for {@link PropertiesLoader}
 * 
 * @author mvogel
 *
 */
public class PropertiesLoaderTest {

	@Test
	public void shouldReadPropertyFromFile() throws Exception {
		// == go ==
		String propertyValue = PropertiesLoader.readProperty("dbtest.properties", "de.lander.storedir");
		
		// == verify ==
		assertThat(propertyValue, is("/home/xxx/linklanderNeo4jDatabase"));
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldFailToReadPropertyFromFileBecauseFileDoesNotExist() throws Exception {
		// == go ==
		try {
			PropertiesLoader.readProperty("xxxsdsdwewwe.properties", "de.lander.storedir");
			fail();
		} catch (Exception e) {
			// == verify ==
			assertTrue(e.getMessage().contains("could not be read"));
			throw e;
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToReadPropertyFromFileBecauseThePropertyDoesNotExist() throws Exception {
		// == go ==
		try {
			PropertiesLoader.readProperty("dbtest.properties", "i.do.not.exist");
			fail();
		} catch (Exception e) {
			// == verify ==
			assertTrue(e.getMessage().contains("does not exist in file"));
			throw e;
		}
	}
}
