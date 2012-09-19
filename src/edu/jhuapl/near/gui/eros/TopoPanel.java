package edu.jhuapl.near.gui.eros;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.DirectoryChooser;
import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.MapletBoundaryCollection;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;

public class TopoPanel extends JPanel implements ActionListener
{
    private ModelManager modelManager;
    private JToggleButton selectRegionButton;
    private JFormattedTextField nameTextField;
    private JFormattedTextField outputFolderTextField;
    private JButton submitButton;
    private JButton loadButton;
    private PickManager pickManager;
    private JSpinner halfSizeSpinner;

    public TopoPanel(final ModelManager modelManager,
            final PickManager pickManager)
    {
        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        this.modelManager = modelManager;
        this.pickManager = pickManager;

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                selectRegionButton.setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        JPanel pane = new JPanel();
        pane.setLayout(new MigLayout("wrap 1"));

        JPanel selectRegionPanel = new JPanel();
        //selectRegionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
        nameTextField.setPreferredSize(new Dimension(125, 24));
        nameTextField.setText("map");

        JPanel namePanel = new JPanel();
        namePanel.add(nameLabel);
        namePanel.add(nameTextField);

        final JLabel halfSizeLabel = new JLabel("Half Size (pixels)");
        halfSizeSpinner = new JSpinner(new SpinnerNumberModel(513, 1, 513, 1));
        halfSizeSpinner.setMaximumSize(new Dimension(50, 23));
        JPanel halfSizePanel = new JPanel();
        halfSizePanel.add(halfSizeLabel);
        halfSizePanel.add(halfSizeSpinner);


        JPanel outputFolderPanel = new JPanel(new MigLayout("wrap 2"));
        final JButton outputFolderButton = new JButton("Output Folder...");
        outputFolderTextField = new JFormattedTextField();
        outputFolderTextField.setPreferredSize(new Dimension(150, 24));
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
        });
        outputFolderPanel.add(outputFolderButton);
        outputFolderPanel.add(outputFolderTextField);

        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        submitButton = new JButton("Run Mapmaker");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);
        pane.add(submitPanel, "align center");

        submitPanel.add(submitButton);

        final JPanel loadPanel = new JPanel();
        loadButton = new JButton("Load Cube File...");
        loadButton.setEnabled(true);
        loadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                loadCubeFile();
            }
        });
        pane.add(loadPanel, "align center");

        loadPanel.add(loadButton);

        pane.add(selectRegionPanel, "align center");
        pane.add(namePanel);
        pane.add(halfSizePanel);
        pane.add(outputFolderPanel);
        pane.add(submitPanel, "align center");
        pane.add(loadPanel, "align center");

        add(pane);

    }

    public void actionPerformed(ActionEvent e)
    {
        pickManager.setPickMode(PickMode.DEFAULT);
        selectRegionButton.setSelected(false);

        // Run Bob Gaskell's map maker fortran program

        // First get the center point and radius of the selection circle
        double [] centerPoint = null;
        double radius = 0.0;

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
                    "Please select a region on the asteroid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String name = this.nameTextField.getText();
        if (name == null || name.length() == 0)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Please enter a name.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String outputFolderStr = outputFolderTextField.getText();
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
        }

        // Next download the entire map maker suite to the users computer
        // if it has never been downloaded before.
        // Ask the user beforehand if it's okay to continue.
        final MapmakerSwingWorker mapmakerWorker =
            new MapmakerSwingWorker(this, "Running Mapmaker", "/MSI/mapmaker.zip");

        // If we need to download, promt the user that it will take a long time
        if (mapmakerWorker.getIfNeedToDownload())
        {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this),
                    "Before Mapmaker can be run for the first time, a large 700 MB file needs to be downloaded.\n" +
                    "This may take several minutes. Would you like to continue?",
                    "Confirm Download",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        mapmakerWorker.setCenterPoint(centerPoint);
        mapmakerWorker.setName(name);
        mapmakerWorker.setRadius(radius);
        mapmakerWorker.setHalfSize((Integer)halfSizeSpinner.getValue());
        mapmakerWorker.setOutputFolder(outputFolder);

        mapmakerWorker.executeDialog();

        if (mapmakerWorker.isCancelled())
            return;

        try
        {
            new TopoViewer(mapmakerWorker.getCubeFile(), mapmakerWorker.getLabelFile(),
                    (MapletBoundaryCollection) modelManager.getModel(ModelNames.MAPLET_BOUNDARY));
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    private void loadCubeFile()
    {
        File file = CustomFileChooser.showOpenDialog(this, "Load Cube File", "cub");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        File lblFile = new File(filename.substring(0, filename.length()-3) + "lbl");
        try
        {
            new TopoViewer(file, lblFile,
                    (MapletBoundaryCollection) modelManager.getModel(ModelNames.MAPLET_BOUNDARY));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
