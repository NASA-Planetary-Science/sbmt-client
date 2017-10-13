package edu.jhuapl.sbmt.config;

import edu.jhuapl.saavtk.config.Configurable;
import edu.jhuapl.saavtk.config.Entry;
import edu.jhuapl.saavtk.config.ExtensibleTypedLookup;
import edu.jhuapl.saavtk.config.FixedTypedLookup;
import edu.jhuapl.saavtk.config.Key;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;

public class SessionConfiguration extends ExtensibleTypedLookup
        implements Configurable
{
    // Required keys.
    public static final Key<SBMTBodyConfiguration> BODY_CONFIG = Key.of("Body configuration");
    public static final Key<ShapeModelConfiguration> SHAPE_MODEL_CONFIG = Key.of("Shape model configuration");
    public static final Key<SBMTFileLocator> FILE_LOCATOR = Key.of("File locator");

    // Optional keys.
    public static final Key<ImagingInstrumentConfiguration> IMAGING_INSTRUMENT_CONFIG = Key.of("Imaging instrument configuration");

    public static final Builder<SessionConfiguration> builder(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration shapeConfig, SBMTFileLocator fileLocator)
    {
        final FixedTypedLookup.Builder fixedBuilder = FixedTypedLookup.builder();

        fixedBuilder.put(Entry.of(BODY_CONFIG, bodyConfig));
        fixedBuilder.put(Entry.of(SHAPE_MODEL_CONFIG, shapeConfig));
        fixedBuilder.put(Entry.of(FILE_LOCATOR, fileLocator));

        return new Builder<SessionConfiguration>(fixedBuilder) {
            @Override
            public SessionConfiguration build()
            {
                return new SessionConfiguration(fixedBuilder);
            }
        };
    }

    protected SessionConfiguration(FixedTypedLookup.Builder builder)
    {
        super(builder);
    }

}
