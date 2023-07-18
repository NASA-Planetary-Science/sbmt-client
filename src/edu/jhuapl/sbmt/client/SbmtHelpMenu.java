package edu.jhuapl.sbmt.client;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Configuration.ReleaseType;


public class SbmtHelpMenu extends HelpMenu
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

    public SbmtHelpMenu(JPanel rootPanel)
    {
        super(rootPanel);
        dataSourceMenuItem.setText("SBMT Data Sources");

     // On macs the about action is in the Application menu not the help menu
        if (Configuration.isMac())
        {
            try
            {
                Desktop.getDesktop().setAboutHandler(new java.awt.desktop.AboutHandler() {
                    public void handleAbout(AboutEvent e) {
                    	showAbout();
                    }
                });
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
     }

    public void showAbout()
    {
        final String COPYRIGHT  = "\u00a9";
        String versionString = (Configuration.getReleaseType() == ReleaseType.DEVELOPMENT) ? "" : SbmtMultiMissionTool.versionString;
        Date compileDate = SbmtMultiMissionTool.compileDate;
        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getRootPanel()),
                "Small Body Mapping Tool " + versionString + "\n\n" + SbmtMultiMissionTool.getMission() + " edition" + (compileDate != null ? " built " + DATE_FORMAT.format(compileDate) : "") + "\n\n"  +
                COPYRIGHT + " " + Calendar.getInstance().get(Calendar.YEAR) + " The Johns Hopkins University Applied Physics Laboratory, LLC\n",
                "About Small Body Mapping Tool",
                JOptionPane.PLAIN_MESSAGE);
    }

    protected void showHelp()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://sbmt.jhuapl.edu/index.php#help"));
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
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://sbmt.jhuapl.edu/index.php#data"));
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
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://sbmt.jhuapl.edu/Release-Notes.php"));
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
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://sbmt.jhuapl.edu/Tutorials.php"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
