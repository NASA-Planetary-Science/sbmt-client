package edu.jhuapl.sbmt.client;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewMenu;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterDialog;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;

public class SbmtViewMenu extends ViewMenu
{
    public SbmtViewMenu(SbmtViewManager viewManager, RecentlyViewed viewed)
    {
        // Note the base class constructor calls addMenuItem.
        super("Body", viewManager, viewed);
        ShapeModelImporterDialog.pcl = SbmtViewMenu.this;

    }

    @Override
	protected void initialize()
	{
		customImageMenu = new JMenu("Custom Shape Models");
		this.add(customImageMenu);

		JMenuItem mi = new JMenuItem(new ImportShapeModelsAction());
		customImageMenu.add(mi);

		if (rootPanel.getNumberOfCustomViews() > 0)
			customImageMenu.addSeparator();

		for (int i = 0; i < rootPanel.getNumberOfCustomViews(); ++i)
		{
			View view = rootPanel.getCustomView(i);
			mi = new JMenuItem(new ShowBodyAction(view));
			mi.setText(view.getModelDisplayName());
			if (i == 0)
				mi.setSelected(true);
			customImageMenu.add(mi);
		}

		for (int i = 0; i < getRootPanel().getNumberOfBuiltInViews(); ++i)
		{
			View view = getRootPanel().getBuiltInView(i);
			mi = new JMenuItem(new ShowBodyAction(view));
			mi.setText(view.getDisplayName());
			if (i == 0)
				mi.setSelected(true);

			BasicConfigInfo smallBodyConfig = ((SbmtView)view).getConfigInfo();

			addMenuItem(mi, smallBodyConfig);

//			ViewConfig smallBodyConfig = view.getConfig();
//
//			addMenuItem(mi, smallBodyConfig);
		}

		setSubMenuEnabledState(this);
		// This very top menu should always be enabled.
		this.setEnabled(true);
	}

    protected void addMenuItem(JMenuItem mi, BasicConfigInfo config)
    {
        // Note the base class constructor calls this method. For this reason
        // cannot move the following code block to the constructor and store
        // the SbmtViewManager as a field, as would be more natural.

        // Set up a hierarchy like "Body" -> Asteroids -> Near Earth -> Eros -> Image-based -> Gaskell.
        // Encode this as a list of strings.

        List<String> tree = new ArrayList<>();
        if (config.type != null)
            tree.add(config.type.toString());
        if (config.population != null && config.population != ShapeModelPopulation.NA)
            tree.add(config.population.toString());
        if (config.body != null)
            tree.add(config.body.toString());
        if (config.dataUsed != null && config.dataUsed != ShapeModelDataUsed.NA)
            tree.add(config.dataUsed.toString());

        // Go through the list of strings and generate a hierarchical menu tree.
        JMenu parentMenu = this;
        for (String subMenu : tree)
        {
            JMenu childMenu = getChildMenu(parentMenu, subMenu);
            if (childMenu == null)
            {
                childMenu = new JMenu(subMenu);
                // Removed separator and label facilities (which were broken anyway)
                // while working on Redmine issue #1916. Should separators and/or labels
                // become necessary, probably best to augment the config body metadata
                // and the BasicConfigInfo class to flag these two options. Leaving the
                // original code just to provide the way it used to be as a model.
//                if (viewManager.isAddSeparator(config, subMenu))
//                {
//                    parentMenu.addSeparator();
//                }
//                if (viewManager.isAddLabel(config, subMenu))
//                {
//                    String label = viewManager.getLabel(config);
//                    parentMenu.add(label);
//                    parentMenu.getItem(parentMenu.getItemCount() - 1).setEnabled(false);
//                }
                parentMenu.add(childMenu);
            }
            parentMenu = childMenu;
        }
        parentMenu.add(mi);

        String urlString = config.getConfigURL();
        FileCache.instance().addStateListener(urlString, state -> {
            try
            {
                Configuration.runAndWaitOnEDT(() -> {
                    mi.setEnabled(state.isAccessible());
                    setSubMenuEnabledState(SbmtViewMenu.this);
                    // This very top menu should always be enabled.
                    SbmtViewMenu.this.setEnabled(true);
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
