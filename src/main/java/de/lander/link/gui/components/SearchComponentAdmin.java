package de.lander.link.gui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import com.vaadin.cdi.UIScoped;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

import de.lander.link.gui.components.tag.SelectTagWindow;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

/**
 * Main user search component used in the main screen.
 * 
 * @author max
 *
 */
@UIScoped
public class SearchComponentAdmin extends SearchComponentBase {

	private static final long serialVersionUID = -7211735915211883666L;

	@Inject
	private Logger LOGGER;
	
	@Override
	public List<String> getLinkComponentNames() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("edit");
		list.add("delete");
		list.add("addTag");
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

		// addTag button
		Button addTagButton = new Button();
		addTagButton.addClickListener(event -> {
			String linkUUID = link.getUuid();
			List<Tag> tagsForLink = persistenceGatewayImpl.getTagsForLink(linkUUID);

			// Get a set of all Tag UUIDS for the current link
				Set<String> selectedTagUUIDs = tagsForLink.stream().map(t -> t.getUuid()).collect(Collectors.toSet());

				// Open a window for all tags, with the previously found tags
				// pre-selected
				SelectTagWindow selectTagWindow = new SelectTagWindow(persistenceGatewayImpl, selectedTagUUIDs);

				// When the tag-selection-window closes: Retrieve the new set of
				// tags for the link
				Set<String> newSelectedTagUUIDs = new HashSet<String>();
				selectTagWindow.addCloseListener(new Window.CloseListener() {
					private static final long serialVersionUID = 9097791817292585294L;

					@Override
					public void windowClose(CloseEvent e) {
						newSelectedTagUUIDs.addAll(selectTagWindow.getSelectedTagUUIDs());
						
						// Store modifications in the database
						
						//// 1) Add added Tags
						HashSet<String> addedTags = new HashSet<String>(newSelectedTagUUIDs);
						addedTags.removeAll(selectedTagUUIDs);
						LOGGER.trace("Link["+linkUUID+"] - Adding tags:"+addedTags);
						
						for (String addedTagUUID : addedTags) {
							persistenceGatewayImpl.addTagToLink(linkUUID, addedTagUUID);
						}
						
						//// 2) Remove removed tags
						HashSet<String> removedTags = new HashSet<String>(selectedTagUUIDs);
						removedTags.removeAll(newSelectedTagUUIDs);
						LOGGER.trace("Link["+linkUUID+"] - Removing tags:"+removedTags);
						
						for (String removedTagUUID : removedTags) {
							persistenceGatewayImpl.removeTagFromLink(linkUUID, removedTagUUID);
						}
					}
				});
				
				// Open the window
				UI.getCurrent().addWindow(selectTagWindow);
				
				
			});
		addTagButton.setCaption("Add tag");
		list.add(addTagButton);

		return list;
	}

}
