package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;

public interface IFootprintConfinedPlateValues extends ITimeCalculatedPlateValues
{
	public void setFacetColoringDataForFootprint(PerspectiveImageFootprint footprint);
}
