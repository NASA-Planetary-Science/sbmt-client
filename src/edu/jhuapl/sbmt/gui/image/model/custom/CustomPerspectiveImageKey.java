package edu.jhuapl.sbmt.gui.image.model.custom;

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

public class CustomPerspectiveImageKey extends ImageKey implements MetadataManager
{
    public String name = ""; // name to call this image for display purposes
    public String imagefilename = ""; // filename of image on disk
    public ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    public double rotation = 0.0;
    public String flip = "None";
    public String sumfilename = "null"; // filename of sumfile on disk
    public String infofilename = "null"; // filename of infofile on disk

    final Key<String> nameKey = Key.of("name");
    final Key<String> imageFileNameKey = Key.of("imagefilename");
    final Key<String> projectionKey = Key.of("projectionType");
    final Key<String> imageTypeKey = Key.of("imageType");
    final Key<Double> rotationKey = Key.of("rotation");
    final Key<String> flipKey = Key.of("flip");
    final Key<String> sumfilenameKey = Key.of("sumfilename");
    final Key<String> infofileKey = Key.of("infofilename");

	public CustomPerspectiveImageKey(String name, ImageSource source)
	{
		super(name, source);
		// TODO Auto-generated constructor stub
	}

	public CustomPerspectiveImageKey(String name, ImageSource source, ImagingInstrument instrument)
	{
		super(name, source, instrument);
		// TODO Auto-generated constructor stub
	}

	public CustomPerspectiveImageKey(String name, ImageSource source, FileType fileType, ImageType imageType,
			ImagingInstrument instrument, String band, int slice, String pointingFile)
	{
		super(name, source, fileType, imageType, instrument, band, slice, pointingFile);
		// TODO Auto-generated constructor stub
	}

	//the above constructors are used to aid in generating the necessary parent objects.  The constructor below will call one of those to
	//help build up the properties as needed


    @Override
    public String toString()
    {
        if (imageType == ImageType.GENERIC_IMAGE)
            return name + ", Perspective" + ", " + imageType + ", Rotate " + rotation + ", Flip " + flip;
        else
            return name + ", Perspective" + ", " + imageType;
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
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(nameKey, name);
        result.put(imageFileNameKey, imagefilename);
        result.put(projectionKey, projectionType.toString());
        result.put(imageTypeKey, imageType.toString());
        result.put(rotationKey, rotation);
        result.put(flipKey, flip);
        result.put(sumfilenameKey, sumfilename);
        result.put(infofileKey, infofilename);
        return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
        name = source.get(nameKey);
        imagefilename = source.get(imageFileNameKey);
        projectionType = ProjectionType.valueOf(source.get(projectionKey));
        imageType = ImageType.valueOf(source.get(imageTypeKey));
        rotation = source.get(rotationKey);
        flip = source.get(flipKey);
        sumfilename = source.get(sumfilenameKey);
        infofilename = source.get(infofileKey);
    }

}
