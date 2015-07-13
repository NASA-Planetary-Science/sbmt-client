/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageInfoPanel2.java
 *
 * Created on May 30, 2011, 12:24:26 PM
 */
package edu.jhuapl.near.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.table.DefaultTableModel;

import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropCollection;
import vtk.vtkPropPicker;
import vtk.vtkTransform;

import edu.jhuapl.near.gui.joglrendering.vtksbmtJoglCanvasComponent;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.util.IntensityRange;


public class ImageInfoPanel extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener //, KeyListener
{
    public static final double VIEWPOINT_DELTA = 1.0;
    public static final double ROTATION_DELTA = 5.0;

    private vtksbmtJoglCanvasComponent renWin;
    private Image image;
    private ImageCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private vtkImageSlice actor;
    private vtkImageReslice reslice;
    private vtkPropPicker imagePicker;
    private boolean initialized = false;
    private boolean centerFrustumMode = false;
//    private double adjustFactor = 1.0;

//    private class MouseListener extends MouseAdapter
//    {
//        @Override
//        public void mouseClicked(MouseEvent e)
//        {
//            renWin.lock();
//            int pickSucceeded = imagePicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
//            renWin.unlock();
//            if (pickSucceeded == 1)
//            {
//                double[] p = imagePicker.GetPickPosition();
//                // Note we reverse x and y so that the pixel is in the form the camera
//                // position/orientation program expects.
//                System.out.println(p[1] + " " + p[0]);
//                double[][] spectrumRegion = { { p[1], p[2] } };
//                if (this.image instanceof PerspectiveImage)
//                    this.image.set
//            }
//        }
//    }

