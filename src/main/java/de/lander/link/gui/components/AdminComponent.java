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
		
		verticalLayout.addComponent(new Label("TTTTEEEEESSSSTTT"));
		
		Button addButton = new Button("Add link");
		addButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 12356126124L;
			@Override
			public void buttonClick(ClickEvent event) {
//				EditLinkWindow editLinkWindow = new EditLinkWindow(persistence, "decbd1d4-61a5-4ed0-b962-8471ca5ce785");
				EditLinkWindow editLinkWindow = new EditLinkWindow(persistence);
				
				UI.getCurrent().addWindow(editLinkWindow);
			}
		});
		verticalLayout.addComponent(addButton);
		
		setCompositionRoot(verticalLayout);
	}
}
