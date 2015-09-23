package de.lander.link.gui.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;

public class DefaultSearch implements SearchProvider {

	@Inject
	protected PersistenceGateway persistenceGatewayImpl;

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
			SearchHit hit = new SearchHit(link, 0.0);
			result.add(hit);
		}

		return result;
	}

}
