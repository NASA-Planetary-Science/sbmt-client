package edu.jhuapl.sbmt.gui.lidar.v2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.sbmt.gui.lidar.LidarPopupMenu;
import edu.jhuapl.sbmt.gui.lidar.LidarTrackTranslationDialog;


public class LidarSearchView extends JPanel
{
    private JScrollPane tableScrollPane;
    private JTable table;
    private JPanel tablePanel;
    private JButton loadTracksButton;
    private JComboBox fileTypeComboBox;
    private JButton hideAllButton;
    private JButton showAllButton;
    private JButton removeAllButton;
    private JButton translateAllButton;
    private JToggleButton dragTracksButton;
    private JCheckBox showErrorCheckBox;
    private JLabel errorLabel;
    protected LidarPopupMenu lidarPopupMenu;
    private LidarTrackTranslationDialog translateDialog;
    protected edu.jhuapl.saavtk.gui.RadialOffsetChanger radialOffsetSlider;
    private JComboBox sourceComboBox;
    private JButton manageSourcesButton;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JFormattedTextField minTrackSizeTextField;
    private JFormattedTextField trackSeparationTextField;
    private JToggleButton selectRegionButton;
    private JButton clearRegionButton;
    private JButton searchButton;
    private JSpinner pointSizeSpinner;
    private JPanel searchPanel;
    private JPanel trackInfoPanel;

