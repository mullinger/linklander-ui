/**
 *
 */
package de.lander.persistence.daos;

import java.util.List;

import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

/**
 * The Persistence Layer / Gateway
 *
 * @author mvogel
 *
 */
public interface PersistenceGateway {

	/**
	 * Supported Link Types for CRUD operations
	 */
	enum LinkProperty {
		NAME, URL, CLICK_COUNT, SCORE;
	}

	/**
	 * Supported Tag Types CRUD operations
	 */
	enum TagProperty {
		NAME, CLICK_COUNT;
	}

	/**
	 * The mode for deletions
	 */
	enum DeletionMode {
		/**
		 * The value of the property has to match exactly
		 */
		EXACT,

		/**
		 * The value of the property can also be a substring
		 */
		SOFT;
	}

	// /////////////
	// CRUD LINK
	// /////////////
	/**
	 * Adds a new link with the given properties<br>
	 * Initializes {@link Link#SCORE} and {@link Link#CLICK_COUNT} with 0
	 * 
	 * @param name
	 *            the name the link can be searched later (MANDATORY)
	 * @param url
	 *            the url (MANDATORY)
	 * @param title
	 *            the title of the weburl (Optional)
	 */
	void addLink(String name, String url, String title);

	/**
	 * Updates the given property of a link
	 * 
	 * @param property
	 *            the {@link LinkProperty} to update (MANDATORY)
	 * @param propertyValue
	 *            the value to search for. This value has to match exactly
	 *            (MANDATORY)
	 * @param newPropertyValue
	 *            the value to set the found property of the link (MANDATORY)
	 * 
	 * @throws {@link IllegalArgumentException} if no link with the property was
	 *         found
	 */
	void updateLink(LinkProperty property, String propertyValue, String newPropertyValue);

	/**
	 * Deletes the links matching the given property<br>
	 * Note: the propertyString can also be a substring!
	 * 
	 * @param property
	 *            the property of the link (MANDATORY)
	 * @param propertyValue
	 *            the value of the property (MANDATORY)
	 * @param mode
	 *            the mode of deletion
	 */
	void deleteLink(LinkProperty property, String propertyValue, DeletionMode mode);

	// /////////////
	// CRUD TAG
	// /////////////
	/**
	 * Adds a Tag with the given properties<br>
	 * Initializes {@link Tag#CLICK_COUNT} with 0
	 * 
	 * @param name
	 *            the name (MANDATORY)
	 * @param description
	 *            the description (MANDATORY with max 255 chars)
	 */
	void addTag(String name, String description);

	/**
	 * Updates the given property of a tag
	 * 
	 * @param property
	 *            the property to update (MANDATORY)
	 * @param propertyValue
	 *            the current property value. This value has to match exactly
	 *            (MANDATORY)
	 * @param newPropertyValue
	 *            the new property value (MANDATORY)
	 * @throws {@link IllegalArgumentException} if no tag with the property was
	 *         found
	 */
	void updateTag(TagProperty property, String propertyValue, String newPropertyValue);

	/**
	 * Deletes all the tags with the given property
	 * 
	 * @param property
	 *            the property (MANDATORY)
	 * @param propertyValue
	 *            the value of the property (MANDATORY)
	 * @param mode
	 *            the mode of deletion
	 */
	void deleteTag(TagProperty property, String propertyValue, DeletionMode mode);

	// /////////////
	// RELATIONS
	// /////////////
	/**
	 * Tags a link with the given linkName with a tag<br>
	 * NOTE: the parameters do not have to match exactly. They can also be
	 * substrings of existing names of tag or links
	 * 
	 * @param linkName
	 *            the name of the link to tag (MANDATORY)
	 * @param tagName
	 *            the name of the tag to tag the link with (MANDATORY)
	 */
	void addTagToLink(String linkName, String tagName);

	// /////////////
	// SEARCH
	// /////////////
	/**
	 * Retrieves a list of links matching the given property
	 * 
	 * @param property
	 *            the property of the link to search for (MANDATORY)
	 * @param propertyValue
	 *            the value of the property (MANDATORY)
	 * @return a list of matched links which will can be empty but never
	 *         <code>null</code>
	 */
	List<Link> searchLinks(LinkProperty property, String propertyValue);

	/**
	 * Retrieves {@link Tag}s with the given property
	 * 
	 * @param property
	 *            the property (MANDATORY)
	 * @param propertyValue
	 *            the value of the property (MANDATORY)
	 * @return a list of {@link Tag}s
	 */
	List<Tag> searchTags(TagProperty property, String propertyValue);

	// /////////////
	// MISC
	// /////////////
	/**
	 * Returns all tags for a link
	 * 
	 * @param linkName
	 *            the name of the link (MANDATORY)
	 * @return the tags or an empty list of tags if no such link was found
	 */
	List<Tag> getTagsForLink(final String linkName);

	// /////////////
	// CLICKS
	// /////////////
	/**
	 * Increments the link click count
	 * 
	 * @param linkName
	 *            the name of the link
	 * @throws {@link IllegalArgumentException} if there is no link with the
	 *         given name
	 */
	void incrementLinkClick(final String linkName);

	/**
	 * Updates the score of the link
	 * 
	 * @param linkName
	 *            the name of the link
	 * @param newScore
	 *            the score to update
	 * @throws {@link IllegalArgumentException} if there is no link with the
	 *         given name
	 */
	void updateLinkScore(final String linkName, final double newScore);

	/**
	 * Increments the tag click count
	 * 
	 * @param tagName
	 *            the name of the tag
	 * @throws {@link IllegalArgumentException} if there is no tag with the
	 *         given name
	 */
	void incrementTagClick(final String tagName);

	/**
	 * Return a single link by the unique link uuid
	 * 
	 * @param uuid the uuid identifying the link
	 * @return the link object, or null if no link was found
	 */
	Link getLinkByUUID(String uuid);

	/**
	 * Set a single property of the link identified by the given UUID
	 * @param linkUUID the uuid identifying the link
	 * @param property the property to update
	 * @param value the value to set
	 */
	void setLinkPropertyValue(String linkUUID, String property, String value);

	// /////////////
	// STATISTICS
	// /////////////

	// get number of all tags
	// get number of all links
	// get number of all taggings
	// get number average tags for a link
}
