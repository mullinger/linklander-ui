package de.lander.link.gui.logic;

import java.util.Set;


public interface SearchProvider {

	public Set<SearchHit> performSearch(String text);
}
