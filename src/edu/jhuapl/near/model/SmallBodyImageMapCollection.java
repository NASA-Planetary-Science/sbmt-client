package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.model.SmallBodyImageMap.ImageInfo;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.MapUtil;
import edu.jhuapl.near.util.Properties;

public class SmallBodyImageMapCollection extends Model implements PropertyChangeListener
{
    private ArrayList<SmallBodyImageMap> images = new ArrayList<SmallBodyImageMap>();
    private SmallBodyModel smallBodyModel = null;

    // Store the opacity even though each individual image has it's own opacity
    // since we need this in case the user edits the images shown for custom models.
    private double opacity = 1.0;

    public SmallBodyImageMapCollection(SmallBodyModel smallBodyModel)
    {
        super(ModelNames.SMALL_BODY_IMAGE_MAP);

        this.smallBodyModel = smallBodyModel;

        loadImages();
    }

    private void loadImages()
    {
        images.clear();

        if (smallBodyModel.isImageMapAvailable())
        {
            String[] imageNames = smallBodyModel.getImageMapNames();

            // If the imageNames array contains a single path which is a folder,
            // then look in the folder for the images and replace imageNames with that
            // list.
            if (imageNames.length == 1 && new File(imageNames[0]).isDirectory())
            {
                File[] dirs = new File(imageNames[0]).listFiles();
                ArrayList<String> images = new ArrayList<String>();
                if (dirs != null && dirs.length > 0)
                {
                    for (File file : dirs)
                    {
                        if (file.getAbsolutePath().endsWith(".png"))
                        {
                            images.add(FileCache.FILE_PREFIX + file.getAbsolutePath());
                        }
                    }

                    if (images.size() > 0)
                    {
                        String[] names = new String[images.size()];
                        imageNames = images.toArray(names);
                    }
                }

                if (images.isEmpty())
                {
                    this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
                    return;
                }
            }

            // The following 5 variables are used only for imported shape models
            // using the new version of tool that creates a config.txt file in the
            // model folder.
            Map<String, String> configMap = null;
            String[] lllats = null;
            String[] lllons = null;
            String[] urlats = null;
            String[] urlons = null;

            for (int i = 0; i<imageNames.length; ++i)
            {
                String name = imageNames[i];
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.filename = name;

                // By default the image covers the entire shape model. If however,
                // this is a custom shape model, then it may not. To determine if
                // this is a custom shape model, look to see if the imageName begins
                // with the file:// prefix as this is only the case for custom models.
                if (name.startsWith(FileCache.FILE_PREFIX))
                {
                    name = name.substring(FileCache.FILE_PREFIX.length());
                    try
                    {
                        // First see if a config.txt file exists in the directory
                        File dir = new File(name).getParentFile();
                        File configFile = new File(dir, "config.txt");

                        if (configFile.exists())
                        {
                            // Only need to load the config.txt file for the first image
                            if (configMap == null)
                            {
                                configMap = MapUtil.loadMap(configFile.getAbsolutePath());
                                lllats = configMap.get(CustomShapeModel.LOWER_LEFT_LATITUDES).split(",");
                                lllons = configMap.get(CustomShapeModel.LOWER_LEFT_LONGITUDES).split(",");
                                urlats = configMap.get(CustomShapeModel.UPPER_RIGHT_LATITUDES).split(",");
                                urlons = configMap.get(CustomShapeModel.UPPER_RIGHT_LONGITUDES).split(",");
                            }

                            imageInfo.lllat = Double.parseDouble(lllats[i]);
                            imageInfo.lllon = Double.parseDouble(lllons[i]);
                            imageInfo.urlat = Double.parseDouble(urlats[i]);
                            imageInfo.urlon = Double.parseDouble(urlons[i]);
                        }
                        else
                        {
                            // If there's a corners.txt file, the following call
                            // will parse and return the values. If not, default
                            // values covering entire shape model will be returned.
                            double[] corners = loadTextureCorners();

                            imageInfo.lllat = corners[0];
                            imageInfo.lllon = corners[1];
                            imageInfo.urlat = corners[2];
                            imageInfo.urlon = corners[3];
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                SmallBodyImageMap image = new SmallBodyImageMap(smallBodyModel, imageInfo);
                image.addPropertyChangeListener(this);
                images.add(image);
            }
        }
    }

    public void reset()
    {
        loadImages();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

        this.setShowImageMaps(isVisible());
        this.setImageMapOpacities(opacity);
    }

    public void setShowImageMaps(boolean b)
    {
        super.setVisible(b);
        for (SmallBodyImageMap image : images)
            image.setShowImageMap(b);
    }

    public void setImageMapOpacities(double opacity)
    {
        this.opacity = opacity;
        for (SmallBodyImageMap image : images)
            image.setImageMapOpacity(opacity);
    }

    public SmallBodyImageMap getImage(vtkActor actor)
    {
        for (SmallBodyImageMap image : images)
        {
            if (image.getProps().contains(actor))
                return image;
        }

        return null;
    }

    public ArrayList<vtkProp> getProps()
    {
        ArrayList<vtkProp> props = new ArrayList<vtkProp>();
        for (SmallBodyImageMap image : images)
            props.addAll(image.getProps());
        return props;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private double[] loadTextureCorners()
    {
        try
        {
            // Load in the corners.txt file
            String cornersFilename = Configuration.getImportedShapeModelsDir() +
                    File.separator +
                    smallBodyModel.getModelName() +
                    File.separator +
                    "corners.txt";

            ArrayList<String> words = FileUtil.getFileWordsAsStringList(cornersFilename);
            double lllat = Double.parseDouble(words.get(0));
            double lllon = Double.parseDouble(words.get(1));
            double urlat = Double.parseDouble(words.get(2));
            double urlon = Double.parseDouble(words.get(3));

            return new double[]{lllat, lllon, urlat, urlon};
        }
        catch (IOException ex)
        {
            // silently ignore
        }

        return new double[]{-90.0, 0.0, 90.0, 360.0};
    }
}
