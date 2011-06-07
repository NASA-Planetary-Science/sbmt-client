package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DirectoryChooser extends FileChooserBase
{
    public static File showOpenDialog(Component parent)
    {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Output Folder");
        fc.setCurrentDirectory(getLastDirectory());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(JOptionPane.getFrameForComponent(parent));
        if (result == JFileChooser.APPROVE_OPTION)
        {
            setLastDirectory(fc.getSelectedFile());
            return fc.getSelectedFile();
        }
        else
        {
            return null;
        }
    }


}
