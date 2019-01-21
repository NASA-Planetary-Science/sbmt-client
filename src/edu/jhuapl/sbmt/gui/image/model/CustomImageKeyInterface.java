package edu.jhuapl.sbmt.gui.image.model;

import edu.jhuapl.saavtk.metadata.InstanceGetter;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.ProvidableFromMetadata;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;

public interface CustomImageKeyInterface extends ImageKeyInterface
{

	public ProjectionType getProjectionType();

	public void setImagefilename(String imagefilename);

	static CustomImageKeyInterface retrieve(Metadata objectMetadata)
	{
		Key key = objectMetadata.get(Key.of("customimagetype"));
		if (key.toString().equals("CUSTOM_CYLINDRICAL_IMAGE_KEY"))
		{
			Key<CustomCylindricalImageKey> CUSTOM_CYLINDRICAL_IMAGE_KEY = Key.of("customCylindricalImage");
			ProvidableFromMetadata<CustomCylindricalImageKey> metadata = InstanceGetter.defaultInstanceGetter().of(CUSTOM_CYLINDRICAL_IMAGE_KEY);
			return metadata.provide(objectMetadata);
		}
		else
		{
			Key<CustomPerspectiveImageKey> CUSTOM_PERSPECTIVE_IMAGE_KEY = Key.of("customPerspectiveImage");
			ProvidableFromMetadata<CustomPerspectiveImageKey> metadata = InstanceGetter.defaultInstanceGetter().of(CUSTOM_PERSPECTIVE_IMAGE_KEY);
			return metadata.provide(objectMetadata);
		}
	}
}
