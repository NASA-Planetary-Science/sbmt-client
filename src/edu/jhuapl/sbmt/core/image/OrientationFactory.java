package edu.jhuapl.sbmt.core.image;

import edu.jhuapl.sbmt.image.model.ImageFlip;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class OrientationFactory
{
    public OrientationFactory()
    {
        super();
    }

    public Orientation of(ImageFlip flip, double rotation, boolean transpose)
    {
        initializeSerializationProxy();
        return new Orientation(flip, rotation, transpose);
    }

    public Orientation of(Metadata md)
    {
        initializeSerializationProxy();
        return InstanceGetter.defaultInstanceGetter().providesGenericObjectFromMetadata(MetadataKey).provide(md);
    }

    public Metadata toMetadata(Orientation orientation)
    {
        initializeSerializationProxy();
        return InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(Orientation.class).provide(orientation);
    }

    protected static Key<Orientation> MetadataKey = Key.of("ImageOrientation");

    protected void initializeSerializationProxy()
    {
        InstanceGetter instanceGetter = InstanceGetter.defaultInstanceGetter();
        if (!instanceGetter.isProvidableFromMetadata(MetadataKey))
        {
            instanceGetter.register(MetadataKey, source -> {
                ImageFlip flip = source.get(Key.of("flip"));
                double rotation = source.get(Key.of("rotation"));
                boolean transpose = source.get(Key.of("transpose"));

                return new Orientation(flip, rotation, transpose);
            }, OrientationFactory.class, orientation -> {
                SettableMetadata md = SettableMetadata.of(Version.of(1, 0));
                md.put(Key.of("flip"), orientation.getFlip());
                md.put(Key.of("rotation"), orientation.getRotation());
                md.put(Key.of("transpose"), orientation.isTranspose());

                return FixedMetadata.of(md);
            });
        }
    }

}
