package edu.jhuapl.near.gui;


import java.io.File;
import java.awt.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * @author kahneg1
 *
 */
public class CustomFileChooser
{
    private static final JFileChooser fc = new JFileChooser();

    private static class CustomExtensionFilter extends FileFilter
    {
    	private String extension;

    	public CustomExtensionFilter(String extension)
    	{
    		if (extension != null)
    			extension = extension.toLowerCase();

    		this.extension = extension;
    	}

        //Accept all directories and all files with specified extension.
        public boolean accept(File f)
        {
            if (f.isDirectory() || (extension == null || extension.isEmpty()))
            {
                return true;
            }

            String ext = getExtension(f);
            if (ext != null)
            {
                if (ext.equals(extension))
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
        	if (extension == null || extension.isEmpty())
        		return "All Files";
        	else
        		return extension.toUpperCase() + " Files";
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

    public static File showOpenDialog(Component parent, String title)
    {
    	return showOpenDialog(parent, title, null);
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

    public static File showSaveDialog(Component parent, String title)
    {
    	return showSaveDialog(parent, title, null, null);
    }

    public static File showSaveDialog(Component parent, String title, String defaultFilename)
    {
    	return showSaveDialog(parent, title, defaultFilename, null);
    }

    public static File showSaveDialog(Component parent, String title, String defaultFilename, String extension)
    {
    	fc.setDialogTitle(title);
    	fc.setFileFilter(new CustomExtensionFilter(extension));
    	if (defaultFilename != null)
    		fc.setSelectedFile(new File(defaultFilename));
    	int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            if (file.exists())
            {
                int response = JOptionPane.showConfirmDialog (null,
                  "Overwrite existing file?","Confirm Overwrite",
                   JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION)
                	return null;
            }

        	String filename = file.getAbsolutePath();
        	if (extension != null && !extension.isEmpty())
        	{
        		if (!filename.toLowerCase().endsWith("." + extension))
        			filename += "." + extension;
        	}
        	return new File(filename);
        }
        else
        {
        	return null;
        }
    }
}
