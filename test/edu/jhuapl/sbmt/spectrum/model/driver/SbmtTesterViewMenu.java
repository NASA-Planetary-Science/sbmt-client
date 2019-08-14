package edu.jhuapl.sbmt.spectrum.model.driver;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterDialog;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;

public class SbmtTesterViewMenu extends ViewMenu
{
    public SbmtTesterViewMenu(SbmtTesterViewManager viewManager, RecentlyViewed viewed)
    {
        // Note the base class constructor calls addMenuItem.
        super("Body", viewManager, viewed);
        ShapeModelImporterDialog.pcl = SbmtTesterViewMenu.this;

    }

    @Override
    protected void addMenuItem(JMenuItem mi, ViewConfig config)
    {
        // Note the base class constructor calls this method. For this reason
        // cannot move the following code block to the constructor and store
        // the SbmtViewManager as a field, as would be more natural.
        ViewManager manager = getRootPanel();
        SbmtTesterViewManager viewManager = null;
        if (manager instanceof SbmtTesterViewManager)
        {
            viewManager = (SbmtTesterViewManager) manager;
        }
        else
        {
            throw new AssertionError();
        }

        // Set up a hierarchy like "Body" -> Asteroids -> Near Earth -> Eros -> Image-based -> Gaskell.
        // Encode this as a list of strings.
        SmallBodyViewConfigTest smallBodyConfig = (SmallBodyViewConfigTest)config;
        List<String> tree = new ArrayList<>();
        if (smallBodyConfig.type != null)
            tree.add(smallBodyConfig.type.toString());
        if (smallBodyConfig.population != null && smallBodyConfig.population != ShapeModelPopulation.NA)
            tree.add(smallBodyConfig.population.toString());
        if (smallBodyConfig.body != null)
            tree.add(smallBodyConfig.body.toString());
        if (smallBodyConfig.dataUsed != null && smallBodyConfig.dataUsed != ShapeModelDataUsed.NA)
            tree.add(smallBodyConfig.dataUsed.toString());

        // Go through the list of strings and generate a hierarchical menu tree.
        JMenu parentMenu = this;
        for (String subMenu : tree)
        {
            JMenu childMenu = getChildMenu(parentMenu, subMenu);
            if (childMenu == null)
            {
                childMenu = new JMenu(subMenu);
                if (viewManager.isAddSeparator(config, subMenu))
                {
                    parentMenu.addSeparator();
                }
                if (viewManager.isAddLabel(config, subMenu))
                {
                    String label = viewManager.getLabel(config);
                    parentMenu.add(label);
                    parentMenu.getItem(parentMenu.getItemCount() - 1).setEnabled(false);
                }
                parentMenu.add(childMenu);
            }
            parentMenu = childMenu;
        }
        parentMenu.add(mi);

        String urlString = config.getShapeModelFileNames()[0];
        FileCache.instance().addStateListener(urlString, state -> {
            try
            {
                Configuration.runAndWaitOnEDT(() -> {
                    mi.setEnabled(state.isAccessible());
                    setSubMenuEnabledState(SbmtTesterViewMenu.this);
                    // This very top menu should always be enabled.
                    SbmtTesterViewMenu.this.setEnabled(true);
                });
            }
            catch (Exception e)
            {
                // Don't clutter up the log with this exception.
            }
       });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.CUSTOM_MODEL_ADDED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            File modelDir = new File(Configuration.getImportedShapeModelsDir() + File.separator + name);
            View view = null;
            if (new File(modelDir, "model.vtk").isFile())
            {
                if (new File(modelDir, "model.json").isFile())
                {
                    view = getRootPanel().createCustomView(modelDir.getName(), false, new File(modelDir, "model.json"));
                    getRootPanel().addMetadataBackedCustomView(view);
                }
                else
                {
                    view = getRootPanel().addCustomView(name);
                }
            }

            addCustomMenuItem(view);
            reloadCustomMenuItems();
        }
        else
        {
            super.propertyChange(evt);
        }
    }
}
