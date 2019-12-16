package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

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
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.StateHistoryRenderModel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalGenerationPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalPlaybackPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.table.StateHistoryTableView;

import glum.item.ItemEventListener;
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

//    private StateHistoryPanel2 view;
    private ModelManager modelManager;
    private SmallBodyModel bodyModel;
    private Renderer renderer;
    private vtkJoglPanelComponent renWin;

    private boolean initialized = false;
    private StateHistoryCollection runs;
    private File path;
    final int lineLength = 121;

    private SmallBodyViewConfig config;
    private StateHistoryModel historyModel;
    private StateHistoryRenderModel renderModel;
    private StateHistoryIntervalGenerationController intervalGenerationController;
    private StateHistoryIntervalSelectionController intervalSelectionController;
    private StateHistoryIntervalPlaybackController intervalPlaybackController;
    private StateHistoryViewControlsController viewControlsController;
//    private StateHistoryRenderManager renderManager;

    //time control pane
    private StateHistoryCollection stateHistoryCollection;

    public StateHistoryController(
            final ModelManager modelManager,
            Renderer renderer, boolean earthEnabled)
    {

        stateHistoryCollection = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        this.modelManager = modelManager;
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();
        bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
        config = (SmallBodyViewConfig) bodyModel.getConfig();
        try {
            path = FileCache.getFileFromServer(config.timeHistoryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        this.model = stateHistoryCollection;
        runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString(lineLength, path));//oldDate.parse(currentRun.getIntervalTime()[0]);
        DateTime end = ISODateTimeFormat.dateTimeParser().parseDateTime(StateHistoryUtil.readString((int)StateHistoryUtil.getBinaryFileLength(path, lineLength)*lineLength-lineLength, path));

        historyModel = new StateHistoryModel(start, end, bodyModel, renderer, modelManager);
//        renderManager = new StateHistoryRenderManager(renderer, historyModel);
        renderModel = new StateHistoryRenderModel();

        this.intervalGenerationController = new StateHistoryIntervalGenerationController(historyModel, start, end);
        this.intervalSelectionController = new StateHistoryIntervalSelectionController(historyModel, bodyModel, renderer);
        this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(historyModel, renderer);
        this.viewControlsController = new StateHistoryViewControlsController(historyModel, renderer);

        intervalPlaybackController.getView().setEnabled(false);
        viewControlsController.getView().setEnabled(false);

//        intervalPlaybackController.createTimer();
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

        runs.addListener(new ItemEventListener()
		{

			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				if (aEventType == ItemEventType.ItemsSelected)
				{
					intervalPlaybackController.getView().setEnabled(runs.getSelectedItems().size() > 0);
					viewControlsController.getView().setEnabled(runs.getSelectedItems().size() > 0);
				}

			}
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
    	StateHistoryIntervalPlaybackPanel intervalPlaybackPanel = intervalPlaybackController.getView();
    	StateHistoryViewControlsPanel viewControlsPanel = viewControlsController.getView();

    	timeControlsPanel.add(intervalGenerationPanel);
    	timeControlsPanel.add(intervalSelectionPanel);

    	JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(timeControlsPanel);
    	panel.add(intervalPlaybackPanel);
    	panel.add(viewControlsPanel);
    	return panel;

    }



//    private void setViewControlPanelEnabled(boolean enabled)
//    {
//        view.getDistanceOptions().setEnabled(enabled);
//        view.getViewOptions().setEnabled(enabled);
//        view.getShowEarthPointer().setEnabled(enabled);
//        view.getShowSunPointer().setEnabled(enabled);
//        view.getShowSpacecraftMarker().setEnabled(enabled);
//        view.getShowSpacecraft().setEnabled(enabled);
//        view.getShowLighting().setEnabled(enabled);
//        view.getEarthText().setEnabled(enabled);
//        view.getSunText().setEnabled(enabled);
//        view.getSpacecraftText().setEnabled(enabled);
//        view.getEarthSlider().setEnabled(enabled);
//        view.getSunSlider().setEnabled(enabled);
//        view.getSpacecraftSlider().setEnabled(enabled);
//        view.getViewOptions().setEnabled(enabled);
//        view.getBtnResetCameraTo().setEnabled(enabled);
//        view.getViewInputAngle().setEnabled(enabled);
//        view.getSetViewAngle().setEnabled(enabled);
//        view.getSaveAnimationButton().setEnabled(enabled);
//        view.getLblSelectView().setEnabled(enabled);
//        view.getLblVerticalFov().setEnabled(enabled);
//        view.getDistanceOptions().setEnabled(enabled);
//    }
//
//    public void setModel(HasTime model)
//    {
//        this.model = model;
//    }
//
//    public StateHistoryPanel2 getView()
//    {
//        return view;
//    }

//    private RunInfo getRunInfo(int index)
//    {
//        return (RunInfo)((DefaultListModel)optionsTable.getModel()).get(index);
//    }

//    private String getFileName(RunInfo runInfo)
//    {
//        //        return getCustomDataFolder() + File.separator + runInfo.runfilename;
//        return runInfo.runfilename;
//    }

//    private String getFileName(int index)
//    {
//        return getFileName(getRunInfo(index));
//    }
}
