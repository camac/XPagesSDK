package org.openntf.xsp.sdk.intellij.ui;

import org.jetbrains.annotations.NotNull;
import org.openntf.xsp.sdk.intellij.DominoRunProperties;
import org.osmorc.run.OsgiRunConfiguration;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class LaunchDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JEditorPane infoPane;
    private JTextField programDirField;
    private JTextField dataDirField;
    private JButton buttonCancel;

    private final OsgiRunConfiguration osgiRunConfiguration;

    public LaunchDialog(@NotNull OsgiRunConfiguration osgiRunConfiguration, Runnable callback) {
        this.osgiRunConfiguration = osgiRunConfiguration;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setPreferredSize(new Dimension(750, 300));
        setResizable(false);
        setLocationRelativeTo(null);

        buttonOK.addActionListener(e -> {
            dispose();
            if(callback != null) {
                callback.run();
            }
        });
        buttonCancel.addActionListener(e -> dispose());

        infoPane.setText(
            MessageFormat.format(
                "<p style='margin: 0'><b>Information:</b><br/>To use this configuration the Domino server's "
                    + "<b>HTTP task must be restarted</b>. In order to debug the plug-ins in this workspace<br/>"
                    + "the Domino server must be running in \"debug mode\" and a separate \"Remote Java Application\" "
                    + "debug configuration must<br/>be created and launched. To start the Domino OSGI framework normally,\n"
                    + "first delete the pde.launch.ini file located in<br/>"
                    + "the {0} (or related profile) directory and then restart the HTTP task.</p>",
                getWorkspaceFolder()
            )
        );


        String programDir = DominoRunProperties.getProgramDir(osgiRunConfiguration.getAdditionalProperties());
        programDirField.setText(programDir);
        String dataDir = DominoRunProperties.getDataDir(osgiRunConfiguration.getAdditionalProperties());
        dataDirField.setText(dataDir);
    }

    private String getWorkspaceFolder() {
        String dataDir = DominoRunProperties.getDataDir(osgiRunConfiguration.getAdditionalProperties());
        Path path = Paths.get(dataDir);
        Path workspaceDir = path.resolve("domino").resolve("workspace");

        return workspaceDir.toAbsolutePath().toString();
    }
}
