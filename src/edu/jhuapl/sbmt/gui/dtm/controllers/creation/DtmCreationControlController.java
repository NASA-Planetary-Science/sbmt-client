package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreator;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationControlPanel;
import edu.jhuapl.sbmt.model.dem.DEMCollection;

public class DtmCreationControlController implements ActionListener, PropertyChangeListener
{
	DtmCreationControlPanel panel;
	DtmCreationModel model;
	PickManager pickManager;
	private DEMCreator creationTool;
	private SmallBodyViewConfig config;
	Task task = null;

	public DtmCreationControlController(SmallBodyViewConfig config, DtmCreationModel model, final PickManager pickManager, DEMCreator creationTool)
	{
		panel = new DtmCreationControlPanel(config.hasMapmaker, config.hasBigmap);
		this.creationTool = creationTool;
		this.model = model;
		this.pickManager = pickManager;
		this.config = config;
		initControls();
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
		                	 if(demInfo.demfilename.endsWith(".fit") || demInfo.demfilename.endsWith(".fits") ||
		                             demInfo.demfilename.endsWith(".FIT") || demInfo.demfilename.endsWith(".FITS"))
		                     {
		                		 model.saveDEM(demInfo);
		                     }
		                	 else
		                     {
		                         JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
		                                 "DEM file does not have valid FIT extension.",
		                                 "Error",
		                                 JOptionPane.ERROR_MESSAGE);
		                     }
		                }
		                catch (IOException e2)
		                {
		                    e2.printStackTrace();
		                }
		            }
		        }

			}
		});

//		panel.getBigmapSubmitButton().addActionListener(this);
		if (config.hasMapmaker)
			panel.getMapmakerSubmitButton().addActionListener(this);

		panel.getRenameButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
//				DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(imageList.getSelectedIndex());
				DEMInfo demInfo = model.getSelectedItem();

		        String newDEMName = JOptionPane.showInputDialog(panel, "Enter a new name", "Rename the DEM", JOptionPane.PLAIN_MESSAGE);
		        if (newDEMName != null && !newDEMName.equals(""))
		        {
		            demInfo.name = newDEMName;
		            model.updateConfigFile();
		            model.fireInfoChangedListeners(model.getInfoList());
		        }

			}
		});

		if (config.hasBigmap || config.hasMapmaker)
		{
			panel.getSelectRegionButton().addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (panel.getSelectRegionButton().isSelected())
		                    pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
	                else
	                    pickManager.setPickMode(PickMode.DEFAULT);
				}
			});

			panel.getClearRegionButton().addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					 AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
		             selectionModel.removeAllStructures();
				}
			});

			panel.getSetSpecifyRegionManuallyCheckbox().addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					panel.getLatitudeTextField().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
					panel.getLatitudeLabel().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
					panel.getLongitudeTextField().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
					panel.getLongitudeLabel().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
					panel.getPixelScaleTextField().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
					panel.getPixelScaleLabel().setEnabled(panel.getSetSpecifyRegionManuallyCheckbox().isSelected());
				}
			});
		}

		panel.getDeleteButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				DEMInfo[] infos = model.getSelectedItems();
				model.fireInfosRemovedListeners(infos);
				model.removeDEM(infos);
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

        pickManager.setPickMode(PickMode.DEFAULT);
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
        int pixHalfSize = (Integer)panel.getHalfSizeSpinner().getValue();
        if(e.getSource() == panel.getMapmakerSubmitButton())
        {
            String demName = JOptionPane.showInputDialog(panel, "Enter a name", "Name the DEM", JOptionPane.PLAIN_MESSAGE);
            if (demName != null && !demName.equals(""))
            {
        		creationTool.setCompletionBlock(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							model.saveDEM(creationTool.getDEMKey());
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});

            	if (panel.getSetSpecifyRegionManuallyCheckbox().isSelected())
            	{
            		task = creationTool.getCreationTask(demName, Double.parseDouble(panel.getLatitudeTextField().getText()),
            												Double.parseDouble(panel.getLongitudeTextField().getText()),
            												Double.parseDouble(panel.getPixelScaleTextField().getText()), pixHalfSize);
            	}
            	else
            	{
            		task = creationTool.getCreationTask(demName, centerPoint, radius, pixHalfSize);
            	}
            	if (task == null) return;
        		task.run();

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

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model2 = model.getModelManager().getModel(e.getPickedProp());
            if (model2 instanceof DEMCollection)
            {
                String name = ((DEMCollection)model2).getDEM((vtkActor)e.getPickedProp()).getKey().fileName;

                int idx = -1;
//                int size = imageList.getModel().getSize();
                int size = ((DEMCollection) model2).getImages().size();
                for (int i=0; i<size; ++i)
                {
//                    DEMInfo demInfo = model.getInfoList().get(i);
//                    String demFilename = getCustomDataFolder() + File.separator + demInfo.demfilename;
//                    if (name.equals(demFilename))
//                    {
//                        idx = i;
//                        break;
//                    }
                }

//                if (idx >= 0)
//                {
//                    imageList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = imageList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        imageList.scrollRectToVisible(cellBounds);
//                }
            }
        }
    }
}
