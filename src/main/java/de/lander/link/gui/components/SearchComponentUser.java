package de.lander.link.gui.components;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.cdi.UIScoped;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import de.lander.persistence.entities.Link;

/**
 * Main user search component used in the main screen.
 * @author max
 *
 */
@UIScoped
public class SearchComponentUser extends SearchComponentBase {

	private static final long serialVersionUID = -7211735915211883666L;

	@Override
	public List<String> getLinkComponentNames() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("edit");
		list.add("delete");
		return list;
	}

	@Override
	protected List<Component> getLinkComponents(Link link) {
		ArrayList<Component> list = new ArrayList<Component>();

		// Edit Button
		Button editButton = new Button();
		editButton.addClickListener(event -> {
			EditLinkWindow editLinkWindow = new EditLinkWindow(persistenceGatewayImpl, link.getUuid());
			editLinkWindow.setSaveCallback(in -> performSearch(input.getValue()));
			UI.getCurrent().addWindow(editLinkWindow);
		});
		editButton.setCaption("edit");
		list.add(editButton);

		// Delete button
		Button deleteButton = new Button();
		deleteButton.addClickListener(event -> {
			persistenceGatewayImpl.deleteLink(link.getUuid());
			this.links.removeItem(link.getUuid());
		});
		deleteButton.setCaption("delete");
		list.add(deleteButton);

		return list;
	}

}
