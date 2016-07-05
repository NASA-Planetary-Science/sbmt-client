package edu.jhuapl.near.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
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

import net.miginfocom.swing.MigLayout;
import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;

public class DEMPanel extends JPanel implements ActionListener
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

    public DEMPanel(final ModelManager modelManager,
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

        // Overall panel layout
        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        // ???
        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                selectRegionButton.setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        // Only add a DEM generation panel if Bigmap or Mapmaker is enabled
        if(hasMapmaker || hasBigmap)
        {
            addDEMGeneratorPanel(hasMapmaker, hasBigmap);
        }
    }

    // Adds panel for DEM generation, i.e., Mapmaker/Bigmap
    private void addDEMGeneratorPanel(boolean hasMapmaker, boolean hasBigmap)
    {
        JPanel pane = new JPanel();
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

        /*final JButton outputFolderButton = new JButton("Output Folder...");
        outputFolderTextField = new JFormattedTextField();
        outputFolderTextField.setPreferredSize(new Dimension(200, 24));
        outputFolderTextField.setText(System.getProperty("user.home"));
        outputFolderButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File file = DirectoryChooser.showOpenDialog(outputFolderTextField);
                if (file != null)
                {
                    outputFolderTextField.setText(file.getAbsolutePath());
                }
            }
        });*/

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

        /*final JPanel loadPanel = new JPanel();
        loadButton = new JButton("Load FITS Cube File...");
        loadButton.setEnabled(true);
        loadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                loadCubeFile();
            }
        });
        pane.add(loadPanel, "align center");

        loadPanel.add(loadButton);*/

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

        //pane.add(outputFolderButton, "split 2");
        //pane.add(outputFolderTextField);
        pane.add(submitPanel, "align center");
        //pane.add(loadPanel, "align center");

        add(pane);
    }

    // Implements general preprocessing steps shared between Mapmaker and Bigmap
    // and then starts Mapmaker/Bigmap-specific swing workers
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

        /*String outputFolderStr = outputFolderTextField.getText();
        if (outputFolderStr == null || outputFolderStr.isEmpty())
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Please enter an output folder.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        File outputFolder = new File(outputFolderStr);
        if (!outputFolder.exists())
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "The output folder does not exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!outputFolder.canWrite())
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "The output folder is not writable.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        // Start and manage the appropriate swing worker
        if(e.getSource() == mapmakerSubmitButton)
        {
            runMapmakerSwingWorker(demName, centerPoint, radius, new File(""));
        }
        else if(e.getSource() == bigmapSubmitButton)
        {
            runBigmapSwingWorker(demName, centerPoint, radius, new File(""));
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

        try
        {
            new DEMView(mapmakerWorker.getMapletFile(),
                    modelManager.getSmallBodyModel());
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        catch (FitsException e1)
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

        try
        {
            new DEMView(bigmapWorker.getMapletFile(),
                    modelManager.getSmallBodyModel());
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        catch (FitsException e1)
        {
            e1.printStackTrace();
        }
    }

    /*private void loadCubeFile()
    {
        File file = CustomFileChooser.showOpenDialog(this, "Load Maplet", "fit");
        if (file == null)
        {
            return;
        }

        try
        {
            // twupy1
            DEM dem = new DEM(file.getAbsolutePath());
            ((MapletBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY)).addBoundary(dem);

            DEMKey key = new DEMKey(file.getAbsolutePath());
            DEMCollection demCollection = (DEMCollection) modelManager.getModel(ModelNames.DEM);
            demCollection.addDEM(key);

            //new DEMView(file, modelManager.getSmallBodyModel(),
            //        (MapletBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY));

            new DEMView(key, demCollection,
                    modelManager.getSmallBodyModel(),
                    (MapletBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (FitsException e)
        {
            e.printStackTrace();
        }
    }*/
}
