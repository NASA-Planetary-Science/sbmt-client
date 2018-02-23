package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.util.Configuration;


public class SbmtHelpMenu extends HelpMenu
{

    public SbmtHelpMenu(JPanel rootPanel)
    {
        super(rootPanel);
        dataSourceMenuItem.setText("SBMT Data Sources");
     }

    public void showAbout()
    {
        final String COPYRIGHT  = "\u00a9";


        String versionString = "\n";
        try
        {
            // note: currently this seems to be broken, perhaps because this file isn't being generated anymore by the release process -turnerj1
            InputStream is = this.getClass().getResourceAsStream("/svn.version");
            byte[] data = new byte[256];
            is.read(data, 0, data.length);
            String[] tmp = (new String(data)).trim().split("\\s+");
            // Don't want to make the assumption that release names contain only dates.
            // Release names can now be anything. So, display it exactly as it is.
            //tmp[3] = tmp[3].replace('-', '.');
           versionString = "Version: " + tmp[3] + "\n\n";
        }
        catch (Exception e)
        {
//            System.out.println("exception = " + e.toString());
        }

        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getRootPanel()),
                "Small Body Mapping Tool\n" + versionString +
                COPYRIGHT + " 2018 The Johns Hopkins University Applied Physics Laboratory\n",
                "About Small Body Mapping Tool",
                JOptionPane.PLAIN_MESSAGE);
    }

    protected void showHelp()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://sbmt2.jhuapl.edu/index.php#documentation"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showDataSources()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://sbmt2.jhuapl.edu/Object-Index.php"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showRecentChanges()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://sbmt2.jhuapl.edu/Recent-Changes.php"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showTutorial()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://sbmt2.jhuapl.edu/How-To-Tutorials.php"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
