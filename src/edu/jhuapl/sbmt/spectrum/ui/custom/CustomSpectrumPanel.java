package edu.jhuapl.sbmt.spectrum.ui.custom;
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
///*
// * CustomImageLoaderPanel.java
// *
// * Created on Jun 5, 2012, 3:56:56 PM
// */
//package edu.jhuapl.sbmt.gui.spectrum;
//
//import java.awt.BorderLayout;
//import java.awt.FlowLayout;
//import java.awt.Rectangle;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.MouseEvent;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import javax.swing.BoundedRangeModel;
//import javax.swing.DefaultBoundedRangeModel;
//import javax.swing.DefaultListModel;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JSlider;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//
//import vtk.vtkActor;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.model.FileType;
//import edu.jhuapl.saavtk.model.Model;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.saavtk.model.PolyhedralModel;
//import edu.jhuapl.saavtk.pick.PickEvent;
//import edu.jhuapl.saavtk.pick.PickManager;
//import edu.jhuapl.saavtk.util.FileUtil;
//import edu.jhuapl.saavtk.util.MapUtil;
//import edu.jhuapl.saavtk.util.Properties;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
//import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumKey;
//import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
//import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
//import edu.jhuapl.sbmt.model.image.ImagingInstrument;
//import edu.jhuapl.sbmt.model.spectrum.ISpectralInstrument;
//import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
//import edu.jhuapl.sbmt.model.spectrum.SpectraType;
//import edu.jhuapl.sbmt.model.spectrum.Spectrum;
//import edu.jhuapl.sbmt.model.spectrum.SpectrumInstrumentFactory;
//import edu.jhuapl.sbmt.model.spectrum.SpectrumKeyInterface;
//import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;
//
//
//public class CustomSpectrumPanel extends javax.swing.JPanel implements PropertyChangeListener, ActionListener, ChangeListener, ListSelectionListener
//{
//
//    private ModelManager modelManager;
//    private SpectrumPopupMenu spectrumPopupMenu;
//    private SbmtInfoWindowManager infoPanelManager;
//    private SbmtSpectrumWindowManager spectrumPanelManager;
//    final PickManager pickManager;
//    Renderer renderer;
//    private SpectralInstrument instrument;
//    int numSpectraInCollection = -1;
//
//    private boolean initialized = false;
//
//    private JPanel bandPanel;
//    private JLabel bandValue;
//    private JSlider monoSlider;
////    private JCheckBox defaultFrustum;
//    private BoundedRangeModel monoBoundedRangeModel;
//
//    private int nbands = 1;
//    private int currentSlice = 0;
//
//    public int getCurrentSlice() { return currentSlice; }
//
//    public String getCurrentBand() { return Integer.toString(currentSlice); }
//
//    protected ModelManager getModelManager()
//    {
//        return modelManager;
//    }
//
//    protected ModelNames getSpectraCollectionModelName()
//    {
//        return ModelNames.SPECTRA;
//    }
//
//    protected ModelNames getModelName()
//    {
//        return ModelNames.SMALL_BODY;
//    }
//
//    private ModelNames getSpectraBoundaryCollectionModelName()
//    {
//        return ModelNames.CIRCLE_SELECTION;
//    }
//
//    /** Creates new form CustomSpectrumLoaderPanel */
//    public CustomSpectrumPanel(
//            final ModelManager modelManager,
//            SbmtInfoWindowManager infoPanelManager,
//            SbmtSpectrumWindowManager spectrumPanelManager,
//            final PickManager pickManager,
//            Renderer renderer,
//            SpectralInstrument instrument)
//    {
//        this.modelManager = modelManager;
//        this.infoPanelManager = infoPanelManager;
//        this.spectrumPanelManager = spectrumPanelManager;
//        this.pickManager = pickManager;
//        this.renderer = renderer;
//        this.instrument = instrument;
//
//        pickManager.getDefaultPicker().addPropertyChangeListener(this);
//
////        initComponents();
//
////        pickManager.getDefaultPicker().addPropertyChangeListener(this);
////
////        spectraList.setModel(new DefaultListModel());
////
////        ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
////        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
////        imagePopupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, spectrumPanelManager, renderer, this);
////
////        addComponentListener(new ComponentAdapter()
////        {
////            @Override
////            public void componentShown(ComponentEvent e)
////            {
////                try
////                {
////                    initializeImageList();
////                }
////                catch (IOException e1)
////                {
////                    e1.printStackTrace();
////                }
////            }
////        });
//    }
//
//    protected void initExtraComponents()
//    {
//        spectraList.setModel(new DefaultListModel());
//
//    }
//
//    public CustomSpectrumPanel init()
//    {
////        pickManager.getDefaultPicker().addPropertyChangeListener(this);
//
//        initComponents();
//        initExtraComponents();
//        populateMonochromePanel(monochromePanel);
//
////        spectraList.setModel(new DefaultListModel());
//
////        ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
////        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
////        imagePopupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, spectrumPanelManager, renderer, this);
//
////        postInitComponents(instrument);
//
//        SpectraCollection images = (SpectraCollection)modelManager.getModel(getSpectraCollectionModelName());
////        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getSpectraBoundaryCollectionModelName());
//        spectrumPopupMenu = new SpectrumPopupMenu(modelManager, infoPanelManager, renderer);
//
//
////        boundaries.addPropertyChangeListener(this);
//        images.addPropertyChangeListener(this);
////
////        ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
////        colorImagePopupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, modelManager, this);
//
//        addComponentListener(new ComponentAdapter()
//        {
//            @Override
//            public void componentShown(ComponentEvent e)
//            {
//                try
//                {
//                    initializeSpectraList();
//                }
//                catch (IOException e1)
//                {
//                    e1.printStackTrace();
//                }
//            }
//        });
//
//        spectraList.addListSelectionListener(this);
//
//        return this;
//    }
//
//    protected void populateMonochromePanel(JPanel panel)
//    {
//        panel.setLayout(new BorderLayout());
//
//
//
//        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//        bandPanel.add(new JLabel("Band:"));
//        int midband = (nbands-1) / 2;
//        String midbandString = Integer.toString(midband);
//        bandValue = new JLabel(midbandString);
//        bandPanel.add(bandValue);
//        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
//        monoSlider = new JSlider(monoBoundedRangeModel);
//        monoSlider.addChangeListener(this);
//
////        defaultFrustum = new JCheckBox("Default Frame");
////        defaultFrustum.addActionListener(new java.awt.event.ActionListener() {
////            public void actionPerformed(java.awt.event.ActionEvent evt) {
////                defaultFrustumActionPerformed(evt);
////            }
////        });
////
////        bandPanel.add(defaultFrustum);
//
//        panel.add(bandPanel, BorderLayout.NORTH);
//        panel.add(monoSlider, BorderLayout.CENTER);
//
//
//
//    }
//
//    private void setNumberOfBands(int nbands)
//    {
//        // Select midband by default
//        setNumberOfBands(nbands, (nbands-1)/2);
//    }
//
//    private void setNumberOfBands(int nbands, int activeBand)
//    {
//        this.nbands = nbands;
//        String activeBandString = Integer.toString(activeBand);
//        bandValue.setText(activeBandString);
//        monoBoundedRangeModel = new DefaultBoundedRangeModel(activeBand, 0, 0, nbands-1);
//        monoSlider.setModel(monoBoundedRangeModel);
//    }
//
//    private void defaultFrustumActionPerformed(java.awt.event.ActionEvent evt)
//    {
////        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
////
////        Set<Image> imageSet = images.getImages();
////        for (Image i : imageSet)
////        {
////            PerspectiveImage image = (PerspectiveImage)i;
////            ImageKey key = image.getKey();
////            ImageType type = key.instrument.type;
////            if (type == ImageType.LEISA_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
////            {
////                if (image instanceof PerspectiveImage)
////                {
////                   ((PerspectiveImage)image).setUseDefaultFootprint(defaultFrustum.isSelected());
////                }
////            }
////        }
//    }//GEN-LAST:event_greenMonoCheckboxActionPerformed
//
//    private void postInitComponents(ImagingInstrument instrument)
//    {
//
//
//    }
//
//
//    private String getCustomDataFolder()
//    {
//        return modelManager.getPolyhedralModel().getCustomDataFolder();
//    }
//
//    private String getConfigFilename()
//    {
//        return modelManager.getPolyhedralModel().getConfigFilename();
//    }
//
//    private void initializeSpectraList() throws IOException
//    {
//        if (initialized)
//            return;
//
//        MapUtil configMap = new MapUtil(getConfigFilename());
//
////        if (configMap.containsKey(CylindricalImage.LOWER_LEFT_LATITUDES) || configMap.containsKey(Image.PROJECTION_TYPES))
//        {
//            boolean needToUpgradeConfigFile = false;
//            String[] spectrumNames = configMap.getAsArray(Spectrum.SPECTRUM_NAMES);
//            System.out.println(
//                    "CustomSpectrumPanel: initializeSpectraList: spectrum name size " + spectrumNames.length);
//            String[] spectrumFilenames = configMap.getAsArray(Spectrum.SPECTRUM_FILENAMES);
////            String[] projectionTypes = configMap.getAsArray(Spectrum.PROJECTION_TYPES);
//            String[] spectraTypes = configMap.getAsArray(Spectrum.SPECTRUM_TYPES);
////            String[] imageRotations = configMap.getAsArray(Image.IMAGE_ROTATIONS);
////            String[] imageFlips = configMap.getAsArray(Image.IMAGE_FLIPS);
////            if (spectrumFilenames == null)
////            {
////                // for backwards compatibility
////                spectrumFilenames = configMap.getAsArray(Spectrum.SPECTRUM_FILENAMES);
////                spectrumNames = new String[spectrumFilenames.length];
//////                projectionTypes = new String[imageFilenames.length];
////                spectraTypes = new String[spectrumFilenames.length];
//////                imageRotations = new String[imageFilenames.length];
//////                imageFlips = new String[imageFilenames.length];
////                for (int i=0; i<spectrumFilenames.length; ++i)
////                {
////                    spectrumNames[i] = new File(spectrumFilenames[i]).getName();
////                    spectrumFilenames[i] = "spectrum" + i + ".spect";
//////                    projectionTypes[i] = ProjectionType.CYLINDRICAL.toString();
//////                    spectrumTypes[i] = SpectraType.GENERIC_SPECTRA.toString();
//////                    imageRotations[i] = Double.toString(0.0);
//////                    imageFlips[i] = "None";
////                }
////
////                // Mark that we need to upgrade config file to latest version
////                // which we'll do at end of function.
////                needToUpgradeConfigFile = true;
////            }
////            double[] lllats = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LATITUDES);
////            double[] lllons = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LONGITUDES);
////            double[] urlats = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LATITUDES);
////            double[] urlons = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LONGITUDES);
//            String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
//            String[] infofileNames = configMap.getAsArray(CustomPerspectiveImage.INFOFILENAMES);
//
////            int numImages = lllats != null ? lllats.length : (projectionTypes != null ? projectionTypes.length : 0);
//            int numImages = infofileNames.length;
//            for (int i=0; i<numImages; ++i)
//            {
//                System.out.println(
//                        "CustomSpectrumPanel: initializeSpectraList: spectrum name " + i + " " + spectrumNames[i]);
//                SpectrumInfo spectrumInfo = new SpectrumInfo();
//                spectrumInfo.name = spectrumNames[i];
//                spectrumInfo.spectrumfilename = spectrumFilenames[i];
////                imageInfo.projectionType = ProjectionType.valueOf(projectionTypes[i]);
//                spectrumInfo.spectraType = SpectraType.valueOf(spectraTypes[i]);
////                imageInfo.rotation = imageRotations == null ? 0.0 : Double.valueOf(imageRotations[i]);
////                imageInfo.flip = imageFlips == null ? "None" : imageFlips[i];
//
////                if (projectionTypes == null || ProjectionType.CYLINDRICAL.toString().equals(projectionTypes[i]))
////                {
////                    imageInfo.lllat = lllats[i];
////                    imageInfo.lllon = lllons[i];
////                    imageInfo.urlat = urlats[i];
////                    imageInfo.urlon = urlons[i];
////                }
////                else if (ProjectionType.PERSPECTIVE.toString().equals(projectionTypes[i]))
//                {
////                    if (sumfileNames.length > 0)
//                        spectrumInfo.sumfilename = sumfileNames[i];
////                    if (infofileNames.length > 0)
//                        spectrumInfo.infofilename = infofileNames[i];
//                }
//
//                ((DefaultListModel)spectraList.getModel()).addElement(spectrumInfo);
//            }
//
//            if (needToUpgradeConfigFile)
//                updateConfigFile();
//        }
//
//        initialized = true;
//    }
//
//    private void saveSpectrum(int index, SpectrumInfo oldSpectrumInfo, SpectrumInfo newSpectrumInfo) throws IOException
//    {
//        String uuid = UUID.randomUUID().toString();
//
//        // If newImageInfo.imagefilename is null, that means we are in edit mode
//        // and should continue to use the existing image
//        if (newSpectrumInfo.spectrumfilename == null)
//        {
//            newSpectrumInfo.spectrumfilename = oldSpectrumInfo.spectrumfilename;
//        }
//        else
//        {
////            // Check if this image is any of the supported formats
////            if(VtkENVIReader.isENVIFilename(newImageInfo.spectrumfilename)){
////                // We were given an ENVI file (binary or header)
////                // Can assume at this point that both binary + header files exist in the same directory
////
////                // Get filenames of the binary and header files
////                String enviBinaryFilename = VtkENVIReader.getBinaryFilename(newImageInfo.spectrumfilename);
////                String enviHeaderFilename = VtkENVIReader.getHeaderFilename(newImageInfo.spectrumfilename);
////
////                // Rename newImageInfo as that of the binary file
////                newImageInfo.spectrumfilename = "image-" + uuid;
////
////                // Copy over the binary file
////                Files.copy(new File(enviBinaryFilename),
////                        new File(getCustomDataFolder() + File.separator
////                                + newImageInfo.spectrumfilename));
////
////                // Copy over the header file
////                Files.copy(new File(enviHeaderFilename),
////                        new File(getCustomDataFolder() + File.separator
////                                + VtkENVIReader.getHeaderFilename(newImageInfo.spectrumfilename)));
////            }
////            else if(newImageInfo.spectrumfilename.endsWith(".fit") || newImageInfo.spectrumfilename.endsWith(".fits") ||
////                    newImageInfo.spectrumfilename.endsWith(".FIT") || newImageInfo.spectrumfilename.endsWith(".FITS"))
//            {
//                // Copy FIT file to cache
//                String newFilename = "spectrum-" + uuid + ".spect";
//                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
//                FileUtil.copyFile(newSpectrumInfo.spectrumfilename,  newFilepath);
//                String newFileInfoname = "spectrum-" + uuid + ".INFO";
//                String newFileInfopath = getCustomDataFolder() + File.separator + newFileInfoname;
//                FileUtil.copyFile(newSpectrumInfo.infofilename,  newFileInfopath);
//                // Change newImageInfo.imagefilename to the new location of the file
//                newSpectrumInfo.spectrumfilename = newFilename;
//                newSpectrumInfo.infofilename = newFileInfoname;
//            }
////            else
////            {
////                // Convert native VTK supported image to PNG and save to cache
////                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
////                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(newImageInfo.spectrumfilename);
////                if (imageReader == null)
////                {
////                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
////                        "The format of the specified file is not supported.",
////                        "Error",
////                        JOptionPane.ERROR_MESSAGE);
////                    return;
////                }
////                imageReader.SetFileName(newImageInfo.spectrumfilename);
////                imageReader.Update();
////
////                vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
////                vtkPNGWriter imageWriter = new vtkPNGWriter();
////                imageWriter.SetInputConnection(imageReaderOutput);
////                // We save out the image using a new name that makes use of a UUID
////                newImageInfo.spectrumfilename = "image-" + uuid + ".png";
////                imageWriter.SetFileName(getCustomDataFolder() + File.separator + newImageInfo.spectrumfilename);
////                //imageWriter.SetFileTypeToBinary();
////                imageWriter.Write();
////            }
//        }
//
////        // Operations specific for perspective projection type
////        if (newImageInfo.projectionType == ProjectionType.PERSPECTIVE)
////        {
////            // If newImageInfo.sumfilename and infofilename are both null, that means we are in edit mode
////            // and should continue to use the existing sumfile
////            if (newImageInfo.sumfilename == null && newImageInfo.infofilename == null)
////            {
////                newImageInfo.sumfilename = oldImageInfo.sumfilename;
////                newImageInfo.infofilename = oldImageInfo.infofilename;
////            }
////            else
////            {
////                if (newImageInfo.sumfilename != null)
////                {
////                    // We save out the sumfile using a new name that makes use of a UUID
////                    String newFilename = "sumfile-" + uuid + ".SUM";
////                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
////                    FileUtil.copyFile(newImageInfo.sumfilename, newFilepath);
////                    // Change newImageInfo.sumfilename to the new location of the file
////                    newImageInfo.sumfilename = newFilename;
////                }
////                else if (newImageInfo.infofilename != null)
////                {
////                    // We save out the infofile using a new name that makes use of a UUID
////                    String newFilename = "infofile-" + uuid + ".INFO";
////                    String newFilepath = getCustomDataFolder() + File.separator + newFilename;
////                    FileUtil.copyFile(newImageInfo.infofilename, newFilepath);
////                    // Change newImageInfo.infofilename to the new location of the file
////                    newImageInfo.infofilename = newFilename;
////                }
////            }
////        }
//
//        DefaultListModel model = (DefaultListModel)spectraList.getModel();
//        if (index >= model.getSize())
//        {
//            model.addElement(newSpectrumInfo);
//        }
//        else
//        {
//            model.set(index, newSpectrumInfo);
//        }
//
//        updateConfigFile();
//    }
//
////    /**
////     * This function unmaps the image from the renderer and maps it again,
////     * if it is currently shown.
////     * @throws IOException
////     * @throws FitsException
////     */
////    private void remapImageToRenderer(int index) throws FitsException, IOException
////    {
////        ImageInfo imageInfo = (ImageInfo)((DefaultListModel)spectraList.getModel()).get(index);
////
////        // Remove the image from the renderer
////        String name = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
////        ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
////        FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
////        ImageType imageType = imageInfo.imageType;
////        ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
////        ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0);
////
////        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
////
////        if (imageCollection.containsImage(imageKey))
////        {
////            Image image = imageCollection.getImage(imageKey);
////            boolean visible = image.isVisible();
////            if (visible)
////                image.setVisible(false);
////            imageCollection.removeImage(imageKey);
////            imageCollection.addImage(imageKey);
////            if (visible)
////                image.setVisible(true);
////        }
////    }
//
//    private void removeAllSpectraFromRenderer()
//    {
//        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(ModelNames.IMAGES);
//        spectrumCollection.removeAllSpectra();
////        (ImageSource.LOCAL_CYLINDRICAL);
////        spectrumCollection.removeSpectra(ImageSource.LOCAL_PERSPECTIVE);
//    }
//
//    private void removeSpectra(int index)
//    {
//        SpectrumInfo spectrumInfo = (SpectrumInfo)((DefaultListModel)spectraList.getModel()).get(index);
//
//        String name = getCustomDataFolder() + File.separator + spectrumInfo.spectrumfilename;
//        new File(name).delete();
//
//        // Remove the image from the renderer
////        ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
//        FileType fileType = spectrumInfo.sumfilename != null && !spectrumInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
//        SpectraType spectraType = spectrumInfo.spectraType;
////        ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(spectrumInfo.rotation, spectrumInfo.flip) : null;
//        SpectrumKey spectrumKey = new SpectrumKey(name, /*ImageSource.LOCAL_PERSPECTIVE,*/ fileType, spectraType, instrument);
//
//        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//        spectrumCollection.removeSpectrum(spectrumKey);
//
////        if (spectrumInfo.projectionType == ProjectionType.PERSPECTIVE)
////        {
//            if (spectrumInfo.sumfilename != null)
//            {
//                name = getCustomDataFolder() + File.separator + spectrumInfo.sumfilename;
//                new File(name).delete();
//            }
//            if (spectrumInfo.infofilename != null)
//            {
//                name = getCustomDataFolder() + File.separator + spectrumInfo.infofilename;
//                new File(name).delete();
//            }
//
////            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getSpectraBoundaryCollectionModelName());
////            boundaries.removeBoundary(spectrumKey);
////        }
//
//        ((DefaultListModel)spectraList.getModel()).remove(index);
//    }
//
//    private void moveDown(int i)
//    {
//        DefaultListModel model = (DefaultListModel)spectraList.getModel();
//
//        if (i >= model.getSize())
//            return;
//
//        Object o = model.get(i);
//
//        model.remove(i);
//        model.add(i+1, o);
//    }
//
//    private void updateConfigFile()
//    {
//        MapUtil configMap = new MapUtil(getConfigFilename());
//
//        String spectrumNames = "";
//        String spectrumFilenames = "";
////        String projectionTypes = "";
//        String spectrumTypes = "";
////        String imageRotations = "";
////        String imageFlips = "";
////        String lllats = "";
////        String lllons = "";
////        String urlats = "";
////        String urlons = "";
//        String sumfilenames = "";
//        String infofilenames = "";
//
//        DefaultListModel imageListModel = (DefaultListModel)spectraList.getModel();
//        for (int i=0; i<imageListModel.size(); ++i)
//        {
//            SpectrumInfo spectrumInfo = (SpectrumInfo)imageListModel.get(i);
//            System.out.println("CustomSpectrumPanel: updateConfigFile: saving name " + spectrumInfo.name);
//            spectrumFilenames += spectrumInfo.spectrumfilename;
//            spectrumNames += spectrumInfo.name;
////            projectionTypes += spectrumInfo.projectionType;
//            spectrumTypes += spectrumInfo.spectraType;
////            imageRotations += Math.floor(spectrumInfo.rotation / 90.0) * 90.0;
////            imageFlips += spectrumInfo.flip;
////            lllats += String.valueOf(spectrumInfo.lllat);
////            lllons += String.valueOf(spectrumInfo.lllon);
////            urlats += String.valueOf(spectrumInfo.urlat);
////            urlons += String.valueOf(spectrumInfo.urlon);
//            sumfilenames += spectrumInfo.sumfilename;
//            infofilenames += spectrumInfo.infofilename;
//
//            if (i < imageListModel.size()-1)
//            {
//                spectrumNames += CustomShapeModel.LIST_SEPARATOR;
//                spectrumFilenames += CustomShapeModel.LIST_SEPARATOR;
////                projectionTypes += CustomShapeModel.LIST_SEPARATOR;
//                spectrumTypes += CustomShapeModel.LIST_SEPARATOR;
////                imageRotations += CustomShapeModel.LIST_SEPARATOR;
////                imageFlips += CustomShapeModel.LIST_SEPARATOR;
////                lllats += CustomShapeModel.LIST_SEPARATOR;
////                lllons += CustomShapeModel.LIST_SEPARATOR;
////                urlats += CustomShapeModel.LIST_SEPARATOR;
////                urlons += CustomShapeModel.LIST_SEPARATOR;
//                sumfilenames += CustomShapeModel.LIST_SEPARATOR;
//                infofilenames += CustomShapeModel.LIST_SEPARATOR;
//            }
//        }
//
//        Map<String, String> newMap = new LinkedHashMap<String, String>();
//
//        newMap.put(Spectrum.SPECTRUM_NAMES, spectrumNames);
//        newMap.put(Spectrum.SPECTRUM_FILENAMES, spectrumFilenames);
////        newMap.put(Image.PROJECTION_TYPES, projectionTypes);
//        newMap.put(Spectrum.SPECTRUM_TYPES, spectrumTypes);
////        newMap.put(Image.IMAGE_ROTATIONS, imageRotations);
////        newMap.put(Image.IMAGE_FLIPS, imageFlips);
////        newMap.put(CylindricalImage.LOWER_LEFT_LATITUDES, lllats);
////        newMap.put(CylindricalImage.LOWER_LEFT_LONGITUDES, lllons);
////        newMap.put(CylindricalImage.UPPER_RIGHT_LATITUDES, urlats);
////        newMap.put(CylindricalImage.UPPER_RIGHT_LONGITUDES, urlons);
//        newMap.put(CustomPerspectiveImage.SUMFILENAMES, sumfilenames);
//        newMap.put(CustomPerspectiveImage.INFOFILENAMES, infofilenames);
//
//        configMap.put(newMap);
//    }
//
//    private void spectraListMaybeShowPopup(MouseEvent e)
//    {
//        if (e.isPopupTrigger())
//        {
//            int index = spectraList.locationToIndex(e.getPoint());
//
//            if (index >= 0 && spectraList.getCellBounds(index, index).contains(e.getPoint()))
//            {
//                // If the item right-clicked on is not selected, then deselect all the
//                // other items and select the item right-clicked on.
//                if (!spectraList.isSelectedIndex(index))
//                {
//                    spectraList.clearSelection();
//                    spectraList.setSelectedIndex(index);
//                }
//
//                int[] selectedIndices = spectraList.getSelectedIndices();
//                List<SpectrumKeyInterface> spectrumKeys = new ArrayList<SpectrumKeyInterface>();
//                for (int selectedIndex : selectedIndices)
//                {
//                    SpectrumInfo spectraInfo = (SpectrumInfo)((DefaultListModel)spectraList.getModel()).get(selectedIndex);
//                    String name = getCustomDataFolder() + File.separator + spectraInfo.spectrumfilename;
//                    spectrumPopupMenu.setCurrentSpectrum(name);
//
////                    ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
//                    FileType fileType = spectraInfo.sumfilename != null && !spectraInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
//                    SpectraType spectrumType = spectraInfo.spectraType;
//                    System.out.println(
//                            "CustomSpectrumPanel: spectraListMaybeShowPopup: display name " + spectrumType.getDisplayName());
//                    ISpectralInstrument instrument = SpectrumInstrumentFactory.getInstrumentForName(spectrumType.getDisplayName());
////                    ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
//                    System.out.println(
//                            "CustomSpectrumPanel: spectraListMaybeShowPopup: instrument is " + instrument);
//                    spectrumPopupMenu.setInstrument(instrument);
//
//                    SpectrumKey spectrumKey = new SpectrumKey(name, fileType, spectrumType, instrument);
//                    spectrumKeys.add(spectrumKey);
//                }
//                //TODO FIX THIS
//
//                spectrumPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//            }
//        }
//    }
//
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
//        {
//            PickEvent e = (PickEvent)evt.getNewValue();
//            Model model = modelManager.getModel(e.getPickedProp());
//            if (model instanceof SpectraCollection)// || model instanceof PerspectiveImageBoundaryCollection)
//            {
//                // Get the actual filename of the selected image
//                SpectrumKey key = ((SpectraCollection)model).getSpectrum((vtkActor)e.getPickedProp()).getKey();
//                String name = new File(key.name).getName();
//
//                int idx = -1;
//                int size = spectraList.getModel().getSize();
//                for (int i=0; i<size; ++i)
//                {
//                    // We want to compare the actual image filename here, not the displayed name which may not be unique
//                    SpectrumInfo spectrumInfo = (SpectrumInfo)((DefaultListModel)spectraList.getModel()).get(i);
//                    String spectrumFilename = spectrumInfo.spectrumfilename;
//                    if (name.equals(spectrumFilename))
//                    {
//                        idx = i;
//                        break;
//                    }
//                }
//
//                if (idx >= 0)
//                {
//                    spectraList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = spectraList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        spectraList.scrollRectToVisible(cellBounds);
//                }
//            }
//        }
//        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
//        {
//            // If an image was added/removed, then
//            SpectraCollection spectra = (SpectraCollection)getModelManager().getModel(getSpectraCollectionModelName());
//            int currSpectraInCollection = spectra.getSelectedSpectra().size();
//
//            if(currSpectraInCollection != numSpectraInCollection)
//            {
//                // Update count of number of images in collection and update slider
//                numSpectraInCollection = currSpectraInCollection;
//                valueChanged(null);
//            }
//        }
//    }
//
//    /** This method is called from within the constructor to
//     * initialize the form.
//     * WARNING: Do NOT modify this code. The content of this method is
//     * always regenerated by the Form Editor.
//     */
//    @SuppressWarnings("unchecked")
//    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
//    private void initComponents() {
//        java.awt.GridBagConstraints gridBagConstraints;
//
//        jScrollPane1 = new javax.swing.JScrollPane();
//        spectraList = new javax.swing.JList();
//        newButton = new javax.swing.JButton();
//        editButton = new javax.swing.JButton();
//        jLabel1 = new javax.swing.JLabel();
//        moveUpButton = new javax.swing.JButton();
//        moveDownButton = new javax.swing.JButton();
//        jPanel1 = new javax.swing.JPanel();
//        deleteButton = new javax.swing.JButton();
//        removeAllButton = new javax.swing.JButton();
//        monochromePanel = new javax.swing.JPanel();
//
//        setLayout(new java.awt.GridBagLayout());
//
//        spectraList.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                spectraListMousePressed(evt);
//            }
//            public void mouseReleased(java.awt.event.MouseEvent evt) {
//                spectraListMouseReleased(evt);
//            }
//        });
//        spectraList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
//            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
//                imageListValueChanged(evt);
//            }
//        });
//        jScrollPane1.setViewportView(spectraList);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.gridwidth = 6;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.ipadx = 377;
//        gridBagConstraints.ipady = 241;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        add(jScrollPane1, gridBagConstraints);
//
//        newButton.setText("New...");
//        newButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                newButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
//        add(newButton, gridBagConstraints);
//
//        editButton.setText("Edit...");
//        editButton.setEnabled(false);
//        editButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                editButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
//        add(editButton, gridBagConstraints);
//
//        jLabel1.setText("Images");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.gridwidth = 5;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
//        add(jLabel1, gridBagConstraints);
//
//        moveUpButton.setText("Move Up");
//        moveUpButton.setEnabled(false);
//        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                moveUpButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
//        add(moveUpButton, gridBagConstraints);
//
//        moveDownButton.setText("Move Down");
//        moveDownButton.setEnabled(false);
//        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                moveDownButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 3;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
//        add(moveDownButton, gridBagConstraints);
//
//        jPanel1.setLayout(new java.awt.GridBagLayout());
//
//        deleteButton.setText("Delete from List");
//        deleteButton.setEnabled(false);
//        deleteButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                deleteButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
//        jPanel1.add(deleteButton, gridBagConstraints);
//
//        removeAllButton.setText("Remove All From View");
//        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                removeAllButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.gridwidth = 4;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
//        jPanel1.add(removeAllButton, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 4;
//        gridBagConstraints.gridwidth = 6;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(jPanel1, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.gridwidth = 6;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        add(monochromePanel, gridBagConstraints);
//    }// </editor-fold>//GEN-END:initComponents
//
//    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
//        SpectrumInfo imageInfo = new SpectrumInfo();
//        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, false, instrument);
//        dialog.setSpectrumInfo(imageInfo, modelManager.getPolyhedralModel().isEllipsoid());
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
//
//        // If user clicks okay add to list
//        if (dialog.getOkayPressed())
//        {
//            imageInfo = dialog.getSpectrumInfo();
//
////            System.out.println("Image Type: " + imageInfo.imageType);
////            System.out.println("Image Rotate: " + imageInfo.rotation);
////            System.out.println("Image Flip: " + imageInfo.flip);
//            System.out.println(
//                    "CustomSpectrumPanel: newButtonActionPerformed: info spectra type " + imageInfo.spectraType);
//            PolyhedralModel body = modelManager.getPolyhedralModel();
//
//            try
//            {
//                saveSpectrum(((DefaultListModel)spectraList.getModel()).getSize(), null, imageInfo);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }//GEN-LAST:event_newButtonActionPerformed
//
//    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
//        int[] selectedIndices = spectraList.getSelectedIndices();
//        Arrays.sort(selectedIndices);
//        for (int i=selectedIndices.length-1; i>=0; --i)
//        {
//            removeSpectra(selectedIndices[i]);
//        }
//
//        updateConfigFile();
//    }//GEN-LAST:event_deleteButtonActionPerformed
//
//    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
//        int selectedItem = spectraList.getSelectedIndex();
//        if (selectedItem >= 0)
//        {
//            SpectrumInfo oldImageInfo = (SpectrumInfo)((DefaultListModel)spectraList.getModel()).get(selectedItem);
//
//            CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, true, instrument);
//            dialog.setSpectrumInfo(oldImageInfo, modelManager.getPolyhedralModel().isEllipsoid());
//            dialog.setLocationRelativeTo(this);
//            dialog.setVisible(true);
//
//            // If user clicks okay replace item in list
//            if (dialog.getOkayPressed())
//            {
//                SpectrumInfo newImageInfo = dialog.getSpectrumInfo();
//                try
//                {
//                    saveSpectrum(selectedItem, oldImageInfo, newImageInfo);
////                    remapImageToRenderer(selectedItem);
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
////                catch (FitsException e)
////                {
////                    e.printStackTrace();
////                }
//            }
//        }
//    }//GEN-LAST:event_editButtonActionPerformed
//
//    private void spectraListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMousePressed
//        spectraListMaybeShowPopup(evt);
//    }//GEN-LAST:event_imageListMousePressed
//
//    private void spectraListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMouseReleased
//        spectraListMaybeShowPopup(evt);
//    }//GEN-LAST:event_imageListMouseReleased
//
//    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
//        removeAllSpectraFromRenderer();
//    }//GEN-LAST:event_removeAllButtonActionPerformed
//
//    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
//        int minSelectedItem = spectraList.getMinSelectionIndex();
//        if (minSelectedItem > 0)
//        {
//            int[] selectedIndices = spectraList.getSelectedIndices();
//            Arrays.sort(selectedIndices);
//            for (int i=0; i<selectedIndices.length; ++i)
//            {
//                --selectedIndices[i];
//                moveDown(selectedIndices[i]);
//            }
//
//            spectraList.clearSelection();
//            spectraList.setSelectedIndices(selectedIndices);
//            spectraList.scrollRectToVisible(spectraList.getCellBounds(minSelectedItem-1, minSelectedItem-1));
//
//            updateConfigFile();
//        }
//    }//GEN-LAST:event_moveUpButtonActionPerformed
//
//    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
//        int maxSelectedItem = spectraList.getMaxSelectionIndex();
//        if (maxSelectedItem >= 0 && maxSelectedItem < spectraList.getModel().getSize()-1)
//        {
//            int[] selectedIndices = spectraList.getSelectedIndices();
//            Arrays.sort(selectedIndices);
//            for (int i=selectedIndices.length-1; i>=0; --i)
//            {
//                moveDown(selectedIndices[i]);
//                ++selectedIndices[i];
//            }
//
//            spectraList.clearSelection();
//            spectraList.setSelectedIndices(selectedIndices);
//            spectraList.scrollRectToVisible(spectraList.getCellBounds(maxSelectedItem+1, maxSelectedItem+1));
//
//            updateConfigFile();
//        }
//    }//GEN-LAST:event_moveDownButtonActionPerformed
//
//    private void imageListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_imageListValueChanged
//        int[] indices = spectraList.getSelectedIndices();
//        if (indices == null || indices.length == 0)
//        {
//            editButton.setEnabled(false);
//            moveUpButton.setEnabled(false);
//            moveDownButton.setEnabled(false);
//            deleteButton.setEnabled(false);
//        }
//        else
//        {
//            editButton.setEnabled(indices.length == 1);
//            deleteButton.setEnabled(true);
//            int minSelectedItem = spectraList.getMinSelectionIndex();
//            int maxSelectedItem = spectraList.getMaxSelectionIndex();
//            moveUpButton.setEnabled(minSelectedItem > 0);
//            moveDownButton.setEnabled(maxSelectedItem < spectraList.getModel().getSize()-1);
//        }
//    }//GEN-LAST:event_imageListValueChanged
//
//    @Override
//    public void stateChanged(ChangeEvent e)
//    {
////        // Custom image slider moved
////        int index = spectraList.getSelectedIndex();
////        Object selectedValue = spectraList.getSelectedValue();
////        if (selectedValue == null)
////            return;
////
////        // Get the actual filename of the selected image
////        String imagename = ((ImageInfo)selectedValue).imagefilename;
////
////        JSlider source = (JSlider)e.getSource();
////        currentSlice = (int)source.getValue();
////        bandValue.setText(Integer.toString(currentSlice));
////
////        SpectraCollection images = (SpectraCollection)getModelManager().getModel(getSpectraCollectionModelName());
////        Set<Image> imageSet = images.getImages();
////        for (Image i : imageSet)
////        {
////            if (i instanceof PerspectiveImage)
////            {
////                // We want to compare the actual image filename here, not the displayed name which may not be unique
////                PerspectiveImage image = (PerspectiveImage)i;
////                ImageKey key = image.getKey();
////                String name = new File(key.name).getName();
////
////                if (name.equals(imagename))
////                {
////                    image.setCurrentSlice(currentSlice);
////                    image.setDisplayedImageRange(null);
////                    if (!source.getValueIsAdjusting())
////                    {
////                         image.loadFootprint();
////                         image.firePropertyChange();
////                    }
////                    return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since differeent cubical images can have different numbers of bands.
////                }
////            }
////        }
//
////            System.out.println("State changed: " + fps);
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e)
//    {
//        // Custom image list selected row changed
//        int index = spectraList.getSelectedIndex();
//        Object selectedValue = spectraList.getSelectedValue();
//        if (selectedValue == null)
//            return;
//
//        // Get the actual filename of the selected image
//        String imagename = ((SpectrumInfo)selectedValue).spectrumfilename;
//
////        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
////        Set<Image> imageSet = images.getImages();
////        for (Image i : imageSet)
////        {
////            if (i instanceof PerspectiveImage)
////            {
////                // We want to compare the actual image filename here, not the displayed name which may not be unique
////                PerspectiveImage image = (PerspectiveImage)i;
////                ImageKey key = image.getKey();
////                String name = new File(key.name).getName();
////                if (name.equals(imagename))
////                {
////                    int depth = image.getImageDepth();
////                    currentSlice = image.getCurrentSlice();
////                    setNumberOfBands(depth,currentSlice);
////                    image.setDisplayedImageRange(null);
////                    return; // twupy1: Only do this for a single image now even if multiple ones are highlighted since different cubical images can have different numbers of bands.
////                }
////            }
////        }
////
////        // if no multi-band image found, set number of bands in slider to 1
////        setNumberOfBands(1);
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent arg0)
//    {
////        String newBandName = (String)((JComboBox)arg0.getSource()).getSelectedItem();
////        System.out.println("ComboBox Value Changed: " + newBandName);
//    }
//
//
//    // Variables declaration - do not modify//GEN-BEGIN:variables
//    private javax.swing.JButton deleteButton;
//    private javax.swing.JButton editButton;
//    private javax.swing.JList spectraList;
//    private javax.swing.JLabel jLabel1;
//    private javax.swing.JPanel jPanel1;
//    private javax.swing.JScrollPane jScrollPane1;
//    private javax.swing.JPanel monochromePanel;
//    private javax.swing.JButton moveDownButton;
//    private javax.swing.JButton moveUpButton;
//    private javax.swing.JButton newButton;
//    private javax.swing.JButton removeAllButton;
//    // End of variables declaration//GEN-END:variables
//}
