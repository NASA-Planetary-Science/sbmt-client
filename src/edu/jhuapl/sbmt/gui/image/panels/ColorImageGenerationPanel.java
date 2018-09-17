package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.gui.image.ColorImagePopupMenu;

public class ColorImageGenerationPanel extends JPanel //implements TableModelListener, MouseListener, ListSelectionListener
{
    private ColorImagePopupMenu colorImagePopupMenu;
//    private JList colorImagesDisplayedList;
    private JButton removeColorImageButton;
    private JButton removeImageCubeButton;
    private JButton generateColorImageButton;

    private JComboBox redComboBox;
    private JComboBox greenComboBox;
    private JComboBox blueComboBox;
    private JButton redButton;
    private JButton greenButton;
    private JButton blueButton;
    private JLabel redLabel;
    private JLabel blueLabel;
    private JLabel greenLabel;

    private ImageResultsTable displayedImageList;

    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;


    public ColorImageGenerationPanel()
    {
        setBorder(new TitledBorder(null, "Color Image Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                JPanel panel = new JPanel();
                add(panel);
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

                        redButton = new JButton("Red");
                        panel.add(redButton);

                        Component horizontalGlue_1 = Box.createHorizontalGlue();
                        panel.add(horizontalGlue_1);
                        redLabel = new JLabel("");
                        panel.add(redLabel);

                JPanel panel_1 = new JPanel();
                add(panel_1);
                panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

                        greenButton = new JButton("Green");
                        panel_1.add(greenButton);

                        Component horizontalGlue_2 = Box.createHorizontalGlue();
                        panel_1.add(horizontalGlue_2);
                        greenLabel = new JLabel("");
                        panel_1.add(greenLabel);

                JPanel panel_2 = new JPanel();
                add(panel_2);
                panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

                        blueButton = new JButton("Blue");
                        panel_2.add(blueButton);

                        Component horizontalGlue_3 = Box.createHorizontalGlue();
                        panel_2.add(horizontalGlue_3);
                        blueLabel = new JLabel("");
                        panel_2.add(blueLabel);

        JPanel panel_3 = new JPanel();
        add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        generateColorImageButton = new JButton("Generate Color Image");
        panel_3.add(generateColorImageButton);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue);

        removeColorImageButton = new JButton("Remove Color Image");
        panel_3.add(removeColorImageButton);

        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        panel_4.add(scrollPane);

        scrollPane.setViewportView(displayedImageList);
        redComboBox = new JComboBox();
        greenComboBox = new JComboBox();
        blueComboBox = new JComboBox();
        redButton = new JButton();
        greenButton = new JButton();
        blueButton = new JButton();
        redLabel = new JLabel();
        greenLabel = new JLabel();
        blueLabel = new JLabel();

        mapColumnIndex=0;
        showFootprintColumnIndex=1;
        frusColumnIndex=2;
        bndrColumnIndex=3;
        idColumnIndex=4;
        filenameColumnIndex=5;
        dateColumnIndex=6;

        displayedImageList = new ImageResultsTable();

//        displayedImageList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        displayedImageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        displayedImageList.getColumnModel().getColumn(mapColumnIndex).setPreferredWidth(31);
//        displayedImageList.getColumnModel().getColumn(showFootprintColumnIndex).setPreferredWidth(35);
//        displayedImageList.getColumnModel().getColumn(frusColumnIndex).setPreferredWidth(31);
//        displayedImageList.getColumnModel().getColumn(bndrColumnIndex).setPreferredWidth(31);
//        displayedImageList.getColumnModel().getColumn(mapColumnIndex).setResizable(true);
//        displayedImageList.getColumnModel().getColumn(showFootprintColumnIndex).setResizable(true);
//        displayedImageList.getColumnModel().getColumn(frusColumnIndex).setResizable(true);
//        displayedImageList.getColumnModel().getColumn(bndrColumnIndex).setResizable(true);
    }

    public ColorImageGenerationPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public ColorImageGenerationPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public ColorImageGenerationPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public JComboBox getRedComboBox()
    {
        return redComboBox;
    }

    public JComboBox getGreenComboBox()
    {
        return greenComboBox;
    }

    public JComboBox getBlueComboBox()
    {
        return blueComboBox;
    }

    protected ComboBoxModel getRedComboBoxModel()
    {
        return null;
    }

    protected ComboBoxModel getGreenComboBoxModel()
    {
        return null;
    }

    protected ComboBoxModel getBlueComboBoxModel()
    {
        return null;
    }

    public ColorImagePopupMenu getColorImagePopupMenu()
    {
        return colorImagePopupMenu;
    }

    public javax.swing.JButton getRemoveColorImageButton()
    {
        return removeColorImageButton;
    }

    public javax.swing.JButton getRemoveImageCubeButton()
    {
        return removeImageCubeButton;
    }

    public javax.swing.JButton getGenerateColorImageButton()
    {
        return generateColorImageButton;
    }

    public JButton getRedButton()
    {
        return redButton;
    }

    public JButton getGreenButton()
    {
        return greenButton;
    }

    public JButton getBlueButton()
    {
        return blueButton;
    }

    public JLabel getRedLabel()
    {
        return redLabel;
    }

    public JLabel getBlueLabel()
    {
        return blueLabel;
    }

    public JLabel getGreenLabel()
    {
        return greenLabel;
    }

    public void setColorImagePopupMenu(ColorImagePopupMenu colorImagePopupMenu)
    {
        this.colorImagePopupMenu = colorImagePopupMenu;
    }

    public JTable getDisplayedImageList()
    {
        return displayedImageList;
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
}
