package de.lander.link.gui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.vaadin.activelink.ActiveLink;
import org.vaadin.activelink.ActiveLink.LinkActivatedEvent;
import org.vaadin.activelink.ActiveLink.LinkActivatedListener;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

import de.lander.link.gui.logic.AdvancedSearch.Advanced;
import de.lander.link.gui.logic.SearchHit;
import de.lander.link.gui.logic.SearchHitComparator;
import de.lander.link.gui.logic.SearchProvider;
import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

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
	@Inject
	@Advanced
	private SearchProvider searchProvider;

	@PostConstruct
	public void postConstruct() {
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

		links.setColumnCollapsingAllowed(true);
		// Result table
		links.addContainerProperty("id", String.class, null);
		setColumnVisibility("id", false);
		links.addContainerProperty("name", String.class, null);
		links.addContainerProperty("link", Component.class, null);
		links.addContainerProperty("tags", String.class, null);
		links.addContainerProperty("clickCount", String.class, null);
		setColumnVisibility("clickCount", false);

		List<String> linkButtonComponentNames = getLinkComponentNames();
		for (String componentName : linkButtonComponentNames) {
			links.addContainerProperty(componentName, Component.class, null);
		}

		links.setWidth("100%");
		// TODO: Work on the column width/scaling

		links.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		links.setFooterVisible(false);
		links.setVisible(true);

	}
	
	protected void setColumnVisibility(String id, boolean isVisible) {
		links.setColumnCollapsible(id, true);
		links.setColumnCollapsed(id, !isVisible);
	}

	protected void doSearch() {
		performSearch(input.getValue());
	}

	protected void performSearch(String searchText) {
		Set<SearchHit> searchResult = searchProvider.performSearch(searchText);

		links.removeAllItems();

		List<SearchHit> sortedResult = searchResult.stream().sorted(SearchHitComparator.get())
				.collect(Collectors.toList());

		for (int i = 0; i < sortedResult.size(); i++) {
			Object[] tableValues = convertLinkToTableData(sortedResult.get(i).getLink(), sortedResult.get(i).getTags());
			// Use the UUID for objectId, e.g. to delete the row later
			links.addItem(tableValues, sortedResult.get(i).getLink().getUuid());
		}
	}

	protected abstract List<Component> getLinkComponents(Link link);

	private Object[] convertLinkToTableData(Link linkEntity, Collection<Tag> tagsForLink) {
		// Create a Vaading HTTP clickable link
		// ActiveLink externalLink = getActiveLinkLink(linkEntity);
		// Button externalLink = getButtonLink(linkEntity);
		Component externalLink = getLayoutLink(linkEntity);

		// Get the additional components for each link -> See concrete
		// implementation
		List<Component> linkComponents = getLinkComponents(linkEntity);

		// Build Vaadin table object array
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(linkEntity.getUuid());
		list.add(linkEntity.getName());
		list.add(externalLink);

		// Add the tags, sorted alphabetically
		List<String> tags = tagsForLink.stream().map(t -> t.getName()).collect(Collectors.toList());
		Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
		String output = "";
		for (int i = 0; i < tags.size(); i++) {
			if (i > 0) {
				output += ", ";
			}
			output += tags.get(i);
		}
		list.add(output);
		
		list.add(linkEntity.getClicks()+"");

		// Finally all components for the links
		list.addAll(linkComponents);

		return list.toArray();
	}

	private Component getLayoutLink(Link linkEntity) {
		com.vaadin.ui.Link externalLink = new com.vaadin.ui.Link();
		externalLink.setTargetName("_blank");
		externalLink.setResource(new ExternalResource(linkEntity.getUrl()));
		externalLink.setCaption(linkEntity.getUrl());
		externalLink.setData(linkEntity.getUuid());

		HorizontalLayout layout = new HorizontalLayout(externalLink);
		layout.addLayoutClickListener(event -> {
			HorizontalLayout source = (HorizontalLayout) event.getSource();
			com.vaadin.ui.Link sourceLink = (com.vaadin.ui.Link) source.getComponent(0);
			String linkUUID = (String) sourceLink.getData();
			try {
				persistenceGatewayImpl.incrementLinkClick(linkUUID);
			} catch (Exception e) {
				LOGGER.error("Error incrementing link click count for link " + linkUUID, e);
			}
		});

		return layout;
	}

	private ActiveLink getActiveLinkLink(Link linkEntity) {
		ActiveLink externalLink = new ActiveLink(linkEntity.getUrl(), new ExternalResource(linkEntity.getUrl()));
		externalLink.setTargetName("_blank"); // Open in new Tab, see docs
		externalLink.addListener(new LinkActivatedListener() {
			private static final long serialVersionUID = -748748321823194745L;

			@Override
			public void linkActivated(LinkActivatedEvent event) {
				System.out.println("ACTICATED");
				try {
					persistenceGatewayImpl.incrementLinkClick(linkEntity.getUuid());
					LOGGER.info(linkEntity.getUrl() + " -> " + linkEntity.getClicks());
				} catch (Exception e) {
					LOGGER.error(e);
					// Dont do anything, we dont care if click increase didnt
					// work. Just let user navigate
				}
			}
		});
		return externalLink;
	}

	private Button getButtonLink(Link linkEntity) {
		Button externalLink = new Button(linkEntity.getUrl());
		externalLink.setStyleName(BaseTheme.BUTTON_LINK);

		Resource res = new ExternalResource(linkEntity.getUrl());
		final ResourceReference rr = ResourceReference.create(res, links, "link");

		externalLink.addClickListener(event -> {
			Page.getCurrent().open(rr.getURL(), "");
			System.out.println("Link clicked!");
		});
		return externalLink;
	}
}
