package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import edu.jhuapl.near.gui.custom.CustomViewer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.CylindricalImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;
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

        JMenu gaskellMenu = new JMenu("Gaskell Shape Models");
        JMenu thomasMenu = new JMenu("Thomas Shape Models");
        JMenu stookeMenu = new JMenu("Stooke Shape Models");
        JMenu hudsonMenu = new JMenu("Hudson (Radar) Shape Models");
        JMenu moreMenu = new JMenu("More Shape Models");

        for (int i=0; i < rootPanel.getNumberOfBuiltInViewers(); ++i)
        {
            Viewer viewer = rootPanel.getBuiltInViewer(i);
            JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(viewer));
            mi.setText(viewer.getDisplayName());
            if (i==0)
                mi.setSelected(true);
            group.add(mi);
            this.add(mi);

            if ("Gaskell".equals(viewer.getSubmenu()))
                gaskellMenu.add(mi);
            else if ("Thomas".equals(viewer.getSubmenu()))
                thomasMenu.add(mi);
            else if ("Stooke".equals(viewer.getSubmenu()))
                stookeMenu.add(mi);
            else if ("Hudson".equals(viewer.getSubmenu()))
                hudsonMenu.add(mi);
            else if ("Other".equals(viewer.getSubmenu()))
                moreMenu.add(mi);
            else
                System.out.println("Error: invalid submenu");
        }

        this.add(gaskellMenu);
        this.add(thomasMenu);
        this.add(stookeMenu);
        this.add(hudsonMenu);
        this.add(moreMenu);

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
                mi.setText(viewer.getDisplayName());
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
        mi.setText(viewer.getDisplayName());
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
        else if (Properties.CUSTOM_MODEL_EDITED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            CustomViewer viewer = (CustomViewer) rootPanel.getCustomViewer(name);

            ModelManager modelManager = viewer.getModelManager();

            // If model manager is null, it means the model has not been displayed yet,
            // so no need to reset anything.
            if (modelManager != null)
            {
                SmallBodyModel smallBodyModel =
                        (SmallBodyModel) modelManager.getModel(ModelNames.SMALL_BODY);
                try
                {
                    smallBodyModel.reloadShapeModel();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                CylindricalImageCollection imageCollection =
                        (CylindricalImageCollection) modelManager.getModel(ModelNames.CYLINDRICAL_IMAGES);
                imageCollection.reset();
            }
        }
    }

    private class ShowBodyAction extends AbstractAction
    {
        private Viewer viewer;

        public ShowBodyAction(Viewer viewer)
        {
            super(viewer.getUniqueName());
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
                shapeModelImportedDialog = new ShapeModelImporterManagerDialog(null);
                shapeModelImportedDialog.addPropertyChangeListener(ViewMenu.this);
            }

            shapeModelImportedDialog.setLocationRelativeTo(rootPanel);
            shapeModelImportedDialog.setVisible(true);
        }
    }
}
