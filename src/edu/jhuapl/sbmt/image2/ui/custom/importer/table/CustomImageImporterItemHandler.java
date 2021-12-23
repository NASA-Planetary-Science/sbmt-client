package edu.jhuapl.sbmt.image2.ui.custom.importer.table;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.CustomPerspectiveImageCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class CustomImageImporterItemHandler<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable > extends BasicItemHandler<G1, CustomImageImporterColumnLookup>
{
	private final CustomPerspectiveImageCollection imageCollection;

	public CustomImageImporterItemHandler(CustomPerspectiveImageCollection aManager, QueryComposer<CustomImageImporterColumnLookup> aComposer)
	{
		super(aComposer);

		imageCollection = aManager;
	}

	@Override
	public Object getColumnValue(G1 image, CustomImageImporterColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case IMAGE_PATH:
				return image.getFilename();
			case IMAGE_NAME:
				return image.getName();
			case POINTING_FILE:
				return image.getPointingSource();
			case IMAGE_ROTATION:
				return image.getRotation();
			case IMAGE_FLIP:
				return image.getFlip();
			case LATITUDE_MIN:
				return image.getBounds().getMinLatitude();
			case LATITUDE_MAX:
				return image.getBounds().getMaxLatitude();
			case LONGITUDE_MIN:
				return image.getBounds().getMinLongitude();
			case LONGITUDE_MAX:
				return image.getBounds().getMaxLongitude();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(IPerspectiveImage image, CustomImageImporterColumnLookup aEnum, Object aValue)
	{
//		if (aEnum == CustomImageImporterColumnLookup.Map)
//		{
//			imageCollection.setImageMapped(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Offlimb)
//		{
//			imageCollection.setImageOfflimbShowing(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Frustum)
//		{
//			imageCollection.setImageFrustumVisible(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Boundary)
//		{
//			imageCollection.setImageBoundaryShowing(image, (Boolean)aValue);
//		}
//		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}
