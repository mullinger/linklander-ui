package de.lander.link.gui.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AdminComponent extends CustomComponent {

	private static final long serialVersionUID = 3560070101035371324L;

	private VerticalLayout verticalLayout;

	public AdminComponent() {
		buildLayout();
	}


	private void buildLayout() {
		verticalLayout = new VerticalLayout();
		
		verticalLayout.addComponent(new Label("TTTTEEEEESSSSTTT"));
		
		setCompositionRoot(verticalLayout);
	}
}
