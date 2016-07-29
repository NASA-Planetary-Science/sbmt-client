package edu.jhuapl.near.model.eros;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import vtk.vtkImageData;
import vtk.vtkImageReslice;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.BackPlanesXml;
import edu.jhuapl.near.util.BackPlanesXmlMeta;
import edu.jhuapl.near.util.BackPlanesXmlMeta.BPMetaBuilder;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;

import nom.tam.fits.FitsException;

public class MSIImage extends PerspectiveImage
{
    // Size of image after resampling. Before resampling image is 537 by 244 pixels.
    // MSI pixels are resampled to make them square. According to SPICE kernel msi15.ti,
    // MSI pixel size in degrees is 2.2623/244 in Y; 2.9505/537 in X. To square the
    // pixels, resample X to 2.2623 * 537/2.9505 = ~412.
    public static final int RESAMPLED_IMAGE_WIDTH = 537;
    public static final int RESAMPLED_IMAGE_HEIGHT = 412;

    // Number of pixels on each side of the image that are
    // masked out (invalid) due to filtering.
    private static final int LEFT_MASK = 14;
    private static final int RIGHT_MASK = 14;
    private static final int TOP_MASK = 2;
    private static final int BOTTOM_MASK = 2;
    private static final File xmlTemplate = new File((new File(".")), "misc/xmlTemplate/msiXmlTemplate.xml");

    public MSIImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);

        //the parent class looks like it only wants to set the labelFileFullPath if the ImageSource = LABEL
        //but the initialization of pngFileFullPath sets it to the label file. Just copy that to the
        //labelFileFullPath.
        setLabelFileFullPath(getPngFileFullPath());
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        int[] dims = rawImage.GetDimensions();
        int originalHeight = dims[1];

        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInputData(rawImage);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)RESAMPLED_IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, RESAMPLED_IMAGE_WIDTH-1, 0, RESAMPLED_IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();

        vtkImageData resliceOutput = reslice.GetOutput();
        rawImage.DeepCopy(resliceOutput);
        rawImage.SetSpacing(1, 1, 1);
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{TOP_MASK, RIGHT_MASK, BOTTOM_MASK, LEFT_MASK};
    }

    @Override
    protected String initializeFitFileFullPath()
    {
        ImageKey key = getKey();
        return FileCache.getFileFromServer(key.name + ".FIT").getAbsolutePath();
    }

    @Override
    protected String initializeLabelFileFullPath()
    {
        ImageKey key = getKey();
        String imgLblFilename = key.name + ".LBL";
        return FileCache.getFileFromServer(imgLblFilename).getAbsolutePath();
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String infoFilename = keyFile.getParentFile().getParent()
        + "/infofiles/" + keyFile.getName() + ".INFO";
        return FileCache.getFileFromServer(infoFilename).getAbsolutePath();
    }

    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent()
        + "/sumfiles/" + keyFile.getName().substring(0, 11) + ".SUM";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    public int getFilter()
    {
        String fitName = new File(getFitFileFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
    }

    /**
     * Note although there is only 1 MSI camera, we are abusing the following function
     * to return 1 if image is IOF or 2 if image is CIF.
     */
    @Override
    public int getCamera()
    {
        String fitName = new File(getFitFileFullPath()).getName();
        if (fitName.toUpperCase().contains("_IOF_"))
            return 1;
        else // CIF
            return 2;
    }

    public String getCameraName()
    {
        return "MSI";
    }

    /**
     * Return File containing path to Xml Template on disk.
     * @return
     */
    public File getXmlTemplate() {
        return xmlTemplate;
    }

    @Override
    public void generateBackplanesLabel(String imgName, String lblFileName) throws IOException
    {
        //generate XML metadata from PDS3 label
        BackPlanesXmlMeta xmlMetaData = pds3ToXmlMeta(this.getLabelFileFullPath(), lblFileName);

        //generate Xml Document
        BackPlanesXml xmlLabel = metaToXmlDoc(xmlMetaData, getXmlTemplate().getAbsolutePath());

        //create PDS4 XML label
        try
        {
            xmlLabel.writeXML(lblFileName);
        }
        catch (XPathExpressionException | TransformerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("ERROR! Could not write XML label:" + lblFileName);
        }

//        System.err.println(MSIImage.class.getName() + ": PDS 4 label creation for MSIImage backplanes not yet implemented.");
//        //
//        // TBD: create PDS4 XML label here. The following line generates the PDS 3 labels. Remove it when the XML writer is complete.
//        //
//        super.generateBackplanesLabel(imgName, lblFileName);
    }

    @Override
    public BackPlanesXmlMeta pds3ToXmlMeta(String pds3LblFname, String outXmlFname) {
        BPMetaBuilder builder = metaBfromPDS3(pds3LblFname, outXmlFname);
        return builder.build();
    }

    @Override
    public BackPlanesXml metaToXmlDoc(BackPlanesXmlMeta metaData, String xmlTemplate) {
        BackPlanesXml xmlLabel = new BackPlanesXml(metaData, xmlTemplate);
        return xmlLabel;
    }


    /**
     * Load and parse PDS3 label for original PDS3 MSI image. Returns builder so that
     * a follow-on method can add to the builder if needed.
     */
    private BPMetaBuilder metaBfromPDS3(String pds3LblFname, String outXmlFname) {
        //load and parse the PDS3 label file.
        ArrayList<String> labelContents = new ArrayList<String>();
        try
        {
            labelContents = FileUtil.getFileLinesAsStringList(pds3LblFname);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Error! Could not parse label file:" + pds3LblFname);
            System.exit(1);
        }

        //initialize metadata builder
        BPMetaBuilder metaBuilder = new BPMetaBuilder(outXmlFname);
        String[] temp;
        String temp2;
        for (String line : labelContents) {
            if (line.contains("START_TIME")) {
                metaBuilder.startDate(BackPlanesXmlMeta.valFromKeyVal(line));
            } else if (line.contains("STOP_TIME")) {
                metaBuilder.stopDate(BackPlanesXmlMeta.valFromKeyVal(line));
            }
        }

        return metaBuilder;
    }



}
