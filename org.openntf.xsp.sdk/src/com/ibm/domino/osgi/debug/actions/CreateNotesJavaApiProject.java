/*
 * © Copyright IBM Corp. 2012
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
package com.ibm.domino.osgi.debug.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.preferences.XPagesSDKPreferences;

/**
 * @author dtaieb
 * @author doconnor
 * 
 *         A menu/toolbar action that allows the user to create a plug-in
 *         project
 *         that contains and exports the packages of the Notes Java Api plugin
 *         which
 *         This plugin must be included in the workspace in order to perform
 *         development
 *         of XPages extension library plug-ins
 *
 */
public class CreateNotesJavaApiProject implements IWorkbenchWindowActionDelegate {
	private static final String JAVA_API_PROJECT_NAME = "com.ibm.notes.java.api";
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public CreateNotesJavaApiProject() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	@Override
	public void run(IAction action) {
		if (exists(getBinDirectoryFromPreferenceStore())) {
			doCreateNotesJavaApiProject();
			return;
		}
		int status = new MessageDialog(window.getShell(), "Question", null,
				"Domino Bin directory not set, please click on the link to set it", MessageDialog.WARNING,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0) {
			@Override
			protected Control createCustomArea(Composite parent) {
				Link link = new Link(parent, SWT.NONE);
				link.setFont(parent.getFont());
				link.setText("<A>Domino Debug Plugin Preferences....</A>");
				link.addSelectionListener(new SelectionListener() {
					/*
					 * (non-Javadoc)
					 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
					 */
					@Override
					public void widgetSelected(SelectionEvent e) {
						doLinkActivated();
					}

					/*
					 * Open the appropriate preference page
					 */
					private void doLinkActivated() {
						String id = "com.ibm.domino.osgi.debug.preferences.DominoOSGiDebugPreferencePage";
						PreferencesUtil.createPreferenceDialogOn(window.getShell(), id, new String[] { id }, null)
								.open();

						updateButtons();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						doLinkActivated();
					}
				});
				return link;
			}

			private void updateButtons() {
				getButton(0).setEnabled(exists(getBinDirectoryFromPreferenceStore()));
			}

			@Override
			protected Control createContents(Composite parent) {
				Control ctrl = super.createContents(parent);
				updateButtons();
				return ctrl;
			};
		}.open();
		if (status == MessageDialog.OK) {
			doCreateNotesJavaApiProject();
		}

	}

	/**
	 * @return
	 */
	private String getBinDirectoryFromPreferenceStore() {
		return XPagesSDKPreferences.getDominoInstall();
	}

	/**
	 * @param dir
	 * @return
	 */
	private boolean exists(String dir) {
		if (dir == null || dir.length() == 0) {
			return false;
		}
		File f = new File(dir);
		return f.exists() && f.isDirectory();
	}

	private void doCreateNotesJavaApiProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(JAVA_API_PROJECT_NAME);
		if (project.exists()) {
			boolean bContinue = MessageDialog.openQuestion(window.getShell(), "Debug plug-in for Domino OSGi",
					MessageFormat.format("Project {0} already exists. Would you like to update it?",
							JAVA_API_PROJECT_NAME));
			if (!bContinue) {
				return;
			}
		}

