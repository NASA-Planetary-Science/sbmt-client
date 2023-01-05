package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

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
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.offlimb.OfflimbPlaneGeneratorOperators;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.FilenameToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
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
    	System.out.println("PerspectiveImagePreRenderer2: saveToDisk: writing " + new File(outputDir, filename).getAbsolutePath());
    	vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(polydata);
        if (!(new File(filename).exists()))new File(filename).getParentFile().mkdir();
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
        ImageSource imageSource = ImageSource.valueOf(args[1]);
//        boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
//        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
//        String rootURL = "http://sbmt.jhuapl.edu/sbmt/prod/";
        String rootURL = "http://sbmt-web.jhuapl.edu/internal/multi-mission/test";
        Configuration.setAPLVersion(true);
        Configuration.setRootURL(rootURL);
        System.setProperty("edu.jhuapl.sbmt.mission", "DART_TEST");
        System.out.println("PerspectiveImagePreRenderer2: main: mission property " + System.getProperty("edu.jhuapl.sbmt.mission"));
        SbmtMultiMissionTool.configureMission();
        System.out.println("PerspectiveImagePreRenderer2: main: configured mission");
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
    	System.out.println("PerspectiveImagePreRenderer2: main: selected instrument empty? " + selectedInstrument.isEmpty());
        if (selectedInstrument.isEmpty()) return;
        System.out.println("PerspectiveImagePreRenderer2: main: input file " + inputFile);
        File[] fileList = new File[1];
        fileList[0] = new File(inputFile);
        System.out.println("PerspectiveImagePreRenderer2: main: file list 0 " + fileList[0].getAbsolutePath());

//        File input = new File(inputDirectory);
//        File[] fileList;
//        if (input.isDirectory())
//        {
//            fileList = new File(inputDirectory).listFiles(new FilenameFilter()
//            {
//                @Override
//                public boolean accept(File dir, String name)
//                {
//                    return FilenameUtils.getExtension(name).contains("fit") || FilenameUtils.getExtension(name).contains("fits");
//                }
//            });
//        }
//        else
//        {
//            fileList = new File[] {input};
//        }
//        Arrays.sort(fileList);
        PerspectiveImagePreRenderer2 preRenderer;
        System.out.println("PerspectiveImagePreRenderer2: main: number of res levels " + smallBodyModel.getNumberResolutionLevels());
        for (int i=2; i<smallBodyModel.getNumberResolutionLevels(); i++)
        {
            smallBodyModel.setModelResolution(i);
        	for (File filename : fileList)
        	{
        		System.out.println("PerspectiveImagePreRenderer2: main: file is " + filename);
        		System.out.println("PerspectiveImagePreRenderer2: main: image source is " + imageSource);
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

	        	FilenameToRenderableImagePipeline pipeline = FilenameToRenderableImagePipeline.of(filename.getAbsolutePath(), imageSource, config, selectedInstrument.get());
	        	List<RenderablePointedImage> images = pipeline.getImages();
	        	System.out.println("PerspectiveImagePreRenderer2: main: number of images " + images.size());
	            preRenderer = new PerspectiveImagePreRenderer2(images.get(0), List.of(smallBodyModel),"", outputDirectory, reprocess);
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
