package de.lander.link.gui.logic;

import java.util.Comparator;

public class SearchHitComparator implements Comparator<SearchHit> {

	public static SearchHitComparator get() {
		return new SearchHitComparator();
	}
	
	@Override
	public int compare(SearchHit h1, SearchHit h2) {
		// 1) Sort by score
		int scoreCompare = Double.compare(h2.getScore(), h1.getScore());
		if (scoreCompare != 0) {
			return scoreCompare;
		}
		
		// 2) Sort by clicks
		int clickCompare = h2.getLink().getClicks() - h1.getLink().getClicks();
		if (clickCompare != 0) {
			return clickCompare;
		}

		// 3) Sort by Name
		int nameCompare = h2.getLink().getName().compareTo(h1.getLink().getName());
		if (nameCompare != 0) {
			return nameCompare;
		}

		// 4) Sort by UUID
		return h2.getLink().getUuid().compareTo(h1.getLink().getUuid());
	}
}
