package de.lander.link.gui.components;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;

/**
 * Base class for all search components Offers input field, result table,
 * immediate search, and customizable actions for each link
 * 
 * @author max
 *
 */
public abstract class SearchComponentBase extends CustomComponent {

	private static final long serialVersionUID = 242479147429347833L;

	private VerticalLayout verticalLayout;
	@Inject
	private Logger LOGGER;

	/*
	 * Protected fields
	 */
	protected Table links;
	protected TextField input;
	@Inject
	protected PersistenceGateway persistenceGatewayImpl;

	@PostConstruct
	public void postConstruct() {
		// TODO: remove when no more testdata is needed
		persistenceGatewayImpl.addLink("Name", "http://name.de", "name");
		// Initialize table to show all links
		performSearch("");
	}

	/**
	 * Return the internal component names for the implementation specific link
	 * actions/components in the order they are to be added to the table row
	 * from left to right
	 * 
	 * @return
	 */
	protected abstract List<String> getLinkComponentNames();

	public SearchComponentBase() {
		buildLayout();
		addListeners();
	}

	private void addListeners() {
		// Search with each event
		input.addTextChangeListener(event -> {
			this.performSearch(event.getText());
		});
	}

	private void buildLayout() {
		verticalLayout = new VerticalLayout();
		verticalLayout.setWidth("100.0%");

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// input
		input = new TextField();
		input.setImmediate(true);
		input.setWidth("100.0%");
		input.setHeight("-1px");
		verticalLayout.addComponent(input);

		// links
		links = new Table();
		links.setImmediate(false);
		links.setWidth("100.0%");
		// Important to avoid "the thin grey table bottom line"
		links.setPageLength(0);
		links.setVisible(false);
		verticalLayout.addComponent(links);

		setCompositionRoot(verticalLayout);

		// Search bar
		input.focus();
		input.setInputPrompt("type to land a link...");
		input.setTextChangeEventMode(TextChangeEventMode.LAZY);

		// Result table
		links.addContainerProperty("id", String.class, null);
		links.addContainerProperty("name", String.class, null);
		links.addContainerProperty("link", Component.class, null);

		List<String> linkButtonComponentNames = getLinkComponentNames();
		for (String componentName : linkButtonComponentNames) {
			links.addContainerProperty(componentName, Component.class, null);
		}

		links.setWidth("100%");
		// TODO: Work on the column width/scaling
		links.setColumnWidth("id", 400);

		links.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		links.setFooterVisible(false);
		links.setVisible(true);

	}

	protected void performSearch(String searchText) {
		loadLinks(searchText.trim());
	}

	private void loadLinks(final String searchText) {
		links.removeAllItems();

		List<Link> searchLinks;
		if (searchText.isEmpty()) {
			searchLinks = persistenceGatewayImpl.getAllLinks();
		} else {
			searchLinks = persistenceGatewayImpl.searchLinks(PersistenceGateway.LinkProperty.NAME, searchText);
		}

		for (int i = 0; i < searchLinks.size(); i++) {
			Object[] tableValues = convertLinkToTableData(searchLinks.get(i));
			// Use the UUID for objectId, e.g. to delete the row later
			links.addItem(tableValues, searchLinks.get(i).getUuid());
		}
	}

	protected abstract List<Component> getLinkComponents(Link link);

	private Object[] convertLinkToTableData(Link link) {
		// Create a Vaading HTTP clickable link
		com.vaadin.ui.Link externalLink = new com.vaadin.ui.Link(link.getUrl(), new ExternalResource(link.getUrl()));
		externalLink.setTargetName("_blank"); // Open in new Tab, see docs

		// Get the additional components for each link -> See concrete
		// implementation
		List<Component> linkComponents = getLinkComponents(link);

		// Build Vaadin table object array
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(link.getUuid());
		list.add(link.getName());
		list.add(externalLink);

		list.addAll(linkComponents);

		return list.toArray();
	}

}
