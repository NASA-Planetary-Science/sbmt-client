package edu.jhuapl.sbmt.stateHistory.controllers.lidars;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.IPositionOrientation;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.PlannedImageView;
import edu.jhuapl.sbmt.stateHistory.ui.lidars.LidarPlanningView;

public class PlannedLidarTableController
{
	LidarPlanningView view;
	IPositionOrientation positionOrientationManager;

	public PlannedLidarTableController(final ModelManager modelManager, Renderer renderer, SmallBodyViewConfig config)
	{
		view = new LidarPlanningView(config);
	}

	public void setPositionOrientationManager(IPositionOrientation manager)
	{
		this.positionOrientationManager = manager;
	}

	public LidarPlanningView getView()
	{
		return view;
	}

}
