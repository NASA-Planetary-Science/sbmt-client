package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

/**
 * Enum to describe possible coloring stayles
 * @author steelrj1
 *
 */
public enum SpectrumColoringStyle
{
    RGB("RGB"),
    GREYSCALE("Greyscale"),
    EMISSION_ANGLE("Emission Angle");

    private String name;

    public String toString()
    {
        return name;
    }

    private SpectrumColoringStyle(String name)
    {
        this.name = name;
    }

    /**
     * Returns the style for given name
     * @param name
     * @return
     */
    public static SpectrumColoringStyle getStyleForName(String name)
    {
        for (SpectrumColoringStyle style : values())
        {
            if (style.toString().equals(name))
                return style;
        }

        return null;
    }
}