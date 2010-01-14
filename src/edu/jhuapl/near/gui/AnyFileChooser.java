package edu.jhuapl.near.gui;


import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * @author kahneg1
 *
 */
public class AnyFileChooser
{
    private static final JFileChooser fc = new JFileChooser();

    private static class AnyFilter extends FileFilter 
    {
        //Accept all directories and all files.
        public boolean accept(File f) 
        {
        	return true;
        }

        //The description of this filter
        public String getDescription() 
        {
            return "All Files";
        }
    }

    static
    {
    	fc.setFileFilter(new AnyFilter());
    	fc.setAcceptAllFileFilterUsed(false);    	
    }
    
    public static File showOpenDialog(Component parent, String title)
    {
    	fc.setDialogTitle(title);
    	int returnVal = fc.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
        	return fc.getSelectedFile();
        }	
        else
        {
        	return null;
        }
    }
    
    public static File showSaveDialog(Component parent, String title)
    {
    	fc.setDialogTitle(title);
    	int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            File file = fc.getSelectedFile();
            if (file.exists ()) 
            {
                int response = JOptionPane.showConfirmDialog (null,
                  "Overwrite existing file?","Confirm Overwrite",
                   JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) 
                	return null;
            }
            
        	String filename = file.getAbsolutePath();
        	return new File(filename);
        }	
        else
        {
        	return null;
        }
    }
    
}
