package de.lander.link.gui.logic;

import java.util.Comparator;

public class SearchHitComparator implements Comparator<SearchHit> {

	public static SearchHitComparator get() {
		return new SearchHitComparator();
	}
	
	@Override
	public int compare(SearchHit h1, SearchHit h2) {
		// 1) Sort by score
		int scoreCompare = Double.compare(h1.getScore(), h2.getScore());
		if (scoreCompare != 0) {
			return scoreCompare;
		}

		// 2) Sort by Name
		int nameCompare = h1.getLink().getName().compareTo(h2.getLink().getName());
		if (nameCompare != 0) {
			return nameCompare;
		}

		// 3) Sort by UUID
		return h1.getLink().getUuid().compareTo(h2.getLink().getUuid());
	}
}
