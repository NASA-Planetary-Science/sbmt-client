package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel; 

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.cmu.relativelayout.Binding;
import edu.cmu.relativelayout.BindingFactory;
import edu.cmu.relativelayout.Direction;
import edu.cmu.relativelayout.Edge;
import edu.cmu.relativelayout.RelativeConstraints;
import edu.cmu.relativelayout.RelativeLayout;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelIngestor;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelLoadStatusListener;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard.PregenStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.time.TimeWindow;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.displayedInterval.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.intervalSelection.table.StateHistoryTableView;

import glum.item.ItemEventType;

/**
 * The controller that drives the main panel for the StateHistory tab.
 * @author steelrj1
 *
 */
public class StateHistoryController
{
    /**
     * Controller for the interval generation panel
     */
    private StateHistoryIntervalGenerationController intervalGenerationController;

    /**
     * Controller for the interval selection panel
     */
    private StateHistoryIntervalSelectionController intervalSelectionController;

    /**
     * Controller for the displayed interval panel
     */
    private StateHistoryDisplayedIntervalController intervalDisplayedController;

    /**
     * Renderer that the controller interacts with
     */
    private Renderer renderer;

    /**
     * State history model that manages state histories
     */
    private StateHistoryModel historyModel = null;

    /**
     *	State history time model object
     */
    private StateHistoryTimeModel timeModel = null;

    private StateHistoryRendererManager rendererManager;

    private StateHistoryViewControlsController viewControlsController;