		String binDirectory = getBinDirectoryFromPreferenceStore();
		if (binDirectory == null || binDirectory.length() == 0) {
			int ret = new MessageDialog(window.getShell(), "Question", null,
					"Domino Bin directory not set, please click on the link to set it", MessageDialog.WARNING,
					new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0) {
				/*
				 * (non-Javadoc)
				 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
				 */
				@Override
				protected Control createCustomArea(Composite parent) {
					Link link = new Link(parent, SWT.NONE);
					link.setFont(parent.getFont());
					link.setText("<A>Domino Debug Plugin Preferences....</A>");
					link.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							doLinkActivated();
						}

						private void doLinkActivated() {
							String id = "com.ibm.domino.osgi.debug.preferences.DominoOSGiDebugPreferencePage";
							PreferencesUtil.createPreferenceDialogOn(window.getShell(), id, new String[] { id }, null)
									.open();
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							doLinkActivated();
						}
					});
					return link;
				}
			}.open();

			if (ret == 1) {
				return;
			}
		}

		binDirectory = getBinDirectoryFromPreferenceStore();
		if (binDirectory == null || binDirectory.length() == 0) {
			MessageDialog.openError(window.getShell(), "Error", "Domino Bin directory not set");
			return;
		}

		try {
			final String sBinDir = binDirectory;
			new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						if (!project.exists()) {
							project.create(monitor);
						}
						project.open(monitor);

						IProjectDescription description = project.getDescription();
						String[] natures = description.getNatureIds();
						String[] newNatures = new String[natures.length + 2];
						System.arraycopy(natures, 0, newNatures, 0, natures.length);
						newNatures[natures.length] = JavaCore.NATURE_ID;
						newNatures[natures.length + 1] = "org.eclipse.pde.PluginNature";
						description.setNatureIds(newNatures);
						project.setDescription(description, monitor);
						// Copy the resources under res
						copyOneLevel(project, monitor, "res", "");

						// Copy notes.jar
						File notesJar = new File(sBinDir + "/jvm/lib/ext/Notes.jar");
						if (!notesJar.exists() || !notesJar.isFile()) {
							MessageDialog.openError(window.getShell(), "Error",
									MessageFormat.format("{0} does not exist", notesJar.getAbsolutePath()));
							return;
						}

						copyFile(notesJar, project.getFile("Notes.jar"), monitor);

					} catch (Throwable t) {
						throw new InvocationTargetException(t);
					}
				}
			});
		} catch (Throwable t) {
			MessageDialog.openError(window.getShell(), "error", t.getMessage());
			t.printStackTrace();
		}
	}

	/**
	 * @param srcFile
	 * @param target
	 * @param progressMonitor
	 * @throws CoreException
	 * @throws IOException
	 */
	private void copyFile(File srcFile, IFile target, IProgressMonitor progressMonitor) throws CoreException,
			IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			if (target.exists()) {
				target.setContents(fis, IResource.KEEP_HISTORY | IResource.FORCE, progressMonitor);
			} else {
				target.create(fis, true, progressMonitor);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * @param project
	 * @param progressMonitor
	 * @param rootParent
	 * @param level
	 * @throws CoreException
	 * @throws IOException
	 */
	private void copyOneLevel(IProject project, IProgressMonitor progressMonitor, String rootParent, String level)
			throws CoreException, IOException {
		Enumeration<?> en = Activator.getDefault().getBundle().getEntryPaths(rootParent + level);
		while (en.hasMoreElements()) {
			String path = (String) en.nextElement();
			String rel = makeRelative(rootParent, path);
			if (isFolder(path)) {
				createFolder(project, rel, progressMonitor);
				copyOneLevel(project, progressMonitor, rootParent, rel);
			} else {
				URL url = Activator.getDefault().getBundle().getEntry(path);
				InputStream is = null;
				try {
					is = url.openStream();
					IFile newFile = project.getFile(rel);
					if (newFile.exists()) {
						newFile.setContents(is, IResource.KEEP_HISTORY | IResource.FORCE, progressMonitor);
					} else {
						newFile.create(is, true, progressMonitor);
					}
				} finally {
					is.close();
				}
			}
		}
	}

	/**
	 * @param parent
	 * @param path
	 * @return
	 */
	private String makeRelative(String parent, String path) {
		if (path.startsWith(parent)) {
			path = path.substring(parent.length());
		}
		return path;
	}

	/**
	 * @param path
	 * @return
	 */
	private boolean isFolder(String path) {
		return path.endsWith("/");
	}

	/**
	 * @param project
	 * @param folderName
	 * @param progressMonitor
	 * @throws CoreException
	 */
	private IFolder createFolder(IProject project, String folderName, IProgressMonitor progressMonitor)
			throws CoreException {
		IFolder folder = project.getFolder(folderName);
		if (!folder.exists()) {
			folder.create(true, true, progressMonitor);
		}
		return folder;
	}

	/**
	 * Selection in the workbench has been changed. We
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after
	 * the delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	@Override
	public void dispose() {
		this.window = null;
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}