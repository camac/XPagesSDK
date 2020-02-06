/*
 * � Copyright IBM Corp. 2012
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ibm.domino.osgi.debug.launch;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormText;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.preferences.XspPreferences;

/**
 * @author dtaieb
 * @author doconnor
 *         The dialog that will be popped up when creating an OSGi framework
 *         configuration
 *
 */
public class LaunchDialog extends TitleAreaDialog {
	private final LaunchHandler launchHandler;
	private final INotesDominoPlatform targetPlatform;
	private Text dataDirectoryText;
	private Text binDirectoryText;
	private Combo profileCombo;

	/**
	 * @param parent
	 */
	public LaunchDialog(LaunchHandler launchHandler, Shell parent) {
		super(parent);

		this.launchHandler = launchHandler;
		this.targetPlatform = launchHandler.getTargetPlatform();
	}

	@Override
	public boolean close() {
		launchHandler.setReturnCode(getReturnCode());
		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(top, SWT.NULL);

		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FormText header = new FormText(container, SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		header.setLayoutData(gd);
		header.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		new Label(container, SWT.NULL).setText("Domino Bin Directory: ");
		binDirectoryText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		binDirectoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		binDirectoryText.setEditable(false);
		
		new Label(container, SWT.NULL).setText("Domino Data Directory: ");
		dataDirectoryText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		dataDirectoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dataDirectoryText.setEditable(false);

		String[] profiles = launchHandler.getProfiles();
		if (profiles != null && profiles.length > 0) {
			String prefProfile = XspPreferences.getPreferenceString(XspPreferences.PREF_PROFILE, null);

			new Label(container, SWT.NULL).setText("Profile");
			profileCombo = new Combo(container, SWT.BORDER);
			profileCombo.setItems(profiles);
			if (prefProfile != null) {
				profileCombo.setText(prefProfile);
			}
			profileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		getShell().setText("IBM Lotus Domino Debug Plugin");
		setTitle("Create Domino OSGI PDE Configuration");
		setMessage("This launch configuration doesn't actually launch the Domino OSGI Framework.\n"
				+ "It creates and sets up the OSGi configuration that will be run "
				+ "the next time you run the http task on your domino server\n");

		String prefBinValue = targetPlatform.getRemoteInstallFolder();
		String prefDataValue = targetPlatform.getRemoteDataFolder();

		binDirectoryText.setText(prefBinValue);
		dataDirectoryText.setText(prefDataValue);
		
		updateControls();

		header.setText(
				MessageFormat
						.format("<form><p><b>Information:</b><br/>To use this configuration the Domino server&apos;s "
								+ "<b>HTTP task must be restarted</b>. In order to debug the plug-ins in this workspace<br/>"
								+ "the Domino server must be running in \"debug mode\" and a separate \"Remote Java Application\" "
								+ "debug configuration must<br/>be created and launched. To start the Domino OSGI framework normally,\n"
								+ "first delete the pde.launch.ini file located in<br/>"
								+ "the {0} (or related profile) directory and then restart the HTTP task.</p></form>",
								targetPlatform.getLocalWorkspaceFolder()), true, false);

		return top;
	}

	@Override
	protected void okPressed() {
		// Update the profile if any
		if (profileCombo != null) {
			String selectedProfile = profileCombo.getText();
			if (selectedProfile != null && selectedProfile.length() > 0) {
				launchHandler.setProfile(selectedProfile);
				XspPreferences.setPreferenceString(XspPreferences.PREF_PROFILE, selectedProfile);
			}
		}
		super.okPressed();
	}

	/**
	 * @param path
	 */
	protected void updateControls() {
		try {
			setErrorMessage(null);
			setOKEnabled(true);
			launchHandler.isValid();
		} catch (Throwable e) {
			setErrorMessage(e.getMessage());
			setOKEnabled(false);
		}
	}

	/**
	 * @param bSet
	 */
	private void setOKEnabled(boolean bSet) {
		Button btn = getButton(IDialogConstants.OK_ID);
		if (btn != null) {
			btn.setEnabled(bSet);
		}
	}
	
	static class LaunchThread implements Runnable {

		private final LaunchHandler launchHandler;
		
		public LaunchThread(LaunchHandler launchHandler) {
			this.launchHandler = launchHandler;
		}
		
		@Override
		public void run() {
			new LaunchDialog(launchHandler,
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).open();
		}
	}

}
