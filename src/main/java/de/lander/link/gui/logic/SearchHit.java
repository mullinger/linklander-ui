package de.lander.link.gui.logic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

public class SearchHit {

	private Link link;
	private double score = 0.0;
	private Set<Tag> tags = new HashSet<Tag>();
	
	public SearchHit(Link link, double score, Tag... tags) {
		this.link = link;
		this.score = score;
		this.tags.addAll(Arrays.asList(tags));
	}
	
	public SearchHit(Link link, double score, Collection<Tag> tags) {
		this.link = link;
		this.score = score;
		this.tags.addAll(tags);
	}
	
	public Link getLink() {
		return link;
	}
	public void setLink(Link link) {
		this.link = link;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public Set<Tag> getTags() {
		return tags;
	}
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	
}