    /**
     * Constructor.  Initializes properties, sets listeners, etc
     * @param modelManager
     * @param renderer
     */
    public StateHistoryController(final ModelManager modelManager, StateHistoryRendererManager rendererManager, StateHistoryTimeModel timeModel)
    {
    	File path = null;
    	int lineLength = 121;
    	this.renderer = rendererManager.getRenderer();
    	this.rendererManager = rendererManager;
    	this.timeModel = timeModel;
    	vtkJoglPanelComponent renWin = renderer.getRenderWindowPanel();
        SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
        SmallBodyViewConfig config = (SmallBodyViewConfig) bodyModel.getConfig();
        DateTime start, end;
        try {
            path = FileCache.getFileFromServer(config.timeHistoryFile);
            //grab the min max times from the input
            start = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString(lineLength, path));
            end = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString((int)StateHistoryUtil.getBinaryFileLength(path, lineLength)*lineLength-lineLength, path));
        	this.timeModel.setTimeWindow(new TimeWindow(start, end));
        } catch (Exception e) {
            //attempt to grab the times from the config file
        	start = new DateTime(config.getStateHistoryStartDate());
        	end = new DateTime(config.getStateHistoryEndDate());
        	this.timeModel.setTimeWindow(new TimeWindow(start, end));
        }

		try
		{
			historyModel = new StateHistoryModel(bodyModel, rendererManager);
			historyModel.registerIntervalGenerator(StateHistorySourceType.SPICE, new SpiceStateHistoryIntervalGenerator());	//TODO update this to be a parameter in view config
			historyModel.setIntervalGenerator(StateHistorySourceType.SPICE);
			if (config.timeHistoryFile != null && !config.timeHistoryFile.equals(""))
			{
				historyModel.registerIntervalGenerator(StateHistorySourceType.PREGEN, new PregenStateHistoryIntervalGenerator(config));
				historyModel.setIntervalGenerator(StateHistorySourceType.PREGEN);

			}
			List<StateHistory> invalidStateHistories = historyModel.loadRunList();
			SpiceKernelIngestor kernelIngestor = new SpiceKernelIngestor(historyModel.getCustomDataFolder());
			Iterator<StateHistory> iterator = invalidStateHistories.iterator();
			while (iterator.hasNext())
			{
				StateHistory history = iterator.next();
				String[] options = {"Yes", "No"};
				int result = JOptionPane.showOptionDialog(null, "Cannot find metakernel " + FilenameUtils.getBaseName(history.getLocationProvider().getSourceFile())
																+ " at specified location for state history " + history.getMetadata().getStateHistoryName()
																+ ". Would you like to choose a new file?  Otherwise the state history will be removed.", "Metakernel not found",
																JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				if (result == 0)
				{
					JFileChooser chooser = new JFileChooser();
					int fileResult = chooser.showOpenDialog(null);
					if (fileResult == JFileChooser.APPROVE_OPTION)
					{
						File selectedMetakernel = chooser.getSelectedFile();
						String newKernelLocationAfterIngestion = kernelIngestor.ingestMetaKernelToCache(selectedMetakernel.getAbsolutePath(), null);
						history.getLocationProvider().setSourceFile(newKernelLocationAfterIngestion);
					}
				}
				else
				{
					historyModel.removeRun(history);
					iterator.remove();
				}
			}

			historyModel.initializeRunList();
		}
		catch (IOException | StateHistoryInputException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (StateHistoryInvalidTimeException shie)
		{
			JOptionPane.showMessageDialog(null, shie.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
		}
		catch (StateHistoryIOException shioe)
		{
			JOptionPane.showMessageDialog(null, shioe.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
		}

        this.intervalGenerationController = new StateHistoryIntervalGenerationController(historyModel, start, end);
        this.intervalSelectionController = new StateHistoryIntervalSelectionController(historyModel, bodyModel, rendererManager);

        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                rendererManager.updateTimeBarValue();
                rendererManager.updateTimeBarLocation(e.getComponent().getWidth(), e.getComponent().getHeight());
                rendererManager.updateStatusBarLocation(e.getComponent().getWidth(), e.getComponent().getHeight());
            }
        });

        rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
				intervalDisplayedController.getView().setEnabled(rendererManager.getSelectedItems().size() > 0);
				timeModel.setTime(rendererManager.getHistoryCollection().getCurrentRun().getMetadata().getStartTime());
				timeModel.setFractionDisplayed(0.0, 1.0);
				timeModel.setTimeFraction(0.0);

		});

        this.intervalDisplayedController = new StateHistoryDisplayedIntervalController(rendererManager, timeModel);

        intervalDisplayedController.getView().setEnabled(false);
    }

    /**
     * Returns a JPanel made of the child views that comprise this parent view
     * @return
     */
    public JPanel getView()
    {
    	intervalSelectionController.setIntervalGenerationController(intervalGenerationController);
    	StateHistoryTableView intervalSelectionPanel = intervalSelectionController.getView();

    	intervalSelectionPanel.setup();

    	StateHistoryDisplayedIntervalPanel displayedPanel = intervalDisplayedController.getView();

    	JPanel panel = new JPanel();
    	BindingFactory factory = new BindingFactory();
    	panel.setLayout(new RelativeLayout());
    	panel.add(intervalSelectionPanel, new RelativeConstraints(factory.leftEdge(), factory.rightEdge(), factory.topEdge(), new Binding(Edge.BOTTOM, 300, Direction.BELOW, Edge.TOP, panel)));
    	panel.add(viewControlsController.getView(), new RelativeConstraints(factory.leftEdge(), factory.rightEdge(), factory.below(intervalSelectionPanel), factory.above(displayedPanel)));
    	panel.add(displayedPanel, new RelativeConstraints(factory.leftEdge(), factory.rightEdge(), factory.bottomEdge()));
    	return panel;
    }


    /**
     * Private method used to respond to the "EndEvent" event above.  Note that since it is called as a response to a VTK event, it isn't directly invoked in Java; therefore
     * the warning that it is never used locally gets shown; I've added the @SupressWarnings annotation for this.
     */
    @SuppressWarnings("unused")
	private void updateTimeBarPosition()
    {
    	rendererManager.updateTimeBarLocation(renderer.getRenderWindowPanel().getComponent().getWidth(), renderer.getRenderWindowPanel().getComponent().getHeight());
    }

	/**
	 * @return the historyModel
	 */
	public StateHistoryModel getHistoryModel()
	{
		return historyModel;
	}

	/**
	 * @param viewControlsController the viewControlsController to set
	 */
	public void setViewControlsController(StateHistoryViewControlsController viewControlsController)
	{
		this.viewControlsController = viewControlsController;
	}
}