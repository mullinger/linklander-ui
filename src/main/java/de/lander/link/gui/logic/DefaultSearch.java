package de.lander.link.gui.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

public class DefaultSearch implements SearchProvider {

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

		return result;
	}

}
