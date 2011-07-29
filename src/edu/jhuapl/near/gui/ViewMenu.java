package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.Properties;

public class ViewMenu extends JMenu implements PropertyChangeListener
{
    private ViewerManager rootPanel;
    private ButtonGroup group;
    private ShapeModelImporterManagerDialog shapeModelImportedDialog;

    public ViewMenu(ViewerManager rootPanel)
    {
        super("View");

        this.rootPanel = rootPanel;

        group = new ButtonGroup();

        for (int i=0; i < rootPanel.getNumberOfBuiltInViewers(); ++i)
        {
            Viewer viewer = rootPanel.getBuiltInViewer(i);
            JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(viewer));
            if (i==0)
                mi.setSelected(true);
            group.add(mi);
            this.add(mi);
        }

        if (Configuration.isAPLVersion())
        {
            this.addSeparator();

            JMenuItem mi = new JMenuItem(new ImportShapeModelsAction());
            this.add(mi);

            if (rootPanel.getNumberOfCustomViewers() > 0)
                this.addSeparator();

            for (int i=0; i < rootPanel.getNumberOfCustomViewers(); ++i)
            {
                Viewer viewer = rootPanel.getCustomViewer(i);
                mi = new JRadioButtonMenuItem(new ShowBodyAction(viewer));
                if (i==0)
                    mi.setSelected(true);
                group.add(mi);
                this.add(mi);
            }
        }
    }

    public void addCustomMenuItem(Viewer viewer)
    {
        if (rootPanel.getNumberOfCustomViewers() == 1)
            this.addSeparator();

        JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(viewer));
        group.add(mi);
        this.add(mi);
    }

    public void removeCustomMenuItem(Viewer viewer)
    {
        int numberItems = this.getItemCount();
        for (int i=0; i<numberItems; ++i)
        {
            JMenuItem item = this.getItem(i);
            if (item != null)
            {
                Action action = item.getAction();
                if (action instanceof ShowBodyAction)
                {
                    ShowBodyAction showBodyAction = (ShowBodyAction) action;
                    if (viewer == showBodyAction.viewer)
                    {
                        this.remove(item);
                        group.remove(item);

                        // Remove the final separator if no custom models remain
                        if (rootPanel.getNumberOfCustomViewers() == 0)
                            removeFinalSeparator();

                        return;
                    }
                }
            }
        }
    }

    public void removeFinalSeparator()
    {
        int numberItems = this.getItemCount();
        for (int i=numberItems-1; i>=0; --i)
        {
            JMenuItem item = this.getItem(i);
            if (item == null)
            {
                this.remove(i);
                return;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.CUSTOM_MODEL_ADDED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            Viewer viewer = rootPanel.addCustomViewer(name);
            addCustomMenuItem(viewer);
        }
        else if (Properties.CUSTOM_MODEL_DELETED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            Viewer viewer = rootPanel.removeCustomViewer(name);
            removeCustomMenuItem(viewer);
        }
    }

    private class ShowBodyAction extends AbstractAction
    {
        private Viewer viewer;

        public ShowBodyAction(Viewer viewer)
        {
            super(viewer.getName());
            this.viewer = viewer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.setCurrentViewer(viewer);
        }
    }

    private class ImportShapeModelsAction extends AbstractAction
    {
        public ImportShapeModelsAction()
        {
            super("Import Shape Models...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            if (shapeModelImportedDialog == null)
            {
                shapeModelImportedDialog = new ShapeModelImporterManagerDialog(JOptionPane.getFrameForComponent(rootPanel));
                shapeModelImportedDialog.addPropertyChangeListener(ViewMenu.this);
            }

            shapeModelImportedDialog.setVisible(true);
        }
    }
}
