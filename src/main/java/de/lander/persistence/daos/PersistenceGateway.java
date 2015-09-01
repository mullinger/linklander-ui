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
		NAME, URL, CLICK_COUNT, SCORE, UUID;
	}

	/**
	 * Supported Tag Types CRUD operations
	 */
	enum TagProperty {
		NAME, CLICK_COUNT, UUID;
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
	 * @return uuid of added link
	 */
	String addLink(String name, String url, String title);

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

	/**
	 * Delete the link specified by the given unique identifier.
	 * 
	 * @param uuid
	 */
	void deleteLink(String uuid);

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
	 * @return uuid of added tag
	 */
	String addTag(String name, String description);

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
	 * Tags a link with the given linkUUID with a tag<br>
	 * 
	 * @param linkUUID
	 *            the UUID of the link to tag (MANDATORY)
	 * @param tagUUID
	 *            the UUID of the tag to tag the link with (MANDATORY)
	 */
	void addTagToLink(String linkUUID, String tagUUID);

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
	 * Get all links that are stored in the database
	 * @return
	 */
	List<Link> getAllLinks();

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
	
	/**
	 * Get all tags that are stored in the DB
	 * @return a list of all tags
	 */
	List<Tag> getAllTags();

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
	 * @param uuid
	 *            the uuid identifying the link
	 * @return the link object, or null if no link was found
	 */
	Link getLinkByUUID(String uuid);

	/**
	 * Set a single property of the link identified by the given UUID
	 * 
	 * @param linkUUID
	 *            the uuid identifying the link
	 * @param property
	 *            the property to update
	 * @param value
	 *            the value to set
	 */
	void setLinkPropertyValue(String linkUUID, String property, String value);

	//TODO: Write JDOC
	void removeTagFromLink(String linkUUID, String tagUUID);

}
