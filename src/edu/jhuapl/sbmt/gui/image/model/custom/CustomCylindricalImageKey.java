package edu.jhuapl.sbmt.gui.image.model.custom;

import java.text.DecimalFormat;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

public class CustomCylindricalImageKey extends ImageKey implements MetadataManager
{
    public String name = ""; // name to call this image for display purposes
    public String imagefilename = ""; // filename of image on disk
    public ProjectionType projectionType = ProjectionType.CYLINDRICAL;
    public double lllat = -90.0;
    public double lllon = 0.0;
    public double urlat = 90.0;
    public double urlon = 360.0;

    final Key<String> nameKey = Key.of("name");
    final Key<String> imageFileNameKey = Key.of("imagefilename");
    final Key<String> projectionKey = Key.of("projectionType");
    final Key<String> imageTypeKey = Key.of("imageType");
    final Key<Double> lllatKey = Key.of("lllat");
    final Key<Double> lllonKey = Key.of("lllon");
    final Key<Double> urlatKey = Key.of("urlat");
    final Key<Double> urlonKey = Key.of("urlon");

	public CustomCylindricalImageKey(String name, ImageSource source)
	{
		super(name, source);
		// TODO Auto-generated constructor stub
	}

	public CustomCylindricalImageKey(String name, ImageSource source, ImagingInstrument instrument)
	{
		super(name, source, instrument);
		// TODO Auto-generated constructor stub
	}

	public CustomCylindricalImageKey(String name, ImageSource source, FileType fileType, ImageType imageType,
			ImagingInstrument instrument, String band, int slice, String pointingFile)
	{
		super(name, source, fileType, imageType, instrument, band, slice, pointingFile);
		// TODO Auto-generated constructor stub
	}

    public String getName()
    {
    	return name;
    }

    public String getImageFilename()
    {
    	return imagefilename;
    }

    @Override
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("#.#####");

        return name + ", Cylindrical  ["
                + df.format(lllat) + ", "
                + df.format(lllon) + ", "
                + df.format(urlat) + ", "
                + df.format(urlon)
                + "]";
    }

	@Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(nameKey, name);
        result.put(imageFileNameKey, imagefilename);
        result.put(projectionKey, projectionType.toString());
        result.put(imageTypeKey, imageType.toString());
        result.put(lllatKey, lllat);
        result.put(lllonKey, lllon);
        result.put(urlatKey, urlat);
        result.put(urlonKey, urlon);
        return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
        name = source.get(nameKey);
        imagefilename = source.get(imageFileNameKey);
        projectionType = ProjectionType.valueOf(source.get(projectionKey));
        imageType = ImageType.valueOf(source.get(imageTypeKey));
        lllat = source.get(lllatKey);
        lllon = source.get(lllonKey);
        urlat = source.get(urlatKey);
        urlon = source.get(urlonKey);
    }

}
