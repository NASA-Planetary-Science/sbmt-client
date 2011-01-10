package edu.jhuapl.near.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class HelpMenu extends JMenu
{
    private JPanel rootPanel;

    public HelpMenu(JPanel rootPanel)
    {
        super("Help");

        JMenuItem mi = new JMenuItem(new ShowHelpContentsAction());
        this.add(mi);

        this.addSeparator();

        mi = new JMenuItem(new AboutAction());
        this.add(mi);

    }

    private class ShowHelpContentsAction extends AbstractAction
    {
        private JFrame frame = null;

        public ShowHelpContentsAction()
        {
            super("Help Contents");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            if (frame == null)
            {
                frame = new JFrame();

                java.net.URL helpURL = HelpMenu.class.getResource(
                "/edu/jhuapl/near/data/helpcontents.html");

                JEditorPane label;
                try
                {
                    label = new JEditorPane(helpURL);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
                label.setEditable(false);
                label.setContentType("text/html");
                label.setBackground(Color.WHITE);

                JScrollPane scrollPane = new JScrollPane(label);
                scrollPane.setPreferredSize(new Dimension(800, 600));
                scrollPane.setMinimumSize(new Dimension(10, 10));
                scrollPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

                frame.add(scrollPane);

                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setTitle("Help Contents");
                frame.pack();
            }

            frame.setVisible(true);
            frame.toFront();
        }
    }

    private class AboutAction extends AbstractAction
    {
        private static final String COPYRIGHT  = "\u00a9";

        public AboutAction()
        {
            super("About Small Body Mapping Tool");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String versionString = "\n";
            try
            {
                InputStream is = this.getClass().getResourceAsStream("/svn.version");
                byte[] data = new byte[256];
                is.read(data, 0, data.length);
                String[] tmp = (new String(data)).trim().split("\\s+");
                tmp[3] = tmp[3].replace('-', '.');
                versionString = "Version: " + tmp[3] + "\n\n";
            }
            catch (Exception e)
            {
            }

            JOptionPane.showMessageDialog(rootPanel,
                    "Small Body Mapping Tool\n" + versionString +
                    COPYRIGHT + " 2010 The Johns Hopkins University Applied Physics Laboratory\n",
                    "About Small Body Mapping Tool",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }
}
