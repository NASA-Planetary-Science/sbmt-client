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
		PlannedDataActorFactory.registerInstrument(new String[]{"MSI"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				footprint.setBoundaryVisible(true);
				return footprint;

			}
		},
		Color.blue);

		PlannedDataActorFactory.registerInstrument(new String[]{"ORX_OCAMS_POLYCAM", "POLYCAM"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		}, Color.blue);

		PlannedDataActorFactory.registerInstrument(new String[]{"ORX_OCAMS_MAPCAM", "MAPCAM"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		}, Color.green);

		PlannedDataActorFactory.registerInstrument(new String[]{"ORX_NAVCAM1", "NAVCAM1"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		}, Color.orange);

		PlannedDataActorFactory.registerInstrument(new String[]{"ORX_NAVCAM2", "NAVCAM2"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		}, Color.orange);

		PlannedDataActorFactory.registerInstrument(new String[]{"ORX_OCAMS_SAMCAM", "SAMCAM"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
				footprint.setTime(data.getTime());
				footprint.setInstrumentName(data.getInstrumentName());
				footprint.setSmallBodyModel(model);
				return footprint;
			}
		}, Color.green);

		PlannedDataActorFactory.registerInstrument(new String[]{"NIS"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
		{

			@Override
			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
			{
				return null;
//				PlannedSpectrumActor actor = new PlannedSpectrumActor(data, model/*, positionOrientationManager*/);
//				return actor;
			}
		}, Color.blue);

//		PlannedDataActorFactory.registerInstrument(new String[]{"OTES"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
//		{
//
//			@Override
//			public PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model)
//			{
//				PerspectiveImageFootprint footprint = new PerspectiveImageFootprint();
//				footprint.setTime(data.getTime());
//				footprint.setInstrumentName(data.getInstrumentName());
//				footprint.setSmallBodyModel(model);
//				return footprint;
//			}
//		}, Color.green);

		PlannedDataActorFactory.registerInstrument(new String[]{"OLAHIGH", "ORX_OLA_HIGH"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
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
			}
		}, Color.blue);

		PlannedDataActorFactory.registerInstrument(new String[]{"OLALOW", "ORX_OLA_LOW"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
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
			}
		}, Color.blue);

		PlannedDataActorFactory.registerInstrument(new String[]{"MMX_MEGANE", "MEGANE"}, new IPlanningDataActorBuilder<PlannedInstrumentData>()
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
		}, Color.blue);
	}

//	public void setPositionOrientationManager(IPositionOrientation positionOrientationManager)
//	{
//		this.positionOrientationManager = positionOrientationManager;
//	}

}
