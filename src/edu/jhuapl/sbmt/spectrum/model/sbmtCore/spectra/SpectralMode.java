package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

public enum SpectralMode
{
    MONO {
        public String toString()
        {
            return "Monospectral";
        }
    },
    MULTI {
        public String toString()
        {
            return "Multispectral";
        }
    },
    HYPER {
        public String toString()
        {
            return "Hyperspectral";
        }
    },
}