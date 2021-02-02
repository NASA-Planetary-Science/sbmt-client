package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;

public interface IFootprintConfinedPlateValues extends ITimeCalculatedPlateValues
{
	public void setFacetColoringDataForFootprint(PerspectiveImageFootprint footprint);
}
