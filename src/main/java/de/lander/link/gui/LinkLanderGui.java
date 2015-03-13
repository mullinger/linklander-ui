package de.lander.link.gui;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;

@CDIUI("linklander-ui")
@Theme("mytheme")
@SuppressWarnings("serial")
public class LinkLanderGui extends UI {

	private VerticalLayout verticalLayout;
	private Table links;
	private TextField input;

	@Inject
	private Logger LOGGER;

	@Inject
	private PersistenceGateway persistenceGatewayImpl;

	// @WebServlet(value = "/*", asyncSupported = true)
	// @VaadinServletConfiguration(productionMode = false, ui =
	// LinkLanderGui.class, widgetset = "de.lander.link.gui.AppWidgetSet")
	// public static class Servlet extends VaadinServlet {
	// }

	@PostConstruct
	public void postConstruct() {
		persistenceGatewayImpl.addLink("Name", "http://name.de", "name");
	}

	@Override
	protected void init(VaadinRequest request) {
		buildLayout();

		input.focus();
		input.setInputPrompt("type to land a link...");
		input.setTextChangeEventMode(TextChangeEventMode.LAZY);

		links.addContainerProperty("links", String.class, null);
		links.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		links.setFooterVisible(false);

		input.addTextChangeListener(new TextChangeListener() {

			@Override
			public void textChange(final TextChangeEvent event) {
				if (event.getText().equals("")) {
					links.setVisible(false);
					links.removeAllItems();
				} else {
					loadLinks(event.getText());
					links.setVisible(true);
				}
			}

		});

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
//		return new Object[] { link.getTitle(), link.getName(), link.getUrl(), link.getClicks() };
		return new Object[] { link.getUrl()};
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

		setContent(verticalLayout);
	}

}
