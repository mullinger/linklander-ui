/**
 *
 */
package de.lander.persistence.daos;

import static scala.collection.JavaConversions.asJavaIterator;
import static scala.collection.JavaConversions.asJavaList;
import static scala.collection.JavaConversions.asJavaMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
public class PersistenceGatewayImpl implements PersistenceGateway, Relationships {

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
		registerShutdownHook(graphDb);
	}

	/**
	 * Creates the desired indexes and constraints
	 */
	private void createIndexesAndConstraints() {
		// NOTE: contraints add also an index
		// Name, Tag and Link Property should not need to be unique -> try to
		// find other unique properties
		// + ") ASSERT link." + Link.NAME + " IS UNIQUE");
		// this.cypher.execute("CREATE CONSTRAINT ON (link:" + Tag.LABEL
		// + ") ASSERT link." + Tag.NAME + " IS UNIQUE");
		// this.cypher.execute("CREATE INDEX ON :" + Link.LABEL + "("
		// + LinkProperty.URL + ")");
	}

	@Override
	public String addLink(final String name, final String url, final String title) {
		Validate.notBlank(name, "the name of the link is blank");
		Validate.notBlank(url, "the url of the link is blank");
		Validate.notNull(title, "the title of the link is null");

		String uuid = null;

		Node node;
		try (Transaction tx = this.graphDb.beginTx()) {
			node = this.graphDb.createNode();
			node.addLabel(Link.LABEL);
			node.setProperty(Link.NAME, name);
			node.setProperty(Link.URL, url);
			node.setProperty(Link.TITLE, title);
			node.setProperty(Link.CLICK_COUNT, 0);
			node.setProperty(Link.SCORE, 0);

			uuid = UUID.randomUUID().toString();
			node.setProperty(Link.UUID, uuid);
			tx.success();
			LOGGER.debug("Added link: name={}, url={}, title={}", new Object[] { name, url, title });
		} catch (ConstraintViolationException cve) {
			LOGGER.error(cve.getMessage(), cve);
			throw new IllegalArgumentException(String.format(
					"Error on creating link with name=%s, url=%s, title=%s, because=%s", name, url, title,
					cve.getMessage()));
		}

		return uuid;

	}

	@Override
	public void setLinkPropertyValue(final String linkUUID, final String property, final String value) {
		try (Transaction tx = this.graphDb.beginTx()) {
			Node link = getNodeByUUID(linkUUID);
			link.setProperty(property, value);
			tx.success();
		}
	}

	@Override
	public void updateLink(final LinkProperty property, final String propertyValue, final String newPropertyValue) {
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
				int oldClickCount = Integer.parseInt(String.valueOf(linkToUpdate.getProperty(Link.CLICK_COUNT)));
				int newClickCount = ++oldClickCount;
				internalNewPropertyValue = String.valueOf(newClickCount);
				linkToUpdate.setProperty(Link.CLICK_COUNT, newClickCount);
				break;
			case SCORE:
				Validate.isTrue(isDouble(newPropertyValue), "Score must be a double value");
				linkToUpdate.setProperty(Link.SCORE, newPropertyValue);
				break;
			default:
				throw new IllegalArgumentException("property={" + property + "} is not supported");
			}

			LOGGER.debug("Updated link: property={}, newValue={}", new Object[] { property, internalNewPropertyValue });
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
	private Node retrieveLinkByExactProperty(final LinkProperty property, final String propertyValue) {

		ResourceIterable<Node> links = null;
		switch (property) {
		case NAME:
		case CLICK_COUNT:
		case SCORE:
			links = this.graphDb.findNodesByLabelAndProperty(Link.LABEL, Link.NAME, propertyValue);
			break;
		case URL:
			links = this.graphDb.findNodesByLabelAndProperty(Link.LABEL, Link.URL, propertyValue);
			break;
		case UUID:
			links = this.graphDb.findNodesByLabelAndProperty(Link.LABEL, Link.UUID, propertyValue);
			break;
		default:
			throw new IllegalArgumentException("property={" + property + "} is not supported");
		}

		ResourceIterator<Node> iterator = links.iterator();
		if (iterator.hasNext()) {
			// NOTE: only first node will returned
			// there should only be one node with this searchable property!
			return iterator.next();
		} else {
			throw new IllegalArgumentException("no link node was found for property={" + property + "} and value={"
					+ propertyValue + "}");
		}
	}

	private Node getNodeByUUID(final String uuid) {
		String sql = new StringBuilder(128).append("MATCH (link:").append(Link.LABEL).append(") WHERE link.uuid  =~ '")
				.append(uuid).append("'").append(" RETURN link").toString();

		ExecutionResult execute = null;

		execute = this.cypher.execute(sql);
		Iterator<Node> links = asJavaIterator(execute.columnAs("link"));
		while (links.hasNext()) {
			Node link = links.next();
			return link;
		}
		return null;
	}

	/**
	 *
	 * @param uuid
	 * @return
	 */
	@Override
	public Link getLinkByUUID(final String uuid) {
		try (Transaction tx = this.graphDb.beginTx()) {
			return convert(getNodeByUUID(uuid));
		}
	}

	@Override
	public List<Link> getAllLinks() {
		List<Link> retrievedLinks = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (link:").append(Link.LABEL).append(")")
				.append(" RETURN link").toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			execute = this.cypher.execute(sql);

			Iterator<Node> links = asJavaIterator(execute.columnAs("link")); // from
																				// return
			// statement
			while (links.hasNext()) {
				Node link = links.next();
				retrievedLinks.add(convert(link));
			}
		}

		return retrievedLinks;
	}

	@Override
	public List<Tag> getAllTags() {
		List<Tag> retrievedTags = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (tag:").append(Tag.LABEL).append(")").append(" RETURN tag")
				.toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			execute = this.cypher.execute(sql);

			Iterator<Node> tags = asJavaIterator(execute.columnAs("tag")); // from
																			// return
			// statement
			while (tags.hasNext()) {
				Node tag = tags.next();
				retrievedTags.add(convertTag(tag));
			}
		}

		return retrievedTags;
	}

	private Tag convertTag(Node tag) {
		String name = String.valueOf(tag.getProperty(Tag.NAME));
		String description = String.valueOf(tag.getProperty(Tag.DESCRIPTION));
		int clicks = Integer.valueOf(String.valueOf(tag.getProperty(Tag.CLICK_COUNT)));
		String uuid = String.valueOf(tag.getProperty(Tag.UUID));

		return new Tag(name, description, clicks, uuid);
	}

	@Override
	public List<Link> searchLinks(final String value) {
		List<Link> retrievedLinks = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (link:").append(Link.LABEL)
				.append(") WHERE link.{Link.NAME}  =~ '(?i).*").append(value).append(".*'")
				.append(" OR link.{Link.URL}  =~ '(?i).*").append(value).append(".*'").append(" RETURN link")
				.toString();

		sql = sql.replace("{Link.URL}", Link.URL);
		sql = sql.replace("{Link.NAME}", Link.NAME);

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			execute = cypher.execute(sql);
			Iterator<Node> links = asJavaIterator(execute.columnAs("link")); // from
																				// return
			// statement
			while (links.hasNext()) {
				Node link = links.next();
				retrievedLinks.add(convert(link));
			}
		}

		return retrievedLinks;
	}

	@Override
	public List<Link> searchLinks(final LinkProperty property, final String propertyValue) {
		Validate.notNull(property);

		List<Link> retrievedLinks = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (link:").append(Link.LABEL)
				.append(") WHERE link.{property}  =~ '(?i).*")
				// (?i): Case
				// insensitive
				// matching
				.append(propertyValue).append(".*'").append(" RETURN link").toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				execute = this.cypher.execute(sql.replace("{property}", Link.NAME));
				break;
			case URL:
				execute = this.cypher.execute(sql.replace("{property}", Link.URL));
				break;
			default:
				throw new IllegalArgumentException("property '" + property.name() + "' is not supported");
			}

			Iterator<Node> links = asJavaIterator(execute.columnAs("link")); // from
																				// return
			// statement
			while (links.hasNext()) {
				Node link = links.next();
				retrievedLinks.add(convert(link));
			}
		}

		LOGGER.debug("Retrieved links: property={}, value={}", new Object[] { property, propertyValue });
		return retrievedLinks;
	}

	private Link convert(final Node linkNode) {
		String name = String.valueOf(linkNode.getProperty(Link.NAME));
		String title = String.valueOf(linkNode.getProperty(Link.TITLE));
		String url = String.valueOf(linkNode.getProperty(Link.URL));
		int clicks = Integer.valueOf(String.valueOf(linkNode.getProperty(Link.CLICK_COUNT)));
		double score = Double.valueOf(String.valueOf(linkNode.getProperty(Link.SCORE)));
		String uuid = String.valueOf(linkNode.getProperty(Link.UUID));

		return new Link(name, title, url, clicks, score, uuid);
	}

	@Override
	public void deleteLink(String uuid) {
		Validate.notBlank(uuid);

		String query = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			// step 1: build query
			query = "MATCH (link:" + Link.LABEL + " {" + Link.UUID + ":'" + uuid + "'}) DELETE link";

			this.cypher.execute(query);
			tx.success();
		}
	}

	@Override
	public void deleteLink(final LinkProperty property, final String propertyValue, final DeletionMode mode) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notNull(mode);

		String query = null;

		// step 1: build query
		switch (mode) {
		case EXACT:
			query = "MATCH (link:" + Link.LABEL + " {<property>: '" + propertyValue + "'}) DELETE link";
			break;
		case SOFT:
			query = new StringBuilder(128).append("MATCH (link:").append(Link.LABEL).append(")")
					.append("WHERE link.<property>  =~ '.*").append(propertyValue).append(".*'").append(" DELETE link")
					.toString();
			break;
		default:
			throw new IllegalArgumentException("Deletion mode={" + mode.name() + "} is not supported");
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
				throw new IllegalArgumentException("property '" + property.name() + "' is not supported");
			}

			LOGGER.debug("Delete Link query=\"{}\" for linkProperty={}, value={} and mode={}", new Object[] { query,
					property, propertyValue, mode });

			this.cypher.execute(query);
			tx.success();
		}
	}

	@Override
	public String addTag(final String name, final String description) {
		Validate.notBlank(name, "the name of the tag is blank");
		Validate.isTrue(description.length() <= 255, "the description is longer than 255 chars");

		String uuid = null;

		Node node;
		try (Transaction tx = this.graphDb.beginTx()) {
			node = this.graphDb.createNode();
			node.addLabel(Tag.LABEL);
			uuid = UUID.randomUUID().toString();
			node.setProperty(Tag.UUID, uuid);
			node.setProperty(Tag.NAME, name);
			node.setProperty(Tag.DESCRIPTION, description);
			node.setProperty(Tag.CLICK_COUNT, 0);
			LOGGER.debug("Added tag: name={}, description={}", new Object[] { name, description });
			tx.success();
		} catch (ConstraintViolationException cve) {
			LOGGER.error(cve.getMessage(), cve);
			throw new IllegalArgumentException(String.format(
					"Error on creating tag with name=%s, description=%s, because=%s", name, description,
					cve.getMessage()));
		}

		return uuid;
	}

	@Override
	public void updateTag(final TagProperty property, final String propertyValue, final String newPropertyValue) {
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
				int oldClickCount = Integer.parseInt(String.valueOf(tagToUpdate.getProperty(Tag.CLICK_COUNT)));
				int newClickCount = ++oldClickCount;
				internalNewPropertyValue = String.valueOf(newClickCount);
				tagToUpdate.setProperty(Tag.CLICK_COUNT, newClickCount);
				break;
			default:
				throw new IllegalArgumentException("property={" + property + "} is not supported");
			}

			LOGGER.debug("Updated tag: property={}, newValue={}", new Object[] { property, internalNewPropertyValue });
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
	private Node retrieveTagByExactProperty(final TagProperty property, final String propertyValue) {

		ResourceIterable<Node> links = null;
		switch (property) {
		case NAME:
		case CLICK_COUNT:
			links = this.graphDb.findNodesByLabelAndProperty(Tag.LABEL, Tag.NAME, propertyValue);
			break;
		default:
			throw new IllegalArgumentException("property={" + property + "} is not supported");
		}

		ResourceIterator<Node> iterator = links.iterator();
		if (iterator.hasNext()) {
			// NOTE: only first node will returned
			// there should only be one node with this searchable property!
			return iterator.next();
		} else {
			throw new IllegalArgumentException("no tag node was found for property={" + property + "} and value={"
					+ propertyValue + "}");
		}
	}

	@Override
	public Map<Tag, Set<Link>> searchLinksForTagName(final String tagName) {
		Validate.notNull(tagName);

		Map<Tag, Set<Link>> resultMap = new HashMap<Tag, Set<Link>>();

		//@formatter:off
		String sql = new StringBuilder(128).append("MATCH (tag:").append(Tag.LABEL)
				.append(") -[:TAGGED]-> (link:"+Link.LABEL+") WHERE tag." + Tag.NAME + "  =~ '(?i).*").append(tagName).append(".*'")
				.append(" RETURN  {t:tag, l:collect(link)} as hits")
				.toString();
		//@formatter:on

		ExecutionResult result = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			result = this.cypher.execute(sql);

			// Return the wrapper structure. It is useless, remove it somehow if
			// possible
			Iterator<scala.collection.Map<Node, scala.collection.immutable.List<Node>>> hits = asJavaIterator(result
					.columnAs("hits"));

			// The wrapper stucture contains the objects we are interested in
			while (hits.hasNext()) {
				// First we extract the 'map key' with label 't' -> see cypher
				// and convert it to TAG
				Map<Node, scala.collection.immutable.List<Node>> next = asJavaMap(hits.next());
				Tag tag = convertTag((Node) next.get("t"));

				// Then we extract the 'map value' with label 'l' -> see cypher
				// and convert it to set of LINK
				HashSet<Link> links = new HashSet<Link>();
				List<Node> linkJavaList = asJavaList(next.get("l"));
				for (Node node : linkJavaList) {
					Link link = convert(node);
					links.add(link);
				}
				resultMap.put(tag, links);
			}

		}

		return resultMap;
	}

	@Override
	public List<Tag> searchTags(final TagProperty property, final String propertyValue) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);

		List<Tag> retrievedTags = new ArrayList<>();

		String sql = new StringBuilder(128).append("MATCH (tag:").append(Tag.LABEL)
				.append(") WHERE tag.{property}  =~ '(?i).*").append(propertyValue).append(".*'").append(" RETURN tag")
				.toString();

		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				execute = this.cypher.execute(sql.replace("{property}", Tag.NAME));
				break;
			default:
				throw new IllegalArgumentException("property '" + property.name() + "' is not supported");
			}

			Iterator<Node> tags = asJavaIterator(execute.columnAs("tag")); // from
																			// return
			// statement
			while (tags.hasNext()) {
				Node tag = tags.next();
				String name = String.valueOf(tag.getProperty(Tag.NAME));
				String description = String.valueOf(tag.getProperty(Tag.DESCRIPTION));
				int clicks = Integer.valueOf(String.valueOf(tag.getProperty(Tag.CLICK_COUNT)));
				String uuid = (String) tag.getProperty(Tag.UUID);

				retrievedTags.add(new Tag(name, description, clicks, uuid));
			}
		}

		LOGGER.debug("Retrieved tags: property={}, value={}", new Object[] { property, propertyValue });
		return retrievedTags;
	}

	@Override
	public void deleteTag(final TagProperty property, final String propertyValue, final DeletionMode mode) {
		Validate.notNull(property);
		Validate.notBlank(propertyValue);
		Validate.notNull(mode);

		String query = null;

		// step 1: build query
		switch (mode) {
		case EXACT:
			query = "MATCH (tag:" + Tag.LABEL + " {<property>: '" + propertyValue + "'}) DELETE tag";
			break;
		case SOFT:
			query = new StringBuilder(128).append("MATCH (tag:").append(Tag.LABEL).append(")")
					.append("WHERE tag.<property>  =~ '.*").append(propertyValue).append(".*'").append(" DELETE tag")
					.toString();
			break;
		default:
			throw new IllegalArgumentException("Deletion mode={" + mode.name() + "} is not supported");
		}

		// step 2: replace variables and execute query
		try (Transaction tx = this.graphDb.beginTx()) {
			switch (property) {
			case NAME:
				query = query.replace("<property>", Tag.NAME);
				break;
			default:
				throw new IllegalArgumentException("property '" + property.name() + "' is not supported");
			}

			LOGGER.debug("Delete Tag query=\"{}\" for tagProperty={}, value={} and mode={}", new Object[] { query,
					property, propertyValue, mode });

			this.cypher.execute(query);
			tx.success();
		}
	}

	@Override
	public void addTagToLink(final String linkUUID, final String tagUUID) {
		Validate.notBlank(linkUUID);
		Validate.notBlank(tagUUID);

		try (Transaction tx = this.graphDb.beginTx()) {
			//@formatter:off
			String query = "MATCH "
					+ "(link:" + Link.LABEL + " {" + Link.UUID + ": '" + linkUUID + "'}), "
					+ "(tag:" + Tag.LABEL + " {" + Tag.UUID + ": '" + tagUUID + "'}) "
					+ "CREATE (tag)-[:" + TAGGED + "]->(link)";
			//@formatter:on
			// LOGGER.debug("Build Tagging query=\"{}\" for link='{}' and tag='{}'",
			// new Object[] { query, linkUUID,
			// tagUUID });
			this.cypher.execute(query);

			LOGGER.debug("Added tag to link: {}-[TAGGED]-{}", new Object[] { tagUUID, linkUUID });
			tx.success();
		}
	}

	@Override
	public void removeTagFromLink(final String linkUUID, final String tagUUID) {
		Validate.notBlank(linkUUID);
		Validate.notBlank(tagUUID);

		try (Transaction tx = this.graphDb.beginTx()) {
			//@formatter:off
			String query = "MATCH "
					+ "(link:" + Link.LABEL + " {" + Link.UUID + ": '" + linkUUID + "'})"
					+ "<-[t:TAGGED]-"
					+ "(tag:" + Tag.LABEL + " {" + Tag.UUID + ": '" + tagUUID + "'}) "
					+ "DELETE t";
			//@formatter:on

			LOGGER.debug("Build Tagging query=\"{}\" for link='{}' and tag='{}'", new Object[] { query, linkUUID,
					tagUUID });
			ExecutionResult executionResult = this.cypher.execute(query);

			LOGGER.debug(executionResult.dumpToString());
			LOGGER.debug("Removed tag from link: {}-[TAGGED]->{}", new Object[] { linkUUID, tagUUID });
			tx.success();
		}
	}

	/**
	 * Retrieves all tags for a link
	 *
	 * @param linkUUID
	 *            the uuid of the link
	 * @return the {@link Tag}s
	 */
	@Override
	public List<Tag> getTagsForLink(final String linkUUID) {
		Validate.notBlank(linkUUID);

		List<Tag> foundTags = new ArrayList<Tag>();
		ExecutionResult execute = null;
		try (Transaction tx = this.graphDb.beginTx()) {
			execute = this.cypher.execute("MATCH (:Link {uuid: '" + linkUUID + "'})<-[:" + TAGGED
					+ "]-(tag:Tag) RETURN tag");
			tx.success();

			Iterator<Node> tags = asJavaIterator(execute.columnAs("tag")); // from
																			// return
			// statement
			while (tags.hasNext()) {
				Node tag = tags.next();
				String name = String.valueOf(tag.getProperty(Tag.NAME));
				String description = String.valueOf(tag.getProperty(Tag.DESCRIPTION));
				int clicks = Integer.valueOf(String.valueOf(tag.getProperty(Link.CLICK_COUNT)));
				String uuid = (String) tag.getProperty(Tag.UUID);

				foundTags.add(new Tag(name, description, clicks, uuid));
			}
		}

		return foundTags;
	}

	@Override
	public void incrementLinkClick(final String linkUUID) {
		Node linkToUpdate;
		try (Transaction tx = this.graphDb.beginTx()) {
			linkToUpdate = retrieveLinkByExactProperty(LinkProperty.UUID, linkUUID);
			int oldClickCount = Integer.parseInt(String.valueOf(linkToUpdate.getProperty(Link.CLICK_COUNT)));
			int newClickCount = ++oldClickCount;
			linkToUpdate.setProperty(Link.CLICK_COUNT, newClickCount);

			LOGGER.debug("Increasing click count for link '" + linkUUID + "' to: " + newClickCount);
			tx.success();
		}
	}

	@Override
	public void updateLinkScore(final String linkName, final double newScore) {
		updateLink(LinkProperty.SCORE, linkName, String.valueOf(newScore));
	}

	@Override
	public void incrementTagClick(final String tagName) {
		updateTag(TagProperty.CLICK_COUNT, tagName, tagName);
	}

	/**
	 * Shutdown hook for the graphDb
	 *
	 * @param graphDb
	 *            the db to securely shutdown
	 */
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

}
