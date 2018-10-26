package edu.jhuapl.sbmt.gui.dtm.ui.creation;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import edu.jhuapl.saavtk.gui.FileDownloadSwingWorker;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.saavtk2.task.TaskFinishedEvent;
import edu.jhuapl.saavtk2.task.TaskProgressEvent;
import edu.jhuapl.saavtk2.task.TaskStartedEvent;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.model.dtm.DEMConfigFile;
import edu.jhuapl.sbmt.model.dtm.DEMTable;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class DEMCreationPanel extends JPanel implements ActionListener
{
    protected final DEMCollection dems;
    protected final DEMBoundaryCollection boundaries;
    protected final DEMCreator creationTool;
    protected final AbstractEllipsePolygonModel regionSelectionModel;
    protected final DEMTable table;
    protected final ExecutorService executorService = Executors.newSingleThreadExecutor(); // use single thread pool, so that mapmaker tasks don't overlap in time (which could be dangerous because mapmaker is presumably not thread safe)
    protected final PickManager pickManager;

    protected final JTextField latitudeTextField = new JTextField();
    protected final JTextField longitudeTextField = new JTextField();
    protected final JTextField pixelScaleTextField = new JTextField();
    protected final JLabel latitudeLabel = new JLabel("Latitude (deg)");
    protected final JLabel longitudeLabel = new JLabel("Longitude (deg)");
    protected final JLabel pixelScaleLabel = new JLabel("Pixel Scale (meters)");

    protected final JTextField nameTextField = new JTextField("map0001");
    protected final JLabel lblName = new JLabel("Name");
    protected final JSpinner halfSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 1, 8192, 1));
    protected final JLabel lblHalfSizepixels = new JLabel("Half Size (pixels)");;
    protected final JButton runCreationToolButton;
    protected final JToggleButton selectRegionButton = new JToggleButton("Select Region");;
    protected final JButton clearRegionButton = new JButton("Clear Region");;
    protected final JCheckBox manualRegionCheckbox = new JCheckBox("Enter Manual Region:");
    protected final JLabel progressBarLabel;

    RegionSelectionMode regionSelectionMode;
    DEMConfigFile configFile;

    static enum RegionSelectionMode
    {
        LatLon, Region, Disabled;
    }

    protected void setInteractionMode(RegionSelectionMode mode)
    {
        this.regionSelectionMode = mode;
        if (mode == RegionSelectionMode.Disabled)
        {
            latitudeLabel.setEnabled(false);
            longitudeLabel.setEnabled(false);
            pixelScaleLabel.setEnabled(false);
            latitudeTextField.setEnabled(false);
            longitudeTextField.setEnabled(false);
            pixelScaleTextField.setEnabled(false);
            selectRegionButton.setEnabled(false);
            clearRegionButton.setEnabled(false);
            manualRegionCheckbox.setEnabled(false);
            runCreationToolButton.setEnabled(false);
            nameTextField.setEnabled(false);
            halfSizeSpinner.setEnabled(false);
            lblHalfSizepixels.setEnabled(false);
            lblName.setEnabled(false);
            pickManager.setPickMode(PickMode.DEFAULT);
        }
        else
        {
            boolean latlon = mode == RegionSelectionMode.LatLon;
            latitudeLabel.setEnabled(latlon);
            longitudeLabel.setEnabled(latlon);
            pixelScaleLabel.setEnabled(latlon);
            latitudeTextField.setEnabled(latlon);
            longitudeTextField.setEnabled(latlon);
            pixelScaleTextField.setEnabled(latlon);
            selectRegionButton.setEnabled(!latlon);
            clearRegionButton.setEnabled(!latlon);
            selectRegionButton.setSelected(false);
            nameTextField.setEnabled(true);
            halfSizeSpinner.setEnabled(true);
            lblHalfSizepixels.setEnabled(true);
            lblName.setEnabled(true);
            manualRegionCheckbox.setEnabled(true);
            runCreationToolButton.setEnabled(true);
        }
    }

    public DEMCreationPanel(ModelManager modelManager, PickManager pickManager, DEMCreator creationTool)
    {
        this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
        this.creationTool = creationTool;
        this.regionSelectionModel = (AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        this.pickManager = pickManager;

        table = new DEMTable();
        table.addListener(dems);
        table.addListener(boundaries);
        configFile=new DEMConfigFile(Paths.get(modelManager.getPolyhedralModel().getDEMConfigFilename()), table);
        configFile.load();

        setLayout(new MigLayout("", "[1px][grow]", "[1px][288.00][][134.00,top][][43.00][]"));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(DEMTable.createSwingWrapper(table).getComponent());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 0, 0));
        panel.add(selectRegionButton);
        panel.add(clearRegionButton);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(new MigLayout("", "[69.00px][129.00px,grow,left][240.00,grow][51.00]", "[][][][][][][]"));
        panel_1.add(manualRegionCheckbox, "cell 0 0 2 1,grow");
        panel_1.add(latitudeLabel, "cell 1 1,alignx left,growy");
        panel_1.add(latitudeTextField, "cell 2 1,grow");
        panel_1.add(longitudeLabel, "cell 1 2,alignx left");
        panel_1.add(longitudeTextField, "cell 2 2,growx");
        panel_1.add(pixelScaleLabel, "cell 1 3,alignx left");
        panel_1.add(pixelScaleTextField, "cell 2 3,growx");

        latitudeTextField.setColumns(10);
        longitudeTextField.setColumns(10);
        pixelScaleTextField.setColumns(10);

        add(scrollPane, "cell 1 1,grow");
        add(panel, "cell 1 2,grow");
        add(panel_1, "cell 1 3,growx,aligny top");

        manualRegionCheckbox.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (manualRegionCheckbox.isSelected())
                    setInteractionMode(RegionSelectionMode.LatLon);
                else
                    setInteractionMode(RegionSelectionMode.Region);
            }
        });

        JPanel panel_2 = new JPanel();
        add(panel_2, "flowx,cell 1 4,alignx center,growy");
        panel_2.setLayout(new MigLayout("", "[93.00][]", "[][]"));
        panel_2.add(lblHalfSizepixels, "cell 0 0");
        panel_2.add(halfSizeSpinner, "cell 1 0,growx");
        panel_2.add(lblName, "cell 0 1");
        panel_2.add(nameTextField, "cell 1 1");

        nameTextField.setText("map0001");
        nameTextField.setColumns(10);

        runCreationToolButton = new JButton("Run " + creationTool.getExecutableDisplayName());
        add(runCreationToolButton, "cell 1 5,alignx center");
        runCreationToolButton.addActionListener(this);

        progressBarLabel = new JLabel();
        progressBarLabel.setHorizontalAlignment(SwingConstants.LEFT);
        progressBarLabel.setVisible(false);

        add(progressBarLabel, "flowx,cell 1 6,growx");

        selectRegionButton.addActionListener(this);
        clearRegionButton.addActionListener(this);