    /** Creates new form ImageInfoPanel2 */
    public ImageInfoPanel(
            final Image image,
            ImageCollection imageCollection,
            PerspectiveImageBoundaryCollection imageBoundaryCollection)
    {
        initComponents();

        this.image = image;
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;

        renWin = new vtksbmtJoglCanvasComponent();
        renWin.getComponent().setPreferredSize(new Dimension(550, 550));

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = (vtkImageData)image.getTexture().GetInput();

        // Only allow contrast changing for images with exactly 1 channel
//        if (image.getNumberOfComponentsOfOriginalImage() > 1)
//        {
//            slider.setEnabled(false);
//            jLabel1.setEnabled(false);
//        }

        if (image instanceof PerspectiveImage)
            applyAdjustmentsButton.setSelected(((PerspectiveImage)image).getApplyFramedAdjustments());

        int[] masking = image.getCurrentMask();
        leftSpinner.setValue(masking[0]);
        topSpinner.setValue(masking[1]);
        rightSpinner.setValue(masking[2]);
        bottomSpinner.setValue(masking[3]);

        double[] center = displayedImage.GetCenter();
        int[] dims = displayedImage.GetDimensions();

        // Rotate image by 90 degrees so it appears the same way as when you
        // use the Center in Image option.
        vtkTransform imageTransform = new vtkTransform();
        imageTransform.Translate(center[0], center[1], 0.0);
        imageTransform.RotateZ(-90.0);
        imageTransform.Translate(-center[1], -center[0], 0.0);

        reslice = new vtkImageReslice();
        reslice.SetInputData(displayedImage);
        reslice.SetResliceTransform(imageTransform);
        reslice.SetInterpolationModeToNearestNeighbor();
        reslice.SetOutputSpacing(1.0, 1.0, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, dims[1]-1, 0, dims[0]-1, 0, 0);
        reslice.Update();

        vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
        imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
        imageSliceMapper.Update();

        actor = new vtkImageSlice();
        actor.SetMapper(imageSliceMapper);
        actor.GetProperty().SetInterpolationTypeToLinear();

// for testing backplane generation
//        {
//            int band = 10;
//            vtkImageData plane = new vtkImageData();
//            plane.DeepCopy(displayedImage);
//            float[] bp = image.generateBackplanes();
//            double minValue = Double.MAX_VALUE;
//            double maxValue = -Double.MAX_VALUE;
//            int pixelStart = image.index(0, 0, band);
//            int pixelEnd = image.index(0, 0, band+1);
//            for (int i=pixelStart; i<pixelEnd; ++i)
//            {
//                if (bp[i] == Image.PDS_NA) continue;
//                if (bp[i] < minValue) minValue = bp[i];
//                if (bp[i] > maxValue) maxValue = bp[i];
//            }
//            System.out.println("min band " + band + " : " + minValue);
//            System.out.println("max band " + band + " : " + maxValue);
//            for (int i=0; i<image.getImageHeight(); ++i)
//                for (int j=0; j<image.getImageWidth(); ++j)
//                {
//                    double v = bp[image.index(j, i, band)];
//                    if (v == Image.PDS_NA)
//                        v = minValue;
//                    else
//                        v = (v-minValue) * 255.0 / (maxValue - minValue);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 0, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 1, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 2, v);
//                }
//            actor.SetInput(plane);
//        }

        renWin.getRenderer().AddActor(actor);

        renWin.setSize(550, 550);

        imagePicker = new vtkPropPicker();
        imagePicker.PickFromListOn();
        imagePicker.InitializePickList();
        vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        imagePicker.AddPickList(actor);
        renWin.getComponent().addMouseListener(this);
        renWin.getComponent().addMouseMotionListener(this);
//        renWin.addKeyListener(this);

        // Trying to add a vtksbmtJoglCanvasComponent in the netbeans gui
        // does not seem to work so instead add it here.
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(renWin.getComponent(), gridBagConstraints);


        // Add a text box for showing information about the image
        String[] columnNames = {"Property", "Value"};

        LinkedHashMap<String, String> properties = null;
        Object[][] data = { {"", ""} };
        try
        {
            properties = image.getProperties();
            int size = properties.size();
            data = new Object[size][2];

            int i=0;
            for (String key : properties.keySet())
            {
                data[i][0] = key;
                data[i][1] = properties.get(key);

                ++i;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }


        DefaultTableModel model = new DefaultTableModel(data, columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table.setModel(model);

        createMenus();

        // Finally make the frame visible
        String name = new File(image.getImageName()).getName();
        if (image instanceof PerspectiveImage)
        {
            PerspectiveImage pimage = (PerspectiveImage)image;
            int depth = pimage.getImageDepth();
            if (depth > 1)
            {
                String band = pimage.getCurrentBand();
                name = band + ":" + name;
            }
        }
        setTitle("Image " + name + " Properties");

        pack();
        setVisible(true);

        initialized = true;

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                renWin.resetCamera();
                renWin.Render();
            }
        });
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
        {
            public void actionPerformed(ActionEvent e)
            {
                File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...", "image.png", "png");
                Renderer.saveToFile(file, renWin);
            }
        });
        fileMenu.add(mi);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        /**
         * The following is a bit of a hack. We want to reuse the PopupMenu
         * class, but instead of having a right-click popup menu, we want instead to use
         * it as an actual menu in a menu bar. Therefore we simply grab the menu items
         * from that class and put these in our new JMenu.
         */
        ImagePopupMenu imagesPopupMenu =
            new ImagePopupMenu(imageCollection, imageBoundaryCollection, null, null, null, this);

        imagesPopupMenu.setCurrentImage(image.getKey());

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = imagesPopupMenu.getComponents();
        for (Component item : components)
        {
            if (item instanceof JMenuItem)
            {
                // Do not show the "Show Image" option since that creates problems
                // since it's supposed to close this window also.
                if (!(((JMenuItem)item).getAction() instanceof ImagePopupMenu.MapImageAction))
                    menu.add(item);
            }
        }

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    public Model getModel()
    {
        return image;
    }

    public Model getCollectionModel()
    {
        return imageCollection;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (centerFrustumMode && e.getButton() == 1)
        {
            if (e.isAltDown())
            {
                System.out.println("Resetting pointing...");
                ((PerspectiveImage)image).resetSpacecraftState();
            }
            else
            {
                centerFrustumOnPixel(e);

                ((PerspectiveImage)image).loadFootprint();
//                ((PerspectiveImage)image).calculateFrustum();
            }
//            PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(image.getKey());
//            boundary.update();
//            ((PerspectiveImageBoundary)boundary).firePropertyChange();

            ((PerspectiveImage)image).firePropertyChange();
        }
    }



    @Override
    public void mouseEntered(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

        if (!e.isAltDown())
        {
            ((PerspectiveImage)image).calculateFrustum();
            ((PerspectiveImage)image).firePropertyChange();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (centerFrustumMode && e.getButton() == 1)
        {
            if (!e.isAltDown())
            {
                centerFrustumOnPixel(e);
                ((PerspectiveImage)image).loadFootprint();
            }

            ((PerspectiveImage)image).firePropertyChange();
        }
        else
            updateSpectrumRegion(e);
    }

    private void updateSpectrumRegion(MouseEvent e)
    {
        renWin.getVTKLock().lock();
        // Note that on some displays, such as a retina display, the height used by
        // OpenGL is different than the height used by Java. Therefore we need
        // scale the mouse coordinates to get the right position for OpenGL.
        double openGlHeight = renWin.getComponent().getSurfaceHeight();
        double javaHeight = renWin.getComponent().getHeight();
        double scale = openGlHeight / javaHeight;
        int pickSucceeded = imagePicker.Pick(scale*e.getX(), scale*(javaHeight-e.getY()-1), 0.0, renWin.getRenderer());
        renWin.getVTKLock().unlock();
        if (pickSucceeded == 1)
        {
            double[] p = imagePicker.GetPickPosition();
            // Note we reverse x and y so that the pixel is in the form the camera
            // position/orientation program expects.
            System.out.println(p[1] + " " + p[0]);
            double[][] spectrumRegion = { { p[0], p[1] } };
            if (image instanceof PerspectiveImage)
                ((PerspectiveImage)image).setSpectrumRegion(spectrumRegion);
        }
    }

    private void centerFrustumOnPixel(MouseEvent e)
    {
        System.out.println("Center Frustum");

        renWin.getVTKLock().lock();
        // Note that on some displays, such as a retina display, the height used by
        // OpenGL is different than the height used by Java. Therefore we need
        // scale the mouse coordinates to get the right position for OpenGL.
        double openGlHeight = renWin.getComponent().getSurfaceHeight();
        double javaHeight = renWin.getComponent().getHeight();
        double scale = openGlHeight / javaHeight;
        int pickSucceeded = imagePicker.Pick(scale*e.getX(), scale*(javaHeight-e.getY()-1), 0.0, renWin.getRenderer());
        renWin.getVTKLock().unlock();
        if (pickSucceeded == 1)
        {
            double[] pickPosition = imagePicker.GetPickPosition();
            // Note we reverse x and y so that the pixel is in the form the camera
            // position/orientation program expects.
            if (image instanceof PerspectiveImage)
            {
                PerspectiveImage pi = (PerspectiveImage)image;
                pi.setFrustumOffset(pickPosition);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
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

        slider = new com.jidesoft.swing.RangeSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        leftSpinner = new javax.swing.JSpinner();
        bottomSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        topSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        leftButton = new javax.swing.JButton();
        rightButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        rotateLeftButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        zoomInButton = new javax.swing.JButton();
        rotateRightButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        interpolateCheckBox1 = new javax.swing.JCheckBox();
        applyAdjustmentsButton = new javax.swing.JCheckBox();
        resetFrameAdjustmentsButton = new javax.swing.JButton();
        adjustFrameCheckBox3 = new javax.swing.JCheckBox();
        factorTextField = new javax.swing.JTextField();
        factorLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        slider.setMajorTickSpacing(10);
        slider.setMaximum(255);
        slider.setPaintTicks(true);
        slider.setHighValue(255);
        slider.setLowValue(0);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        getContentPane().add(slider, gridBagConstraints);

        jLabel1.setText("Contrast:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel7.setText("Crop Image:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 0);
        getContentPane().add(jLabel7, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(452, 200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 200));

        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        leftSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        leftSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        leftSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                leftSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(leftSpinner, gridBagConstraints);

        bottomSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        bottomSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        bottomSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bottomSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(bottomSpinner, gridBagConstraints);

        jLabel3.setText("Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel3, gridBagConstraints);

        rightSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rightSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        rightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rightSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(rightSpinner, gridBagConstraints);

        topSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        topSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        topSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                topSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(topSpinner, gridBagConstraints);

        jLabel6.setText("Bottom");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel4.setText("Top");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        leftButton.setText("<");
        leftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel2.add(leftButton, gridBagConstraints);

        rightButton.setText(">");
        rightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightButtonActionPerformed(evt);
            }
        });
        jPanel2.add(rightButton, new java.awt.GridBagConstraints());

        upButton.setText("^");
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(upButton, gridBagConstraints);

        downButton.setText("v");
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(downButton, gridBagConstraints);

        rotateLeftButton.setText("\\");
            rotateLeftButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    rotateLeftButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 0;
            jPanel2.add(rotateLeftButton, gridBagConstraints);

            zoomOutButton.setText("-><-");
            zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    zoomOutButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            jPanel2.add(zoomOutButton, gridBagConstraints);

            zoomInButton.setText("<-->");
            zoomInButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    zoomInButtonActionPerformed(evt);
                }
            });
            jPanel2.add(zoomInButton, new java.awt.GridBagConstraints());

            rotateRightButton.setText("/");
            rotateRightButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    rotateRightButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 7;
            gridBagConstraints.gridy = 0;
            jPanel2.add(rotateRightButton, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 2;
            getContentPane().add(jPanel2, gridBagConstraints);

            jPanel3.setLayout(new java.awt.GridBagLayout());

            interpolateCheckBox1.setSelected(true);
            interpolateCheckBox1.setText("Interpolate Pixels");
            interpolateCheckBox1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    interpolateCheckBox1ActionPerformed(evt);
                }
            });
            jPanel3.add(interpolateCheckBox1, new java.awt.GridBagConstraints());

            if (image instanceof PerspectiveImage)
                applyAdjustmentsButton.setSelected(((PerspectiveImage)image).getApplyFramedAdjustments());
            applyAdjustmentsButton.setText("Apply Adjustments");
            applyAdjustmentsButton.setName(""); // NOI18N
            applyAdjustmentsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    applyAdjustmentsButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            jPanel3.add(applyAdjustmentsButton, gridBagConstraints);

            resetFrameAdjustmentsButton.setText("Reset Frame Adjustments");
            resetFrameAdjustmentsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    resetFrameAdjustmentsButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 0;
            jPanel3.add(resetFrameAdjustmentsButton, gridBagConstraints);

            adjustFrameCheckBox3.setText("Select Target Mode");
            adjustFrameCheckBox3.setName(""); // NOI18N
            adjustFrameCheckBox3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    adjustFrameCheckBox3ActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            jPanel3.add(adjustFrameCheckBox3, gridBagConstraints);

            factorTextField.setText("1.0");
            factorTextField.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    factorTextFieldActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            jPanel3.add(factorTextField, gridBagConstraints);

            factorLabel.setText("Factor");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            jPanel3.add(factorLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            getContentPane().add(jPanel3, gridBagConstraints);

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sliderStateChanged
    {//GEN-HEADEREND:event_sliderStateChanged
        if (slider.getValueIsAdjusting())
            return;

        int lowVal = slider.getLowValue();
        int highVal = slider.getHighValue();
        if (image != null)
            image.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
    }//GEN-LAST:event_sliderStateChanged

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void topSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_topSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_topSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void bottomSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bottomSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_bottomSpinnerStateChanged

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        System.out.println("Zoom In");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveZoomFactorBy(-0.01 * getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void leftButtonActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_leftButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { getAdjustFactor(), 0.0 };
            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_leftButtonActionPerformed

    private void rightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { -getAdjustFactor(), 0.0 };
            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rightButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { 0.0, -getAdjustFactor() };
            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { 0.0, getAdjustFactor() };
            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void rotateLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateLeftButtonActionPerformed
        System.out.println("Rotate Left");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveRotationAngleBy(getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rotateLeftButtonActionPerformed

    private void rotateRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateRightButtonActionPerformed
        System.out.println("Rotate Right");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveRotationAngleBy(-getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rotateRightButtonActionPerformed

    private void interpolateCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolateCheckBox1ActionPerformed
        if (image instanceof PerspectiveImage)
        {
            boolean interpolate = interpolateCheckBox1.isSelected();
            ((PerspectiveImage)image).setInterpolate(interpolate);
            if (interpolate)
                actor.GetProperty().SetInterpolationTypeToLinear();
            else
                actor.GetProperty().SetInterpolationTypeToNearest();
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_interpolateCheckBox1ActionPerformed

    private void applyAdjustmentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyAdjustmentsButtonActionPerformed
        System.out.println("Apply Adjustments");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).setApplyFrameAdjustments(applyAdjustmentsButton.isSelected());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_applyAdjustmentsButtonActionPerformed

    private void resetFrameAdjustmentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetFrameAdjustmentsButtonActionPerformed
        System.out.println("Reset Frame Adjustments");
        ((PerspectiveImage)image).resetSpacecraftState();
        ((PerspectiveImage)image).firePropertyChange();
    }//GEN-LAST:event_resetFrameAdjustmentsButtonActionPerformed

    private void adjustFrameCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adjustFrameCheckBox3ActionPerformed
        System.out.println("Adjust frame...");
        centerFrustumMode = adjustFrameCheckBox3.isSelected();
    }//GEN-LAST:event_adjustFrameCheckBox3ActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        System.out.println("Zoom Out");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveZoomFactorBy(0.01 * getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void factorTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_factorTextFieldActionPerformed
    }//GEN-LAST:event_factorTextFieldActionPerformed

    private double getAdjustFactor()
    {
        double result = 1.0;
        try {
            double delta = 1.0 * Double.parseDouble(factorTextField.getText());
            result = delta;
        } catch (Exception e) { }

        return result;
    }

    private void croppingChanged()
    {
        if (!initialized)
            return;

        Integer top = (Integer) leftSpinner.getValue();
        Integer right = (Integer) topSpinner.getValue();
        Integer bottom = (Integer) rightSpinner.getValue();
        Integer left = (Integer) bottomSpinner.getValue();

        int[] masking = {top, right, bottom, left};

        image.setCurrentMask(masking);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox adjustFrameCheckBox3;
    private javax.swing.JCheckBox applyAdjustmentsButton;
    private javax.swing.JSpinner bottomSpinner;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel factorLabel;
    private javax.swing.JTextField factorTextField;
    private javax.swing.JCheckBox interpolateCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton leftButton;
    private javax.swing.JSpinner leftSpinner;
    private javax.swing.JButton resetFrameAdjustmentsButton;
    private javax.swing.JButton rightButton;
    private javax.swing.JSpinner rightSpinner;
    private javax.swing.JButton rotateLeftButton;
    private javax.swing.JButton rotateRightButton;
    private com.jidesoft.swing.RangeSlider slider;
    private javax.swing.JTable table;
    private javax.swing.JSpinner topSpinner;
    private javax.swing.JButton upButton;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

//    @Override
//    public void keyTyped(KeyEvent evt) {
//        if (centerFrustumMode)
//            System.out.println("Key pressed: " + evt.getKeyCode() + ", " + evt.getExtendedKeyCode() + ", " + evt.getKeyChar() + ", " + evt.getKeyLocation());
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//     }
}
