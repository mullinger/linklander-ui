/**
 *
 */
package de.lander.persistence.daos;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.util.StringLogger;

import scala.collection.Iterator;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Relationships;
import de.lander.persistence.entities.Tag;

/**
 * The persistence gateway for the linklander, providing all CRUD, search and
 * more operations
 *
 * @author mvogel
 *
 */
@Named
// makes: 'persistenceGatewayImpl' for bean name
public class PersistenceGatewayImpl implements PersistenceGateway,
		Relationships {

	@Inject
	public static transient Logger LOGGER;

	private final GraphDatabaseService graphDb;
	private final ExecutionEngine cypher;

	/**
	 * Creates a new AdminDao
	 *
	 * @param graphDb
	 *            the {@link GraphDatabaseService} to use
	 */
	@Inject
	public PersistenceGatewayImpl(final GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
		this.cypher = new ExecutionEngine(graphDb, StringLogger.DEV_NULL);
		createIndexesAndConstraints();
	}

	/**
	 * Creates the desired indexes and constraints
	 */
	private void createIndexesAndConstraints() {
		// NOTE: contraints add also an index
//		this.cypher.execute("CREATE CONSTRAINT ON (link:" + Link.LABEL
//				+ ") ASSERT link." + Link.NAME + " IS UNIQUE");
		this.cypher.execute("CREATE CONSTRAINT ON (link:" + Tag.LABEL
				+ ") ASSERT link." + Tag.NAME + " IS UNIQUE");
		this.cypher.execute("CREATE INDEX ON :" + Link.LABEL + "("
				+ LinkProperty.URL + ")");
	}

	@Override
	public void addLink(final String name, final String url, final String title) {
		Validate.notBlank(name, "the name of the link is blank");
		Validate.notBlank(url, "the url of the link is blank");
		Validate.notNull(title, "the title of the link is null");

		Node node;
		try (Transaction tx = this.graphDb.beginTx()) {
			node = this.graphDb.createNode();
			node.addLabel(Link.LABEL);
			node.setProperty(Link.NAME, name);
			node.setProperty(Link.URL, url);
			node.setProperty(Link.TITLE, title);
			node.setProperty(Link.CLICK_COUNT, 0);
			node.setProperty(Link.SCORE, 0);
			tx.success();
			LOGGER.debug("Added link: name={}, url={}, title={}", new Object[] {
					name, url, title });
		} catch (ConstraintViolationException cve) {
			LOGGER.error(cve.getMessage(), cve);
			throw new IllegalArgumentException(
					String.format(
							"Error on creating link with name=%s, url=%s, title=%s, because=%s",
							name, url, title, cve.getMessage()));
		}

	}

	@Override
	public void updateLink(final LinkProperty property,
			final String propertyValue, final String newPropertyValue) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notBlank(newPropertyValue);
		String internalNewPropertyValue = newPropertyValue; // for logging

		Node linkToUpdate;
		try (Transaction tx = this.graphDb.beginTx()) {
			linkToUpdate = retrieveLinkByExactProperty(property, propertyValue);
			switch (property) {
			case NAME:
				linkToUpdate.setProperty(Link.NAME, newPropertyValue);
				break;
			case URL:
				linkToUpdate.setProperty(Link.URL, newPropertyValue);
				break;
			case CLICK_COUNT:
				int oldClickCount = Integer.parseInt(String
						.valueOf(linkToUpdate.getProperty(Link.CLICK_COUNT)));
				int newClickCount = ++oldClickCount;
				internalNewPropertyValue = String.valueOf(newClickCount);
				linkToUpdate.setProperty(Link.CLICK_COUNT, newClickCount);
				break;
			case SCORE:
				Validate.isTrue(isDouble(newPropertyValue),
						"Score must be a double value");
				linkToUpdate.setProperty(Link.SCORE, newPropertyValue);
				break;
			default:
				throw new IllegalArgumentException("property={" + property
						+ "} is not supported");
			}

			LOGGER.debug("Updated link: property={}, newValue={}",
					new Object[] { property, internalNewPropertyValue });
			tx.success();
		}
	}

	/**
	 * Determines if a string value can be parsed into a double
	 *
	 * @param stringValue
	 *            the string value
	 * @return <code>true</code> if i can be parsed, <code>false</code> othewise
	 */
	private boolean isDouble(final String stringValue) {
		try {
			Double.parseDouble(stringValue);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Retrieves a link by matching the property exactly
	 *
	 * @param property
	 *            the property
	 * @param propertyValue
	 *            the value of the property
	 * @return the {@link Node} or <code>null</code> if no node was found
	 */
	private Node retrieveLinkByExactProperty(final LinkProperty property,
			final String propertyValue) {

		ResourceIterable<Node> links = null;
		switch (property) {
		case NAME:
		case CLICK_COUNT:
		case SCORE:
			links = this.graphDb.findNodesByLabelAndProperty(Link.LABEL,
					Link.NAME, propertyValue);
			break;
		case URL:
			links = this.graphDb.findNodesByLabelAndProperty(Link.LABEL,
					Link.URL, propertyValue);
			break;
		default:
			throw new IllegalArgumentException("property={" + property
					+ "} is not supported");
		}

		ResourceIterator<Node> iterator = links.iterator();
		if (iterator.hasNext()) {
			// NOTE: only first node will returned
			// there should only be one node with this searchable property!
			return iterator.next();
		} else {
			throw new IllegalArgumentException(
					"no link node was found for property={" + property
							+ "} and value={" + propertyValue + "}");
		}
	}

	@Override
	public List<Link> searchLinks(final LinkProperty property,
			final String propertyValue) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);

		List<Link> retrievedLinks = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (link:")
				.append(Link.LABEL).append(") WHERE link.{property}  =~ '(?i).*") //(?i): Case insensitive matching
				.append(propertyValue).append(".*'").append(" RETURN link")
				.toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				execute = this.cypher.execute(sql.replace("{property}",
						Link.NAME));
				break;
			case URL:
				execute = this.cypher.execute(sql.replace("{property}",
						Link.URL));
				break;
			default:
				throw new IllegalArgumentException("property '"
						+ property.name() + "' is not supported");
			}

			Iterator<Node> links = execute.columnAs("link"); // from return
																// statement
			while (links.hasNext()) {
				Node link = links.next();
				String name = String.valueOf(link.getProperty(Link.NAME));
				String title = String.valueOf(link.getProperty(Link.TITLE));
				String url = String.valueOf(link.getProperty(Link.URL));
				int clicks = Integer.valueOf(String.valueOf(link
						.getProperty(Link.CLICK_COUNT)));
				double score = Double.valueOf(String.valueOf(link
						.getProperty(Link.SCORE)));

				retrievedLinks.add(new Link(name, title, url, clicks, score));
			}
		}

		LOGGER.debug("Retrieved links: property={}, value={}", new Object[] {
				property, propertyValue });
		return retrievedLinks;
	}

	@Override
	public void deleteLink(final LinkProperty property,
			final String propertyValue, final DeletionMode mode) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notNull(mode);

		String query = null;

		// step 1: build query
		switch (mode) {
		case EXACT:
			query = "MATCH (link:" + Link.LABEL + " {<property>: '"
					+ propertyValue + "'}) DELETE link";
			break;
		case SOFT:
			query = new StringBuilder(128).append("MATCH (link:")
					.append(Link.LABEL).append(")")
					.append("WHERE link.<property>  =~ '.*")
					.append(propertyValue).append(".*'").append(" DELETE link")
					.toString();
			break;
		default:
			throw new IllegalArgumentException("Deletion mode={" + mode.name()
					+ "} is not supported");
		}

		// step 2: replace variables and execute query
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				query = query.replace("<property>", Link.NAME);
				break;
			case URL:
				query = query.replace("<property>", Link.URL);
				break;
			default:
				throw new IllegalArgumentException("property '"
						+ property.name() + "' is not supported");
			}

			LOGGER.debug(
					"Delete Link query=\"{}\" for linkProperty={}, value={} and mode={}",
					new Object[] { query, property, propertyValue, mode });

			this.cypher.execute(query);
			tx.success();
		}
	}

	@Override
	public void addTag(final String name, final String description) {
		Validate.notBlank(name, "the name of the tag is blank");
		Validate.notBlank(description, "the description of the tag is blank");
		Validate.isTrue(description.length() <= 255,
				"the description is longer than 255 chars");

		Node node;
		try (Transaction tx = this.graphDb.beginTx()) {
			node = this.graphDb.createNode();
			node.addLabel(Tag.LABEL);
			node.setProperty(Tag.NAME, name);
			node.setProperty(Tag.DESCRIPTION, description);
			node.setProperty(Tag.CLICK_COUNT, 0);
			LOGGER.debug("Added tag: name={}, description={}", new Object[] {
					name, description });
			tx.success();
		} catch (ConstraintViolationException cve) {
			LOGGER.error(cve.getMessage(), cve);
			throw new IllegalArgumentException(
					String.format(
							"Error on creating tag with name=%s, description=%s, because=%s",
							name, description, cve.getMessage()));
		}
	}

	@Override
	public void updateTag(final TagProperty property,
			final String propertyValue, final String newPropertyValue) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notBlank(newPropertyValue);
		String internalNewPropertyValue = newPropertyValue; // for logging

		Node tagToUpdate;
		try (Transaction tx = this.graphDb.beginTx()) {
			tagToUpdate = retrieveTagByExactProperty(property, propertyValue);
			switch (property) {
			case NAME:
				tagToUpdate.setProperty(Tag.NAME, newPropertyValue);
				break;
			case CLICK_COUNT:
				int oldClickCount = Integer.parseInt(String.valueOf(tagToUpdate
						.getProperty(Tag.CLICK_COUNT)));
				int newClickCount = ++oldClickCount;
				internalNewPropertyValue = String.valueOf(newClickCount);
				tagToUpdate.setProperty(Tag.CLICK_COUNT, newClickCount);
				break;
			default:
				throw new IllegalArgumentException("property={" + property
						+ "} is not supported");
			}

			LOGGER.debug("Updated tag: property={}, newValue={}", new Object[] {
					property, internalNewPropertyValue });
			tx.success();
		}
	}

	/**
	 * Retrieves a tag by matching the property exactly
	 *
	 * @param property
	 *            the property
	 * @param propertyValue
	 *            the value of the property
	 * @return the {@link Node} or <code>null</code> if no node was found
	 */
	private Node retrieveTagByExactProperty(final TagProperty property,
			final String propertyValue) {

		ResourceIterable<Node> links = null;
		switch (property) {
		case NAME:
		case CLICK_COUNT:
			links = this.graphDb.findNodesByLabelAndProperty(Tag.LABEL,
					Tag.NAME, propertyValue);
			break;
		default:
			throw new IllegalArgumentException("property={" + property
					+ "} is not supported");
		}

		ResourceIterator<Node> iterator = links.iterator();
		if (iterator.hasNext()) {
			// NOTE: only first node will returned
			// there should only be one node with this searchable property!
			return iterator.next();
		} else {
			throw new IllegalArgumentException(
					"no tag node was found for property={" + property
							+ "} and value={" + propertyValue + "}");
		}
	}

	@Override
	public List<Tag> searchTags(final TagProperty property,
			final String propertyValue) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);

		List<Tag> retrievedTags = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (tag:")
				.append(Tag.LABEL).append(") WHERE tag.{property}  =~ '.*")
				.append(propertyValue).append(".*'").append(" RETURN tag")
				.toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				execute = this.cypher.execute(sql.replace("{property}",
						Tag.NAME));
				break;
			default:
				throw new IllegalArgumentException("property '"
						+ property.name() + "' is not supported");
			}

			Iterator<Node> links = execute.columnAs("tag"); // from return
															// statement
			while (links.hasNext()) {
				Node link = links.next();
				String name = String.valueOf(link.getProperty(Tag.NAME));
				String description = String.valueOf(link
						.getProperty(Tag.DESCRIPTION));
				int clicks = Integer.valueOf(String.valueOf(link
						.getProperty(Link.CLICK_COUNT)));

				retrievedTags.add(new Tag(name, description, clicks));
			}
		}

		LOGGER.debug("Retrieved tags: property={}, value={}", new Object[] {
				property, propertyValue });
		return retrievedTags;
	}

	@Override
	public void deleteTag(final TagProperty property,
			final String propertyValue, final DeletionMode mode) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notNull(mode);

		String query = null;

		// step 1: build query
		switch (mode) {
		case EXACT:
			query = "MATCH (tag:" + Tag.LABEL + " {<property>: '"
					+ propertyValue + "'}) DELETE tag";
			break;
		case SOFT:
			query = new StringBuilder(128).append("MATCH (tag:")
					.append(Tag.LABEL).append(")")
					.append("WHERE tag.<property>  =~ '.*")
					.append(propertyValue).append(".*'").append(" DELETE tag")
					.toString();
			break;
		default:
			throw new IllegalArgumentException("Deletion mode={" + mode.name()
					+ "} is not supported");
		}

		// step 2: replace variables and execute query
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				query = query.replace("<property>", Tag.NAME);
				break;
			default:
				throw new IllegalArgumentException("property '"
						+ property.name() + "' is not supported");
			}

			LOGGER.debug(
					"Delete Tag query=\"{}\" for tagProperty={}, value={} and mode={}",
					new Object[] { query, property, propertyValue, mode });

			this.cypher.execute(query);
			tx.success();
		}
	}

	@Override
	public void addTagToLink(final String linkName, final String tagName) {
		Validate.notBlank(linkName);
		Validate.notBlank(tagName);

		// step 1: get existing tags
		List<Tag> existingTags = searchTags(TagProperty.NAME, tagName);
		List<Link> existingLinks = searchLinks(LinkProperty.NAME, linkName);

		try (Transaction tx = this.graphDb.beginTx()) {
			for (Tag existingTag : existingTags) {
				for (Link existingLink : existingLinks) {
					this.cypher.execute(buildTaggingQuery(
							existingLink.getName(), existingTag.getName()));
				}
			}

			LOGGER.debug("Added tag to link: {}-[TAGGED]-{}", new Object[] {
					tagName, linkName });
			tx.success();
		}
	}

	/**
	 * Builds a cypher tagging query for a link
	 *
	 * @param linkName
	 *            the name of the link
	 * @param tagName
	 *            the name of the tag
	 * @return the query to create the tagging relationship
	 */
	private String buildTaggingQuery(final String linkName, final String tagName) {
		String query = "MATCH "
				// link
				+ "(link:" + Link.LABEL + " {" + Link.NAME + ": '" + linkName
				+ "'}), "
				// tag
				+ "(tag:" + Tag.LABEL + " {" + Tag.NAME + ": '" + tagName
				+ "'}) "
				// relationship
				+ "CREATE (tag)-[:" + TAGGED + "]->(link)";

		LOGGER.debug("Build Tagging query=\"{}\" for link='{}' and tag='{}'",
				new Object[] { query, linkName, tagName });
		return query;
	}

	/**
	 * Retrieves all tags for a link
	 *
	 * @param linkName
	 *            the name of the link
	 * @return the {@link Tag}s
	 */
	@Override
	public List<Tag> getTagsForLink(final String linkName) {
		Validate.notBlank(linkName);

		List<Tag> foundTags = new ArrayList<Tag>();
		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			execute = this.cypher.execute("MATCH (:Link {name: '" + linkName
					+ "'})<-[:" + TAGGED + "]-(tag:Tag) RETURN tag");
			tx.success();

			Iterator<Node> tags = execute.columnAs("tag"); // from return
															// statement
			while (tags.hasNext()) {
				Node tag = tags.next();
				String name = String.valueOf(tag.getProperty(Tag.NAME));
				String description = String.valueOf(tag
						.getProperty(Tag.DESCRIPTION));
				int clicks = Integer.valueOf(String.valueOf(tag
						.getProperty(Link.CLICK_COUNT)));

				foundTags.add(new Tag(name, description, clicks));
			}
		}

		return foundTags;
	}

	@Override
	public void incrementLinkClick(final String linkName) {
		updateLink(LinkProperty.CLICK_COUNT, linkName, linkName);
	}

	@Override
	public void updateLinkScore(final String linkName, final double newScore) {
		updateLink(LinkProperty.SCORE, linkName, String.valueOf(newScore));
	}

	@Override
	public void incrementTagClick(final String tagName) {
		updateTag(TagProperty.CLICK_COUNT, tagName, tagName);
	}

	// /**
	// * Shutdown hook for the graphDb
	// *
	// * @param graphDb
	// * the db to securely shutdown
	// */
	// private static void registerShutdownHook(final GraphDatabaseService
	// graphDb) {
	// // Registers a shutdown hook for the Neo4j instance so that it
	// // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	// // running application).
	// Runtime.getRuntime().addShutdownHook(new Thread() {
	// @Override
	// public void run() {
	// graphDb.shutdown();
	// }
	// });
	// }
}
