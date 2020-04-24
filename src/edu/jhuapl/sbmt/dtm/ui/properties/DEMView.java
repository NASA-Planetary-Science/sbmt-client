package edu.jhuapl.sbmt.dtm.ui.properties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import com.google.common.collect.ImmutableList;

import vtk.vtkObject;
import vtk.vtkProp;
import vtk.vtkRenderer;

import edu.jhuapl.saavtk.colormap.Colorbar;
import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.ScaleDataRangeDialog;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.pick.ControlPointsPicker;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.PolyLineMode;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtModelManager;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

import glum.gui.action.PopupMenu;
import net.miginfocom.swing.MigLayout;

public class DEMView extends JFrame implements ActionListener, PickListener, PropertyChangeListener, WindowListener
{
    // Constants
    private static final String Profile = "Profile";
    private static final String StartLatitude = "StartLatitude";
    private static final String StartLongitude = "StartLongitude";
    private static final String StartRadius = "StartRadius";
    private static final String EndLatitude = "EndLatitude";
    private static final String EndLongitude = "EndLongitude";
    private static final String EndRadius = "EndRadius";
    private static final String Color = "Color";

    // State vars
 	 private final PopupMenu<PolyLine> popupMenu;
    private final LineModel<PolyLine> lineModel;
    private final ModelManager modelManager;
    private final PickManager pickManager;
    private final Picker priPicker;
    private final DEMPlot plot;
    private final DEM priDEM;
    private final DEMKey key;
    private final DEMCollection demCollection;
    private final Colorbar refColorbar;

    // Gui vars
    private ScaleDataRangeDialog scaleDataDialog;
    private JComboBox<Object> colormapBox;
    private JLabel colormapL;

    private JComboBox<?> coloringTypeBox;
    private JButton scaleColoringButton;
    private JCheckBox syncColoringCB;

    private JButton newButton;
    private JToggleButton editButton;
    private JButton deleteAllButton;
    private JButton loadButton;
    private JButton saveButton;
    private JLabel fileL;
    private Renderer renderer;

    public Renderer getRenderer()
    {
        return renderer;
    }

    public DEMView(DEMKey key, DEMCollection demCollection, PolyhedralModel parentPolyhedralModel) throws IOException //, FitsException
    {
        this.key = key;
        this.demCollection = demCollection;

        ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
        setIconImage(erosIcon.getImage());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        StatusBar statusBar = new StatusBar(true);
        add(statusBar, BorderLayout.PAGE_END);

        // Look up dem object in main view
        DEM secDEM = demCollection.getDEM(key);

        // Create an entirely new DEM object to go with this model manager
        // We must do this, things get screwed up if we use the same DEM object in both main and DEM views
        priDEM = new DEM(secDEM); // Use copy constructor, much faster than creating DEM file from scratch

        // Set this primary DEM to have the same properties as the macroDEM
        for(int i=0; i<secDEM.getNumberOfColors(); i++)
        {
            priDEM.setCurrentColoringRange(i, secDEM.getCurrentColoringRange(i));
        }
        priDEM.setColoringIndex(secDEM.getColoringIndex());

        lineModel = new LineModel<>(priDEM, PolyLineMode.PROFILE);
        lineModel.setMaximumVerticesPerLine(2);
        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, priDEM);
        allModels.put(ModelNames.LINE_STRUCTURES, lineModel);

        modelManager = new SbmtModelManager(priDEM, allModels);

        renderer = new Renderer(modelManager);
        renderer.setMinimumSize(new Dimension(0, 0));

        // Popup menu
        popupMenu = new PopupMenu<>(lineModel);
        popupMenu.installPopAction(new SaveGravityProfileAction<>(lineModel, parentPolyhedralModel, renderer), "Save Profile...");

        pickManager = new PickManager(renderer, modelManager);
        PickUtil.installDefaultPickHandler(pickManager, statusBar, renderer, modelManager);
        priPicker = new ControlPointsPicker<>(renderer, pickManager, modelManager, lineModel);

        refColorbar = new Colorbar(renderer);

