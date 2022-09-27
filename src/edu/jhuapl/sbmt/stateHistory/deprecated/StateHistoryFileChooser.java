//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//
//import java.awt.Component;
//import java.io.File;
//
//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;
//import javax.swing.filechooser.FileFilter;
//
//import edu.jhuapl.saavtk.gui.FileChooserBase;
//
//
//public class StateHistoryFileChooser extends FileChooserBase
//{
//    private static class CustomExtensionFilter extends FileFilter
//    {
//        private String extension;
//
//        public CustomExtensionFilter(String extension)
//        {
//            if (extension != null)
//                extension = extension.toLowerCase();
//
//            this.extension = extension;
//        }
//
//        //Accept all directories and all files with specified extension.
//        public boolean accept(File f)
//        {
//            if (f.isDirectory())
//            {
//                return true;
//            }
//
//            String ext = getExtension(f);
//            if (ext != null)
//            {
//                if (ext.equals(extension))
//                {
//                    return true;
//                }
//                else
//                {
//                    return false;
//                }
//            }
//
//            return false;
//        }
//
//        //The description of this filter
//        public String getDescription()
//        {
//            if (extension == null || extension.isEmpty())
//                return "Time History Files";
//            else
//                return extension.toUpperCase() + " Files";
//        }
//
//        private String getExtension(File f)
//        {
//            String ext = null;
//            String s = f.getName();
//            int i = s.lastIndexOf('.');
//
//            if (i > 0 &&  i < s.length() - 1)
//            {
//                ext = s.substring(i+1).toLowerCase();
//            }
//            return ext;
//        }
//    }
//
//    public static File showOpenDialog(Component parent, String title)
//    {
//        return showOpenDialog(parent, title, null);
//    }
//
//    public static File showOpenDialog(Component parent, String title, String extension)
//    {
//        File[] files = showOpenDialog(parent, title, extension, false);
//        if (files == null || files.length < 1)
//            return null;
//        else
//            return files[0];
//    }
//
//    public static File[] showOpenDialog(Component parent, String title, String extension, boolean multiSelectionEnabled)
//    {
//        JFileChooser fc = new JFileChooser();
////        fc.setAcceptAllFileFilterUsed(true);
//        fc.setMultiSelectionEnabled(multiSelectionEnabled);
//        fc.setDialogTitle(title);
//        if (extension != null)
//            fc.addChoosableFileFilter(new CustomExtensionFilter(extension));
//        fc.setCurrentDirectory(getLastDirectory());
//        int returnVal = fc.showOpenDialog(JOptionPane.getFrameForComponent(parent));
//        if (returnVal == JFileChooser.APPROVE_OPTION)
//        {
//            setLastDirectory(fc.getCurrentDirectory());
//            if (multiSelectionEnabled)
//                return fc.getSelectedFiles();
//            else
//                return new File[] {fc.getSelectedFile()};
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    public static File showSaveDialog(Component parent, String title)
//    {
//        return showSaveDialog(parent, title, null, null);
//    }
//
//    public static File showSaveDialog(Component parent, String title, String defaultFilename)
//    {
//        return showSaveDialog(parent, title, defaultFilename, null);
//    }
//
//    public static File showSaveDialog(Component parent, String title, String defaultFilename, String extension)
//    {
//        JFileChooser fc = new JFileChooser();
//        fc.setAcceptAllFileFilterUsed(true);
//        fc.setDialogTitle(title);
//        if (extension != null)
//            fc.addChoosableFileFilter(new CustomExtensionFilter(extension));
//        fc.setCurrentDirectory(getLastDirectory());
//        if (defaultFilename != null)
//            fc.setSelectedFile(new File(defaultFilename));
//        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(parent));
//        if (returnVal == JFileChooser.APPROVE_OPTION)
//        {
//            setLastDirectory(fc.getCurrentDirectory());
//            File file = fc.getSelectedFile();
//
//            String filename = file.getAbsolutePath();
//            if (extension != null && !extension.isEmpty())
//            {
//                if (!filename.toLowerCase().endsWith("." + extension))
//                    filename += "." + extension;
//            }
//            file = new File(filename);
//
//            if (file.exists())
//            {
//                int response = JOptionPane.showConfirmDialog (JOptionPane.getFrameForComponent(parent),
//                  "Overwrite existing file?","Confirm Overwrite",
//                   JOptionPane.OK_CANCEL_OPTION,
//                   JOptionPane.QUESTION_MESSAGE);
//                if (response == JOptionPane.CANCEL_OPTION)
//                    return null;
//            }
//
//            return file;
//        }
//        else
//        {
//            return null;
//        }
//    }
//}
