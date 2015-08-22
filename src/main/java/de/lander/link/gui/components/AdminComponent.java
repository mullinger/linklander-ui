package de.lander.link.gui.components;

import javax.inject.Inject;

import com.vaadin.cdi.UIScoped;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import de.lander.persistence.daos.PersistenceGateway;

@UIScoped
public class AdminComponent extends CustomComponent {

	private static final long serialVersionUID = 3560070101035371324L;

	private VerticalLayout verticalLayout;

	@Inject
	private PersistenceGateway persistence;
	
	public AdminComponent() {
		buildLayout();
	}


	private void buildLayout() {
		verticalLayout = new VerticalLayout();
		
		// Add Link Button
		Button addLinkButton = new Button("Add link");
		addLinkButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 12356126124L;
			@Override
			public void buttonClick(ClickEvent event) {
				EditLinkWindow editLinkWindow = new EditLinkWindow(persistence);
				
				UI.getCurrent().addWindow(editLinkWindow);
			}
		});
		verticalLayout.addComponent(addLinkButton);
		
		// Add Tag Button
		Button addTagButton = new Button("Add tag");
		addTagButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 12356126124L;
			@Override
			public void buttonClick(ClickEvent event) {
				EditTagWindow editTagWindow = new EditTagWindow(persistence);
				
				UI.getCurrent().addWindow(editTagWindow);
			}
		});
		verticalLayout.addComponent(addTagButton);
		
		setCompositionRoot(verticalLayout);
	}
}