//        populateWithDummyData();

        setInteractionMode(RegionSelectionMode.Region);
    }

//    protected void populateWithDummyData()
//    {
//        for (int i = 1; i <= 3; i++)
//        {
//            Path file = Paths.get("/Users/zimmemi1/Desktop/dems/dem" + i + ".obj");
//            table.appendRow(new DEMKey(file.toString(), file.getFileName().toString()));
//        }
//    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == runCreationToolButton)
        {

            // DOWNLOAD CREATION TOOL EXECUTABLE IF REQUIRED
            if (creationTool.needToDownloadExecutable())
            {
                int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(DEMCreationPanel.this), "Before " + creationTool.getExecutableDisplayName() + " can be run for the first time, a very large file needs to be downloaded.\n" + "This may take several minutes. Would you like to continue?", "Confirm Download", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION)
                    return;
                else
                    new FileDownloadSwingWorker(DEMCreationPanel.this, "Download " + creationTool.getExecutableDisplayName(), creationTool.getExecutablePathOnServer().toString()).executeDialog();
            }

            // SET DEM NAME
            final String demName = this.nameTextField.getText();
            if (demName == null || demName.length() == 0)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // SET CREATION REGION AND GENERATE DEM-CREATION TASK
            Task demCreationTask = null;
            if (regionSelectionMode == RegionSelectionMode.Region)
            {
                if (regionSelectionModel.getNumberOfStructures() > 0)
                {
                    final AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon) regionSelectionModel.getStructure(0);

                    demCreationTask = creationTool.getCreationTask(demName, region.center, region.radius, (Integer) halfSizeSpinner.getValue());

                }
                else
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "Please select a region on the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            else if (regionSelectionMode == regionSelectionMode.LatLon)
            {
                if (latitudeTextField.getText().isEmpty() || longitudeTextField.getText().isEmpty() || pixelScaleTextField.getText().isEmpty())
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "Manual region selection is enabled; please make sure that a lat, lon, and pixel scale are specified", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final double latDeg = Double.parseDouble(latitudeTextField.getText());
                final double lonDeg = Double.parseDouble(longitudeTextField.getText());
                final double pixScaleMeters = Double.parseDouble(pixelScaleTextField.getText());
                demCreationTask = creationTool.getCreationTask(demName, latDeg, lonDeg, pixScaleMeters, (Integer) halfSizeSpinner.getValue());
            }

            // RUN THE TASK
            if (demCreationTask != null)
            {
                executorService.submit(demCreationTask);
                demCreationTask.addListener(demCreationProgressListener);
                demCreationTask.addListener(demCreatedEventListener);
            }
            return;
        }
        else if (e.getSource() == selectRegionButton)
        {
            if (selectRegionButton.isSelected())
                pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
            else
                pickManager.setPickMode(PickMode.DEFAULT);

        }
        else if (e.getSource() == clearRegionButton)
        {
            regionSelectionModel.removeAllStructures();
        }
    }

    EventListener demCreationProgressListener = new EventListener()
    {

        int cnt = 1;
        int cntMax = 15;
        int borderThickness = 3;
        Color borderColor=Color.ORANGE;
        boolean erase=false;
        String ch="\u00b7";
        RegionSelectionMode lastSelectionMode;

        @Override
        public void handle(Event event)
        {
            if (event instanceof TaskStartedEvent)
            {
                lastSelectionMode = regionSelectionMode;
                setInteractionMode(RegionSelectionMode.Disabled);
                progressBarLabel.setVisible(true);
            }
            else if (event instanceof TaskProgressEvent)
            {
                cnt++;
                if (cnt > cntMax)
                {
                    cnt = 1;
                    erase=!erase;
                }
                String str = "Creating " + ((Task) event.getSource()).getDisplayName();
                if (!erase)
                {
                    for (int i = 0; i < cntMax; i++)
                        if (i<cnt)
                            str += ch;
                        else
                            str+= " ";
                }
                else
                {
                    for (int i=0; i<cntMax; i++)
                        if (i>cnt)
                            str += ch;
                        else
                            str+= " ";
                }
                progressBarLabel.setText(str);
                progressBarLabel.setBorder(BorderFactory.createLineBorder(borderColor, borderThickness));
            }
            else if (event instanceof TaskFinishedEvent)
            {
                setInteractionMode(lastSelectionMode);
                progressBarLabel.setVisible(false);
            }
        }

    };

    EventListener demCreatedEventListener = new EventListener()
    {

        @Override
        public void handle(Event event)
        {
            if (event instanceof DEMCreatedEvent)
            {
                DEMKey key = (DEMKey) event.getValue();
                try
                {
                    dems.saveDEM(key);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                table.appendRow(key);
            }

        }
    };

}
