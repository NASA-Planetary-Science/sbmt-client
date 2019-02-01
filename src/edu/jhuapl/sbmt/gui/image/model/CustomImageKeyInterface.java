package edu.jhuapl.sbmt.gui.image.model;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import edu.jhuapl.sbmt.gui.image.model.custom.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.ProvidableFromMetadata;
import crucible.crust.metadata.impl.InstanceGetter;

public interface CustomImageKeyInterface extends ImageKeyInterface
{
	public Date getDate();

	public ProjectionType getProjectionType();

	public void setImagefilename(String imagefilename);

	static CustomImageKeyInterface retrieve(Metadata objectMetadata)
	{
		final Key<String> key = Key.of("customimagetype");
//		Key<String> key = objectMetadata.get(Key.of("customimagetype"));
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

    default List<String> toList()
    {
        List<String> string = new Vector<String>();
        string.add(getName());
        string.add(""+getDate().getTime());
        return string;

    }
}
