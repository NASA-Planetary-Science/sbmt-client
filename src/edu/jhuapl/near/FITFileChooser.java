package edu.jhuapl.near;


import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * @author kahneg1
 *
 */
public class FITFileChooser
{
    private static final JFileChooser fc = new JFileChooser();

    private static class FITFilter extends FileFilter 
    {
        //Accept all directories and all fit files.
        public boolean accept(File f) 
        {
            if (f.isDirectory()) 
            {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) 
            {
                if (extension.equals("fit"))
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
            return "FIT Files";
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
    	fc.setFileFilter(new FITFilter());
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
        	if (!filename.toLowerCase().endsWith(".fit"))
        		filename += ".fit";
        	return new File(filename);
        }	
        else
        {
        	return null;
        }
    }
    
}
