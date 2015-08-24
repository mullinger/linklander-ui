package de.lander.link.gui.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.lander.persistence.daos.PersistenceGateway;

public class EditTagWindow extends Window {

	private static final long serialVersionUID = -7011083399408367324L;

	private PersistenceGateway persistence;

	private String existingTagId;

	private TextField nameField;
	private TextField descriptionField;

	private Button saveButton;

	/**
	 * Open the window to create a new link
	 */
	public EditTagWindow(PersistenceGateway persistence) {
		this(persistence, null);
	}

	/**
	 * Open a window to create a new link or edit an existing link
	 * 
	 * @param existingTagId
	 *            id of the link to edit. Null if new link should be added
	 */
	public EditTagWindow(PersistenceGateway persistence, String existingTagId) {
		super(existingTagId == null ? "Add link" : "Edit link");
		this.persistence = persistence;

		this.existingTagId = existingTagId;
		center();
		setModal(true);

		setWidth("50%");
		setHeight("80%");

		buildLayout();
		if (existingTagId != null) {
			// loadData();
		}
	}

	private void buildLayout() {
		GridLayout root = new GridLayout(1, 4);
		root.setWidth("100%");
		setContent(root);

		nameField = new TextField("Name");
		nameField.setWidth("20%");
		root.addComponent(nameField);

		descriptionField = new TextField("Description");
		descriptionField.setWidth("80%");
		root.addComponent(descriptionField);

		saveButton = new Button("Save");
		saveButton.addClickListener(event -> {
			saveTag();
		});
		root.addComponent(saveButton);
	}

	// private void loadData() {
	// Link link = persistence.getLinkByUUID(existingLinkId);
	// nameField.setValue(link.getName());
	// urlField.setValue(link.getUrl());
	// titleField.setValue(link.getTitle());
	// }

	private void saveTag() {
		if (existingTagId == null) {
			persistence.addTag(nameField.getValue(), descriptionField.getValue());
		} else {
			// persistence.setLinkPropertyValue(existingLinkId, Link.NAME,
			// nameField.getValue());
			// persistence.setLinkPropertyValue(existingLinkId, Link.URL,
			// urlField.getValue());
			// persistence.setLinkPropertyValue(existingLinkId, Link.TITLE,
			// titleField.getValue());
		}

		close();
	}

	public PersistenceGateway getPersistence() {
		return persistence;
	}

	public void setPersistence(PersistenceGateway persistence) {
		this.persistence = persistence;
	}
}
