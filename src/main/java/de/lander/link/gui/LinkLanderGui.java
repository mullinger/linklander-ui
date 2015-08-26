package de.lander.link.gui;


import javax.inject.Inject;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.lander.link.gui.components.AdminComponent;
import de.lander.link.gui.components.SearchComponentUser;

@CDIUI("linklander-ui")
@Theme("mytheme")
@SuppressWarnings("serial")
public class LinkLanderGui extends UI {

	private VerticalLayout verticalLayout;
	private TabSheet tabs;

	@Inject
	private SearchComponentUser searchTab;
	
	@Inject 
	private AdminComponent adminTab;
	
	
	@Override
	protected void init(VaadinRequest request) {
		verticalLayout = new VerticalLayout();
		verticalLayout.setWidth("100.0%");

		// top-level component properties
		setContent(verticalLayout);
		
		tabs = new TabSheet();
		verticalLayout.addComponent(tabs);
		
		tabs.addTab(searchTab).setCaption("Search links");
		tabs.addTab(adminTab).setCaption("Admin");
	}

}
