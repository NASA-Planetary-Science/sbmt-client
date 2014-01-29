package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

        mi = new JMenuItem(new ShowSourceOfDataAction());
        this.add(mi);

        if (Configuration.isAPLVersion())
        {
            mi = new JMenuItem(new ShowRecentChangesAction());
            this.add(mi);
        }

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
                COPYRIGHT + " 2014 The Johns Hopkins University Applied Physics Laboratory\n",
                "About Small Body Mapping Tool",
                JOptionPane.PLAIN_MESSAGE);
    }

    private class ShowHelpContentsAction extends AbstractAction
    {
        public ShowHelpContentsAction()
        {
            super("Help Contents");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String helpRootUrl = Configuration.getHelpRootURL();
            try
            {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(helpRootUrl + "helpcontents.html"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class ShowSourceOfDataAction extends AbstractAction
    {
        public ShowSourceOfDataAction()
        {
            super("Where does the data come from?");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String helpRootUrl = Configuration.getHelpRootURL();
            try
            {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(helpRootUrl + "references.html"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class ShowRecentChangesAction extends AbstractAction
    {
        public ShowRecentChangesAction()
        {
            super("Recent Changes");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String helpRootUrl = Configuration.getHelpRootURL();
            try
            {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(helpRootUrl + "recentchanges.html"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
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
