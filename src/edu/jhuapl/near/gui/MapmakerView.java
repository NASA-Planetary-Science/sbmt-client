package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import nom.tam.fits.FitsException;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import vtk.vtkGlobalJavaHash;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.DEMModel;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Line;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.MapletBoundaryCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.MapmakerLinesPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;

public class MapmakerView extends JFrame
{
    private JButton newButton;
    private JToggleButton editButton;
    private JButton deleteAllButton;
    private JButton saveButton;
    private JButton loadButton;
    private LineModel lineModel;
    private PickManager pickManager;
    private MapmakerPlot plot;
    private int currentColorIndex = 0;
    private MapletBoundaryCollection mapletBoundaries;
    private JComboBox coloringTypeComboBox;
    private DEMModel dem;
    private Renderer renderer;
    private JButton scaleColoringButton;

    private static final String Profile = "Profile";
    private static final String StartLatitude = "StartLatitude";
    private static final String StartLongitude = "StartLongitude";
    private static final String StartRadius = "StartRadius";
    private static final String EndLatitude = "EndLatitude";
    private static final String EndLongitude = "EndLongitude";
    private static final String EndRadius = "EndRadius";
    private static final String Color = "Color";


    public MapmakerView(File cubFile, SmallBodyModel parentSmallBodyModel,
            MapletBoundaryCollection mapletBoundaries) throws IOException, FitsException
    {
        this.mapletBoundaries = mapletBoundaries;

        ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
        setIconImage(erosIcon.getImage());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        StatusBar statusBar = new StatusBar();
        add(statusBar, BorderLayout.PAGE_END);

        String filename = cubFile.getAbsolutePath();
        final ModelManager modelManager = new ModelManager();
        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        dem = new DEMModel(filename);
        lineModel = new LineModel(dem, true);
        lineModel.setMaximumVerticesPerLine(2);
        allModels.put(ModelNames.SMALL_BODY, dem);
        allModels.put(ModelNames.LINE_STRUCTURES, lineModel);
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(dem));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(dem));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(dem));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(dem));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(dem));
        modelManager.setModels(allModels);

        renderer = new Renderer(modelManager);

        PopupManager popupManager = new PopupManager(modelManager, null, renderer);
        // The following replaces LinesPopupMenu with MapmakerLinesPopupMenu
        PopupMenu popupMenu = new MapmakerLinesPopupMenu(modelManager, parentSmallBodyModel, renderer);
        popupManager.registerPopup(lineModel, popupMenu);

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(400, 400));

        JPanel rendererPanel = new JPanel(new BorderLayout());
        rendererPanel.add(renderer, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());

        plot = new MapmakerPlot(lineModel, dem);
        plot.getChartPanel().setMinimumSize(new Dimension(100, 100));
        plot.getChartPanel().setPreferredSize(new Dimension(400, 400));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                renderer, plot.getChartPanel());

        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(createButtonsPanel(), BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        mapletBoundaries.addBoundary(dem);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                MapmakerView.this.mapletBoundaries.removeBoundary(dem);
                System.gc();
                vtkGlobalJavaHash.GC();
            }
        });

        createMenus();

        // Finally make the frame visible
        setTitle("Mapmaker View");
        pack();
        setVisible(true);
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(renderer));
        fileMenu.add(mi);

        JMenu saveShapeModelMenu = new JMenu("Export Shape Model to");
        fileMenu.add(saveShapeModelMenu);

        mi = new JMenuItem(new SaveShapeModelAsPLTAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SaveShapeModelAsOBJAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SaveShapeModelAsSTLAction());
        saveShapeModelMenu.add(mi);

        mi = new JMenuItem(new SavePlateDataAction());
        fileMenu.add(mi);

        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createButtonsPanel()
    {
        JPanel panel = new JPanel();

        Object[] coloringOptions = {
                "Color by elevation relative to gravity",
                "Color by elevation relative to normal plane",
                "Color by slope",
                "No coloring"};
        coloringTypeComboBox = new JComboBox(coloringOptions);
        coloringTypeComboBox.setMaximumSize(new Dimension(150, 23));
        coloringTypeComboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    int index = coloringTypeComboBox.getSelectedIndex();
                    if (index == 3)
                    {
                        scaleColoringButton.setEnabled(false);
                        dem.setColoringIndex(-1);
                    }
                    else
                    {
                        scaleColoringButton.setEnabled(true);
                        dem.setColoringIndex(index);
                    }
                }
                catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        panel.add(coloringTypeComboBox);

        scaleColoringButton = new JButton("Rescale Data Range");
        scaleColoringButton.setEnabled(true);
        scaleColoringButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ScaleDataRangeDialog scaleDataDialog = new ScaleDataRangeDialog(dem);
                scaleDataDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(scaleColoringButton));
                scaleDataDialog.setVisible(true);
            }
        });
        panel.add(scaleColoringButton);

        newButton = new JButton("New Profile");
        newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                removeFaultyProfiles();

                lineModel.addNewStructure();

                // Set the color of this new structure
                int idx = lineModel.getNumberOfStructures() - 1;
                lineModel.setStructureColor(idx, getNextColor());

                pickManager.setPickMode(PickMode.LINE_DRAW);
                editButton.setSelected(true);
            }
        });
        panel.add(newButton);

        editButton = new JToggleButton("Edit Profiles");
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                removeFaultyProfiles();

                if (editButton.isSelected())
                    pickManager.setPickMode(PickMode.LINE_DRAW);
                else
                    pickManager.setPickMode(PickMode.DEFAULT);
            }
        });
        panel.add(editButton);

        deleteAllButton = new JButton("Delete All Profiles");
        deleteAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                removeAllProfiles();
            }
        });
        panel.add(deleteAllButton);

        saveButton = new JButton("Save...");
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File file = null;
                try
                {
                    file = CustomFileChooser.showSaveDialog(saveButton, "Save Profiles", "profiles.txt");
                    if (file != null)
                    {
                        saveView(file);
                    }
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(saveButton),
                            "Unable to save file to " + file.getAbsolutePath(),
                            "Error Saving File",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        panel.add(saveButton);

        loadButton = new JButton("Load...");
        loadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File file = null;
                try
                {
                    file = CustomFileChooser.showOpenDialog(loadButton, "Load Profiles");
                    if (file != null)
                    {
                        loadView(file);
                    }
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(loadButton),
                            "Unable to load file " + file.getAbsolutePath(),
                            "Error Loading File",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        panel.add(loadButton);

        return panel;
    }

    private int[] getNextColor()
    {
        int numColors = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length;
        if (currentColorIndex >= numColors)
            currentColorIndex = 0;
        Color c = (Color)DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[currentColorIndex];
        ++currentColorIndex;
        return new int[] {c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
    }

    private void saveView(File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        String eol = System.getProperty("line.separator");

        int numProfiles = lineModel.getNumberOfStructures();
        for (int i=0; i<numProfiles; ++i)
        {
            Line line = (Line)lineModel.getStructure(i);
            if (line.controlPointIds.size() != 2)
                continue;

            LatLon ll0 = line.controlPoints.get(0);
            LatLon ll1 = line.controlPoints.get(1);
            out.write(eol + Profile + "=" + i + eol);
            out.write(StartLatitude + "=" + ll0.lat + eol);
            out.write(StartLongitude + "=" + ll0.lon + eol);
            out.write(StartRadius + "=" + ll0.rad + eol);
            out.write(EndLatitude + "=" + ll1.lat + eol);
            out.write(EndLongitude + "=" + ll1.lon + eol);
            out.write(EndRadius + "=" + ll1.rad + eol);
            out.write(Color + "=" +
                    line.color[0] + " " +
                    line.color[1] + " " +
                    line.color[2] + " " +
                    line.color[3] + eol);
            out.write(plot.getProfileAsString(i));
        }

        out.close();
    }

    private void loadView(File file) throws IOException
    {
        removeAllProfiles();

        InputStream fs = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        LatLon start = new LatLon();
        LatLon end = new LatLon();
        int lineId = 0;

        while ((line = in.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty())
                continue;
            String[] tokens = line.trim().split("=");
            if (tokens.length != 2)
            {
                in.close();
                throw new IOException("Error parsing file");
            }
            //System.out.println(tokens[0]);

            String key = tokens[0].trim();
            String value = tokens[1].trim();

            if (StartLatitude.equals(key))
                start.lat = Double.parseDouble(value);
            else if (StartLongitude.equals(key))
                start.lon = Double.parseDouble(value);
            else if (StartRadius.equals(key))
                start.rad = Double.parseDouble(value);
            else if (EndLatitude.equals(key))
                end.lat = Double.parseDouble(value);
            else if (EndLongitude.equals(key))
                end.lon = Double.parseDouble(value);
            else if (EndRadius.equals(key))
                end.rad = Double.parseDouble(value);
            else if (Color.equals(key))
            {
                String[] c = value.split("\\s+");

                int[] color = new int[4];
                color[0] = Integer.parseInt(c[0]);
                color[1] = Integer.parseInt(c[1]);
                color[2] = Integer.parseInt(c[2]);
                color[3] = Integer.parseInt(c[3]);

                double[] p1 = MathUtil.latrec(start);
                double[] p2 = MathUtil.latrec(end);

                lineModel.addNewStructure();
                lineModel.activateStructure(lineId);
                lineModel.setStructureColor(lineId, color);
                lineModel.insertVertexIntoActivatedStructure(p1);
                lineModel.insertVertexIntoActivatedStructure(p2);

                ++lineId;

                // Force an increment of the color index. Note this
                // might not work so well since there may be no relationship
                // between the colors loaded from the file and the color index.
                getNextColor();
            }
        }

        in.close();
    }

    // It's possible that sometimes, faulty lines without 2 vertices get created.
    // Remove them here.
    private void removeFaultyProfiles()
    {
        int numProfiles = lineModel.getNumberOfStructures();
        for (int i=numProfiles-1; i>=0; --i)
        {
            Line line = (Line)lineModel.getStructure(i);
            if (line.controlPointIds.size() != 2)
                lineModel.removeStructure(i);
        }

    }

    private void removeAllProfiles()
    {
        lineModel.removeAllStructures();
        pickManager.setPickMode(PickMode.DEFAULT);
        editButton.setSelected(false);
    }

    private class SaveImageAction extends AbstractAction
    {
        private Renderer renderer;

        public SaveImageAction(Renderer renderer)
        {
            super("Export to Image...");
            this.renderer = renderer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            renderer.saveToFile();
        }
    }

    private class SaveShapeModelAsPLTAction extends AbstractAction
    {
        public SaveShapeModelAsPLTAction()
        {
            super("PLT (Gaskell Format)...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to PLT", "model.plt");

            try
            {
                if (file != null)
                    dem.saveAsPLT(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsOBJAction extends AbstractAction
    {
        public SaveShapeModelAsOBJAction()
        {
            super("OBJ...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to OBJ", "model.obj");

            try
            {
                if (file != null)
                    dem.saveAsOBJ(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsSTLAction extends AbstractAction
    {
        public SaveShapeModelAsSTLAction()
        {
            super("STL...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to STL", "model.stl");

            try
            {
                if (file != null)
                    dem.saveAsSTL(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SavePlateDataAction extends AbstractAction
    {
        public SavePlateDataAction()
        {
            super("Export Plate Data...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            String name = "platedata.csv";
            File file = CustomFileChooser.showSaveDialog(MapmakerView.this, "Export Plate Data", name);

            try
            {
                if (file != null)
                    dem.savePlateData(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(MapmakerView.this,
                        "An error occurred exporting the plate data.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
