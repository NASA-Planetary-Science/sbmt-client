package edu.jhuapl.sbmt.imaging.instruments;

import edu.jhuapl.saavtk.config.Configurable;
import edu.jhuapl.saavtk.config.Entry;
import edu.jhuapl.saavtk.config.ExtensibleTypedLookup;
import edu.jhuapl.saavtk.config.FixedTypedLookup;
import edu.jhuapl.saavtk.config.Key;
import edu.jhuapl.saavtk.image.filters.ImageDataFilter;
import edu.jhuapl.sbmt.client.SpectralMode;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.imaging.ImageSource;
import edu.jhuapl.sbmt.query.QueryBase;

public class ImagingInstrumentConfiguration extends ExtensibleTypedLookup implements Configurable
{
    // Required keys.
    public static final Key<Instrument> INSTRUMENT = Key.of("Instrument");
    public static final Key<SpectralMode> SPECTRAL_MODE = Key.of("Spectral Mode");
    public static final Key<QueryBase> QUERY_BASE = Key.of("Search query");
    public static final Key<ImageSource[]> IMAGE_SOURCE = Key.of("Image source for searches");
    public static final Key<SBMTFileLocator> FILE_LOCATOR = Key.of("Image file locator");

    // Optional keys.
    public static final Key<ImageDataFilter> DATA_FILTER = Key.of("Image data filter");
    public static final Key<String> GALLERY_PATH = Key.of("Gallery path"); // If there is a gallery. Relative to image directory.
    public static final Key<String> DISPLAY_NAME = Key.of("Display name"); // If different from instrument.toString().

    public static Builder<ImagingInstrumentConfiguration> builder(
            Instrument instrument,
            SpectralMode spectralMode,
            QueryBase queryBase,
            ImageSource[] imageSource,
            SBMTFileLocator imageFileLocator)
    {
        FixedTypedLookup.Builder fixedBuilder = FixedTypedLookup.builder();
        fixedBuilder.put(Entry.of(INSTRUMENT, instrument));
        fixedBuilder.put(Entry.of(SPECTRAL_MODE, spectralMode));
        fixedBuilder.put(Entry.of(QUERY_BASE, queryBase));
        fixedBuilder.put(Entry.of(IMAGE_SOURCE, imageSource));
        fixedBuilder.put(Entry.of(FILE_LOCATOR, imageFileLocator));
        return new Builder<ImagingInstrumentConfiguration>(fixedBuilder) {
            @Override
            public ImagingInstrumentConfiguration build()
            {
                return new ImagingInstrumentConfiguration(getFixedBuilder());
            }
        };
    }

    protected ImagingInstrumentConfiguration(FixedTypedLookup.Builder builder)
    {
        super(builder);
    }

}
