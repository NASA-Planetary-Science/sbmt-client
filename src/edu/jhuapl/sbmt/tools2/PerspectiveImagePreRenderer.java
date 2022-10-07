package edu.jhuapl.sbmt.tools2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import edu.jhuapl.sbmt.client2.SbmtModelFactory;
import edu.jhuapl.sbmt.client2.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.offlimb.OfflimbPlaneGeneratorOperators;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.FilenameToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class PerspectiveImagePreRenderer
{
    private int resolutionIndex;
    private String outputDir;
    private boolean reprocess = false;

    private Pair<RenderablePointedImage, vtkPolyData>[] offLimbPolydata = new Pair[1];

    public PerspectiveImagePreRenderer(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels, String basename, String outputDir, boolean reprocess) throws IOException, Exception
    {
    	double offLimbFootprintDepth = renderableImage.getOfflimbDepth();
		RenderablePointedImageFootprintGeneratorPipeline pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> footprints = pipeline.getFootprintPolyData();

		double[] boundingBox = footprints.get(0).GetBounds();
		Just.of(renderableImage)
			.operate(new OfflimbPlaneGeneratorOperators(offLimbFootprintDepth, smallBodyModels, boundingBox, footprints.get(0).GetNumberOfPoints()))
			.subscribe(PairSink.of(offLimbPolydata))
			.run();

		String intersectionFileName = basename + "_frustumIntersection.vtk";
        saveToDisk(footprints.get(0), intersectionFileName);

        String filename = basename + "_offLimbImageData.vtk";
        saveToDisk(offLimbPolydata[0].getRight(), filename);
    }

    public void saveToDisk(vtkPolyData polydata, String filename)
    {
    	vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(polydata);
        if (!(new File(filename).exists()))new File(filename).getParentFile().mkdir();
        writer.SetFileName(new File(filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
        compressFile(filename);
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
    	String inputDirectory = args[0];
    	String outputDirectory = args[1];
    	boolean reprocess = Boolean.parseBoolean(args[2]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[3]);
        ShapeModelType type = ShapeModelType.provide(args[4]);
        Instrument instrument = Instrument.valueFor(args[5]);
        ImageSource imageSource = ImageSource.valueFor(args[6]);
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

        File input = new File(inputDirectory);
        File[] fileList;
        if (input.isDirectory())
        {
            fileList = new File(inputDirectory).listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return FilenameUtils.getExtension(name).contains("fit");
                }
            });
        }
        else
        {
            fileList = new File[] {input};
        }
        Arrays.sort(fileList);
        PerspectiveImagePreRenderer preRenderer;
        for (int i=3; i<smallBodyModel.getNumberResolutionLevels(); i++)
        {
            smallBodyModel.setModelResolution(i);
        	for (File filename : fileList)
        	{
//        		Triple<List<List<String>>, ImagingInstrument, List<String>>[] tripleSink = new Triple[1];
//	            List<List<String>> fileInputs = List.of(List.of(filename.getAbsolutePath(), "", imageSource.toString()));
//	            IPipelineOperator<Pair<List<List<String>>, ImagingInstrument>, Triple<List<List<String>>, ImagingInstrument, List<String>>> searchToPointingFilesOperator
//	            		= new SearchResultsToPointingFilesOperator(config);
//	            Just.of(Pair.of(fileInputs, selectedInstrument.get()))
//					.operate(searchToPointingFilesOperator)
//					.subscribe(TripleSink.of(tripleSink))
//					.run();
//
//	            List<String> pointingFilenames = tripleSink[0].getRight();
//
//	        	RenderableImagePipeline pipeline = new RenderableImagePipeline(filename.getAbsolutePath(), pointingFilenames.get(0), selectedInstrument.get());
//	        	List<RenderablePointedImage> images = pipeline.getOutput();

	        	FilenameToRenderableImagePipeline pipeline = FilenameToRenderableImagePipeline.of(filename.getAbsolutePath(), ImageSource.SPICE, config, selectedInstrument.get());
	        	List<RenderablePointedImage> images = pipeline.getImages();

	            preRenderer = new PerspectiveImagePreRenderer(images.get(0), List.of(smallBodyModel),"", outputDirectory, reprocess);
        	}
//            if (imagesWithPointing.isEmpty())
//            {
//                for (File filename : fileList)
//                {
//                    //may need to massage name here, need it to be /bennu/jfkfjksf, also need to strip .fits
//                    String basename = filename.getParent() + File.separator + FilenameUtils.getBaseName(filename.getAbsolutePath());
//        //            basename = basename.substring(basename.indexOf("2") + 2);
//                    basename = basename.substring(basename.indexOf("prod/") + 4);
//                    key = new ImageKey(basename, source, instrument);
//                    System.out.println("PerspectiveImagePreRenderer: main: filename is " + basename);
//                    try
//                    {
//                        image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
//                        String pointingFileString = "";
//                        if (source == ImageSource.SPICE)
//                        {
//                            pointingFileString = image.getInfoFileFullPath();
//                        }
//                        else if (source == ImageSource.GASKELL)
//                        {
//                            pointingFileString = image.getSumfileFullPath();
//
//                        }
//                        System.out.println("PerspectiveImagePreRenderer: main: pointing file is " + pointingFileString + " and reprocess is " + reprocess);
//                        imagesWithPointing.add(filename);
//
//                    }
//                    catch (NonexistentRemoteFile nerf)
//                    {
//                        continue;
//                    }
//                }
//            }
//            if ((fileList.length == 1) && imagesWithPointing.isEmpty()) System.exit(0);
//            for (File filename : imagesWithPointing)
//            {
//                System.out.println("PerspectiveImagePreRenderer: main: processing file " + filename.getAbsolutePath());
//                String basename = filename.getParent() + File.separator + FilenameUtils.getBaseName(filename.getAbsolutePath());
//                basename = basename.substring(basename.indexOf("prod/") + 4);
//                key = new ImageKey(basename, source, instrument);
//                image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
//                preRenderer = new PerspectiveImagePreRenderer(image, outputDirectory, reprocess);
//            }
        }
    }
}
