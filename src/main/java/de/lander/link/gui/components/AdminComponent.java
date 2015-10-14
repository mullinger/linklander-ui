package de.lander.link.gui.components;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import com.vaadin.cdi.UIScoped;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.lander.persistence.daos.PersistenceGateway;

@UIScoped
public class AdminComponent extends CustomComponent {

	private static final long serialVersionUID = 3560070101035371324L;

	private VerticalLayout verticalLayout;

	@Inject
	private PersistenceGateway persistence;

	@Inject
	private Logger LOGGER;

	@Inject
	private SearchComponentAdmin search;

	public AdminComponent() {
		buildLayout();
	}

	@PostConstruct
	private void postConstruct() {
		// Search Component
		verticalLayout.addComponent(search);
	}

	private void buildLayout() {
		verticalLayout = new VerticalLayout();

		// Add Link Button
		Button addLinkButton = new Button("Add link");
		addLinkButton.addClickListener(event -> {
			EditLinkWindow editLinkWindow = new EditLinkWindow(persistence);
			UI.getCurrent().addWindow(editLinkWindow);
		});
		verticalLayout.addComponent(addLinkButton);

		// Add Tag Button
		Button addTagButton = new Button("Add tag");
		addTagButton.addClickListener(event -> {
			EditTagWindow editTagWindow = new EditTagWindow(persistence);
			UI.getCurrent().addWindow(editTagWindow);
		});
		verticalLayout.addComponent(addTagButton);

		setCompositionRoot(verticalLayout);
	}
}
