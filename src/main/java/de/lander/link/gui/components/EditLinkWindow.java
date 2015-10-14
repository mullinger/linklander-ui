package de.lander.link.gui.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.lander.link.gui.components.tag.SelectTagWindow;
import de.lander.persistence.daos.PersistenceGateway;
import de.lander.persistence.entities.Link;
import de.lander.persistence.entities.Tag;

public class EditLinkWindow extends Window {

	private static final long serialVersionUID = -7011083399408367324L;

	private PersistenceGateway persistence;

	private final String existingLinkId;

	private TextField nameField;

	private TextField urlField;

	private Button editTagsButtong;
	private Set<String> addedTags = new HashSet<String>();
	private Set<String> removedTags = new HashSet<String>();
	private SelectTagWindow selectTagWindow = null;

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

		// Edit Tags button
		editTagsButtong = new Button("Edit Tags");
		editTagsButtong.addClickListener(event -> {
			editTags();
		});
		root.addComponent(editTagsButtong);

		// Save Button
		saveButton = new Button("Save");
		saveButton.addClickListener(event -> {
			saveLink();
		});
		root.addComponent(saveButton);
	}

	private void editTags() {
		Set<String> previousUuidSet = new HashSet<String>();
		if (existingLinkId == null) {
			// New Link -> No Tags preselected
			selectTagWindow = new SelectTagWindow(persistence, addedTags);
		} else {
			// Existing Link -> Load tags
			List<Tag> tagsForLink = persistence.getTagsForLink(existingLinkId);
			previousUuidSet.addAll(tagsForLink.stream().map(t -> t.getUuid()).collect(Collectors.toSet()));

			// Calculate the current (possibly not ye persisted) selection of
			// tags
			HashSet<String> currentSelection = new HashSet<String>();
			currentSelection.addAll(previousUuidSet);
			currentSelection.addAll(addedTags);
			currentSelection.removeAll(removedTags);

			selectTagWindow = new SelectTagWindow(persistence, currentSelection);
		}

		selectTagWindow.addCloseListener(event -> {
			/*
			 * Store the changed tags in this instance of editLinkWindow. if the
			 * user cancels we just ignore the changes if the user saves we
			 * store the changed tags in he persistence.
			 */
			Set<String> selectedTagUUIDs = selectTagWindow.getSelectedTagUUIDs();

			// Added Tags:
				this.addedTags = new HashSet<String>();
				this.addedTags.addAll(selectedTagUUIDs);
				this.addedTags.removeAll(previousUuidSet);

				// Removed tags
				this.removedTags = new HashSet<String>();
				this.removedTags.addAll(previousUuidSet);
				this.removedTags.removeAll(selectedTagUUIDs);
			});

		// Open the window
		UI.getCurrent().addWindow(selectTagWindow);

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
			// New Link
			String linkUUID = persistence.addLink(nameField.getValue(), urlField.getValue(), titleField.getValue());
			for (String tagUUID : addedTags) {
				persistence.addTagToLink(linkUUID, tagUUID);
			}
		} else {
			// Update Link
			persistence.setLinkPropertyValue(existingLinkId, Link.NAME, nameField.getValue());
			persistence.setLinkPropertyValue(existingLinkId, Link.URL, urlField.getValue());
			persistence.setLinkPropertyValue(existingLinkId, Link.TITLE, titleField.getValue());

			for (String tagUUID : addedTags) {
				persistence.addTagToLink(existingLinkId, tagUUID);
			}
			for (String tagUUID : removedTags) {
				persistence.removeTagFromLink(existingLinkId, tagUUID);
			}
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
