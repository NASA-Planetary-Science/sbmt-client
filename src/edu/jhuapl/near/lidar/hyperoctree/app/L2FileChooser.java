package edu.jhuapl.near.lidar.hyperoctree.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class L2FileChooser extends JFileChooser implements PropertyChangeListener
{
    Preferences preferences;
    final static String LAST_USED_FOLDER="lastUsedFolder";

    public L2FileChooser()
    {
        preferences=Preferences.userRoot().node(getClass().getName());
        String lastPath=preferences.get(LAST_USED_FOLDER, "none");
        if (lastPath=="none")
        {
            lastPath="/";
            preferences.put(LAST_USED_FOLDER, lastPath);
        }
        setCurrentDirectory(new File(lastPath));
        //
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setFileFilter(new FileNameExtensionFilter("OLA L2 Files", ".l2"));
        setControlButtonsAreShown(false);
        addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY))
            preferences.put(LAST_USED_FOLDER, getCurrentDirectory().getAbsolutePath());
    }
}
