package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

/**
 * Listeners that reports if the active coloring has changed
 * @author steelrj1
 *
 */
public interface SpectrumColoringChangedListener
{
    /**
     * Signifies that the active coloring for spectra has changed
     */
    public void coloringChanged();
}
