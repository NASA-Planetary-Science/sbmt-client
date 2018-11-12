package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import vtk.vtkActor;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dem.BigmapSwingWorker;
import edu.jhuapl.sbmt.gui.dem.MapmakerSwingWorker;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationControlPanel;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationControlController implements ActionListener, PropertyChangeListener
{
	DtmCreationControlPanel panel;
	DtmCreationModel model;
	PickManager pickManager;

	public DtmCreationControlController(SmallBodyViewConfig config, DtmCreationModel model, final PickManager pickManager)
	{
		panel = new DtmCreationControlPanel(config.hasMapmaker, config.hasBigmap);
		this.model = model;
		this.pickManager = pickManager;
	}

	public JPanel getPanel()
	{
		return panel;
	}

	private void initControls()
	{
		pickManager.getDefaultPicker().addPropertyChangeListener(this);

		panel.getLoadButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				File[] files = CustomFileChooser.showOpenDialog(panel, "Load DEM(s)", new ArrayList<String>(Arrays.asList("fit","fits")), true);

		        if (files == null)
		        {
		            return;
		        }

		        for(File file : files)
		        {
		            // Check if the file provided is valid
		            if (file == null || !file.exists())
		            {
		                // Not valid, do nothing
		                continue;
		            }
		            else
		            {
		                // Valid, load it in
		                DEMInfo demInfo = new DEMInfo();
		                demInfo.demfilename = file.getAbsolutePath();
		                demInfo.name = file.getName();

		                // Save it to the list of DEMs
		                try
		                {
		                    saveDEM(demInfo);
		                }
		                catch (IOException e2)
		                {
		                    e2.printStackTrace();
		                }
		            }
		        }

			}
		});

		panel.getBigmapSubmitButton().addActionListener(this);
		panel.getMapmakerSubmitButton().addActionListener(this);

		panel.getRenameButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(imageList.getSelectedIndex());

		        String newDEMName = JOptionPane.showInputDialog(panel, "Enter a new name", "Rename the DEM", JOptionPane.PLAIN_MESSAGE);
		        if (newDEMName != null && !newDEMName.equals(""))
		        {
		            demInfo.name = newDEMName;
		            model.updateConfigFile();
		        }

			}
		});
	}

	public void actionPerformed(ActionEvent e)
    {
        // We only expect actions from Mapmaker and Bigmap submit buttons
        if(e.getSource() != panel.getMapmakerSubmitButton() &&
                e.getSource() != panel.getBigmapSubmitButton())
        {
            System.err.println("Unrecognized action event source");
            return;
        }

        model.getPickManager().setPickMode(PickMode.DEFAULT);
        panel.getSelectRegionButton().setSelected(false);

        // First get the center point and radius of the selection circle
        double [] centerPoint = null;
        double radius = 0.0;

        if (!panel.getSetSpecifyRegionManuallyCheckbox().isSelected())
        {
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

                centerPoint = region.center;
                radius = region.radius;
            }
            else
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "Please select a region on the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Start and manage the appropriate swing worker
        if(e.getSource() == panel.getMapmakerSubmitButton())
        {
            String demName = JOptionPane.showInputDialog(panel, "Enter a name", "Name the DEM", JOptionPane.PLAIN_MESSAGE);
            if (demName != null && !demName.equals(""))
            {
                runMapmakerSwingWorker(demName, centerPoint, radius, new File(model.getCustomDataFolder()));
            }
            else
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "Please enter a name.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


        }
        else if(e.getSource() == panel.getBigmapSubmitButton())
        {
            String demName = JOptionPane.showInputDialog(panel, "Enter a name", "Name the DEM", JOptionPane.PLAIN_MESSAGE);

            if (demName != null && !demName.equals(""))
            {
                runBigmapSwingWorker(demName, centerPoint, radius, new File(model.getCustomDataRootFolder()), new File(model.getCustomDataFolder()));
            }
            else
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "Please enter a name.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

	// Starts and manages a MapmakerSwingWorker
    private void runMapmakerSwingWorker(String demName, double[] centerPoint, double radius, File outputFolder)
    {
        // Download the entire map maker suite to the users computer
        // if it has never been downloaded before.
        // Ask the user beforehand if it's okay to continue.
        final MapmakerSwingWorker mapmakerWorker =
                new MapmakerSwingWorker(panel, "Running Mapmaker", model.getMapmakerPath());

        // If we need to download, prompt the user that it will take a long time
        if (mapmakerWorker.getIfNeedToDownload())
        {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(panel),
                    "Before Mapmaker can be run for the first time, a very large file needs to be downloaded.\n" +
                    "This may take several minutes. Would you like to continue?",
                    "Confirm Download",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        if (panel.getSetSpecifyRegionManuallyCheckbox().isSelected())
        {
            if (panel.getLatitudeTextField().getText().isEmpty() || panel.getLongitudeTextField().getText().isEmpty() || panel.getPixelScaleTextField().getText().isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "Please enter values for the latitude, longitude, and pixel scale.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            mapmakerWorker.setLatitude(model.getLatitude());
            mapmakerWorker.setLongitude(model.getLongitude());
            mapmakerWorker.setPixelScale(model.getPixelScale());
            mapmakerWorker.setRegionSpecifiedWithLatLonScale(true);
        }
        else
        {
            mapmakerWorker.setCenterPoint(centerPoint);
            mapmakerWorker.setRadius(radius);
        }
        mapmakerWorker.setName(demName);
        mapmakerWorker.setHalfSize(model.getHalfSize());
        mapmakerWorker.setOutputFolder(outputFolder);

        mapmakerWorker.executeDialog();

        if (mapmakerWorker.isCancelled())
            return;

        DEMInfo newDemInfo = new DEMInfo();
        newDemInfo.name = demName;
        newDemInfo.demfilename = mapmakerWorker.getMapletFile().getAbsolutePath();
        try
        {
            saveDEM(newDemInfo);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    // Starts and manages a BigmapSwingWorker
    private void runBigmapSwingWorker(String demName, double[] centerPoint, double radius, File tempFolder, File outputFolder)
    {
        // Download the entire map maker suite to the users computer
        // if it has never been downloaded before.
        // Ask the user beforehand if it's okay to continue.
        final BigmapSwingWorker bigmapWorker =
                new BigmapSwingWorker(panel, "Running Bigmap", model.getBigmapPath());

        // If we need to download, promt the user that it will take a long time
        if (bigmapWorker.getIfNeedToDownload())
        {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(panel),
                    "Before Bigmap can be run for the first time, a very large file needs to be downloaded.\n" +
                    "This may take several minutes. Would you like to continue?",
                    "Confirm Download",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        if (panel.getSetSpecifyRegionManuallyCheckbox().isSelected())
        {
            if (panel.getLatitudeTextField().getText().isEmpty() || panel.getLongitudeTextField().getText().isEmpty() || panel.getPixelScaleTextField().getText().isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "Please enter values for the latitude, longitude, and pixel scale.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            bigmapWorker.setLatitude(model.getLatitude());
            bigmapWorker.setLongitude(model.getLongitude());
            bigmapWorker.setPixelScale(model.getPixelScale());
            bigmapWorker.setRegionSpecifiedWithLatLonScale(true);
        }
        else
        {
            bigmapWorker.setCenterPoint(centerPoint);
            bigmapWorker.setRadius(radius);
        }
        bigmapWorker.setGrotesque(panel.getGrotesqueModelCheckbox().isSelected());
        bigmapWorker.setName(demName);
        bigmapWorker.setHalfSize(model.getHalfSize());
        bigmapWorker.setTempFolder(tempFolder);
        bigmapWorker.setOutputFolder(outputFolder);

        bigmapWorker.setSmallBodyModel(model.getModelManager().getPolyhedralModel());

        bigmapWorker.executeDialog();

        if (bigmapWorker.isCancelled())
            return;

        DEMInfo newDemInfo = new DEMInfo();
        newDemInfo.name = demName;
        newDemInfo.demfilename = bigmapWorker.getMapletFile().getAbsolutePath();
        try
        {
            saveDEM(newDemInfo);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    // Popup menu for when using right clicks
    private void imageListMaybeShowPopup(MouseEvent e)
    {
        for (DEM dem : ((DEMCollection)model.getModelManager().getModel(ModelNames.DEM)).getImages())
        {
            DEMKey demkey = dem.getKey();
        }
        if (e.isPopupTrigger())
        {
            int index = imageList.locationToIndex(e.getPoint());

            if (index >= 0 && imageList.getCellBounds(index, index).contains(e.getPoint()))
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!imageList.isSelectedIndex(index))
                {
                    imageList.clearSelection();
                    imageList.setSelectedIndex(index);
                }

                int[] selectedIndices = imageList.getSelectedIndices();
                List<DEMKey> demKeys = new ArrayList<DEMKey>();
                for (int selectedIndex : selectedIndices)
                {
                    DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(selectedIndex);
                    String name = model.getCustomDataFolder() + File.separator + demInfo.demfilename;
                    DEMKey demKey = new DEMKey(name, demInfo.name);
                    demKeys.add(demKey);
                }
                demPopupMenu.setCurrentDEMs(demKeys);
                demPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//                if (demKeys.size() > 0)
//                {
//                    ((DEMInfo)imageList.getModel().getElementAt(index-1)).name = (((DEMCollection)modelManager.getModel(ModelNames.DEM)).getDEM(demKeys.get(0))).getKey().displayName;
//                    updateConfigFile();
//                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof DEMCollection)
            {
                String name = ((DEMCollection)model).getDEM((vtkActor)e.getPickedProp()).getKey().fileName;

                int idx = -1;
                int size = imageList.getModel().getSize();
                for (int i=0; i<size; ++i)
                {
                    DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(i);
                    String demFilename = getCustomDataFolder() + File.separator + demInfo.demfilename;
                    if (name.equals(demFilename))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    imageList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageList.scrollRectToVisible(cellBounds);
                }
            }
        }
    }
}
