package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.lidars.PlannedLidarTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.spectrometers.PlannedSpectrumTableController;
import edu.jhuapl.sbmt.stateHistory.model.IPositionOrientation;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedDataActorRegister;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.ui.ObservationPlanningView;

public class ObservationPlanningController
{
	StateHistoryController stateHistoryController;
	ObservationPlanningView view = new ObservationPlanningView();
	IPositionOrientation positionOrientationManager;
	PlannedImageTableController imageTableController;
	PlannedSpectrumTableController spectrumTableController;
	PlannedLidarTableController lidarTableController;
	PlannedDataActorRegister registrar;

	public ObservationPlanningController(final ModelManager modelManager, Renderer renderer, SmallBodyViewConfig config)
	{
		stateHistoryController = new StateHistoryController(modelManager, renderer);

		view.addTab("S/C Trajectory", stateHistoryController.getView());

		registrar = new PlannedDataActorRegister();
//		PlannedImage image = new PlannedImage();
//		image.setInstrument(Instrument.MSI);
//		System.out.println("ObservationPlanningController: ObservationPlanningController: et " + TimeUtil.str2et("2020-05-05T01:00:00.000"));
//		image.setTime(TimeUtil.str2et("2020-05-05T01:00:00.000"));
//		image.setColor(Color.red);
		PlannedImageCollection imageCollection = new PlannedImageCollection();
//		imageCollection.addImageToList(image);
		imageTableController = new PlannedImageTableController(modelManager, renderer, imageCollection, config);
		if (config.imagingInstruments.length > 0) view.addTab("Imagery", imageTableController.getView());
//		PlannedSpectrum spectrum = new PlannedSpectrum();
//		spectrum.setInstrument(Instrument.NIS);
//		spectrum.setTime(TimeUtil.str2et("2020-05-05T01:00:00.000"));
//		spectrum.setColor(Color.green);
		PlannedSpectrumCollection spectrumCollection = new PlannedSpectrumCollection();
//		spectrumCollection.addSpectrumToList(spectrum);
		spectrumTableController = new PlannedSpectrumTableController(modelManager, renderer, spectrumCollection, config);
		if (config.hasSpectralData) view.addTab("Spectra", spectrumTableController.getView());
		lidarTableController = new PlannedLidarTableController(modelManager, renderer, config);
		if (config.hasLidarData) view.addTab("LIDAR", lidarTableController.getView());
	}

	public IPositionOrientation getPositionOrientationManager()
	{
		return positionOrientationManager;
	}

	public void setPositionOrientationManager(IPositionOrientation positionOrientation)
	{
		this.positionOrientationManager = positionOrientation;
		stateHistoryController.setPositionOrientationManager(positionOrientation);
		imageTableController.setPositionOrientationManager(positionOrientation);
		spectrumTableController.setPositionOrientationManager(positionOrientation);
		lidarTableController.setPositionOrientationManager(positionOrientation);
		registrar.setPositionOrientationManager(positionOrientation);
	}


	public ObservationPlanningView getView()
	{
		return view;
	}

}
