package edu.jhuapl.sbmt.gui.dtm;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import edu.jhuapl.saavtk.gui.FileDownloadSwingWorker;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;
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
    protected final ExecutorService executorService=Executors.newSingleThreadExecutor();    // use single thread pool, so that mapmaker tasks don't overlap in time (which could be dangerous because mapmaker is presumably not thread safe)

    protected final JTextField latitudeTextField=new JTextField();
    protected final JTextField longitudeTextField=new JTextField();
    protected final JTextField pixelScaleTextField=new JTextField();
    protected final JTextField nameTextField=new JTextField("map0001");
    protected final JSpinner halfSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 1, 8192, 1));
    protected final JButton runCreationToolButton;
    protected final JButton selectRegionButton = new JButton("Select Region");;
    protected final JButton clearRegionButton = new JButton("Clear Region");;
    protected final JCheckBox manualRegionCheckbox = new JCheckBox(
            "Enter Manual Region:");


    public DEMCreationPanel(ModelManager modelManager, DEMCreator creationTool)
    {
        this.dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        this.creationTool=creationTool;
        this.regionSelectionModel=(AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);

        table = new DEMTable();
        table.addListener(dems);
        table.addListener(boundaries);

        creationTool.addListener(new EventListener()
        {

            @Override
            public void handle(Event event)
            {
      /*          if (event instanceof DEMCreatedEvent)
                {
                    System.out.println("DEM READY");
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
                    Path mapletPath=(Path)event.getValue();
                    DEMKey key=new DEMKey(mapletPath.toString(), FilenameUtils.getBaseName(mapletPath.toString()));
                    table.appendRow(key);
                }*/

            }
        });

        setLayout(new MigLayout("", "[1px][grow]", "[1px][288.00][][grow]"));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, "cell 1 1,grow");

        scrollPane.setViewportView(
                DEMTable.createSwingWrapper(table).getComponent());

        JPanel panel = new JPanel();
        add(panel, "cell 1 2,grow");
        panel.setLayout(new GridLayout(0, 2, 0, 0));

        panel.add(selectRegionButton);
        panel.add(clearRegionButton);

        JPanel panel_1 = new JPanel();
        add(panel_1, "cell 1 3,grow");
        panel_1.setLayout(new MigLayout("", "[69.00px][129.00px,grow,left][240.00,grow][51.00]", "[][][][][][][]"));


        panel_1.add(manualRegionCheckbox, "cell 0 0 2 1,grow");

        JLabel lblNewLabel = new JLabel("Latitude (deg)");
        panel_1.add(lblNewLabel, "cell 1 1,alignx left,growy");

        panel_1.add(latitudeTextField, "cell 2 1,grow");
        latitudeTextField.setColumns(10);

        JLabel lblLongitudedeg = new JLabel("Longitude (deg)");
        panel_1.add(lblLongitudedeg, "cell 1 2,alignx left");

        longitudeTextField.setColumns(10);
        panel_1.add(longitudeTextField, "cell 2 2,growx");

        JLabel lblPixelScaledeg = new JLabel("Pixel Scale (meters)");
        panel_1.add(lblPixelScaledeg, "cell 1 3,alignx left");

        pixelScaleTextField.setColumns(10);
        panel_1.add(pixelScaleTextField, "cell 2 3,growx");

        JLabel lblHalfSizepixels = new JLabel("Half Size (pixels)");
        panel_1.add(lblHalfSizepixels, "cell 1 4");

        panel_1.add(halfSizeSpinner, "cell 2 4,growx");

        JLabel lblName = new JLabel("Name");
        panel_1.add(lblName, "cell 1 5,alignx left");

        nameTextField.setText("map0001");
        nameTextField.setColumns(10);
        panel_1.add(nameTextField, "cell 2 5,growx");

        runCreationToolButton = new JButton("Run "+creationTool.getExecutableDisplayName());
        panel_1.add(runCreationToolButton, "cell 1 6 2 1,alignx center");
        runCreationToolButton.addActionListener(this);


        manualRegionCheckbox.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                latitudeTextField.setEnabled(manualRegionCheckbox.isSelected());
                longitudeTextField.setEnabled(manualRegionCheckbox.isSelected());
                pixelScaleTextField.setEnabled(manualRegionCheckbox.isSelected());
                selectRegionButton.setEnabled(!manualRegionCheckbox.isSelected());
                clearRegionButton.setEnabled(!manualRegionCheckbox.isSelected());
            }
        });
        manualRegionCheckbox.setSelected(false);

        populateWithDummyData();
    }

    protected void populateWithDummyData()
    {
        for (int i = 1; i <= 3; i++)
        {
            Path file = Paths
                    .get("/Users/zimmemi1/Desktop/dems/dem" + i + ".obj");
            table.appendRow(
                    new DEMKey(file.toString(), file.getFileName().toString()));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == runCreationToolButton)
        {

            // DOWNLOAD CREATION TOOL EXECUTABLE IF REQUIRED
            if (creationTool.needToDownloadExecutable())
            {
                int result = JOptionPane.showConfirmDialog(
                        JOptionPane.getFrameForComponent(DEMCreationPanel.this),
                        "Before " + creationTool.getExecutableDisplayName()
                                + " can be run for the first time, a very large file needs to be downloaded.\n"
                                + "This may take several minutes. Would you like to continue?",
                        "Confirm Download", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION)
                    return;
                else
                    new FileDownloadSwingWorker(DEMCreationPanel.this,
                            "Download " + creationTool.getExecutableDisplayName(),
                            creationTool.getExecutablePathOnServer().toString()).executeDialog();
            }

            // SET DEM NAME
            final String demName = this.nameTextField.getText();
            if (demName == null || demName.length() == 0)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "Please enter a name.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            // SET CREATION REGION
            if (!manualRegionCheckbox.isSelected())
            {
                if (regionSelectionModel.getNumberOfStructures() > 0)
                {
                    final AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)regionSelectionModel.getStructure(0);

                    executorService.submit(new Runnable()
                    {
                        public void run()
                        {
                            creationTool.create(demName, region.center,
                                    region.radius,
                                    (Integer) halfSizeSpinner.getValue());
                        }
                    });

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
            else
            {
                if (latitudeTextField.getText().isEmpty() || longitudeTextField.getText().isEmpty() || pixelScaleTextField.getText().isEmpty())
                {
                    JOptionPane.showMessageDialog(
                            JOptionPane.getFrameForComponent(this),
                            "Manual region selection is enabled; please make sure that a lat, lon, and pixel scale are specified",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final double latDeg=Double.parseDouble(latitudeTextField.getText());
                final double lonDeg=Double.parseDouble(longitudeTextField.getText());
                final double pixScaleMeters=Double.parseDouble(pixelScaleTextField.getText());
                executorService.submit(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        creationTool.create(demName, latDeg, lonDeg, pixScaleMeters, (Integer)halfSizeSpinner.getValue());

                    }
                });
            }


        }
    }


}
