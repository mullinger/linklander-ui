package de.lander.link.gui.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.lander.persistence.daos.PersistenceGateway;

public class EditLinkWindow extends Window {

	private static final long serialVersionUID = -7011083399408367324L;

	private PersistenceGateway persistence;

	private String existingLinkId;

	private TextField nameField;

	private TextField urlField;

	private Button saveButton;

	/**
	 * Open the window to create a new link
	 */
	public EditLinkWindow(PersistenceGateway persistence) {
		this(persistence, null);
	}
	
	/**
	 * Open a window to create a new link or edit an existing link
	 * 
	 * @param existingLinkId id of the link to edit. Null if new link should be added
	 */
	public EditLinkWindow(PersistenceGateway persistence, String existingLinkId) {
		super(existingLinkId == null ? "Add link" : "Edit link");
		this.persistence = persistence;

		this.existingLinkId = existingLinkId;
		center();
		setModal(true);

		buildLayout();
		if (existingLinkId != null) {
			loadData();
		}
	}


	private void buildLayout() {
		GridLayout root = new GridLayout(4, 4);
		setContent(root);

		nameField = new TextField("Name");
		root.addComponent(nameField, 0, 0);
		urlField = new TextField("URL");
		root.addComponent(urlField, 1, 0);
		saveButton = new Button("Save");
		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8608246641439095241L;
			@Override
			public void buttonClick(ClickEvent event) {
				saveLink();
			}
		});
		root.addComponent(saveButton, 2, 0);
	}

	private void loadData() {
		
	}

	private void saveLink() {
		persistence.addLink(nameField.getValue(), urlField.getValue(), "optional!");
		close();
	}

	public PersistenceGateway getPersistence() {
		return persistence;
	}

	public void setPersistence(PersistenceGateway persistence) {
		this.persistence = persistence;
	}
}
