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

    public DominoRunPropertiesEditor() {
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
    }

    @Override
    public void applyEditorTo(@NotNull OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException {
        Map<String, String> props = ContainerUtil.newHashMap();

        DominoRunProperties.setNotesIni(props, notesIniField.getText());
        DominoRunProperties.setProgramDir(props, programDirField.getText());
        DominoRunProperties.setDataDir(props, dataDirField.getText());

        osgiRunConfiguration.putAdditionalProperties(props);
    }

    @Override
    public JPanel getUI() {
        return myMainPanel;
    }
}