        plot = new DEMPlot(lineModel, priDEM, secDEM.getColoringIndex());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                renderer, plot.getChartPanel());
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(secDEM.getColoringIndex()), BorderLayout.WEST);
        panel.add(splitPane, BorderLayout.CENTER); // twupy1: This is what messes up main shape model
        add(panel, BorderLayout.CENTER);

        addWindowListener(this);
        createMenus();

        // Finally make the frame visible
        setTitle("DEM View: " + key.name);
//        pack();
        setVisible(true);
        setSize(800, 450);

        // Register for events of interest
        priDEM.addPropertyChangeListener(this);
        lineModel.addPropertyChangeListener(this);
        modelManager.addPropertyChangeListener(this);
        pickManager.getDefaultPicker().addPropertyChangeListener(this);
        pickManager.getDefaultPicker().addListener(this);

        updateControlPanel(null);

        // Force the renderer's camera to the "reset" default view
        renderer.getCamera().reset();
    }

    @Override
    public void actionPerformed(ActionEvent aEvent)
    {
        Object source = aEvent.getSource();

        // ColorMap ComboBox UI
        if (source == colormapBox)
        {
            doUpdateColoringConfig();
            updateControlPanel(source);
        }
        else if (source == coloringTypeBox)
        {
            doUpdateColoringConfig();
            updateControlPanel(source);
        }
        else if (source == loadButton)
        {
            File file = CustomFileChooser.showOpenDialog(loadButton, "Load Profiles");
            if (file == null)
                return;
            try
            {
                loadView(file);
                fileL.setText("File: " + file.getName());
                fileL.setToolTipText(file.getPath());
            }
            catch (Exception aExp)
            {
                JOptionPane.showMessageDialog(
                        JOptionPane.getFrameForComponent(loadButton),
                        "Unable to load file " + file.getAbsolutePath(),
                        "Error Loading File", JOptionPane.ERROR_MESSAGE);
                aExp.printStackTrace();
            }
        }
        else if (source == saveButton)
        {
            File file = CustomFileChooser.showSaveDialog(saveButton, "Save Profiles", "profiles.txt");
            if (file == null)
                return;
            try
            {
                saveView(file);
                fileL.setText("File: " + file.getName());
                fileL.setToolTipText(file.getPath());
            }
            catch (Exception aExp)
            {
                JOptionPane.showMessageDialog(
                        JOptionPane.getFrameForComponent(saveButton),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File", JOptionPane.ERROR_MESSAGE);
                aExp.printStackTrace();
            }
        }
        else if (source == syncColoringCB)
        {
            // Synchronize the coloring if selected
            if (syncColoringCB.isSelected() == true)
                doSynchronizeColoring();
        }
        else if (source == scaleColoringButton)
        {
            // Lazy init
            if (scaleDataDialog == null)
                scaleDataDialog = new ScaleDataRangeDialog(JOptionPane.getFrameForComponent(scaleColoringButton));

            boolean isSyncColoring = syncColoringCB.isSelected();
            scaleDataDialog.setModelConfiguration(priDEM, demCollection.getDEM(key), isSyncColoring);
            scaleDataDialog.setVisible(true);
        }
        else if (source == newButton)
        {
           removeFaultyProfiles();
           doActionAddNewLine();
        }
        else if (source == editButton)
        {
            removeFaultyProfiles();

            Picker tmpPicker = null;
            if (editButton.isSelected() == true)
            	tmpPicker = priPicker;
            pickManager.setActivePicker(tmpPicker);
        }
        else if (source == deleteAllButton)
        {
            removeAllProfiles();
        }
    }

 	@Override
 	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
 	{
 		// Bail if no picked target
 		if (aPrimaryTarg == PickTarget.Invalid)
 			return;

 		// Bail if the picked item does not correspond to the lineModel's actor
 		if (aPrimaryTarg.getActor() != lineModel.getVtkItemActor())
 			return;

 		// Bail if not a valid pick action
 		if (PickUtil.isPopupTrigger(aEvent) == false || aMode != PickMode.ActiveSec)
 			return;

		// Update the popup to reflect the selected items
 		int cellId = aPrimaryTarg.getCellId();
 		PolyLine tmpItem = lineModel.getItem(cellId);
 		lineModel.setSelectedItems(ImmutableList.of(tmpItem));

 		// Show the popup
 		Component tmpComp = aEvent.getComponent();
 		int posX = ((MouseEvent) aEvent).getX();
 		int posY = ((MouseEvent) aEvent).getY();
 		popupMenu.show(tmpComp, posX, posY);
 	}

    /**
     * Helper method to create the control panel.
     */
    private JPanel createControlPanel(int initialSelectedOption)
    {
        JPanel retPanel = new JPanel(new MigLayout("", "[fill]", ""));

        // Feature area
        String[] coloringOptions = getColoringOptionsFor(priDEM);
        coloringTypeBox = new JComboBox<>(coloringOptions);
        coloringTypeBox.setSelectedIndex(initialSelectedOption < 0 ? coloringOptions.length-1 : initialSelectedOption);
        coloringTypeBox.addActionListener(this);
        retPanel.add(coloringTypeBox, "w 0:0:,wrap");

        scaleColoringButton = new JButton("Rescale Data Range");
        scaleColoringButton.addActionListener(this);
        retPanel.add(scaleColoringButton, "wrap");

        syncColoringCB = new JCheckBox("Sync Coloring");
        syncColoringCB.addActionListener(this);
        syncColoringCB.setToolTipText("Sync coloring with main window");
        retPanel.add(syncColoringCB, "wrap 20");

        // ColorMap area
        colormapL = new JLabel();
        retPanel.add(colormapL, "wrap");

        colormapBox = new JComboBox<>();
        colormapBox.setRenderer(ColormapUtil.getFancyColormapRender2(colormapBox));
        for (String aStr : Colormaps.getAllBuiltInColormapNames())
        {
            Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
            colormapBox.addItem(cmap);
            if (cmap.getName().equals(Colormaps.getCurrentColormapName()))
                colormapBox.setSelectedItem(cmap);
        }
        colormapBox.addActionListener(this);
        retPanel.add(colormapBox, "w 0:0:,wrap 20");

        // Profile edit area
        newButton = new JButton("New Profile");
        newButton.addActionListener(this);
        retPanel.add(newButton, "sg 2,wrap");

        editButton = new JToggleButton("Edit Profiles");
        editButton.addActionListener(this);
        retPanel.add(editButton, "sg 2,wrap");

        deleteAllButton = new JButton("Delete All Profiles");
        deleteAllButton.addActionListener(this);
        retPanel.add(deleteAllButton, "sg 2,wrap 20");

        // File area
        loadButton = new JButton("Load...");
        loadButton.addActionListener(this);
        loadButton.setToolTipText("Load Profile Data");
        saveButton = new JButton("Save...");
        saveButton.addActionListener(this);
        saveButton.setToolTipText("Save Profile Data");
        retPanel.add(loadButton, "sg 3,span,split");
        retPanel.add(saveButton, "sg 3,wrap");

        fileL = new JLabel("");
        retPanel.add(fileL, "w 0:0:");

        return retPanel;
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        // File
        JMenu fileMenu = new JMenu("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(renderer));
        fileMenu.add(mi);

        JMenu saveShapeModelMenu = new JMenu("Export Shape Model to");
        fileMenu.add(saveShapeModelMenu);

        mi = new JMenuItem(new SaveShapeModelAsPLTAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SaveShapeModelAsOBJAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SaveShapeModelAsSTLAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SavePlateDataAction());
        fileMenu.add(mi);

        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

   	/**
   	 * Helper method that handles the "new profile" action.
   	 * <P>
   	 * The action will result in the (immediate) addition of a new line in our
   	 * lineManager(line).
   	 */
   	private void doActionAddNewLine()
   	{
   		// Instantiate the new line
   		int tmpId = StructureMiscUtil.calcNextId(lineModel);
   		List<LatLon> controlPointL = new ArrayList<>();
   		PolyLine tmpItem = new PolyLine(tmpId, null, controlPointL);
   		tmpItem.setColor(getDefaultColor(tmpId));

   		// Install the line
   		List<PolyLine> fullL = new ArrayList<>(lineModel.getAllItems());
   		fullL.add(tmpItem);

   		lineModel.setAllItems(fullL);
   		lineModel.setActivatedItem(tmpItem);

   		// Update other state vars
   		pickManager.setActivePicker(priPicker);
   		editButton.setSelected(true);
   	}

    /**
     * Helper method that will synchronize the DEM(s) to reflect the coloring as
     * specified on the GUI.
     */
    private void doUpdateColoringConfig()
    {
        // Update the primary DEM's ColorMap if necessary
        String name = ((Colormap) colormapBox.getSelectedItem()).getName();
        if (name.equals(priDEM.getColormap().getName()) == false)
        {
            Colormap tmpColormap = Colormaps.getNewInstanceOfBuiltInColormap(name);
            priDEM.setColormap(tmpColormap);
        }

        try
        {
            int index = coloringTypeBox.getSelectedIndex();
            if (index == coloringTypeBox.getItemCount() - 1)
            {
                // No coloring
                priDEM.setColoringIndex(-1);
                plot.setColoringIndex(-1);
            }
            else
            {
                // Coloring
                priDEM.setColoringIndex(index);
                plot.setColoringIndex(index);

                // Reset the primary model's coloring range to the defaults
                double[] tmpArr = priDEM.getDefaultColoringRange(index);
                priDEM.setCurrentColoringRange(index, tmpArr);
            }

            // Synchronize the coloring
            if (syncColoringCB.isSelected() == true)
                doSynchronizeColoring();
        }
        catch (IOException aExp)
        {
            aExp.printStackTrace();
        }
    }

    /**
     * Helper method to configure the Colorbar.
     */
    private void doConfigureColorbar()
    {
        // Disable the Colorbar if it is not needed
        if (priDEM.isColoringDataAvailable() == false || priDEM.getColoringIndex() < 0)
        {
            refColorbar.setVisible(false);
            return;
        }

        // Customize the Colorbar to reflect the dem model
        if (!refColorbar.isVisible())
            refColorbar.setVisible(true);

        refColorbar.setColormap(priDEM.getColormap());
        int index = priDEM.getColoringIndex();
        String title = priDEM.getColoringName(index).trim();
        String units = priDEM.getColoringUnits(index).trim();
        if (units != null && !units.isEmpty())
            title += " (" + units + ")";
        refColorbar.setTitle(title);

        registerIfNotRegistered(renderer.getRenderWindowPanel().getRenderer(), refColorbar.getActor());
        refColorbar.getActor().SetNumberOfLabels(priDEM.getColormap().getNumberOfLabels());
    }

    /**
     * Helper method to ensure the ModelManager's VTK state is kept in sync
     */
    private void doConfigureModelManager()
    {
        for (vtkProp aVtkProp : modelManager.getProps())
            registerIfNotRegistered(renderer.getRenderWindowPanel().getRenderer(), aVtkProp);
    }

    /**
     * Helper method to synchronize the coloring of the base DEM to reflect our
     * internal DEM.
     */
    private void doSynchronizeColoring()
    {
        // Bail if the coloring should not be synchronizing
        if (syncColoringCB.isSelected() == false)
            return;

        // Get the secondary DEM
        DEM secDEM = demCollection.getDEM(key);

        // Update the ColorMap to reflect the primary DEM
        Colormap priColorMap = priDEM.getColormap();
        Colormap secColorMap = secDEM.getColormap();
        if (secColorMap == null || priColorMap.getName().equals(secColorMap.getName()) == false)
        {
            Colormap tmpColorMap = Colormaps.getNewInstanceOfBuiltInColormap(priColorMap.getName());
            secDEM.setColormap(tmpColorMap);
        }

        try
        {
            // Update the secondary DEM's coloring index to match the primary DEM
            int coloringIndex = priDEM.getColoringIndex();
            secDEM.setColoringIndex(coloringIndex);
            if (coloringIndex == -1)
                return;

            // Synchronize coloring ranges
            for (int i = 0; i < priDEM.getNumberOfColors(); i++)
                secDEM.setCurrentColoringRange(i, priDEM.getCurrentColoringRange(i));

            // Reset the primary model's coloring range to the defaults
            double[] tmpArr = priDEM.getDefaultColoringRange(coloringIndex);
            secDEM.setCurrentColoringRange(coloringIndex, tmpArr);
        }
        catch (Exception aExp)
        {
            aExp.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "An error occurred synchronizing macro view DEM coloring with micro view.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper method that returns the default color for the specified index.
     *
     * @param aIdx
     */
    private Color getDefaultColor(int aIdx)
    {
        int numColors = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length;
        int tmpIdx = aIdx % numColors;

        Color retColor = (Color)DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[tmpIdx];
        return retColor;
    }

    private void saveView(File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        String eol = System.getProperty("line.separator");

        int tmpCnt = -1;
        for (PolyLine aLine : lineModel.getAllItems())
        {
            tmpCnt++;

            // Ignore invalid profiles
            if (aLine.getControlPoints().size() != 2)
                continue;

            LatLon ll0 = aLine.getControlPoints().get(0);
            LatLon ll1 = aLine.getControlPoints().get(1);
            Color color = aLine.getColor();
            out.write(eol + Profile + "=" + tmpCnt + eol);
            out.write(StartLatitude + "=" + ll0.lat + eol);
            out.write(StartLongitude + "=" + ll0.lon + eol);
            out.write(StartRadius + "=" + ll0.rad + eol);
            out.write(EndLatitude + "=" + ll1.lat + eol);
            out.write(EndLongitude + "=" + ll1.lon + eol);
            out.write(EndRadius + "=" + ll1.rad + eol);
            out.write(Color + "=" +
                    color.getRed() + " " +
                    color.getGreen() + " " +
                    color.getBlue() + " " +
                    color.getAlpha() + eol);
            out.write(plot.getProfileAsString(aLine));
        }

        out.close();
    }

    private void loadView(File file) throws IOException
    {
        removeAllProfiles();

        InputStream fs = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        double[] llBegArr = new LatLon().get();
        double[] llEndArr = new LatLon().get();
        int lineId = 0;

        List<PolyLine> itemL = new ArrayList<>();
        while ((line = in.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty())
                continue;
            String[] tokens = line.trim().split("=");
            if (tokens.length != 2)
            {
                in.close();
                throw new IOException("Error parsing file");
            }
            //System.out.println(tokens[0]);

            String key = tokens[0].trim();
            String value = tokens[1].trim();

            if (StartLatitude.equals(key))
                llBegArr[0] = Double.parseDouble(value);
            else if (StartLongitude.equals(key))
                llBegArr[1] = Double.parseDouble(value);
            else if (StartRadius.equals(key))
                llBegArr[2] = Double.parseDouble(value);
            else if (EndLatitude.equals(key))
                llEndArr[0] = Double.parseDouble(value);
            else if (EndLongitude.equals(key))
                llEndArr[1] = Double.parseDouble(value);
            else if (EndRadius.equals(key))
                llEndArr[2] = Double.parseDouble(value);
            else if (Color.equals(key))
            {
                String[] c = value.split("\\s+");
                int rVal = Integer.parseInt(c[0]);
                int gVal = Integer.parseInt(c[1]);
                int bVal = Integer.parseInt(c[2]);
                int aVal = Integer.parseInt(c[3]);
                Color color = new Color(rVal, gVal, bVal, aVal);

                LatLon begLL = new LatLon(llBegArr);
                LatLon endLL = new LatLon(llEndArr);
                List<LatLon> controlPointL = ImmutableList.of(begLL, endLL);
                PolyLine tmpItem = new PolyLine(lineId, null, controlPointL);
                tmpItem.setColor(color);
                itemL.add(tmpItem);

                lineId++;
            }
        }

        in.close();

        lineModel.setAllItems(itemL);
    }

 	/**
 	 * Helper method to remove lines without 2 vertices.
 	 * <P>
 	 * It's possible that sometimes, faulty lines without 2 vertices get created.
 	 * Remove them here.
 	 * <P>
 	 * TODO: The fact that this method exists indicates faulty design / logic!
 	 */
     private void removeFaultyProfiles()
     {
    	 List<PolyLine> badItemL = new ArrayList<>();
    	 for (PolyLine aItem : lineModel.getAllItems())
    	 {
    		 if (aItem.getControlPoints().size() != 2)
    			 badItemL.add(aItem);
    	 }

    	 lineModel.removeItems(badItemL);
     }

    private void removeAllProfiles()
    {
        lineModel.removeAllStructures();
        pickManager.setActivePicker(null);
        editButton.setSelected(false);
    }

    /**
     * Helper method to update the control panel.
     */
    private void updateControlPanel(Object aSource)
    {
        // Update the colormap icon (if necessary)
        if (aSource == colormapBox || aSource == null)
        {
            int iconW = colormapL.getWidth();
            int iconH = colormapL.getHeight();
            if (iconH < 16)
                iconH = 16;

            Colormap evalColormap = (Colormap) colormapBox.getSelectedItem();
            colormapL.setIcon(ColormapUtil.createIcon(evalColormap, iconW, iconH));
        }

        // Update enable state of various UI elements
        boolean isEnabled = coloringTypeBox.getSelectedIndex() != coloringTypeBox.getItemCount() - 1;
        scaleColoringButton.setEnabled(isEnabled);
        colormapBox.setEnabled(isEnabled);
        colormapL.setEnabled(isEnabled);
    }

    private class SaveImageAction extends AbstractAction
    {
        private Renderer renderer;

        public SaveImageAction(Renderer renderer)
        {
            super("Export to Image...");
            this.renderer = renderer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            renderer.saveToFile();
        }
    }

    private class SaveShapeModelAsPLTAction extends AbstractAction
    {
        public SaveShapeModelAsPLTAction()
        {
            super("PLT (Gaskell Format)...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to PLT", "model.plt");

            try
            {
                if (file != null)
                    priDEM.saveAsPLT(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsOBJAction extends AbstractAction
    {
        public SaveShapeModelAsOBJAction()
        {
            super("OBJ...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to OBJ", "model.obj");

            try
            {
                if (file != null)
                    priDEM.saveAsOBJ(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsSTLAction extends AbstractAction
    {
        public SaveShapeModelAsSTLAction()
        {
            super("STL...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to STL", "model.stl");

            try
            {
                if (file != null)
                    priDEM.saveAsSTL(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SavePlateDataAction extends AbstractAction
    {
        public SavePlateDataAction()
        {
            super("Export Plate Data...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String name = "platedata.csv";
            File file = CustomFileChooser.showSaveDialog(DEMView.this, "Export Plate Data", name);

            try
            {
                if (file != null)
                    priDEM.savePlateData(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(DEMView.this,
                        "An error occurred exporting the plate data.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aEvent)
    {
        Object source = aEvent.getSource();

        // Configure the Colorbar whenever the DEM model has changed
        if (source == priDEM && aEvent.getPropertyName().equals(Properties.MODEL_CHANGED))
            doConfigureColorbar();

        if (source == modelManager)
            doConfigureModelManager();

        // Force a repaint
        renderer.getRenderWindowPanel().Render();
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
        ; // Nothing to do
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        // Notify the Renderer that it will no longer be needed
        renderer.dispose();

        // Remove self as the macro DEM's view
        DEM secDEM = demCollection.getDEM(key);
        if (secDEM != null)
            secDEM.removeView();

        // Garbage collect
        System.gc();
        vtkObject.JAVA_OBJECT_MANAGER.gc(true);
    }

    /**
     * Utility method that will register the vtkProp with vtkRenderer.
     * <P>
     * In order for vtkProps to be rendered to the scene they must be added as
     * actors.
     * <P>
     * TODO: Promote this to a utility class if more equivalents of this logic
     * block is found.
     *
     * @param aVtkRenderer
     * @param aVtkProp
     */
    public static void registerIfNotRegistered(vtkRenderer aVtkRenderer, vtkProp aVtkProp)
    {
        // Bail if the vtkProp has already been registered
        if (aVtkRenderer.HasViewProp(aVtkProp) != 0)
            return;

        // Register the vtkProp to be rendered
        aVtkRenderer.AddActor(aVtkProp);
    }

    /**
     * Utility method that returns a list of available coloring options for the
     * specified DEM.
     * <P>
     * Included in this list is the None selection ("No coloring").
     * <P>
     * TODO: Move this to a proper utility class.
     *
     * @param aDEM
     */
    public static String[] getColoringOptionsFor(DEM aDEM)
    {
        String[] coloringNames = aDEM.getColoringNames();
        int numColors = coloringNames.length;

        String[] retColoringOptions = new String[numColors + 1];
        for (int i = 0; i < numColors; i++)
            retColoringOptions[i] = coloringNames[i];
        retColoringOptions[numColors] = "No coloring";

        return retColoringOptions;
    }

}
