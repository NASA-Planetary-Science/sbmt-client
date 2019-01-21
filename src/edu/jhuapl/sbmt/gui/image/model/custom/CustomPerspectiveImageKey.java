package edu.jhuapl.sbmt.gui.image.model.custom;

import edu.jhuapl.saavtk.metadata.InstanceGetter;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.StorableAsMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.gui.image.model.CustomImageKeyInterface;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;

public class CustomPerspectiveImageKey implements StorableAsMetadata<CustomPerspectiveImageKey>, CustomImageKeyInterface
{
    public String name = ""; // name to call this image for display purposes
    public String imagefilename = ""; // filename of image on disk
    public ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    public double rotation = 0.0;
    public String flip = "None";
    public String pointingFilename = "null";
    public final ImageType imageType;
    public final FileType fileType;
    public final ImageSource source;

    private static final Key<String> nameKey = Key.of("name");
    private static final Key<String> imageFileNameKey = Key.of("imagefilename");
    private static final Key<String> sourceKey = Key.of("source");
    private static final Key<String> imageTypeKey = Key.of("imageType");
    private static final Key<Double> rotationKey = Key.of("rotation");
    private static final Key<String> flipKey = Key.of("flip");
    private static final Key<String> fileTypeKey = Key.of("fileTypeKey");
    private static final Key<String> pointingFilenameKey = Key.of("pointingfilename");
    private static final Key<CustomPerspectiveImageKey> CUSTOM_PERSPECTIVE_IMAGE_KEY = Key.of("customPerspectiveImage");

    public CustomPerspectiveImageKey(String name, String imagefilename, ImageSource source, ImageType imageType,
    		double rotation, String flip, FileType fileType, String pointingFilename)
    {
    	this.name = name;
    	this.imagefilename = imagefilename;
    	this.source = source;
    	this.imageType = imageType;
    	this.rotation = rotation;
    	this.flip = flip;
    	this.fileType = fileType;
    	this.pointingFilename = pointingFilename;

    }

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


	public void setImagefilename(String imagefilename)
	{
		this.imagefilename = imagefilename;
	}

	public ImageType getImageType()
	{
		return imageType;
	}

	public ProjectionType getProjectionType()
	{
		return projectionType;
	}

	public ImageSource getSource()
	{
		return source;
	}

	public double getRotation()
	{
		return rotation;
	}

	public String getFlip()
	{
		return flip;
	}

	@Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(Key.of("customimagetype"), CUSTOM_PERSPECTIVE_IMAGE_KEY.toString());
        result.put(nameKey, name);
        result.put(imageFileNameKey, imagefilename);
        result.put(sourceKey, source.toString());
        result.put(imageTypeKey, imageType.toString());
        result.put(rotationKey, rotation);
        result.put(flipKey, flip);
        result.put(fileTypeKey, fileType.toString());
        result.put(pointingFilenameKey, pointingFilename);
        return result;
    }

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(CUSTOM_PERSPECTIVE_IMAGE_KEY, (metadata) -> {

	        String name = metadata.get(nameKey);
	        String imagefilename = metadata.get(imageFileNameKey);
	        ImageSource source = ImageSource.valueOf(metadata.get(sourceKey));
	        ImageType imageType = ImageType.valueOf(metadata.get(imageTypeKey));
	        double rotation = metadata.get(rotationKey);
	        String flip = metadata.get(flipKey);
	        FileType fileType = FileType.valueOf(metadata.get(fileTypeKey));
	        String pointingFilename = metadata.get(pointingFilenameKey);

	        CustomPerspectiveImageKey result = new CustomPerspectiveImageKey(name, imagefilename, source, imageType, rotation, flip, fileType, pointingFilename);
	        result.imagefilename = imagefilename;

			return result;
		});
	}

	@Override
	public Key<CustomPerspectiveImageKey> getKey()
	{
		return CUSTOM_PERSPECTIVE_IMAGE_KEY;
	}

//    @Override
//    public void retrieve(Metadata source)
//    {
//        name = source.get(nameKey);
//        imagefilename = source.get(imageFileNameKey);
//        projectionType = ProjectionType.valueOf(source.get(projectionKey));
//        imageType = ImageType.valueOf(source.get(imageTypeKey));
//        rotation = source.get(rotationKey);
//        flip = source.get(flipKey);
//        sumfilename = source.get(sumfilenameKey);
//        infofilename = source.get(infofileKey);
//    }

}
