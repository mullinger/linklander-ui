package de.lander.link.gui.components.tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Tag;

public class SelectTagWindow extends Window {

	private static final long serialVersionUID = -7011083399408367324L;

	private PersistenceGateway persistence;

	private final Set<String> initialSelectedTagUUIDs = new HashSet<String>();
	private final Set<String> selectedTagUUIDs = new HashSet<String>();

	private VerticalLayout verticalLayout;
	private Table tagTable;

	private Button saveButton;
	private Button cancelButton;

	/**
	 * Open the window to create a new link
	 */
	public SelectTagWindow(PersistenceGateway persistence) {
		this(persistence, new HashSet<String>());
	}

	/**
	 * Open a window to create a new link or edit an existing link
	 * 
	 * @param existingTagId
	 *            id of the link to edit. Null if new link should be added
	 */
	public SelectTagWindow(PersistenceGateway persistence, Set<String> selectedTagUUIDs) {
		this.persistence = persistence;

		center();
		setModal(true);

		setWidth("50%");
		setHeight("80%");

		if (selectedTagUUIDs != null) {
			this.initialSelectedTagUUIDs.addAll(selectedTagUUIDs);
			this.selectedTagUUIDs.addAll(selectedTagUUIDs);
		}

		buildLayout();
		loadData();
	}

	private void buildLayout() {
		verticalLayout = new VerticalLayout();
		verticalLayout.setWidth("100.0%");
		setContent(verticalLayout);

		// Table
		tagTable = new Table();

		tagTable.setColumnCollapsingAllowed(true);
		tagTable.addContainerProperty("id", String.class, null);
		tagTable.setColumnCollapsed("id", true);
		tagTable.addContainerProperty("name", String.class, null);
		tagTable.addContainerProperty("description", String.class, null);
		tagTable.addContainerProperty("isSelected", Component.class, null);

		tagTable.setWidth("100%");
		verticalLayout.addComponent(tagTable);

		// Button to okay
		saveButton = new Button("Save");
		saveButton.addClickListener(event -> {
			close();
		});
		verticalLayout.addComponent(saveButton);

		// Button to cancel
		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> {
			this.selectedTagUUIDs.clear();
			this.selectedTagUUIDs.addAll(initialSelectedTagUUIDs);
			close();
		});
		verticalLayout.addComponent(cancelButton);
	}

	private void loadData() {
		List<Tag> allTags = persistence.getAllTags();

		for (Tag tag : allTags) {
			Object[] tableRow = convertTagToTableRow(tag);
			tagTable.addItem(tableRow, tag.getUuid());
		}
	}

	private Object[] convertTagToTableRow(Tag tag) {
		String uuid = tag.getUuid();
		String name = tag.getName();
		String description = tag.getDescription();

		CheckBox isSelected = new CheckBox();
		if (selectedTagUUIDs.contains(uuid)) {
			isSelected.setValue(true);
		}
		isSelected.addValueChangeListener(event -> {
			if (isSelected.getValue()) {
				// Was activated -> Add to set
				selectedTagUUIDs.add(uuid);
			} else {
				// Was deactivated -> Remove from set
				selectedTagUUIDs.remove(uuid);
			}
		});

		return new Object[] { uuid, name, description, isSelected };
	}

	public Set<String> getSelectedTagUUIDs() {
		HashSet<String> result = new HashSet<String>();
		result.addAll(selectedTagUUIDs);

		return result;
	}

	public PersistenceGateway getPersistence() {
		return persistence;
	}

	public void setPersistence(PersistenceGateway persistence) {
		this.persistence = persistence;
	}
}
