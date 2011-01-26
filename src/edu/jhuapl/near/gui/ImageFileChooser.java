package edu.jhuapl.near.gui;


import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 * @author kahneg1
 *
 */
public class ImageFileChooser extends FileChooserBase
{
    private static final JFileChooser fc = new JFileChooser();

    private static String BMP_DESCRIPTION = "BMP Files";
    private static String JPEG_DESCRIPTION = "JPEG Files";
    private static String PNG_DESCRIPTION = "PNG Files";
    private static String PNM_DESCRIPTION = "PNM Files";
    private static String PS_DESCRIPTION = "PostScript Files";
    private static String TIFF_DESCRIPTION = "TIFF Files";

    private static class ImageFilter extends FileFilter
    {
        private String[] extensions;
        private String description;

        public ImageFilter(String[] extensions, String description)
        {
            this.extensions = extensions;
            this.description = description;
        }

        //Accept all directories and all png files.
        public boolean accept(File f)
        {
            if (f.isDirectory())
            {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null)
            {
                for (String str : extensions)
                {
                    if (extension.equals(str))
                    {
                        return true;
                    }
                }

                return false;
            }

            return false;
        }

        //The description of this filter
        public String getDescription()
        {
            return description;
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

        String[] bmp = {"bmp"};
        fc.addChoosableFileFilter(new ImageFilter(bmp, BMP_DESCRIPTION));
        String[] jpeg = {"jpg", "jpeg"};
        fc.addChoosableFileFilter(new ImageFilter(jpeg, JPEG_DESCRIPTION));
        String[] png = {"png"};
        fc.addChoosableFileFilter(new ImageFilter(png, PNG_DESCRIPTION));
        String[] pnm = {"pnm"};
        fc.addChoosableFileFilter(new ImageFilter(pnm, PNM_DESCRIPTION));
        String[] ps = {"ps"};
        fc.addChoosableFileFilter(new ImageFilter(ps, PS_DESCRIPTION));
        String[] tiff = {"tif", "tiff"};
        fc.addChoosableFileFilter(new ImageFilter(tiff, TIFF_DESCRIPTION));
    }

    public static File showOpenDialog(Component parent, String title)
    {
        fc.setDialogTitle(title);
        fc.setCurrentDirectory(getLastDirectory());
        int returnVal = fc.showOpenDialog(JOptionPane.getFrameForComponent(parent));
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            setLastDirectory(fc.getCurrentDirectory());
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
        fc.setCurrentDirectory(getLastDirectory());
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(parent));
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            setLastDirectory(fc.getCurrentDirectory());
            String filename = fc.getSelectedFile().getAbsolutePath();

            String desc = fc.getFileFilter().getDescription();
            if (BMP_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".bmp"))
            {
                filename += ".bmp";
            }
            else if (JPEG_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".jpg") &&
                    !filename.toLowerCase().endsWith(".jpeg") )
            {
                filename += ".jpg";
            }
            else if (PNG_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".png"))
            {
                filename += ".png";
            }
            else if (PNM_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".pnm"))
            {
                filename += ".pnm";
            }
            else if (PS_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".ps"))
            {
                filename += ".ps";
            }
            else if (TIFF_DESCRIPTION.equals(desc) &&
                    !filename.toLowerCase().endsWith(".tif") &&
                    !filename.toLowerCase().endsWith(".tiff"))
            {
                filename += ".tiff";
            }

            File file = new File(filename);
            if (file.exists ())
            {
                int response = JOptionPane.showConfirmDialog (JOptionPane.getFrameForComponent(parent),
                  "Overwrite " + file.getName() + "?","Confirm Overwrite",
                   JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION)
                    return null;
            }

            return file;
        }
        else
        {
            return null;
        }
    }

}
