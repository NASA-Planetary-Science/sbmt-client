package edu.jhuapl.sbmt.core.image;

public enum ImageSource
{
    SPICE("SPICE Derived", "pds", "infofiles"), //
    GASKELL("SPC Derived", "gaskell", "sumfiles"), //
    GASKELL_UPDATED("SPC Derived", "gaskell_updated", "sumfiles-updated"), //
    LABEL("Label Derived", "label", "labels"), //
    CORRECTED("Corrected", "corrected", "sumfiles-corrected"), //
    CORRECTED_SPICE("Corrected SPICE Derived", "corrected_pds", "infofiles-corrected"), //
    IMAGE_MAP("ImageMap", "image_map", ""), //
    LOCAL_CYLINDRICAL("LocalCylindrical", "local_cylindrical", ""), //
    LOCAL_PERSPECTIVE("LocalPerspective", "local_perspective", ""), //
    FALSE_COLOR("FalseColor", "false_color", "");

    // String used in the GUI Pointing drop-down menu
    private final String string;
    // String used in the database table name
    private final String databaseTableName;
    // String locating pointing files under the model/imager directory
    private final String pointingDir;

    private ImageSource(String nameString, String databaseTableName, String pointingDir)
    {
        this.string = nameString;
        this.databaseTableName = databaseTableName;
        this.pointingDir = pointingDir;
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