package edu.jhuapl.sbmt.core.image;

import edu.jhuapl.saavtk.util.FillDetector;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.config.SpectralImageMode;
import edu.jhuapl.sbmt.query.IQueryBase;

import crucible.crust.metadata.api.Metadata;

public interface IImagingInstrument
{
    Metadata store();

    /**
     * Deprecated: phase out in order to move away from enum-like
     * mission-specific types to represent image variations. Nothing should be
     * consulting this to know what to do anymore.
     *
     * @return
     */
    @Deprecated
    ImageType getType();

    /**
     * Deprecated: no longer necessary or useful for queries (fixed list v. db
     * etc.) to be determined by the instrument.
     *
     * @return
     */
    @Deprecated
    IQueryBase getSearchQuery();

    ImageSource[] getSearchImageSources();

    SpectralImageMode getSpectralMode();

    Instrument getInstrumentName();

    FillDetector<Float> getFillDetector(Image image);

    int[] getLinearInterpolationDims();

    int[] getMaskValues();

    public double[] getFillValues();

    int[] getPadValues();

    int[] getMaxSizeValues();

    /**
     * Get the {@link Orientation} to use for images from this instrument when
     * projecting them using the pointing type indicated by the argument.
     *
     * @param source the pointing type
     * @return the orientation
     */
    Orientation getOrientation(ImageSource source);

}
