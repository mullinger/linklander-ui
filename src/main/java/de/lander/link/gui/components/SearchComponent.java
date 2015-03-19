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

		links.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		links.setFooterVisible(false);

		input.addTextChangeListener(event -> {
			if (event.getText().equals("")) {
				links.setVisible(false);
				links.removeAllItems();
			} else {
				loadLinks(event.getText());
				links.setVisible(true);
			}
		});

	}
	
	
	private void performSearch() {
		if (input.getValue().equals("")) {
			links.setVisible(false);
			links.removeAllItems();
		} else {
			loadLinks(input.getValue());
			links.setVisible(true);
		}
	}
	

	private void loadLinks(final String searchText) {
		links.removeAllItems();

		List<Link> searchLinks = persistenceGatewayImpl.searchLinks(PersistenceGateway.LinkProperty.NAME, searchText);
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
			editLinkWindow.setSaveCallback(in->performSearch());
			UI.getCurrent().addWindow(editLinkWindow);
		});
		editButton.setCaption("edit");
		
		// Create the table row object array
		return new Object[] { link.getUuid(), link.getName(), externalLink, editButton};
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
