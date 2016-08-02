package edu.jhuapl.near.gui;


import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JOptionPane;


public class CustomFileChooser extends FileChooserBase
{
    private static class CustomExtensionFilter implements FilenameFilter
    {
        private String extension;

        public CustomExtensionFilter(String extension)
        {
            if (extension != null)
                extension = extension.toLowerCase();

            this.extension = extension;
        }

        //Accept all directories and all files with specified extension.
        public boolean accept(File dir, String filename)
        {
            File f = new File(dir, filename);
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

    public static File showOpenDialog(Component parent, String title)
    {
        return showOpenDialog(parent, title, null);
    }

    public static File showOpenDialog(Component parent, String title, String extension)
    {
        File[] files = showOpenDialog(parent, title, extension, false);
        if (files == null || files.length < 1)
            return null;
        else
            return files[0];
    }

    public static File[] showOpenDialog(Component parent, String title, String extension, boolean multiSelectionEnabled)
    {
        FileDialog fc = new FileDialog(JOptionPane.getFrameForComponent(parent), title, FileDialog.LOAD);
        //fc.setAcceptAllFileFilterUsed(true);
        fc.setMultipleMode(multiSelectionEnabled);
        if (extension != null)
            fc.setFilenameFilter(new CustomExtensionFilter(extension));
        if (getLastDirectory() != null)
            fc.setDirectory(getLastDirectory().getAbsolutePath());
        fc.setVisible(true);
        String returnedFile = fc.getFile();
        if (returnedFile != null)
        {
            setLastDirectory(new File(fc.getDirectory()));
            if (multiSelectionEnabled)
            {
                return fc.getFiles();
            }
            else
            {
                File file = new File(fc.getDirectory(), fc.getFile());
                return new File[] {file};
            }
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
        FileDialog fc = new FileDialog(JOptionPane.getFrameForComponent(parent), title, FileDialog.SAVE);
        //fc.setAcceptAllFileFilterUsed(true);
        if (extension != null)
            fc.setFilenameFilter(new CustomExtensionFilter(extension));
        if (getLastDirectory() != null)
            fc.setDirectory(getLastDirectory().getAbsolutePath());
        if (defaultFilename != null)
            fc.setFile(defaultFilename);
        fc.setVisible(true);
        String returnedFile = fc.getFile();
        if (returnedFile != null)
        {
            setLastDirectory(new File(fc.getDirectory()));
            File file = new File(fc.getDirectory(), fc.getFile());

            String filename = file.getAbsolutePath();
            if (extension != null && !extension.isEmpty())
            {
                if (!filename.toLowerCase().endsWith("." + extension))
                    filename += "." + extension;
            }
            file = new File(filename);

            //if (file.exists())
            //{
            //    int response = JOptionPane.showConfirmDialog (JOptionPane.getFrameForComponent(parent),
            //      "Overwrite existing file?","Confirm Overwrite",
            //       JOptionPane.OK_CANCEL_OPTION,
            //       JOptionPane.QUESTION_MESSAGE);
            //    if (response == JOptionPane.CANCEL_OPTION)
            //        return null;
            //}

            return file;
        }
        else
        {
            return null;
        }

    }
}
