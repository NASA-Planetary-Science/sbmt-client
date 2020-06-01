package edu.jhuapl.sbmt.stateHistory.model.planning;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.IPositionOrientation;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.IPlanningDataActorBuilder;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActorFactory;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedImageActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedSpectrumActor;

//NOTE: this - or the individual components thereof - should go into the separate packages for the instruments
//It is here for testing purposes

public class PlannedDataActorRegister
{
	IPositionOrientation positionOrientationManager;

	public PlannedDataActorRegister()
	{
		PlannedDataActorFactory.registerModel("MSI", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedInstrumentDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PlannedImageActor actor = new PlannedImageActor(data, model, positionOrientationManager);
				return actor;
			}
		});

		PlannedDataActorFactory.registerModel("NIS", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedInstrumentDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PlannedSpectrumActor actor = new PlannedSpectrumActor(data, model, positionOrientationManager);
				return actor;
			}
		});
	}

	public void setPositionOrientationManager(IPositionOrientation positionOrientationManager)
	{
		this.positionOrientationManager = positionOrientationManager;
	}

}
