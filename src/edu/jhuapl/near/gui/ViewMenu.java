package edu.jhuapl.near.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.Properties;

public class ViewMenu extends JMenu implements PropertyChangeListener
{
    private ViewManager rootPanel;
    private ButtonGroup group;
    private ShapeModelImporterManagerDialog shapeModelImportedDialog;

    public ViewMenu(ViewManager rootPanel)
    {
        super("View");

        this.rootPanel = rootPanel;

        group = new ButtonGroup();

        for (int i=0; i < rootPanel.getNumberOfBuiltInViews(); ++i)
        {
            View view = rootPanel.getBuiltInView(i);
            JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(view));
            mi.setText(view.getDisplayName());
            if (i==0)
                mi.setSelected(true);
            group.add(mi);

            ModelConfig modelConfig = view.getModelConfig();

            addMenuItem(mi, modelConfig);
        }

        if (Configuration.isAPLVersion())
        {
            this.addSeparator();

            JMenuItem mi = new JMenuItem(new ImportShapeModelsAction());
            this.add(mi);

            if (rootPanel.getNumberOfCustomViews() > 0)
                this.addSeparator();

            for (int i=0; i < rootPanel.getNumberOfCustomViews(); ++i)
            {
                View view = rootPanel.getCustomView(i);
                mi = new JRadioButtonMenuItem(new ShowBodyAction(view));
                mi.setText(view.getDisplayName());
                if (i==0)
                    mi.setSelected(true);
                group.add(mi);
                this.add(mi);
            }
        }
    }

    private void addMenuItem(JMenuItem mi, ModelConfig modelConfig)
    {
        ArrayList<String> tree = new ArrayList<String>();
        if (modelConfig.type != null)
            tree.add(modelConfig.type.toString());
        if (modelConfig.population != null)
            tree.add(modelConfig.population.toString());
        if (modelConfig.body != null && modelConfig.author != null)
            tree.add(modelConfig.body.toString());
        if (modelConfig.dataUsed != null && modelConfig.author != null)
            tree.add(modelConfig.dataUsed.toString());

        JMenu parentMenu = this;
        for (String subMenu : tree)
        {
            JMenu childMenu = getChildMenu(parentMenu, subMenu);
            if (childMenu == null)
            {
                childMenu = new JMenu(subMenu);
                parentMenu.add(childMenu);
            }
            parentMenu = childMenu;
        }

        parentMenu.add(mi);
    }

    private JMenu getChildMenu(JMenu menu, String childName)
    {
        Component[] components = menu.getMenuComponents();
        for (Component comp : components)
        {
            if (comp instanceof JMenu)
            {
                if (((JMenu)comp).getText().equals(childName))
                    return (JMenu)comp;
            }
        }

        return null;
    }

    private void sortCustomMenuItems()
    {
        // First create a list of the custom menu items and remove them
        // from the menu.
        ArrayList<JMenuItem> customMenuItems = new ArrayList<JMenuItem>();
        int numberItems = this.getItemCount();
        for (int i=numberItems-1; i>=0; --i)
        {
            JMenuItem item = this.getItem(i);
            if (item != null)
            {
                Action action = item.getAction();
                if (action instanceof ShowBodyAction)
                {
                    customMenuItems.add(item);
                    this.remove(item);
                }
            }
        }

        // Then sort them
        Collections.sort(customMenuItems, new Comparator<JMenuItem>()
        {
            @Override
            public int compare(JMenuItem o1, JMenuItem o2)
            {
                return o1.getText().compareTo(o2.getText());
            }
        });


        // Now add back in the items
        numberItems = customMenuItems.size();
        for (int i=0; i<numberItems; ++i)
        {
            JMenuItem item = customMenuItems.get(i);
            this.add(item);
        }
    }

    public void addCustomMenuItem(View view)
    {
        if (rootPanel.getNumberOfCustomViews() == 1)
            this.addSeparator();

        JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(view));
        mi.setText(view.getDisplayName());
        group.add(mi);
        this.add(mi);

        sortCustomMenuItems();
    }

    public void removeCustomMenuItem(View view)
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
                    if (view == showBodyAction.view)
                    {
                        this.remove(item);
                        group.remove(item);

                        // Remove the final separator if no custom models remain
                        if (rootPanel.getNumberOfCustomViews() == 0)
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
            View view = rootPanel.addCustomView(name);
            addCustomMenuItem(view);
        }
        else if (Properties.CUSTOM_MODEL_DELETED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            View view = rootPanel.removeCustomView(name);
            removeCustomMenuItem(view);
        }
        else if (Properties.CUSTOM_MODEL_EDITED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            View view = rootPanel.getCustomView(name);

            ModelManager modelManager = view.getModelManager();

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
            }
        }
    }

    private class ShowBodyAction extends AbstractAction
    {
        private View view;

        public ShowBodyAction(View view)
        {
            super(view.getUniqueName());
            this.view = view;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.setCurrentView(view);
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
