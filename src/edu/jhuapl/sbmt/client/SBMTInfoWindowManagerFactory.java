package edu.jhuapl.sbmt.client;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.gui.image.ui.color.ColorImageInfoPanel;
import edu.jhuapl.sbmt.gui.image.ui.images.ImageInfoPanel;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrum;
import edu.jhuapl.sbmt.model.image.ColorImage;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.rosetta.OsirisImage;
import edu.jhuapl.sbmt.model.rosetta.OsirisImageInfoPanel;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.NIRS3Spectrum;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.ui.info.SpectrumInfoPanel;
import edu.jhuapl.sbmt.spectrum.ui.info.SpectrumStatisticsInfoPanel;

public class SBMTInfoWindowManagerFactory
{
	public static void initializeModels(ModelManager modelManager, StatusBar statusBar)
	{
		SbmtInfoWindowManager.registerInfoWindowManager(ColorImage.class, m ->
		{
			ColorImageCollection images = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
            return new ColorImageInfoPanel((ColorImage)m, images, statusBar);
		});
		SbmtInfoWindowManager.registerInfoWindowManager(Image.class, m ->
		{
			ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            if (m instanceof OsirisImage)
                return new OsirisImageInfoPanel((Image)m, images, boundaries, statusBar);
            return new ImageInfoPanel((Image)m, images, boundaries, statusBar);
		});
		SbmtInfoWindowManager.registerInfoWindowManager(BasicSpectrumRenderer.class, m -> new SpectrumInfoPanel(((BasicSpectrumRenderer)m).getSpectrum(), modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(NIS.class, m -> new SpectrumInfoPanel((NISSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(OTES.class, m -> new SpectrumInfoPanel((OTESSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(OVIRS.class, m -> new SpectrumInfoPanel((OVIRSSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(NIRS3Spectrum.class, m -> new SpectrumInfoPanel((NIRS3Spectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(SpectrumStatistics.class, m -> new SpectrumStatisticsInfoPanel((SpectrumStatistics)m,modelManager));
	}
}
