package de.lander.link.gui.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;

public class EditLinkWindow extends Window {

	private static final long serialVersionUID = -7011083399408367324L;

	private PersistenceGateway persistence;

	private final String existingLinkId;

	private TextField nameField;

	private TextField urlField;

	private Button saveButton;

	private TextField titleField;

	private java.util.function.Consumer<Object> saveCallback;

	/**
	 * Open the window to create a new link
	 */
	public EditLinkWindow(PersistenceGateway persistence) {
		this(persistence, null);
	}

	/**
	 * Open a window to create a new link or edit an existing link
	 * 
	 * @param existingLinkId
	 *            id of the link to edit. Null if new link should be added
	 */
	public EditLinkWindow(PersistenceGateway persistence, String existingLinkId) {
		super(existingLinkId == null ? "Add link" : "Edit link");
		this.persistence = persistence;

		this.existingLinkId = existingLinkId;
		center();
		setModal(true);

		setWidth("50%");
		setHeight("80%");

		buildLayout();
		initData();
	}

	private void buildLayout() {
		GridLayout root = new GridLayout(1, 4);
		root.setWidth("100%");
		setContent(root);

		// Name Field
		nameField = new TextField("Name");
		nameField.setWidth("100%");
		root.addComponent(nameField);

		// URL Field
		urlField = new TextField("URL");
		urlField.setWidth("100%");
		root.addComponent(urlField);

		// TItle Field
		titleField = new TextField("Title");
		titleField.setWidth("100%");
		root.addComponent(titleField);

		// Save Button
		saveButton = new Button("Save");
		saveButton.addClickListener(event -> {
			saveLink();
		});
		root.addComponent(saveButton);
	}

	private void initData() {
		if (existingLinkId != null) {
			// Edit existing link
			Link link = persistence.getLinkByUUID(existingLinkId);
			nameField.setValue(link.getName());
			urlField.setValue(link.getUrl());
			titleField.setValue(link.getTitle());
		} else {
			// New link
			urlField.setValue("http://");
		}

	}

	private void saveLink() {
		if (existingLinkId == null) {
			persistence.addLink(nameField.getValue(), urlField.getValue(), titleField.getValue());
		} else {
			persistence.setLinkPropertyValue(existingLinkId, Link.NAME, nameField.getValue());
			persistence.setLinkPropertyValue(existingLinkId, Link.URL, urlField.getValue());
			persistence.setLinkPropertyValue(existingLinkId, Link.TITLE, titleField.getValue());
		}

		if (saveCallback != null) {
			saveCallback.accept(existingLinkId);
		}

		close();
	}

	public PersistenceGateway getPersistence() {
		return persistence;
	}

	public void setPersistence(PersistenceGateway persistence) {
		this.persistence = persistence;
	}

	public java.util.function.Consumer<Object> getSaveCallback() {
		return saveCallback;
	}

	public void setSaveCallback(java.util.function.Consumer<Object> saveCallback) {
		this.saveCallback = saveCallback;
	}
}
