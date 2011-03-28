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

import edu.jhuapl.near.util.Configuration;


public class HelpMenu extends JMenu
{
    private JPanel rootPanel;

    public HelpMenu(JPanel rootPanel)
    {
        super("Help");
        this.rootPanel = rootPanel;

        JMenuItem mi = new JMenuItem(new ShowHelpContentsAction());
        this.add(mi);

        // On macs the about action is in the Application menu not the help menu
        if (!Configuration.isMac())
        {
            this.addSeparator();

            mi = new JMenuItem(new AboutAction());
            this.add(mi);
        }
        else
        {
            try
            {
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[])null));
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void showAbout()
    {
        final String COPYRIGHT  = "\u00a9";

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

        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(rootPanel),
                "Small Body Mapping Tool\n" + versionString +
                COPYRIGHT + " 2010 The Johns Hopkins University Applied Physics Laboratory\n",
                "About Small Body Mapping Tool",
                JOptionPane.PLAIN_MESSAGE);
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

                JEditorPane label;
                try
                {
                    if (Configuration.isAPLVersion())
                    {
                        java.net.URL helpURL = HelpMenu.class.getResource(
                        "/edu/jhuapl/near/data/helpcontents-apl.html");

                        label = new JEditorPane(helpURL);
                    }
                    else
                    {
                        java.net.URL helpURL = HelpMenu.class.getResource(
                        "/edu/jhuapl/near/data/helpcontents.html");

                        label = new JEditorPane(helpURL);
                    }
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
                scrollPane.setPreferredSize(new Dimension(1024, 768));
                scrollPane.setMinimumSize(new Dimension(10, 10));
                scrollPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

                frame.add(scrollPane);

                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setTitle("Small Body Mapping Tool User Manual");
                frame.pack();
            }

            frame.setVisible(true);
            frame.toFront();
        }

        /*
        private String removeAPLContentsForPublicVersion() throws IOException
        {
            InputStream is = HelpMenu.class.getResourceAsStream(
                    "/edu/jhuapl/near/data/helpcontents.html");

            List<String> lines = IOUtils.readLines(is);
            StringBuilder newFile = new StringBuilder();

            boolean copy = true;
            for (String line : lines)
            {
                if (line.startsWith("<!--APL VERSION START-->"))
                    copy = false;

                if (copy)
                    newFile.append(line + "\n");

                if (line.startsWith("<!--APL VERSION END-->"))
                    copy = true;
            }

            return newFile.toString();
        }
        */
    }

    private class AboutAction extends AbstractAction
    {
        public AboutAction()
        {
            super("About Small Body Mapping Tool");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showAbout();
        }
    }
}
