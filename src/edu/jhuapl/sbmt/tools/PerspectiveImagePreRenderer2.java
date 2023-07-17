package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client2.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.offlimb.OfflimbPlaneGeneratorOperators;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.FilenameToRenderableImagePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.model.SbmtModelFactory;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class PerspectiveImagePreRenderer2
{
    private int resolutionIndex;
    private String outputDir;
    private boolean reprocess = false;

    private Pair<RenderablePointedImage, vtkPolyData>[] offLimbPolydata = new Pair[1];

    public PerspectiveImagePreRenderer2(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels, String basename, String outputDir, boolean reprocess) throws IOException, Exception
    {
    	this.outputDir = outputDir;
    	double offLimbFootprintDepth = renderableImage.getOfflimbDepth();
		RenderablePointedImageFootprintGeneratorPipeline pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();

		double[] boundingBox = footprints.get(0).GetBounds();
		Just.of(renderableImage)
			.operate(new OfflimbPlaneGeneratorOperators(offLimbFootprintDepth, smallBodyModels, boundingBox, (int)footprints.get(0).GetNumberOfPoints()))
			.subscribe(PairSink.of(offLimbPolydata))
			.run();

		basename = basename + "_" + smallBodyModels.get(0).getModelResolution() + "_" + smallBodyModels.get(0).getModelName()/* + "_" + renderableImage.getPointing().hashCode()*/;
		String intersectionFileName = basename + "_footprintImageData.vtk";
        saveToDisk(footprints.get(0), intersectionFileName);

        String filename = basename + "_offLimbImageData.vtk";
        saveToDisk(offLimbPolydata[0].getRight(), filename);
    }

    public void saveToDisk(vtkPolyData polydata, String filename)
    {
    	File outputFile = new File(outputDir, filename);
    	vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(polydata);
        if (!(outputFile.getParentFile().exists())) outputFile.getParentFile().mkdir();
        writer.SetFileName(new File(outputDir, filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
        compressFile(new File(outputDir, filename).getAbsolutePath());
    }

    private void compressFile(String filePath)
    {
        try
        {
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(new File(filePath + ".gz"));
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            FileInputStream is = new FileInputStream(filePath);
            int bytesRead;

            while ((bytesRead = is.read(buffer)) > 0)
            {
                gos.write(buffer, 0, bytesRead);
            }
            is.close();
            gos.finish();
            gos.close();
            //delete the original file
            File file = new File(filePath);
            file.delete();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
    	String inputFile = args[0];
    	String outputDirectory = args[5];
    	boolean reprocess = Boolean.parseBoolean(args[6]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[2]);
        ShapeModelType type = ShapeModelType.provide(args[3]);
//        Instrument instrument = Instrument.valueFor(args[4]);
        PointingSource imageSource = PointingSource.valueOf(args[1]);
//        boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
//        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
//        String rootURL = "http://sbmt.jhuapl.edu/sbmt/prod/";
        String rootURL = "http://sbmt-web.jhuapl.edu/internal/multi-mission/test";
        Configuration.setAPLVersion(true);
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
        //Optional<ImagingInstrument> selectedInstrument = Stream.of(config.imagingInstruments).filter(inst -> inst.getInstrumentName() == instrument).findFirst();
        Optional<ImagingInstrument> selectedInstrument = Optional.of(config.imagingInstruments[Integer.parseInt(args[4])]);
        if (selectedInstrument.isEmpty()) return;
        File[] fileList = new File[1];
        fileList[0] = new File(inputFile);

        for (int i=1; i<smallBodyModel.getNumberResolutionLevels(); i++)
        {
            smallBodyModel.setModelResolution(i);
        	for (File filename : fileList)
        	{
	        	FilenameToRenderableImagePipeline pipeline = FilenameToRenderableImagePipeline.of(filename.getAbsolutePath(), imageSource, config, selectedInstrument.get());
	        	List<RenderablePointedImage> images = pipeline.getImages();
	        	if (images.size() == 0) continue;
	        	String basename = FilenameUtils.getBaseName(filename.getAbsolutePath());
	            new PerspectiveImagePreRenderer2(images.get(0), List.of(smallBodyModel), basename, outputDirectory, reprocess);
        	}
        }
        System.exit(0);
    }
}
