package edu.jhuapl.sbmt.gui.image.ui.cubes;

import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.gui.image.ui.images.ImageResultsTable;

public class ImageCubeGenerationPanel extends JPanel //implements PropertyChangeListener, TableModelListener, MouseListener, ListSelectionListener
{
//    private JList imageCubesDisplayedList;
//    private ImageCubePopupMenu imageCubePopupMenu;
    private JButton generateImageCubeButton;
    private JButton removeImageCubeButton;
//    private JButton greenButton;
//    private JComboBox greenComboBox;
//    private JLabel greenLabel;
//    private JButton blueButton;
//    private JComboBox blueComboBox;
//    private JLabel blueLabel;
//    private JButton redButton;
//    private JComboBox redComboBox;
//    private JLabel redLabel;

    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;
    private ImageResultsTable imageCubeTable;
    private JScrollPane scrollPane;
    protected JPanel panel_1;
//    private JLabel layerLabel;
//    private JLabel layerValue;
//    private JSlider layerSlider;
    private DefaultBoundedRangeModel monoBoundedRangeModel;
    private int nbands;

    public ImageCubeGenerationPanel()
    {
        setBorder(new TitledBorder(null, "Image Cube Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        generateImageCubeButton = new JButton("Generate Image Cube");
        panel.add(generateImageCubeButton);

        removeImageCubeButton = new JButton("Remove Image Cube");
        panel.add(removeImageCubeButton);

        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        add(scrollPane);

        imageCubeTable = new ImageResultsTable();
        scrollPane.setViewportView(imageCubeTable);

        panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

//        layerLabel = new JLabel("Layer:");
//        panel_1.add(layerLabel);
//
//        layerValue = new JLabel("0");
//        panel_1.add(layerValue);

//        layerSlider = new JSlider();
//        panel_1.add(layerSlider);

        mapColumnIndex = 0;
        showFootprintColumnIndex = 1;
        bndrColumnIndex = 2;
        filenameColumnIndex = 3;



    }

//    public void setNBands(int nBands)
//    {
//        int midband = (nbands-1) / 2;
//        String midbandString = Integer.toString(midband);
//        layerValue.setText(midbandString);
//
//        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
//        layerSlider = new JSlider(monoBoundedRangeModel);
//    }


    public JTable getImageCubeTable()
    {
        return imageCubeTable;
    }


    public int getMapColumnIndex()
    {
        return mapColumnIndex;
    }


    public int getShowFootprintColumnIndex()
    {
        return showFootprintColumnIndex;
    }


    public int getFrusColumnIndex()
    {
        return frusColumnIndex;
    }


    public int getBndrColumnIndex()
    {
        return bndrColumnIndex;
    }


    public int getDateColumnIndex()
    {
        return dateColumnIndex;
    }


    public int getIdColumnIndex()
    {
        return idColumnIndex;
    }


    public int getFilenameColumnIndex()
    {
        return filenameColumnIndex;
    }


    public JButton getGenerateImageCubeButton()
    {
        return generateImageCubeButton;
    }


    public JButton getRemoveImageCubeButton()
    {
        return removeImageCubeButton;
    }

//    public JLabel getLayerValue()
//    {
//        return layerValue;
//    }
//
//
//    public JSlider getLayerSlider()
//    {
//        return layerSlider;
//    }
}
