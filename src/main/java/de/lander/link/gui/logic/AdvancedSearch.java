package de.lander.link.gui.logic;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

	public Set<SearchHit> performSearch(final String searchTextInput) {
		searchTextInput.hashCode(); // Simple NPE check
		String searchText = searchTextInput.trim();

		Set<String> tokens = analyzeSearchText(searchText);
		Set<SearchHit> result = new HashSet<SearchHit>();

		// Empty Search bar shows all links
		if (tokens.size() == 0) {
			List<Link> allLinks = persistenceGatewayImpl.getAllLinks();
			// Create a searchHit for each Link
			result = allLinks.stream().map(link -> new SearchHit(link, 1)).collect(Collectors.toSet());
		}

		// Otherwise perform search for each token
		for (String token : tokens) {
			// Step 1: search links
			List<Link> tokenLinks = persistenceGatewayImpl.searchLinks(token);
			for (Link link : tokenLinks) {
				boolean isAlreadyAdded = result.stream().anyMatch(s -> s.getLink().getUuid().equals(link.getUuid()));
				if (isAlreadyAdded) {
					SearchHit searchHit = result.stream().filter(s -> s.getLink().getUuid().equals(link.getUuid()))
							.findAny().get();
					searchHit.setScore(searchHit.getScore() + 1.0);
				} else {
					SearchHit searchHit = new SearchHit(link, 1.0);
					result.add(searchHit);
				}
			}
			// Step 2: Search tags
			Map<Tag, Set<Link>> linksForTags = persistenceGatewayImpl.searchLinksForTagName(token);
			for (Entry<Tag, Set<Link>> linksForTag : linksForTags.entrySet()) {
				for (Link link : linksForTag.getValue()) {
					boolean isAlreadyAdded = result.stream()
							.anyMatch(s -> s.getLink().getUuid().equals(link.getUuid()));
					if (isAlreadyAdded) {
						SearchHit searchHit = result.stream().filter(s -> s.getLink().getUuid().equals(link.getUuid()))
								.findAny().get();
						searchHit.setScore(searchHit.getScore() + 1.0);
					} else {
						SearchHit searchHit = new SearchHit(link, 1.0);
						result.add(searchHit);
					}
				}
			}
		}

		// Final step: Load tags for all the links
		for (SearchHit hit : result) {
			List<Tag> tagsForLink = persistenceGatewayImpl.getTagsForLink(hit.getLink().getUuid());
			hit.addTags(tagsForLink);
		}

		LOGGER.debug("Returning " + result.size() + " results for search text '" + searchText + "'");
		return result;
	}

	private Set<String> analyzeSearchText(String searchText) {
		Set<String> result = new HashSet<String>();

		String[] split = searchText.split("\\s+"); // Split on whitecase characters
		for (String string : split) {
			result.add(string);
		}
		
		return result;
	}

}
