package de.lander.link.gui.logic;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.apache.logging.log4j.Logger;

import de.lander.link.gui.logic.AdvancedSearch.Advanced;
import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

@Advanced
public class AdvancedSearch implements SearchProvider {

	@Qualifier
	@Retention(RUNTIME)
	@Target({ TYPE, METHOD, FIELD, PARAMETER })
	public static @interface Advanced {
	}

	@Inject
	protected PersistenceGateway persistenceGatewayImpl;
	@Inject
	private Logger LOGGER;

	public Set<SearchHit> performSearch(final String searchText) {
		String mySearchText = searchText.trim();

		List<Link> searchLinks;
		if (searchText.isEmpty()) {
			searchLinks = persistenceGatewayImpl.getAllLinks();
		} else {
			searchLinks = persistenceGatewayImpl.searchLinks(PersistenceGateway.LinkProperty.NAME, mySearchText);
		}

		Set<SearchHit> result = new HashSet<SearchHit>();
		for (Link link : searchLinks) {
			List<Tag> tagsForLink = persistenceGatewayImpl.getTagsForLink(link.getUuid());
			SearchHit hit = new SearchHit(link, 0.0, tagsForLink);
			result.add(hit);
		}

		LOGGER.debug("Returning " + result.size() + " results for search text '" + searchText + "'");
		return result;
	}

}
