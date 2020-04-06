package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryUtil;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalGenerationPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalPlaybackPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.table.StateHistoryTableView;

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
     * Controller for the interval playback panel
     */
    private StateHistoryIntervalPlaybackController intervalPlaybackController;

    /**
     * Controller for the view controls panel
     */
    private StateHistoryViewControlsController viewControlsController;

    /**
     * Renderer that the controller interacts with
     */
    private Renderer renderer;

    /**
     * State history model that manages state histories
     */
    private StateHistoryModel historyModel = null;

    /**
     * @param modelManager
     * @param renderer
     */
    public StateHistoryController(final ModelManager modelManager, Renderer renderer)
    {
    	File path = null;
    	int lineLength = 121;
    	vtkJoglPanelComponent renWin = renderer.getRenderWindowPanel();
        SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
        SmallBodyViewConfig config = (SmallBodyViewConfig) bodyModel.getConfig();
        try {
            path = FileCache.getFileFromServer(config.timeHistoryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        //grab the min max times from the input
        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString(lineLength, path));
        DateTime end = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString((int)StateHistoryUtil.getBinaryFileLength(path, lineLength)*lineLength-lineLength, path));


		try
		{
			historyModel = new StateHistoryModel(start, end, bodyModel, renderer, modelManager);
		}
		catch (IOException | StateHistoryInputException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        this.intervalGenerationController = new StateHistoryIntervalGenerationController(historyModel, start, end);
        this.intervalSelectionController = new StateHistoryIntervalSelectionController(historyModel, bodyModel, renderer);
        this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(historyModel, renderer);
        this.intervalDisplayedController = new StateHistoryDisplayedIntervalController(historyModel.getRuns());
        this.viewControlsController = new StateHistoryViewControlsController(historyModel, renderer);

        intervalDisplayedController.getView().setEnabled(false);
        intervalPlaybackController.getView().setEnabled(false);
        viewControlsController.getView().setEnabled(false);

        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                runs.updateTimeBarValue();
                runs.updateTimeBarLocation(e.getComponent().getWidth(), e.getComponent().getHeight());
                runs.updateStatusBarLocation(e.getComponent().getWidth(), e.getComponent().getHeight());
            }
        });

        runs.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			intervalDisplayedController.getView().setEnabled(runs.getSelectedItems().size() > 0);
			intervalPlaybackController.getView().setEnabled(runs.getSelectedItems().size() > 0);
			viewControlsController.getView().setEnabled(runs.getSelectedItems().size() > 0);
		});
    }

    /**
     * Returns a JPanel made of the child views that comprise this parent view
     * @return
     */
    public JPanel getView()
    {
    	JPanel timeControlsPanel = new JPanel();
    	timeControlsPanel.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, null, null),
                "Time Controls", TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
    	timeControlsPanel.setLayout(new BoxLayout(timeControlsPanel, BoxLayout.Y_AXIS));

    	StateHistoryIntervalGenerationPanel intervalGenerationPanel = intervalGenerationController.getView();
    	StateHistoryTableView intervalSelectionPanel = intervalSelectionController.getView();
    	intervalSelectionPanel.setup();

    	StateHistoryDisplayedIntervalPanel displayedPanel = intervalDisplayedController.getView();

    	StateHistoryIntervalPlaybackPanel intervalPlaybackPanel = intervalPlaybackController.getView();
    	StateHistoryViewControlsPanel viewControlsPanel = viewControlsController.getView();

    	timeControlsPanel.add(intervalGenerationPanel);
    	timeControlsPanel.add(intervalSelectionPanel);

    	JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(timeControlsPanel);
    	panel.add(displayedPanel);
    	panel.add(viewControlsPanel);
    	panel.add(intervalPlaybackPanel);
    	return panel;
    }


    /**
     * Private method used to respond to the "EndEvent" event above
     */
    private void updateTimeBarPosition()
    {
    	historyModel.getRuns().updateTimeBarLocation(renderer.getRenderWindowPanel().getComponent().getWidth(), renderer.getRenderWindowPanel().getComponent().getHeight());
    }

}
