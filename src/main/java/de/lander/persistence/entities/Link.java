/**
 *
 */
package de.lander.persistence.entities;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

/**
 * Holds attributes for a link
 *
 * @author mvogel
 *
 */
public class Link {

    public static final Label LABEL = DynamicLabel.label("Link");
    public static final String NAME = "name";
    public static final String TITLE = "title";
    public static final String URL = "url";
    public static final String CLICK_COUNT = "clicks";
    public static final String SCORE = "score";
    public static final String UUID = "uuid";

    private final String name;
    private final String title;
    private final String url;
    private final String uuid;
    private final int clicks;
    private final double score;

    public Link(final String name, final String title, final String url, final int clicks, final double score, final String uuid) {
        this.name = name;
        this.title = title;
        this.url = url;
        this.clicks = clicks;
        this.score = score;
		this.uuid = uuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the clicks
     */
    public int getClicks() {
        return clicks;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Link [name=").append(name).append(", title=").append(title).append(", url=").append(url)
                .append(", clicks=").append(clicks).append(", score=").append(score).append("]");
        return builder.toString();
    }

	public String getUuid() {
		return uuid;
	}
}
