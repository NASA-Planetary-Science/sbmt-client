package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.beans.PropertyChangeEvent;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;

public class PlannedSpectrumCollection  extends BasePlannedDataCollection<PlannedSpectrum>
{

	public PlannedSpectrumCollection(String filename, SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
		this.filename = filename;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			time = (double)evt.getNewValue();
			updateFootprints();
		}
	}

	@Override
	public void updateFootprints()
	{
		if (stateHistorySource == null) return;
		for (PlannedDataActor actor : plannedDataActors)
		{
			//make spectrum version?
			if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
				StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
			actor.getFootprintBoundaryActor().SetVisibility(time > actor.getTime() ? 1 : 0);
			actor.getFootprintBoundaryActor().Modified();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	/**
	 * @param run
	 */
	public void addSpectrumToList(PlannedSpectrum spectrum, ProgressStatusListener listener)
	{
		plannedData.add(spectrum);
		PerspectiveImageFootprint actor = (PerspectiveImageFootprint)addDataToRenderer(spectrum);
		actor.setStaticFootprint(true);
		plannedDataActors.add(actor);
		footprintActors.add(actor.getFootprintBoundaryActor());
		listener.setProgressStatus("Adding spectrum " + plannedData.size(), 0);
		setAllItems(plannedData);
		this.pcs.firePropertyChange("PLANNED_SPECTRA_CHANGED", null, null);
	}
}
