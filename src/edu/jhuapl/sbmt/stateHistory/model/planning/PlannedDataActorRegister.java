package edu.jhuapl.sbmt.stateHistory.model.planning;

import java.awt.Color;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.IPlanningDataActorBuilder;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActorFactory;

//NOTE: this - or the individual components thereof - should go into the separate packages for the instruments
//It is here for testing purposes

public class PlannedDataActorRegister
{
//	IPositionOrientation positionOrientationManager;

	public PlannedDataActorRegister()
	{
		PlannedDataActorFactory.registerModel("MSI", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setColor(Color.blue);
				footprint.setSmallBodyModel(model);
				footprint.setBoundaryVisible(true);

				return footprint;

			}
		});

		PlannedDataActorFactory.registerModel("POLYCAM", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				Color fadedBlue = new Color(0.0f, 0.0f, 1.0f, 0.5f);
				footprint.setColor(fadedBlue);
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		});

		PlannedDataActorFactory.registerModel("MAPCAM", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setColor(Color.green);
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		});

		PlannedDataActorFactory.registerModel("NAVCAM", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setColor(Color.orange);
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		});

		PlannedDataActorFactory.registerModel("NIS", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				return null;
//				PlannedSpectrumActor actor = new PlannedSpectrumActor(data, model/*, positionOrientationManager*/);
//				return actor;
			}
		});

//		PlannedDataActorFactory.registerModel("OTES", new IPlanningDataActorBuilder<PlannedInstrumentData>()
//		{
//
//			@Override
//			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
//			{
//				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
//				footprint.setTime(data.getTime());
//				footprint.setInstrumentName(data.getInstrumentName());
//				footprint.setColor(Color.blue);
//				footprint.setSmallBodyModel(model);
//				return footprint;
//			}
//		});

		PlannedDataActorFactory.registerModel("OLAHIGH", new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setColor(Color.blue);
				footprint.setSmallBodyModel(model);
				return footprint;
//				PlannedLidarActor lidarActor = new PlannedLidarActor((PlannedLidarTrack)data);
//				lidarActor.setInstrumentName(data.getInstrumentName());
////				lidarActor.setTime(data.getTime());
//				return lidarActor;
////				return null;
////				PlannedSpectrumActor actor = new PlannedSpectrumActor(data, model/*, positionOrientationManager*/);
////				return actor;
			}
		});
	}

//	public void setPositionOrientationManager(IPositionOrientation positionOrientationManager)
//	{
//		this.positionOrientationManager = positionOrientationManager;
//	}

}