    /**
     * Create the panel.
     */
    public LidarSearchView()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        JPanel panel = new JPanel();
        scrollPane.add(panel);
        scrollPane.setViewportView(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        searchPanel = new JPanel();
        searchPanel.setBorder(new TitledBorder(null, "Search",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(searchPanel);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();
        searchPanel.add(sourcePanel);
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Source:");
        sourcePanel.add(lblNewLabel);

        sourceComboBox = new JComboBox();
        sourcePanel.add(sourceComboBox);

        manageSourcesButton = new JButton("Manage");
        sourcePanel.add(manageSourcesButton);

        JPanel timeRangePanel = new JPanel();
        timeRangePanel.setBorder(null);
        searchPanel.add(timeRangePanel);
        timeRangePanel
                .setLayout(new BoxLayout(timeRangePanel, BoxLayout.X_AXIS));

        JLabel lblStart = new JLabel("Start:");
        timeRangePanel.add(lblStart);

        startDateSpinner = new JSpinner();
        startDateSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(951714000000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startDateSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                startDateSpinner.getPreferredSize().height));
        timeRangePanel.add(startDateSpinner);

        JLabel lblEnd = new JLabel("End:");
        timeRangePanel.add(lblEnd);

        endDateSpinner = new JSpinner();
        endDateSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(982040400000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endDateSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                endDateSpinner.getPreferredSize().height));
        timeRangePanel.add(endDateSpinner);

        Component verticalStrut = Box.createVerticalStrut(20);
        searchPanel.add(verticalStrut);

        JPanel searchPropertiesPanel = new JPanel();
        searchPropertiesPanel.setBorder(null);
        searchPanel.add(searchPropertiesPanel);
        searchPropertiesPanel.setLayout(
                new BoxLayout(searchPropertiesPanel, BoxLayout.X_AXIS));

        JLabel lblMinTrackSize = new JLabel("Min Track Size:");
        searchPropertiesPanel.add(lblMinTrackSize);

        minTrackSizeTextField = new JFormattedTextField();
        minTrackSizeTextField.setText("10");
        minTrackSizeTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                minTrackSizeTextField.getPreferredSize().height));

        searchPropertiesPanel.add(minTrackSizeTextField);

        JLabel lblNewLabel_1 = new JLabel("Track Separaton (sec):");
        searchPropertiesPanel.add(lblNewLabel_1);

        trackSeparationTextField = new JFormattedTextField();
        trackSeparationTextField.setText("10");
        trackSeparationTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                trackSeparationTextField.getPreferredSize().height));

        searchPropertiesPanel.add(trackSeparationTextField);

        Component verticalStrut_1 = Box.createVerticalStrut(20);
        searchPanel.add(verticalStrut_1);

        JPanel searchButtonsPanel = new JPanel();
        searchPanel.add(searchButtonsPanel);
        searchButtonsPanel
                .setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.X_AXIS));

        selectRegionButton = new JToggleButton("Select Region");
        searchButtonsPanel.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        searchButtonsPanel.add(clearRegionButton);

        searchButton = new JButton("Search");
        searchButtonsPanel.add(searchButton);

        trackInfoPanel = new JPanel();
        trackInfoPanel.setVisible(false);
        trackInfoPanel.setBorder(new TitledBorder(null, "Tracks",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(trackInfoPanel);
        trackInfoPanel
                .setLayout(new BoxLayout(trackInfoPanel, BoxLayout.Y_AXIS));

        JPanel loadPanel = new JPanel();
        trackInfoPanel.add(loadPanel);
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.X_AXIS));

        loadTracksButton = new JButton("Load Tracks");
        loadPanel.add(loadTracksButton);

        JLabel lblFileType = new JLabel("File Type:");
        loadPanel.add(lblFileType);

        fileTypeComboBox = new JComboBox();
        fileTypeComboBox.setToolTipText(
                "<html>\nTrack file can be in either text or binary format.<br><br>\nIf text, file may contain 3 or more space-delimited columns.<br>\nDepending on the number of columns, the file is interpreted the following way:<br>\n - 3 columns: X, Y, and Z target position. Time and spacecraft position set to zero.<br> \n - 4 columns: time, X, Y, and Z target position. Spacecraft position set to zero.<br>\n - 5 columns: time, X, Y, and Z target position. Spacecraft position set to zero. 5th column ignored.<br>\n - 6 columns: X, Y, Z target position, X, Y, Z spacecraft position. Time set to zero.<br>\n - 7 or more columns: time, X, Y, and Z target position, X, Y, Z spacecraft position. Additional columns ignored.<br>\nNote that time is expressed either as a UTC string such as 2000-04-06T13:19:12.153<br>\nor as a floating point ephemeris time such as 9565219.901.<br>\n<br>\nIf binary, each record must consist of 7 double precision values:<br>\n1. ET<br>\n2. X target<br>\n3. Y target<br>\n4. Z target<br>\n5. X spacecraft position<br>\n6. Y spacecraft position<br>\n7. Z spacecraft position<br>\n");

        loadPanel.add(fileTypeComboBox);

        tablePanel = new JPanel();
        panel.add(tablePanel);
        tablePanel.setLayout(new BorderLayout(0, 0));

        JPanel tableDisplayButtonPanel = new JPanel();
        tablePanel.add(tableDisplayButtonPanel, BorderLayout.SOUTH);
        tableDisplayButtonPanel.setLayout(
                new BoxLayout(tableDisplayButtonPanel, BoxLayout.X_AXIS));

        hideAllButton = new JButton("Hide All");
        tableDisplayButtonPanel.add(hideAllButton);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        tableDisplayButtonPanel.add(horizontalGlue_1);

        showAllButton = new JButton("Show All");
        tableDisplayButtonPanel.add(showAllButton);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        tableDisplayButtonPanel.add(horizontalGlue_2);

        removeAllButton = new JButton("Remove All");
        tableDisplayButtonPanel.add(removeAllButton);

        JPanel translationPanel = new JPanel();
        translationPanel.setBorder(new TitledBorder(null, "Translation",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(translationPanel);
        translationPanel
                .setLayout(new BoxLayout(translationPanel, BoxLayout.Y_AXIS));

        JPanel panel_1 = new JPanel();
        translationPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        translateAllButton = new JButton("Translate All Tracks...");
        panel_1.add(translateAllButton);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue);

        dragTracksButton = new JToggleButton("Drag Tracks");
        panel_1.add(dragTracksButton);

        JPanel propertiesPanel = new JPanel();
        propertiesPanel.setBorder(new TitledBorder(null, "Properties",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(propertiesPanel);
        propertiesPanel
                .setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));

        radialOffsetSlider = new RadialOffsetChanger();
        propertiesPanel.add(radialOffsetSlider);

        JPanel pointSizePanel = new JPanel();
        propertiesPanel.add(pointSizePanel);
        pointSizePanel
                .setLayout(new BoxLayout(pointSizePanel, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Point Size:");
        pointSizePanel.add(lblNewLabel_2);

        pointSizeSpinner = new JSpinner();
        pointSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, 100, 1));
        pointSizePanel.add(pointSizeSpinner);

        JPanel errorPanel = new JPanel();
        propertiesPanel.add(errorPanel);
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.X_AXIS));

        showErrorCheckBox = new JCheckBox("Show Error");
        showErrorCheckBox.setToolTipText("<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");

        errorPanel.add(showErrorCheckBox);

        errorLabel = new JLabel("New label");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorPanel.add(errorLabel);

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        errorPanel.add(horizontalGlue_3);

