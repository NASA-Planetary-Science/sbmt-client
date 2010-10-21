package edu.jhuapl.near.gui;


import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * @author kahneg1
 *
 */
public class CustomExtensionFileChooser
{
    private static final JFileChooser fc = new JFileChooser();

    private static class CustomExtensionFilter extends FileFilter 
    {
    	private String extension;
    	
    	public CustomExtensionFilter(String extension)
    	{
    		this.extension = extension;
    	}
    	
        //Accept all directories and all files with specified extension.
        public boolean accept(File f) 
        {
            if (f.isDirectory()) 
            {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) 
            {
                if (extension.equals(extension))
                {
                	return true;
                } 
                else 
                {
                    return false;
                }
            }

            return false;
        }

        //The description of this filter
        public String getDescription() 
        {
            return this.extension.toUpperCase() + " Files";
        }
        
        private String getExtension(File f) 
        {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) 
            {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

    static
    {
    	fc.setAcceptAllFileFilterUsed(false);    	
    }
    
    public static File showOpenDialog(Component parent, String title, String extension)
    {
    	fc.setDialogTitle(title);
    	fc.setFileFilter(new CustomExtensionFilter(extension));
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
}
