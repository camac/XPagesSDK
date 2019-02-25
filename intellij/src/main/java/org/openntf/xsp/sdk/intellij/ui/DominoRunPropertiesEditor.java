package org.openntf.xsp.sdk.intellij.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.openntf.xsp.sdk.intellij.DominoRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Map;

public class DominoRunPropertiesEditor implements FrameworkRunPropertiesEditor {
    private JPanel myMainPanel;

    private JTextField notesIniField;
    private JTextField programDirField;
    private JTextField dataDirField;
    private JButton browseNotesIni;
    private JButton browseDataDir;
    private JButton browseProgramDir;
    private JRadioButton locationLocal;
    private JRadioButton locationRemote;
    private JTextField sharedFolderField;
    private JTextField mappedPathField;
    private JButton browseSharedFolder;

    public DominoRunPropertiesEditor() {
        bindEvents();
        bindBrowseButtons();
    }

    @Override
    public void resetEditorFrom(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
        Map<String, String> props = osgiRunConfiguration.getAdditionalProperties();

        String notesIni = DominoRunProperties.getNotesIni(props);
        if(StringUtil.isNotEmpty(notesIni)) {
            notesIniField.setText(notesIni);
        }

        String programDir = DominoRunProperties.getProgramDir(props);
        if(StringUtil.isNotEmpty(programDir)) {
            programDirField.setText(programDir);
        }

        String dataDir = DominoRunProperties.getDataDir(props);
        if(StringUtil.isNotEmpty(dataDir)) {
            dataDirField.setText(dataDir);
        }

        String sharedDir = DominoRunProperties.getSharedDir(props);
        if(StringUtil.isNotEmpty(sharedDir)) {
            sharedFolderField.setText(sharedDir);
        }

        String mappedPath = DominoRunProperties.getMappedRemotePath(props);
        if(StringUtil.isNotEmpty(mappedPath)) {
            mappedPathField.setText(mappedPath);
        }

        switch(DominoRunProperties.getLocation(props)) {
            case REMOTE:
                locationRemote.setSelected(true);
                break;
            case LOCAL:
            default:
                locationLocal.setSelected(true);
                break;
        }
        updateRemoteFields();
    }

    @Override
    public void applyEditorTo(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
        Map<String, String> props = ContainerUtil.newHashMap();

        DominoRunProperties.setNotesIni(props, notesIniField.getText());
        DominoRunProperties.setProgramDir(props, programDirField.getText());
        DominoRunProperties.setDataDir(props, dataDirField.getText());
        DominoRunProperties.setSharedDir(props, sharedFolderField.getText());
        DominoRunProperties.setMappedRemotePath(props, mappedPathField.getText());
        DominoRunProperties.setLocation(props, getLocation());

        osgiRunConfiguration.putAdditionalProperties(props);
    }

    @Override
    public JPanel getUI() {
        return myMainPanel;
    }

    // *******************************************************************************
    // * Bindings
    // *******************************************************************************

    private void bindEvents() {
        locationLocal.addActionListener(e -> updateRemoteFields());
        locationRemote.addActionListener(e -> updateRemoteFields());
    }

    private void bindBrowseButtons() {
        browseNotesIni.addActionListener(e -> {
            FileChooserDescriptor desc = new FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter(f -> "ini".equals(f.getExtension()))
                    .withHideIgnored(true);

            String path = notesIniField.getText();
            VirtualFile toSelect = null;
            if(StringUtil.isNotEmpty(path)) {
                toSelect = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(path);
            }

            FileChooser.chooseFile(desc, null, toSelect, file -> notesIniField.setText(file.getPath()));
        });

        browseProgramDir.addActionListener(pickFolder(programDirField));

        browseDataDir.addActionListener(pickFolder(dataDirField));

        browseSharedFolder.addActionListener(pickFolder(sharedFolderField));
    }

    private static ActionListener pickFolder(JTextField field) {
        return e -> {
            FileChooserDescriptor desc = new FileChooserDescriptor(false, true, false, false, false, false)
                    .withHideIgnored(true);

            String path = field.getText();
            VirtualFile toSelect = null;
            if(StringUtil.isNotEmpty(path)) {
                toSelect = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(path);
            }

            FileChooser.chooseFile(desc, null, toSelect, file -> field.setText(file.getPath()));
        };
    }

    // *******************************************************************************
    // * Internal utility methods
    // *******************************************************************************

    private DominoRunProperties.Location getLocation() {
        if(locationRemote.isSelected()) {
            return DominoRunProperties.Location.REMOTE;
        } else {
            return DominoRunProperties.Location.LOCAL;
        }
    }

    private void updateRemoteFields() {
        switch(getLocation()) {
            case REMOTE:
                sharedFolderField.setEnabled(true);
                mappedPathField.setEnabled(true);
                browseSharedFolder.setEnabled(true);
                break;
            case LOCAL:
            default:
                sharedFolderField.setEnabled(false);
                mappedPathField.setEnabled(false);
                browseSharedFolder.setEnabled(false);
                break;
        }
    }
}
