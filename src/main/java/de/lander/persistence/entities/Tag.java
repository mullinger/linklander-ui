/**
 *
 */
package de.lander.persistence.entities;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;


/**
 * Hold attributes for a tag
 *
 * @author mvogel
 *
 */
public final class Tag {

    public static final Label LABEL = DynamicLabel.label("Tag");
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CLICK_COUNT = "clicks";
    public static final String UUID = "uuid";
    

    private final String name;
    private final String description;
    private final int clicks;
    private final String uuid;

    public Tag(final String name, final String description, final int clicks, final String uuid) {
        this.name = name;
        this.description = description;
        this.clicks = clicks;
		this.uuid = uuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the clicks
     */
    public int getClicks() {
        return clicks;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tag [name=").append(name).append(", description=").append(description).append(", clicks=")
                .append(clicks).append("]");
        return builder.toString();
    }

	public String getUuid() {
		return uuid;
	}
}

