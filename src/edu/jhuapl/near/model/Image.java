package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import vtk.vtkTexture;

import edu.jhuapl.near.model.SmallBodyConfig.ImageType;
import edu.jhuapl.near.model.SmallBodyConfig.Instrument;
import edu.jhuapl.near.query.QueryBase;
import edu.jhuapl.near.util.IntensityRange;
import edu.jhuapl.near.util.Properties;


public abstract class Image extends Model implements PropertyChangeListener
{
    public static final String IMAGE_NAMES = "ImageNames"; // What name to give this image for display
    public static final String IMAGE_FILENAMES = "ImageFilenames"; // Filename of image on disk
    public static final String IMAGE_MAP_PATHS = "ImageMapPaths"; // For backwards compatibility, still read this in
    public static final String PROJECTION_TYPES = "ProjectionTypes";

    public enum ImageSource
    {
        SPICE {
            public String toString()
            {
                return "SPICE Derived";
            }
        },
        GASKELL {
            public String toString()
            {
                return "Gaskell Derived";
            }
        },
        LABEL {
            public String toString()
            {
                return "Label Derived";
            }
        },
        CORRECTED {
            public String toString()
            {
                return "Corrected";
            }
        },
        CORRECTED_SPICE {
            public String toString()
            {
                return "Corrected SPICE Derived";
            }
        },
        IMAGE_MAP {
            public String toString()
            {
                return "ImageMap";
            }
        },
        LOCAL_CYLINDRICAL {
            public String toString()
            {
                return "LocalCylindrical";
            }
        },
        LOCAL_PERSPECTIVE {
            public String toString()
            {
                return "LocalPerspective";
            }
        },
        FALSE_COLOR {
            public String toString()
            {
                return "FalseColor";
            }
        }
    }


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

    public enum FileType
    {
        SUM {
            public String toString()
            {
                return "SUM";
            }
        },
        INFO {
            public String toString()
            {
                return "INFO";
            }
        },
    }


    public static class ImagingInstrument
    {
        public SpectralMode spectralMode;
        public QueryBase searchQuery;
        public ImageSource[] searchImageSources;
        public ImageType type;
        public Instrument instrumentName;

        public ImagingInstrument()
        {
            this(SpectralMode.MONO, null, null, null, null);
        }

        public ImagingInstrument(SpectralMode spectralMode)
        {
            this(spectralMode, null, null, null, null);
        }

        public ImagingInstrument(SpectralMode spectralMode, QueryBase searchQuery, ImageType type, ImageSource[] searchImageSources, Instrument instrumentName)
        {
            this.spectralMode = spectralMode;
            this.searchQuery = searchQuery;
            this.type = type;
            this.searchImageSources = searchImageSources;
            this.instrumentName = instrumentName;
        }

        public ImagingInstrument clone()
        {
            return new ImagingInstrument(spectralMode, searchQuery.clone(), type, searchImageSources.clone(), instrumentName);
        }
    }


    /**
     * An ImageKey should be used to uniquely distinguish one image from another.
     * It also contains metadata about the image that may be necessary to know
     * before the image is loaded, such as the image projection information and
     * type of instrument used to generate the image.
     *
     * No two images will have the same values for the fields of this class.
     */
    public static class ImageKey
    {
        // The path of the image as passed into the constructor. This is not the
        // same as fullpath but instead corresponds to the name needed to download
        // the file from the server (excluding the hostname and extension).
        public String name;

        public ImageSource source;

        public FileType fileType;

        public ImagingInstrument instrument;

        public String band;

        public int slice;


        public ImageKey(String name, ImageSource source)
        {
            this(name, source, null, null);
        }

        public ImageKey(String name, ImageSource source, FileType fileType)
        {
            this(name, source, fileType, null, null, 0);
        }

        public ImageKey(String name, ImageSource source, ImagingInstrument instrument)
        {
            this(name, source, null, instrument, null, 0);
        }

        public ImageKey(String name, ImageSource source, FileType fileType, ImagingInstrument instrument)
        {
            this(name, source, fileType, instrument, null, 0);
        }

        public ImageKey(String name, ImageSource source, FileType fileType, ImagingInstrument instrument, String band, int slice)
        {
            this.name = name;
            this.source = source;
            this.fileType = fileType;
            this.instrument = instrument;
            this.band = band;
            this.slice = slice;
        }

        @Override
        public boolean equals(Object obj)
        {
            return name.equals(((ImageKey)obj).name) && source.equals(((ImageKey)obj).source);
        }

        @Override
        public int hashCode()
        {
            return name.hashCode();
        }
    }

    protected final ImageKey key;

    public Image(ImageKey key)
    {
        this.key = key;
    }

    public ImageKey getKey()
    {
        return key;
    }

    public String getImageName()
    {
        return new File(key.name).getName();
    }

    public void imageAboutToBeRemoved()
    {
        // By default do nothing. Let subclasses handle this.
    }

    abstract public vtkTexture getTexture();
    abstract public LinkedHashMap<String, String> getProperties() throws IOException;
    abstract public void setDisplayedImageRange(IntensityRange range);


    public void setInterpolate(boolean enable)
    {
        vtkTexture texture = getTexture();
        if (texture != null)
        {
            texture.SetInterpolate(enable ? 1 : 0);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public boolean getInterpolate()
    {
        vtkTexture texture = getTexture();
        if (texture != null)
            return texture.GetInterpolate() == 0 ? false : true;
        else
            return true;
    }

    abstract public int getNumberOfComponentsOfOriginalImage();

    public int[] getCurrentMask()
    {
        return new int[] {0, 0, 0, 0};
    }

    public void setCurrentMask(int[] masking)
    {

    }

}
