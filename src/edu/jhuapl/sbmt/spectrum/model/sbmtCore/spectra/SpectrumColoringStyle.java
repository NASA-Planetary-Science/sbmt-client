package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

/**
 * Enum to describe possible coloring stayles
 * @author steelrj1
 *
 */
public enum SpectrumColoringStyle
{
    RGB("RGB"),
    EMISSION_ANGLE("Emission Angle"),
    GREYSCALE("Greyscale");

    private String name;

    public String toString()
    {
        return name;
    }

    private SpectrumColoringStyle(String name)
    {
        this.name = name;
    }

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
