package edu.jhuapl.sbmt.image2.util;

import java.io.File;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.ImageSource;

public class ImageFileUtil
{
    protected static final CachingMapLookup SumFileLookup = new CachingMapLookup() {

        @Override
        protected File getFile(String mapKey)
        {
        	File file = FileCache.getFileFromServer(SafeURLPaths.instance().getString(mapKey, "make_sumfiles.in"));
        	System.out.println("ImageFileUtil.SumFileLookup.new CachingMapLookup() {...}: getFile: make sumfiles " + file.getAbsolutePath());
            return file;
        }

    };

    protected static final CachingMapLookup CorrectedSumFileLookup = new CachingMapLookup() {

        @Override
        protected File getFile(String mapKey)
        {
            return FileCache.getFileFromServer(SafeURLPaths.instance().getString(mapKey, "make_sumfiles_corrected.in"));
        }

    };

    protected static final int MsiSumFileBaseNameLength = "M0157415573".length();

    public ImageFileUtil()
    {
        super();
    }

    public String getPointingServerPath(String imageFilePath, IImagingInstrument instrument, ImageSource imageSource)
    {
        Preconditions.checkNotNull(imageFilePath);
        Preconditions.checkNotNull(instrument);
        Preconditions.checkNotNull(imageSource);

        String pointingRoot = instrument.getSearchQuery().getRootPath();
        String pointingDir = String.join("/", pointingRoot, imageSource.getPointingDir());
        String pointingType = imageSource.getPointingType();
        String extension = "." + pointingType;

        String imageFileName = new File(imageFilePath).getName();
        String imageBaseName = imageFileName.replaceFirst("\\.[^\\.]*$", "");

        String pointingBaseName = null;
        System.out.println("ImageFileUtil: getPointingServerPath: pointing type " + pointingType);
        System.out.println("ImageFileUtil: getPointingServerPath: image source " + imageSource.toString());
        if (pointingType.equals("SUM"))
        {
            if (instrument.getInstrumentName() == Instrument.MSI)
            {
                pointingBaseName = imageBaseName.substring(0, MsiSumFileBaseNameLength);
            }
            else
            {
                if (imageSource == ImageSource.CORRECTED)
                	pointingBaseName = CorrectedSumFileLookup.lookUp(pointingRoot, imageFileName);
                else
                {
                	pointingBaseName = SumFileLookup.lookUp(pointingRoot, imageFileName);
                	System.out.println("ImageFileUtil: getPointingServerPath: pointing base " + pointingBaseName);
                }
            }
        }

        if (pointingBaseName == null)
        {
            pointingBaseName = imageBaseName;
        }

        String pointingFileName = pointingBaseName + extension;

        return String.join("/", pointingDir, pointingFileName);
    }

}
