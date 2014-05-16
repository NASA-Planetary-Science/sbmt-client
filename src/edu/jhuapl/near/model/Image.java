package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import vtk.vtkTexture;

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
                return "SPICE derived";
            }
        },
        GASKELL {
            public String toString()
            {
                return "Gaskell derived";
            }
        },
        CORRECTED {
            public String toString()
            {
                return "Corrected";
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
        }
    }

    /**
     * An ImageKey should be used to uniquely distinguish one image from another.
     * No two images will have the same values for the fields of this class.
     */
    public static class ImageKey
    {
        // The path of the image as passed into the constructor. This is not the
        // same as fullpath but instead corresponds to the name needed to download
        // the file from the server (excluding the hostname and extension).
        public String name;

        public ImageSource source;

        public ImageKey()
        {
        }

        public ImageKey(String name, ImageSource source)
        {
            this.name = name;
            this.source = source;
        }

        @Override
        public boolean equals(Object obj)
        {
            return name.equals(((ImageKey)obj).name) && source == ((ImageKey)obj).source;
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

    abstract public double getImageOpacity();
    abstract public void setImageOpacity(double imageOpacity);


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
}
