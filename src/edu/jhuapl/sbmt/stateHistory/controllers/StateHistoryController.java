package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.BorderFactory;
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
import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalGenerationPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalPlaybackPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.table.StateHistoryTableView;

import glum.item.ItemEventType;

public class StateHistoryController //implements TableModelListener, IStateHistoryPanel
{
    public static class RunInfo
    {
        public String name = "";        // name to call this run for display purposes
        public String runfilename = ""; // filename of run on disk

        @Override
        public String toString()
        {
            return name;
        }
    }

    private SmallBodyModel bodyModel;
    private vtkJoglPanelComponent renWin;

    private StateHistoryCollection runs;
    private File path;
    final int lineLength = 121;

    private SmallBodyViewConfig config;
    private StateHistoryModel historyModel;
    private StateHistoryIntervalGenerationController intervalGenerationController;
    private StateHistoryIntervalSelectionController intervalSelectionController;
    private StateHistoryDisplayedIntervalController intervalDisplayedController;
    private StateHistoryIntervalPlaybackController intervalPlaybackController;
    private StateHistoryViewControlsController viewControlsController;

    public StateHistoryController(
            final ModelManager modelManager,
            Renderer renderer, boolean earthEnabled)
    {
        this.renWin = renderer.getRenderWindowPanel();
        bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
        config = (SmallBodyViewConfig) bodyModel.getConfig();
        try {
            path = FileCache.getFileFromServer(config.timeHistoryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString(lineLength, path));//oldDate.parse(currentRun.getIntervalTime()[0]);
        DateTime end = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString((int)StateHistoryUtil.getBinaryFileLength(path, lineLength)*lineLength-lineLength, path));

        historyModel = new StateHistoryModel(start, end, bodyModel, renderer, modelManager);

        this.intervalGenerationController = new StateHistoryIntervalGenerationController(historyModel, start, end);
        this.intervalSelectionController = new StateHistoryIntervalSelectionController(historyModel, bodyModel, renderer);
        this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(historyModel, renderer);
        this.intervalDisplayedController = new StateHistoryDisplayedIntervalController(historyModel);
        this.viewControlsController = new StateHistoryViewControlsController(historyModel, renderer);

        intervalDisplayedController.getView().setEnabled(false);
        intervalPlaybackController.getView().setEnabled(false);
        viewControlsController.getView().setEnabled(false);

        //TODO restore this
//        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
//                updateTimeBarValue();
//                updateTimeBarPosition();
//                StateHistory currentRun = stateHistoryCollection.getCurrentRun();
//                if (currentRun != null)
//                {
//                    currentRun.updateStatusBarPosition(e.getComponent().getWidth(), e.getComponent().getHeight());
//                }
            }
        });

        runs.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			intervalDisplayedController.getView().setEnabled(runs.getSelectedItems().size() > 0);
			intervalPlaybackController.getView().setEnabled(runs.getSelectedItems().size() > 0);
			viewControlsController.getView().setEnabled(runs.getSelectedItems().size() > 0);
		});

        historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
        	@Override
        	public void historySegmentCreated(StateHistory historySegment)
        	{
        		runs.setTimeFraction(historySegment, historySegment.getTime());
        		super.historySegmentCreated(historySegment);
        	}
		});
    }

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

//    	PercentIntervalChanger displayedPanel = intervalDisplayedController.getView().getTimeIntervalChanger();
//    	displayedPanel.setBorder(BorderFactory.createTitledBorder("Displayed Track Data"));

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
}
