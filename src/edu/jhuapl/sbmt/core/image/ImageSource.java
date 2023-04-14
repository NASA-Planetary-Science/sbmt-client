package edu.jhuapl.sbmt.core.image;

public enum ImageSource
{
    SPICE("SPICE Derived", "pds", "infofiles", "INFO"), //
    GASKELL("SPC Derived", "gaskell", "sumfiles", "SUM"), //
    GASKELL_UPDATED("SPC Derived Updated", "gaskell_updated", "sumfiles-updated", "SUM"), //
    LABEL("Label Derived", "label", "labels", "LBL"), //
    CORRECTED("Corrected", "corrected", "sumfiles-corrected", "SUM"), //
    CORRECTED_SPICE("Corrected SPICE Derived", "corrected_pds", "infofiles-corrected", "INFO"), //
    IMAGE_MAP("ImageMap", "image_map", "", null), //
    LOCAL_CYLINDRICAL("LocalCylindrical", "local_cylindrical", "", null), //
    LOCAL_PERSPECTIVE("LocalPerspective", "local_perspective", "", null), //
    FALSE_COLOR("FalseColor", "false_color", "", null);

    // String used in the GUI Pointing drop-down menu
    private final String string;
    // String used in the database table name
    private final String databaseTableName;
    // String locating pointing files under the model/imager directory
    private final String pointingDir;
    // String indicating type of pointing, e.g. INFO
    private final String pointingType;

    private ImageSource(String nameString, String databaseTableName, String pointingDir, String pointingType)
    {
        this.string = nameString;
        this.databaseTableName = databaseTableName;
        this.pointingDir = pointingDir;
        this.pointingType = pointingType;
    }

    @Override
    public String toString()
    {
        return string;
    }

    public String getDatabaseTableName()
    {
        return databaseTableName;
    }

    /**
     * Return the directory used to store pointing files for the relevant source
     * type. The path is assumed to be relative to the top model-specific imager
     * directory.
     *
     * @return the pointing directory name
     */
    public String getPointingDir()
    {
        return pointingDir;
    }

    public String getPointingType()
    {
        return pointingType;
    }

    public static String printSources(int tabLen)
    {
        String out = new String();
        String tab = new String();
        int i = 0;
        while (i < tabLen)
        {
            tab = tab + " ";
            i++;
        }
        ImageSource[] sources = ImageSource.values();
        for (ImageSource s : sources)
        {
            out = out + tab + s.name() + "\n";
        }
        return out;
    }

    public static ImageSource valueFor(String description)
    {
        for (ImageSource source : values())
        {
            if (source.toString().equals(description))
            {
                return source;
            }
        }
        return null;
    }

}