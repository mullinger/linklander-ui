package de.lander.link.gui.components;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import com.vaadin.cdi.UIScoped;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Table.ColumnHeaderMode;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;

@UIScoped
public class SearchComponent extends CustomComponent {

	private static final long serialVersionUID = 242479147429347833L;

	private VerticalLayout verticalLayout;
	private Table links;
	private TextField input;

	@Inject
	private Logger LOGGER;

	@Inject
	private PersistenceGateway persistenceGatewayImpl;

	@PostConstruct
	public void postConstruct() {
		persistenceGatewayImpl.addLink("Name", "http://name.de", "name");
		performSearch("");
	}

	public SearchComponent() {
		buildLayout();

		input.focus();
		input.setInputPrompt("type to land a link...");
		input.setTextChangeEventMode(TextChangeEventMode.LAZY);

		links.addContainerProperty("id", String.class, null);
		links.addContainerProperty("name", String.class, null);
		links.addContainerProperty("link", Component.class, null);
		links.addContainerProperty("edit", Component.class, null);
		links.addContainerProperty("delete", Component.class, null);

		links.setWidth("100%");
		// TODO: Work on the column width/scaling
		links.setColumnWidth("id", 400);

		links.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		links.setFooterVisible(false);
		links.setVisible(true);

		input.addTextChangeListener(event -> {
			this.performSearch(event.getText());
		});
		
	}

	private void performSearch(String searchText) {
		loadLinks(searchText.trim());
	}

	private void loadLinks(final String searchText) {
		links.removeAllItems();

		List<Link> searchLinks;
		if (searchText.isEmpty()) {
			searchLinks = persistenceGatewayImpl.getAllLinks();
		} else {
			LOGGER.trace("Searching for:" + searchText);
			searchLinks = persistenceGatewayImpl.searchLinks(PersistenceGateway.LinkProperty.NAME, searchText);
		}

		LOGGER.error("Found " + searchLinks.size() + " links");

		for (int i = 0; i < searchLinks.size(); i++) {
			LOGGER.error("Found link " + i + " " + searchLinks.get(i).toString());
			links.addItem(convert(searchLinks.get(i)), i);
		}
	}

	private Object[] convert(Link link) {
		// Create a Vaading HTTP clickable link
		com.vaadin.ui.Link externalLink = new com.vaadin.ui.Link(link.getUrl(), new ExternalResource(link.getUrl()));
		externalLink.setTargetName("_blank"); // Open in new Tab

		// Edit Button
		Button editButton = new Button();
		editButton.addClickListener(event -> {
			EditLinkWindow editLinkWindow = new EditLinkWindow(persistenceGatewayImpl, link.getUuid());
			editLinkWindow.setSaveCallback(in -> performSearch(input.getValue()));
			UI.getCurrent().addWindow(editLinkWindow);
		});
		editButton.setCaption("edit");

		// Delete button
		Button deleteButton = new Button();
		deleteButton.addClickListener(event -> {
			persistenceGatewayImpl.deleteLink(link.getUuid());
			performSearch(input.getValue());
		});
		deleteButton.setCaption("delete");

		// Create the table row object array
		return new Object[] { link.getUuid(), link.getName(), externalLink, editButton, deleteButton };
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
	}
}
