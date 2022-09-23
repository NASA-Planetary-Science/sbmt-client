package edu.jhuapl.sbmt.image2.pipelineComponents.operators.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NonexistentRemoteFile;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.ImageOrigin;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class CreateImageFromSearchResultOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<List<List<String>>, IImagingInstrument, ImageSource>, G1>
{
	private List<List<String>> results;
	private SmallBodyViewConfig viewConfig;
	private ImageSource imageSource;
	private IImagingInstrument instrument;
	private static final Map<String, ImmutableMap<String, String>> SUM_FILE_MAP = new HashMap<>();


	public CreateImageFromSearchResultOperator(SmallBodyViewConfig viewConfig)
	{
		this.viewConfig = viewConfig;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		results = inputs.get(0).getLeft();
		imageSource = inputs.get(0).getRight();
		instrument = inputs.get(0).getMiddle();
		outputs = new ArrayList<G1>();

		int i=1;
        for (List<String> imageInfo : results)
        {
        	String extension = ".INFO";
        	String pointingDir = "infofiles";
        	if (imageSource == ImageSource.GASKELL || imageSource == ImageSource.GASKELL_UPDATED)
    		{
        		extension = ".SUM";
        		pointingDir = "sumfiles";
        		if (viewConfig.getUniqueName().contains("Eros")) pointingDir = "sumfiles_to_be_delivered";
    		}
        	if (imageSource == ImageSource.LABEL)
    		{
        		extension = ".LBL";
        		pointingDir = "labels";
    		}

        	String imagePath = "images";
        	if (viewConfig.getUniqueName().contains("Bennu")) imagePath = "images/public";

        	String infoBaseName = FilenameUtils.removeExtension(imageInfo.get(0)).replace(imagePath, pointingDir);
        	if (viewConfig.getUniqueName().contains("Eros"))
    		{
        		if (extension == ".SUM")
        		{
	        		String filename = FilenameUtils.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
	            	String filenamePrefix = filename.substring(0, filename.indexOf("_"));
	        		infoBaseName = infoBaseName.replace(filename, filenamePrefix.substring(0, filenamePrefix.length()-2));
        		}
    		}
        	else
        	{
        		if (extension == ".SUM")
					try
					{
						infoBaseName = infoBaseName.substring(0, infoBaseName.lastIndexOf("/")) + File.separator + getSumFileName(instrument.getSearchQuery().getRootPath(), imageInfo.get(0));
					}
        			catch (IOException | ParseException | NonexistentRemoteFile e)
					{
        				String filename = FilenameUtils.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
//        				String sumFileName = filename + ".SUM";
						infoBaseName = infoBaseName.substring(0, infoBaseName.lastIndexOf("/"))
										+ File.separator
										+ filename;


						// TODO Auto-generated catch block
//						e.printStackTrace();
					}
        	}

        	PerspectiveImage image = new PerspectiveImage(imageInfo.get(0), instrument.getType(), imageSource, infoBaseName + extension, new double[] {});
        	image.setFlip(instrument.getFlip());
        	image.setRotation(instrument.getRotation());
        	image.setImageOrigin(ImageOrigin.SERVER);

        	image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
        	image.setMaskValues(instrument.getMaskValues());
        	image.setFillValues(instrument.getFillValues());
        	image.setLongTime(Long.parseLong(imageInfo.get(1)));
        	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
        	compImage.setIndex(i++);
        	outputs.add((G1)compImage);
        }
	}

	private String getImageFileName(String imageName)
    {
        // If the proposed name does not include the extension, add .fits.
        if (!imageName.matches("^.*\\.[^\\\\.]*$"))
        {
            imageName += ".fits";
        }

        return imageName;
    }

	private String getSumFileName(String imagerDirectory, String imageFilename) throws IOException, ParseException
    {
//		System.out.println("ImageSearchOperator: getSumFileName: imager directory " + imagerDirectory);
        if (!SUM_FILE_MAP.containsKey(imagerDirectory))
        {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            File mapFile = FileCache.getFileFromServer(SafeURLPaths.instance().getString(imagerDirectory, "make_sumfiles.in"));
            try (BufferedReader reader = new BufferedReader(new FileReader(mapFile)))
            {
                while (reader.ready())
                {
                    String wholeLine = reader.readLine();
                    String[] line = wholeLine.split("\\s*,\\s*");
                    if (line[0].equals(wholeLine))
                    {
                        line = wholeLine.split("\\s\\s*");
                    }
                    if (line.length < 2) throw new ParseException("Cannot parse line " + String.join(" ", line) + " to get sum file/image file names", line.length > 0 ? line[0].length() : 0);
                    String sumFile = line[0];
                    String imageFile = getImageFileName(line[line.length - 1]);

                    builder.put(imageFile, sumFile);
                }
            }
//            System.out.println("ImageSearchOperator: getSumFileName: adding imager " + imagerDirectory);
            SUM_FILE_MAP.put(imagerDirectory, builder.build());
        }

        File imageFile = new File(imageFilename);
        ImmutableMap<String, String> imagerSumFileMap = SUM_FILE_MAP.get(imagerDirectory);
        if (imagerSumFileMap.containsKey(imageFile.getName()))
        {
            return SUM_FILE_MAP.get(imagerDirectory).get(imageFile.getName());
        }
        return null;
    }

}
