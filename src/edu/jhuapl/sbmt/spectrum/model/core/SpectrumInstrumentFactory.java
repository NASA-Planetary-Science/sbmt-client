package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.Hashtable;

/**
 * Factory to register and get spectrum instruments.  This is used by code that uses the Spectra Library (e.g. SBMT)
 * @author steelrj1
 *
 */
public class SpectrumInstrumentFactory
{
    static Hashtable<String, BasicSpectrumInstrument> spectralInstruments = new Hashtable<String, BasicSpectrumInstrument>();

    static public void registerType(String name, BasicSpectrumInstrument spectralInstrument)
    {
        spectralInstruments.put(name, spectralInstrument);
    }

    static public BasicSpectrumInstrument getInstrumentForName(String name)
    {
        return spectralInstruments.get(name);
    }
}
