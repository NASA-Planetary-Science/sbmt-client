package edu.jhuapl.sbmt.tools2;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client2.SbmtModelFactory;
import edu.jhuapl.sbmt.client2.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.offlimb.OfflimbPlaneGeneratorOperators;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.preview.RenderableImagePipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class ServerOffLimbPlaneCalculator
{
	private Pair<RenderablePointedImage, vtkPolyData>[] polydata = new Pair[1];

    public ServerOffLimbPlaneCalculator(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
    {
		double offLimbFootprintDepth = renderableImage.getOfflimbDepth();
		RenderablePointedImageFootprintGeneratorPipeline pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();

		double[] boundingBox = footprints.get(0).GetBounds();
		Just.of(renderableImage)
			.operate(new OfflimbPlaneGeneratorOperators(offLimbFootprintDepth, smallBodyModels.get(0), boundingBox, footprints.get(0).GetNumberOfPoints()))
			.subscribe(PairSink.of(polydata))
			.run();
    }

    public void saveToDisk(String filename)
    {
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(polydata[0].getRight());
        writer.SetFileName(new File(filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
    }

    public static void main(String[] args) throws Exception
	{
    	String filename = args[0];
    	String pointingFilename = args[1];
    	final ImageSource source = ImageSource.valueOf(args[2]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[3]);
        ShapeModelType type = ShapeModelType.provide(args[4]);
        Instrument instrument = Instrument.valueFor(args[5]);
        boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
//        String rootURL = "http://sbmt.jhuapl.edu/sbmt/prod/";

        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        SbmtMultiMissionTool.configureMission();

        // authentication
        Configuration.authenticate();

        // initialize view config
        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();
    	SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
    	SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
        Optional<ImagingInstrument> selectedInstrument = Stream.of(config.imagingInstruments).filter(inst -> inst.getInstrumentName() == instrument).findFirst();
        if (selectedInstrument.isEmpty()) return;

    	RenderableImagePipeline pipeline = new RenderableImagePipeline(filename, pointingFilename, selectedInstrument.get(), source);
    	List<RenderablePointedImage> images = pipeline.getOutput();

    	ServerOffLimbPlaneCalculator calculator = new ServerOffLimbPlaneCalculator(images.get(0), List.of(smallBodyModel));
    	if (!(new File(filename).exists())) new File(filename).getParentFile().mkdirs();
    	calculator.saveToDisk(filename);
	}
}