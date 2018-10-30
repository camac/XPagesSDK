package org.openntf.xsp.sdk.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.jre.XPagesVMSetup;
import org.openntf.xsp.sdk.utils.CommonUtils;
import org.openntf.xsp.sdk.utils.StaticTextFieldEditor;
import org.openntf.xsp.sdk.utils.StringUtil;

public class XspPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private RadioGroupFieldEditor editorNotesStatus;
	private FileFieldEditor editorNotesNotesIniFile;
	private DirectoryFieldEditor editorNotesInstall;
	private DirectoryFieldEditor editorNotesData;
	private BooleanFieldEditor editorNotesAutoJre;
	
	private RadioGroupFieldEditor editorDominoStatus;
	private FileFieldEditor editorDominoNotesIniFile;
	private DirectoryFieldEditor editorDominoInstall;
	private DirectoryFieldEditor editorDominoData;
	private BooleanFieldEditor editorDominoAutoJre;

	// Here, the scenario is that the remote machine has a mapped drive connecting to a local directory.
	// For instance, in case the VM has "Z:\" mapped to my profile folder on Mac, RemoteJunction will be "Z:\" and
	// LocalJunction will be "/Users/UserName"
	private DirectoryFieldEditor editorLocalJunction;
	private StringFieldEditor editorRemoteJunction;
	
	private String notesStatus;
	private String dominoStatus;
	
	public XspPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench arg0) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for the OpenNTF XPages SDK for Eclipse");
	}

	@Override
	protected void createFieldEditors() {
	
		Composite parent = getFieldEditorParent();
		String[][] statusLabels = {
				{ "Disabled", XspPreferences.STATUS_DISABLED},
				{ "On This Computer", XspPreferences.STATUS_LOCAL},
				{ "On a Remote Computer/VM", XspPreferences.STATUS_REMOTE}
		};

		addField(new StaticTextFieldEditor(parent, ""));

		// Notes-related fields
		editorNotesStatus = new RadioGroupFieldEditor(XspPreferences.NOTES_STATUS, "&Using IBM Notes?", 3, statusLabels, parent, true);
		
		editorNotesNotesIniFile = new FileFieldEditor(XspPreferences.NOTES_INIFILE_PATH, "Notes.ini &File Location for Notes:", true, parent);
		
		editorNotesInstall = new DirectoryFieldEditor(XspPreferences.NOTES_INSTALL_FOLDER, "&Notes Installation Folder:", parent);

		editorNotesData = new DirectoryFieldEditor(XspPreferences.NOTES_DATA_FOLDER, "&Notes Data Folder:", parent);

		editorNotesAutoJre = new BooleanFieldEditor(XspPreferences.NOTES_AUTO_JRE, "&Automatically create JRE for Notes?", parent);

		addField(editorNotesStatus);
		addField(editorNotesNotesIniFile);
		addField(editorNotesInstall);
		addField(editorNotesData);
		addField(editorNotesAutoJre);
		
		addField(new StaticTextFieldEditor(parent, ""));
		
		// Domino-related fields
		editorDominoStatus = new RadioGroupFieldEditor(XspPreferences.DOMINO_STATUS, "&Using IBM Domino?", 3, statusLabels, parent, true);
		
		editorDominoNotesIniFile = new FileFieldEditor(XspPreferences.DOMINO_INIFILE_PATH, "Notes.ini &File Location for Domino:", true, parent);
		
		editorDominoInstall = new DirectoryFieldEditor(XspPreferences.DOMINO_INSTALL_FOLDER, "&Domino Installation Folder:", parent);

		editorDominoData = new DirectoryFieldEditor(XspPreferences.DOMINO_DATA_FOLDER, "&Domino Data Folder:", parent);

		editorDominoAutoJre = new BooleanFieldEditor(XspPreferences.DOMINO_AUTO_JRE, "&Automatically create JRE for Domino?", parent);

		addField(editorDominoStatus);
		addField(editorDominoNotesIniFile);
		addField(editorDominoInstall);
		addField(editorDominoData);
		addField(editorDominoAutoJre);

		addField(new StaticTextFieldEditor(parent, ""));
		addField(new StaticTextFieldEditor(parent, "Refer to the documentation for the following values."));
		
		// Remote-related fields
		editorLocalJunction = new DirectoryFieldEditor(XspPreferences.LOCAL_JUNCTION, "&Shared Directory from Local:", parent);
		editorRemoteJunction = new StringFieldEditor(XspPreferences.REMOTE_JUNCTION, "&Mapping to the Shared Directory:", parent);

		addField(editorLocalJunction);
		addField(editorRemoteJunction);
		
		doHideWhenOnStart();
	}

	@Override
	public boolean performOk() {
		beforeSubmit();
		return super.performOk();
	}

	@Override
	public void performApply() {
		beforeSubmit();
		super.performApply();
	}
		
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);

		if(event.getSource() == editorNotesStatus) {
			this.notesStatus = String.valueOf(event.getNewValue());
			doHideWhenForNotes();
			doHideWhenForRemote();
		}
		
		if(event.getSource() == editorDominoStatus) {
			this.dominoStatus = String.valueOf(event.getNewValue());
			doHideWhenForDomino();
			doHideWhenForRemote();
		}
		
	}

	private void doHideWhenOnStart() {
		this.notesStatus = XspPreferences.getPreferenceString(XspPreferences.NOTES_STATUS);
		this.dominoStatus = XspPreferences.getPreferenceString(XspPreferences.DOMINO_STATUS);

		doHideWhenForNotes();
		doHideWhenForDomino();
		doHideWhenForRemote();
	}
	
	private void doHideWhenForNotes() {
		boolean notesEnabled = ! XspPreferences.STATUS_DISABLED.equals(notesStatus);
		boolean notesLocal = XspPreferences.STATUS_LOCAL.equals(notesStatus);
		
		editorNotesNotesIniFile.setEnabled(notesEnabled, getFieldEditorParent());
		editorNotesInstall.setEnabled(notesEnabled, getFieldEditorParent());
		editorNotesData.setEnabled(notesEnabled, getFieldEditorParent());
		
		if(notesEnabled) {
			editorNotesAutoJre.setEnabled(notesLocal, getFieldEditorParent());
		} else {
			editorNotesAutoJre.setEnabled(false, getFieldEditorParent());
		}
	}
	
	private void doHideWhenForDomino() {
		boolean dominoEnabled = ! XspPreferences.STATUS_DISABLED.equals(dominoStatus);
		boolean dominoLocal = XspPreferences.STATUS_LOCAL.equals(dominoStatus);
		
		editorDominoNotesIniFile.setEnabled(dominoEnabled, getFieldEditorParent());
		editorDominoInstall.setEnabled(dominoEnabled, getFieldEditorParent());
		editorDominoData.setEnabled(dominoEnabled, getFieldEditorParent());

		if(dominoEnabled) {
			editorDominoAutoJre.setEnabled(dominoLocal, getFieldEditorParent());
		} else {
			editorDominoAutoJre.setEnabled(false, getFieldEditorParent());
		}
	}

	private void doHideWhenForRemote() {
		boolean dominoRemote = XspPreferences.STATUS_REMOTE.equals(dominoStatus);
		boolean notesRemote = XspPreferences.STATUS_REMOTE.equals(notesStatus);
		
		editorLocalJunction.setEnabled(notesRemote || dominoRemote, getFieldEditorParent());
		editorRemoteJunction.setEnabled(notesRemote || dominoRemote, getFieldEditorParent());
	}
	
	private void beforeSubmit() {
		boolean notesEnabled = ! XspPreferences.STATUS_DISABLED.equals(notesStatus);
		boolean dominoEnabled = ! XspPreferences.STATUS_DISABLED.equals(dominoStatus);
		boolean dominoRemote = XspPreferences.STATUS_REMOTE.equals(dominoStatus);
		boolean notesRemote = XspPreferences.STATUS_REMOTE.equals(notesStatus);
		
		if(notesEnabled) {
			fixDirectory(editorNotesInstall);
			fixDirectory(editorNotesData);
		}
		
		if(dominoEnabled) {
			fixDirectory(editorDominoInstall);
			fixDirectory(editorDominoData);
		}

		if(notesRemote || dominoRemote) {
			fixDirectory(editorLocalJunction);
		}
		
		setupVMs();
	}
	
	private void setupVMs() {
		// Notes JRE
		Boolean notesAutoJRE = editorNotesAutoJre.getBooleanValue();
		
		if(notesAutoJRE && ! XspPreferences.STATUS_DISABLED.equals(notesStatus)) {
			String installPath = editorNotesInstall.getStringValue();
			
			if (installPath != null && installPath.length() > 0) {
				XPagesVMSetup.setupNotesJRE(installPath + "/jvm");
			}
			
		}
		
		// Domino JRE
		Boolean dominoAutoJRE = editorDominoAutoJre.getBooleanValue();
		
		if(dominoAutoJRE && ! XspPreferences.STATUS_DISABLED.equals(dominoStatus)) {
			String installPath = editorDominoInstall.getStringValue();
			
			if (installPath != null && installPath.length() > 0) {
				XPagesVMSetup.setupDominoJRE(installPath + "/jvm");
			}
		}
	}
	
	private static void fixDirectory(DirectoryFieldEditor fieldEditor) {
		String value = fieldEditor.getStringValue();
		
		// Fix: if the path is empty or root folder ('/'), no need to prune
		if(CommonUtils.isNotEmpty(value) && value.length()>1) { 
			value = StringUtil.prunePath(value);
		}
		
		fieldEditor.setStringValue(value);
	}
	
}
