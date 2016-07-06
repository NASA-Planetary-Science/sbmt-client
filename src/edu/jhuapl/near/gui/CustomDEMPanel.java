/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.near.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import vtk.vtkActor;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.DEM;
import edu.jhuapl.near.model.DEM.DEMKey;
import edu.jhuapl.near.model.DEMBoundaryCollection;
import edu.jhuapl.near.model.DEMCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.DEMPopupMenu;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.MapUtil;
import edu.jhuapl.near.util.Properties;

public class CustomDEMPanel extends javax.swing.JPanel implements PropertyChangeListener, ActionListener, ChangeListener, ListSelectionListener
{
    private ModelManager modelManager;
    private JToggleButton selectRegionButton;
    private JFormattedTextField nameTextField;
    private JFormattedTextField outputFolderTextField;
    private JCheckBox setSpecifyRegionManuallyCheckbox;
    private JCheckBox grotesqueModelCheckbox;
    private JTextField pixelScaleTextField;
    private JTextField latitudeTextField;
    private JTextField longitudeTextField;
    private JButton mapmakerSubmitButton;
    private JButton bigmapSubmitButton;
    private JButton loadButton;
    private PickManager pickManager;
    private JSpinner halfSizeSpinner;
    private String mapmakerPath;
    private String bigmapPath;

    private DEMPopupMenu demPopupMenu;
    Renderer renderer;

    private boolean initialized = false;