//        addComponentListener(new ComponentListener()
//        {
//
//            @Override
//            public void componentShown(ComponentEvent e)
//            {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void componentResized(ComponentEvent e)
//            {
//                System.out.println(
//                        "LidarSearchView.LidarSearchView().new ComponentListener() {...}: componentResized: component resized " + getWidth());
//                tableScrollPane.setPreferredSize(new Dimension(getWidth(), 150));
//                tableScrollPane.setMaximumSize(new Dimension(getWidth(), 150));
//                tableScrollPane.invalidate();
//            }
//
//            @Override
//            public void componentMoved(ComponentEvent e)
//            {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void componentHidden(ComponentEvent e)
//            {
//                // TODO Auto-generated method stub
//
//            }
//        });

    }

    public JTable getTable()
    {
        return table;
    }

    public JScrollPane getTableScrollPane()
    {
        return tableScrollPane;
    }

    public void setTable(JTable table)
    {
        this.table = table;
        this.table.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null}
                },
                new String [] {
                    "Title 1", "Title 2", "Title 3", "Title 4"
                }
            ));
        this.table.setFillsViewportHeight(true);
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(getWidth(), 150));
        tableScrollPane.setMaximumSize(new Dimension(getWidth(), 150));
//        tablePanel.add(tableScrollPane, 0);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
    }

    public JButton getLoadTracksButton()
    {
        return loadTracksButton;
    }

    public JComboBox getFileTypeComboBox()
    {
        return fileTypeComboBox;
    }

    public JButton getHideAllButton()
    {
        return hideAllButton;
    }

    public JButton getShowAllButton()
    {
        return showAllButton;
    }

    public JButton getRemoveAllButton()
    {
        return removeAllButton;
    }

    public JButton getTranslateAllButton()
    {
        return translateAllButton;
    }

    public JToggleButton getDragTracksButton()
    {
        return dragTracksButton;
    }

    public RadialOffsetChanger getRadialOffsetSlider()
    {
        return radialOffsetSlider;
    }

    public JCheckBox getShowErrorCheckBox()
    {
        return showErrorCheckBox;
    }

    public JLabel getErrorLabel()
    {
        return errorLabel;
    }

    public JComboBox getSourceComboBox()
    {
        return sourceComboBox;
    }

    public JButton getManageSourcesButton()
    {
        return manageSourcesButton;
    }

    public JSpinner getStartDateSpinner()
    {
        return startDateSpinner;
    }

    public JSpinner getEndDateSpinner()
    {
        return endDateSpinner;
    }

    public JFormattedTextField getMinTrackSizeTextField()
    {
        return minTrackSizeTextField;
    }

    public JFormattedTextField getTrackSeparationTextField()
    {
        return trackSeparationTextField;
    }

    public JToggleButton getSelectRegionButton()
    {
        return selectRegionButton;
    }

    public JButton getClearRegionButton()
    {
        return clearRegionButton;
    }

    public JButton getSearchButton()
    {
        return searchButton;
    }

    public JSpinner getPointSizeSpinner()
    {
        return pointSizeSpinner;
    }
    public JPanel getSearchPanel() {
        return searchPanel;
    }

    public LidarTrackTranslationDialog getTranslateDialog()
    {
        return translateDialog;
    }

    public LidarPopupMenu getLidarPopupMenu()
    {
        return lidarPopupMenu;
    }

    public void setLidarPopupMenu(LidarPopupMenu lidarPopupMenu)
    {
        this.lidarPopupMenu = lidarPopupMenu;
    }
    public JPanel getTrackInfoPanel() {
        return trackInfoPanel;
    }
}
