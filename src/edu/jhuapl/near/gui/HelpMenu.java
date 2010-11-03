package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


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
        private final String helpContents = "<html>" +
            "Small Body Mapping Tool Help<br><br>" +
            "NAVIGATION:<br>" +
            "Left Mouse Button: rotate camera<br>" +
            "Middle Mouse Button: pan camera<br>" +
            "Right Mouse Button: zoom camera<br>" +
            "Mousewheel: zoom camera<br>" +
            "Shift + Left Mouse Button: pan camera<br>" +
            "Ctrl + Left Mouse Button: spin camera<br>" +
            "Keypress 3: toggle stereo mode<br>" +
            "Keypress f: fly to point most recently clicked<br>" +
            "Keypress r: reset camera<br>" +
            "Keypress s: modify objects in scene to be shown as surfaces<br>" +
            "Keypress w: modify objects in scene to be shown as wireframe<br>" +
            "Keypress x (lowercase): reorient camera to point in positive x direction<br>" +
            "Keypress X (uppercase): reorient camera to point in negative x direction<br>" +
            "Keypress y (lowercase): reorient camera to point in positive y direction<br>" +
            "Keypress Y (uppercase): reorient camera to point in negative y direction<br>" +
            "Keypress z (lowercase): reorient camera to point in positive z direction<br>" +
            "Keypress Z (uppercase): reorient camera to point in negative z direction<br>" +
            "</html>";

        public ShowHelpContentsAction()
        {
            super("Help Contents");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            if (frame == null)
            {
                frame = new JFrame();
                
                JTextPane label = new JTextPane();
                label.setEditable(false);
                label.setContentType("text/html");
                label.setText(helpContents);
                
                JScrollPane scrollPane = new JScrollPane(label);
                
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