    // Helper class to keep track of DEM information
    public static class DEMInfo
    {
        public String name = ""; // name to call this image for display purposes
        public String demfilename = ""; // filename of image on disk

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            /*if (projectionType == ProjectionType.CYLINDRICAL)
            {
                return name + ", Cylindrical  ["
                        + df.format(lllat) + ", "
                        + df.format(lllon) + ", "
                        + df.format(urlat) + ", "
                        + df.format(urlon)
                        + "]";
            }
            else
            {
                if (imageType == ImageType.GENERIC_IMAGE)
                    return name + ", Perspective" + ", " + imageType + ", Rotate " + rotation + ", Flip " + flip;
                else
                    return name + ", Perspective" + ", " + imageType;
            }*/
            return name;
        }
    }

    public CustomDEMPanel(final ModelManager modelManager,
            final PickManager pickManager,
            String shapeRootDirOnServer,
            boolean hasMapmaker,
            boolean hasBigmap)
    {
        // Setup member variables
        this.modelManager = modelManager;
        this.pickManager = pickManager;
        this.mapmakerPath = shapeRootDirOnServer + "/mapmaker.zip";
        this.bigmapPath = shapeRootDirOnServer + "/bigmap.zip";
        this.selectRegionButton = null;

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        // Initialize GUI components, panels, etc.
        initComponents();
        initExtraComponents(hasMapmaker, hasBigmap);

        // Get collections
        DEMCollection dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        DEMBoundaryCollection boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);

        // Construct popup menu (right click action)
        demPopupMenu = new DEMPopupMenu(modelManager.getSmallBodyModel(), dems, boundaries, renderer, this);

        // ???
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                try
                {
                    initializeDEMList();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }

            public void componentHidden(ComponentEvent e)
            {
                if(selectRegionButton != null)
                {
                    selectRegionButton.setSelected(false);
                }
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        imageList.addListSelectionListener(this);
    }

    // Additional initialization not included in auto-generation
    protected void initExtraComponents(boolean hasMapmaker, boolean hasBigmap)
    {
        // Customization of list
        imageList.setModel(new DefaultListModel());

        // Add Mapmaker/Bigmap section if applicable
        if(hasMapmaker || hasBigmap)
        {
            populateMonochromePanel(monochromePanel, hasMapmaker, hasBigmap);
        }
    }

    // Adds Mapmaker/Bigmap panel
    protected void populateMonochromePanel(JPanel pane, boolean hasMapmaker, boolean hasBigmap)
    {
        // Set layout
        pane.setLayout(new MigLayout("wrap 1"));

        JPanel selectRegionPanel = new JPanel();
        selectRegionButton = new JToggleButton("Select Region");
        selectRegionButton.setEnabled(true);
        selectRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (selectRegionButton.isSelected())
                    pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
                else
                    pickManager.setPickMode(PickMode.DEFAULT);
            }
        });
        selectRegionPanel.add(selectRegionButton);

        final JButton clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
            }
        });
        selectRegionPanel.add(clearRegionButton);

        final JLabel nameLabel = new JLabel("Name");
        nameTextField = new JFormattedTextField();
        nameTextField.setPreferredSize(new Dimension(200, 24));
        nameTextField.setText("map");

        final JLabel halfSizeLabel = new JLabel("Half Size (pixels)");
        halfSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 1, 512, 1));
        halfSizeSpinner.setPreferredSize(new Dimension(75, 23));

        setSpecifyRegionManuallyCheckbox = new JCheckBox("Enter Manual Region:");
        setSpecifyRegionManuallyCheckbox.setSelected(false);

        if(hasBigmap)
        {
            grotesqueModelCheckbox = new JCheckBox("Grotesque Model");
            grotesqueModelCheckbox.setSelected(true);
        }

        final JLabel pixelScaleLabel = new JLabel("Pixel Scale (meters)");
        pixelScaleLabel.setEnabled(false);
        pixelScaleTextField = new JTextField();
        pixelScaleTextField.setPreferredSize(new Dimension(200, 24));
        pixelScaleTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(pixelScaleTextField, Double.MIN_VALUE, Double.MAX_VALUE));
        pixelScaleTextField.setEnabled(false);

        final JLabel latitudeLabel = new JLabel("Latitude (deg)");
        latitudeLabel.setEnabled(false);
        latitudeTextField = new JTextField();
        latitudeTextField.setPreferredSize(new Dimension(200, 24));
        latitudeTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(latitudeTextField, -90.0, 90.0));
        latitudeTextField.setEnabled(false);

        final JLabel longitudeLabel = new JLabel("Longitude (deg)");
        longitudeLabel.setEnabled(false);
        longitudeTextField = new JTextField();
        longitudeTextField.setPreferredSize(new Dimension(200, 24));
        longitudeTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(longitudeTextField, -360.0, 360.0));
        longitudeTextField.setEnabled(false);

        setSpecifyRegionManuallyCheckbox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                boolean isSelected = setSpecifyRegionManuallyCheckbox.isSelected();
                pixelScaleLabel.setEnabled(isSelected);
                pixelScaleTextField.setEnabled(isSelected);
                latitudeLabel.setEnabled(isSelected);
                latitudeTextField.setEnabled(isSelected);
                longitudeLabel.setEnabled(isSelected);
                longitudeTextField.setEnabled(isSelected);
                selectRegionButton.setEnabled(!isSelected);
                clearRegionButton.setEnabled(!isSelected);
                if (isSelected)
                {
                    selectRegionButton.setSelected(false);
                    pickManager.setPickMode(PickMode.DEFAULT);
                }
            }
        });

        final JPanel submitPanel = new JPanel();
        if(hasMapmaker)
        {
            mapmakerSubmitButton = new JButton("Run Mapmaker");
            mapmakerSubmitButton.setEnabled(true);
            mapmakerSubmitButton.addActionListener(this);
            submitPanel.add(mapmakerSubmitButton);
        }
        if(hasBigmap)
        {
            bigmapSubmitButton = new JButton("Run Bigmap");
            bigmapSubmitButton.setEnabled(true);
            bigmapSubmitButton.addActionListener(this);
            submitPanel.add(bigmapSubmitButton);
        }
        pane.add(submitPanel, "align center");

        pane.add(selectRegionPanel, "align center");
        if(hasBigmap)
        {
            pane.add(grotesqueModelCheckbox);
        }
        pane.add(setSpecifyRegionManuallyCheckbox, "wrap");
        pane.add(latitudeLabel, ", gapleft 25, split 2");
        pane.add(latitudeTextField, "width 200!, gapleft push, wrap");
        pane.add(longitudeLabel, ", gapleft 25, split 2");
        pane.add(longitudeTextField, "width 200!, gapleft push, wrap");
        pane.add(pixelScaleLabel, ", gapleft 25, split 2");
        pane.add(pixelScaleTextField, "width 200!, gapleft push, wrap");
        pane.add(nameLabel, "split 2");
        pane.add(nameTextField);
        pane.add(halfSizeLabel, "split 2");
        pane.add(halfSizeSpinner);

        pane.add(submitPanel, "align center");
    }

    private String getCustomDataFolder()
    {
        return modelManager.getSmallBodyModel().getCustomDataFolder();
    }

    private String getDEMConfigFilename()
    {
        return modelManager.getSmallBodyModel().getDEMConfigFilename();
    }

    // Initializes the DEM list from config
    private void initializeDEMList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getDEMConfigFilename());

        if (configMap.containsKey(DEM.DEM_NAMES))
        {
            boolean needToUpgradeConfigFile = false;
            String[] demNames = configMap.getAsArray(DEM.DEM_NAMES);
            String[] demFilenames = configMap.getAsArray(DEM.DEM_FILENAMES);
            if (demFilenames == null)
            {
                // for backwards compatibility
                demFilenames = configMap.getAsArray(DEM.DEM_MAP_PATHS);
                demNames = new String[demFilenames.length];
                for (int i=0; i<demFilenames.length; ++i)
                {
                    demNames[i] = new File(demFilenames[i]).getName();
                    demFilenames[i] = "dem" + i + ".FIT";
                }

                // Mark that we need to upgrade config file to latest version
                // which we'll do at end of function.
                needToUpgradeConfigFile = true;
            }

            int numDems = demNames != null ? demNames.length : 0;
            for (int i=0; i<numDems; ++i)
            {
                DEMInfo demInfo = new DEMInfo();
                demInfo.name = demNames[i];
                demInfo.demfilename = demFilenames[i];

                ((DefaultListModel)imageList.getModel()).addElement(demInfo);
            }

            if (needToUpgradeConfigFile)
            {
                updateConfigFile();
            }
        }

        initialized = true;
    }

    // Removes all DEMs
    private void removeAllDEMsFromRenderer()
    {
        System.err.println("Not yet implemented");
    }

    // Removes a DEM
    private void removeDEM(int index)
    {
        // Get the DEM info
        DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(index);

        // Remove from cache
        String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
        new File(name).delete();

        // Remove the DEM from the renderer
        DEMCollection demCollection = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        DEMKey demKey = new DEMKey(name);
        demCollection.removeDEM(demKey);

        // Remove from the list
        ((DefaultListModel)imageList.getModel()).remove(index);
    }

    // Moves an item in list down
    private void moveDown(int i)
    {
        DefaultListModel model = (DefaultListModel)imageList.getModel();

        if (i >= model.getSize())
            return;

        Object o = model.get(i);

        model.remove(i);
        model.add(i+1, o);
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getDEMConfigFilename());

        String demNames = "";
        String demFilenames = "";

        DefaultListModel demListModel = (DefaultListModel)imageList.getModel();
        for (int i=0; i<demListModel.size(); ++i)
        {
            DEMInfo demInfo = (DEMInfo)demListModel.get(i);

            demFilenames += demInfo.demfilename;
            demNames += demInfo.name;

            if (i < demListModel.size()-1)
            {
                demNames += CustomShapeModel.LIST_SEPARATOR;
                demFilenames += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(DEM.DEM_NAMES, demNames);
        newMap.put(DEM.DEM_FILENAMES, demFilenames);

        configMap.put(newMap);
    }

    private void saveDEM(DEMInfo demInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        if(demInfo.demfilename.endsWith(".fit") || demInfo.demfilename.endsWith(".fits") ||
                demInfo.demfilename.endsWith(".FIT") || demInfo.demfilename.endsWith(".FITS"))
        {
            // Copy FIT file to cache
            String newFilename = "dem-" + uuid + ".fit";
            String newFilepath = getCustomDataFolder() + File.separator + newFilename;
            FileUtil.copyFile(demInfo.demfilename,  newFilepath);
            // Change demInfo.demfilename to the new location of the file
            demInfo.demfilename = newFilename;

            DefaultListModel model = (DefaultListModel)imageList.getModel();
            model.addElement(demInfo);

            updateConfigFile();
        }
        else
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "DEM file does not have valid FIT extension.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Popup menu for when using right clicks
    private void imageListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = imageList.locationToIndex(e.getPoint());

            if (index >= 0 && imageList.getCellBounds(index, index).contains(e.getPoint()))
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!imageList.isSelectedIndex(index))
                {
                    imageList.clearSelection();
                    imageList.setSelectedIndex(index);
                }

                int[] selectedIndices = imageList.getSelectedIndices();
                ArrayList<DEMKey> demKeys = new ArrayList<DEMKey>();
                for (int selectedIndex : selectedIndices)
                {
                    DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(selectedIndex);
                    String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
                    DEMKey demKey = new DEMKey(name);
                    demKeys.add(demKey);
                }
                demPopupMenu.setCurrentDEMs(demKeys);
                demPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof DEMCollection)
            {
                String name = ((DEMCollection)model).getDEM((vtkActor)e.getPickedProp()).getKey().name;

                int idx = -1;
                int size = imageList.getModel().getSize();
                for (int i=0; i<size; ++i)
                {
                    DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(i);
                    String demFilename = getCustomDataFolder() + File.separator + demInfo.demfilename;
                    if (name.equals(demFilename))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    imageList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageList.scrollRectToVisible(cellBounds);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        monochromePanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        imageList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                imageListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                imageListMouseReleased(evt);
            }
        });
        imageList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                imageListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(imageList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 377;
        gridBagConstraints.ipady = 241;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        newButton.setText("Load...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        add(newButton, gridBagConstraints);

        jLabel1.setText("DEMs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        add(jLabel1, gridBagConstraints);

        moveUpButton.setText("Move Up");
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        add(moveUpButton, gridBagConstraints);

        moveDownButton.setText("Move Down");
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        add(moveDownButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        deleteButton.setText("Delete from List");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel1.add(deleteButton, gridBagConstraints);

        removeAllButton.setText("Remove All From View");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel1.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(monochromePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        // User specifies a DEM
        File file = CustomFileChooser.showOpenDialog(this, "Load DEM", "fit");

        // Check if the file provided is valid
        if (file == null || !file.exists())
        {
            // Not valid, return and do nothing
            return;
        }
        else
        {
            // Valid, load it in
            DEMInfo demInfo = new DEMInfo();
            demInfo.demfilename = file.getAbsolutePath();
            demInfo.name = file.getName();

            // Save it to the list of DEMs
            try
            {
                saveDEM(demInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        /*
        ImageInfo imageInfo = new ImageInfo();
        CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, instrument);
        dialog.setImageInfo(imageInfo, modelManager.getSmallBodyModel().isEllipsoid());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            imageInfo = dialog.getImageInfo();

            System.out.println("Image Type: " + imageInfo.imageType);
            System.out.println("Image Rotate: " + imageInfo.rotation);
            System.out.println("Image Flip: " + imageInfo.flip);
            SmallBodyModel body = modelManager.getSmallBodyModel();

            try
            {
                saveImage(((DefaultListModel)imageList.getModel()).getSize(), null, imageInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        */
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selectedIndices = imageList.getSelectedIndices();
        Arrays.sort(selectedIndices);
        for (int i=selectedIndices.length-1; i>=0; --i)
        {
            removeDEM(selectedIndices[i]);
        }

        updateConfigFile();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void imageListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMousePressed
        imageListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageListMousePressed

    private void imageListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMouseReleased
        imageListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageListMouseReleased

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        removeAllDEMsFromRenderer();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int minSelectedItem = imageList.getMinSelectionIndex();
        if (minSelectedItem > 0)
        {
            int[] selectedIndices = imageList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=0; i<selectedIndices.length; ++i)
            {
                --selectedIndices[i];
                moveDown(selectedIndices[i]);
            }

            imageList.clearSelection();
            imageList.setSelectedIndices(selectedIndices);
            imageList.scrollRectToVisible(imageList.getCellBounds(minSelectedItem-1, minSelectedItem-1));

            updateConfigFile();
        }
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int maxSelectedItem = imageList.getMaxSelectionIndex();
        if (maxSelectedItem >= 0 && maxSelectedItem < imageList.getModel().getSize()-1)
        {
            int[] selectedIndices = imageList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=selectedIndices.length-1; i>=0; --i)
            {
                moveDown(selectedIndices[i]);
                ++selectedIndices[i];
            }

            imageList.clearSelection();
            imageList.setSelectedIndices(selectedIndices);
            imageList.scrollRectToVisible(imageList.getCellBounds(maxSelectedItem+1, maxSelectedItem+1));

            updateConfigFile();
        }
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void imageListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_imageListValueChanged
        int[] indices = imageList.getSelectedIndices();
        if (indices == null || indices.length == 0)
        {
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else
        {
            deleteButton.setEnabled(true);
            int minSelectedItem = imageList.getMinSelectionIndex();
            int maxSelectedItem = imageList.getMaxSelectionIndex();
            moveUpButton.setEnabled(minSelectedItem > 0);
            moveDownButton.setEnabled(maxSelectedItem < imageList.getModel().getSize()-1);
        }
    }//GEN-LAST:event_imageListValueChanged

    @Override
    public void stateChanged(ChangeEvent e)
    {
        /*ImageCollection images = (ImageCollection)modelManager().getModel(getImageCollectionModelName());

        Set<Image> imageSet = images.getImages();
        for (Image i : imageSet)
        {
            if (i instanceof PerspectiveImage)
            {
                PerspectiveImage image = (PerspectiveImage)i;
                ImageKey key = image.getKey();
                String name = i.getImageName();
                Boolean isVisible = i.isVisible();

                if (image.getImageDepth() > 1)
                {
                    if (image.isVisible())
                    {
                       image.setDisplayedImageRange(null);
                       if (!source.getValueIsAdjusting())
                       {
                            image.loadFootprint();
                            image.firePropertyChange();
                       }
                    }
                }
            }
        }*/
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
       /* int index = imageList.getSelectedIndex();
        Object selectedValue = imageList.getSelectedValue();
        if (selectedValue == null)
            return;

        String imagestring = selectedValue.toString();
        String[]tokens = imagestring.split(",");
        String imagename = tokens[0].trim();

        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());

        Set<Image> imageSet = images.getImages();
        for (Image i : imageSet)
        {
            if (i instanceof PerspectiveImage)
            {
                PerspectiveImage image = (PerspectiveImage)i;
                ImageKey key = image.getKey();
                String name = i.getImageName();
                Boolean isVisible = i.isVisible();
                System.out.println(name + ", " + isVisible);
                if (name.equals(imagename))
                {
                    int depth = image.getImageDepth();
                    if (image.isVisible())
                    {
                       image.setDisplayedImageRange(null);
                       return;
                    }
                }
            }
        }*/

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // We only expect actions from Mapmaker and Bigmap submit buttons
        if(e.getSource() != mapmakerSubmitButton &&
                e.getSource() != bigmapSubmitButton)
        {
            System.err.println("Unrecognized action event source");
            return;
        }

        pickManager.setPickMode(PickMode.DEFAULT);
        selectRegionButton.setSelected(false);

        // First get the center point and radius of the selection circle
        double [] centerPoint = null;
        double radius = 0.0;

        if (!setSpecifyRegionManuallyCheckbox.isSelected())
        {
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

                centerPoint = region.center;
                radius = region.radius;
            }
            else
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "Please select a region on the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        final String demName = this.nameTextField.getText();
        if (demName == null || demName.length() == 0)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Please enter a name.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Start and manage the appropriate swing worker
        if(e.getSource() == mapmakerSubmitButton)
        {
            runMapmakerSwingWorker(demName, centerPoint, radius, new File(getCustomDataFolder()));
        }
        else if(e.getSource() == bigmapSubmitButton)
        {
            runBigmapSwingWorker(demName, centerPoint, radius, new File(getCustomDataFolder()));
        }
    }

    // Starts and manages a MapmakerSwingWorker
    private void runMapmakerSwingWorker(String demName, double[] centerPoint, double radius, File outputFolder)
    {
        // Download the entire map maker suite to the users computer
        // if it has never been downloaded before.
        // Ask the user beforehand if it's okay to continue.
        final MapmakerSwingWorker mapmakerWorker =
                new MapmakerSwingWorker(this, "Running Mapmaker", mapmakerPath);

        // If we need to download, prompt the user that it will take a long time
        if (mapmakerWorker.getIfNeedToDownload())
        {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this),
                    "Before Mapmaker can be run for the first time, a very large file needs to be downloaded.\n" +
                    "This may take several minutes. Would you like to continue?",
                    "Confirm Download",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        if (setSpecifyRegionManuallyCheckbox.isSelected())
        {
            if (latitudeTextField.getText().isEmpty() || longitudeTextField.getText().isEmpty() || pixelScaleTextField.getText().isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "Please enter values for the latitude, longitude, and pixel scale.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            mapmakerWorker.setLatitude(Double.parseDouble(latitudeTextField.getText()));
            mapmakerWorker.setLongitude(Double.parseDouble(longitudeTextField.getText()));
            mapmakerWorker.setPixelScale(Double.parseDouble(pixelScaleTextField.getText()));
            mapmakerWorker.setRegionSpecifiedWithLatLonScale(true);
        }
        else
        {
            mapmakerWorker.setCenterPoint(centerPoint);
            mapmakerWorker.setRadius(radius);
        }
        mapmakerWorker.setName(demName);
        mapmakerWorker.setHalfSize((Integer)halfSizeSpinner.getValue());
        mapmakerWorker.setOutputFolder(outputFolder);

        mapmakerWorker.executeDialog();

        if (mapmakerWorker.isCancelled())
            return;

        DEMInfo newDemInfo = new DEMInfo();
        newDemInfo.name = demName;
        newDemInfo.demfilename = mapmakerWorker.getMapletFile().getAbsolutePath();
        try
        {
            saveDEM(newDemInfo);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    // Starts and manages a BigmapSwingWorker
    private void runBigmapSwingWorker(String demName, double[] centerPoint, double radius, File outputFolder)
    {
        // Download the entire map maker suite to the users computer
        // if it has never been downloaded before.
        // Ask the user beforehand if it's okay to continue.
        final BigmapSwingWorker bigmapWorker =
                new BigmapSwingWorker(this, "Running Bigmap", bigmapPath);

        // If we need to download, promt the user that it will take a long time
        if (bigmapWorker.getIfNeedToDownload())
        {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this),
                    "Before Bigmap can be run for the first time, a very large file needs to be downloaded.\n" +
                    "This may take several minutes. Would you like to continue?",
                    "Confirm Download",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        if (setSpecifyRegionManuallyCheckbox.isSelected())
        {
            if (latitudeTextField.getText().isEmpty() || longitudeTextField.getText().isEmpty() || pixelScaleTextField.getText().isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "Please enter values for the latitude, longitude, and pixel scale.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            bigmapWorker.setLatitude(Double.parseDouble(latitudeTextField.getText()));
            bigmapWorker.setLongitude(Double.parseDouble(longitudeTextField.getText()));
            bigmapWorker.setPixelScale(Double.parseDouble(pixelScaleTextField.getText()));
            bigmapWorker.setRegionSpecifiedWithLatLonScale(true);
        }
        else
        {
            bigmapWorker.setCenterPoint(centerPoint);
            bigmapWorker.setRadius(radius);
        }
        bigmapWorker.setGrotesque(grotesqueModelCheckbox.isSelected());
        bigmapWorker.setName(demName);
        bigmapWorker.setHalfSize((Integer)halfSizeSpinner.getValue());
        bigmapWorker.setOutputFolder(outputFolder);

        bigmapWorker.setSmallBodyModel(modelManager.getSmallBodyModel());

        bigmapWorker.executeDialog();

        if (bigmapWorker.isCancelled())
            return;

        DEMInfo newDemInfo = new DEMInfo();
        newDemInfo.name = demName;
        newDemInfo.demfilename = bigmapWorker.getMapletFile().getAbsolutePath();
        try
        {
            saveDEM(newDemInfo);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JList imageList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel monochromePanel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton removeAllButton;
    // End of variables declaration//GEN-END:variables
}
