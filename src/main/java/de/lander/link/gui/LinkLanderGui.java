package de.lander.link.gui;


import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.lander.link.gui.components.SearchComponent;

@CDIUI("linklander-ui")
@Theme("mytheme")
@SuppressWarnings("serial")
public class LinkLanderGui extends UI {

	private VerticalLayout verticalLayout;


	@Override
	protected void init(VaadinRequest request) {
		verticalLayout = new VerticalLayout();
		verticalLayout.setWidth("100.0%");

//		// top-level component properties
		verticalLayout.addComponent(new SearchComponent());
		
		setContent(verticalLayout);
	}

}
