package org.openntf.xsp.sdk.intellij.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.openntf.xsp.sdk.intellij.DominoRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
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
    public void applyEditorTo(@NotNull OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException {
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
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".ini");
                }

                @Override
                public String getDescription() {
                    return ".ini files";
                }
            });
            if(chooser.showOpenDialog(myMainPanel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                notesIniField.setText(file.getAbsolutePath());
            }
        });

        browseProgramDir.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            String programDir = programDirField.getText();
            if(StringUtil.isNotEmpty(programDir)) {
                chooser.setCurrentDirectory(new File(programDir).getParentFile());
            }

            if(chooser.showOpenDialog(myMainPanel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                programDirField.setText(file.getAbsolutePath());
            }
        });

        browseDataDir.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            String dataDir = dataDirField.getText();
            if(StringUtil.isNotEmpty(dataDir)) {
                chooser.setCurrentDirectory(new File(dataDir).getParentFile());
            } else {
                String programDir = programDirField.getText();
                if(StringUtil.isNotEmpty(programDir)) {
                    chooser.setCurrentDirectory(new File(programDir));
                }
            }

            if(chooser.showOpenDialog(myMainPanel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                dataDirField.setText(file.getAbsolutePath());
            }
        });

        browseSharedFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            String sharedDir = sharedFolderField.getText();
            if(StringUtil.isNotEmpty(sharedDir)) {
                chooser.setCurrentDirectory(new File(sharedDir).getParentFile());
            }

            if(chooser.showOpenDialog(myMainPanel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                sharedFolderField.setText(file.getAbsolutePath());
            }
        });
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
