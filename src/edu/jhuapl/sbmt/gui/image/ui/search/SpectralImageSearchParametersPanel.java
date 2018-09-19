package edu.jhuapl.sbmt.gui.image.ui.search;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.swing.CheckBoxTree;

public class SpectralImageSearchParametersPanel extends ImageSearchParametersPanel
{
    private JTable filterTable;
    private JTable userParamTable;
    private JPanel aux;

    public SpectralImageSearchParametersPanel()
    {
        super();

        //TODO: Override and setup in subclass
        hierarchicalSearchScrollPane = new javax.swing.JScrollPane();


        aux = getAuxPanel();
        aux.setLayout(new BoxLayout(aux, BoxLayout.X_AXIS));

        filterTable = new JTable();

        String[] columnNames = new String[]{
                "Select", "Filter Name"
        };

        filterTable.setModel(new FilterTableModel(new Object[0][2], columnNames));

        filterTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);



        filterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        filterTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        filterTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        filterTable.getColumnModel().getColumn(0).setResizable(true);


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        aux.add(scrollPane);
        scrollPane.setViewportView(filterTable);

        userParamTable = new JTable();

        String[] columnNames2 = new String[]{
                "Select", "User Defined Search Name"
        };

        userParamTable.setModel(new FilterTableModel(new Object[0][2], columnNames2));

        userParamTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);



        userParamTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        userParamTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        userParamTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        userParamTable.getColumnModel().getColumn(0).setResizable(true);


        JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setPreferredSize(new java.awt.Dimension(300, 100));
        aux.add(scrollPane2);
        scrollPane2.setViewportView(userParamTable);

        //maybe these need to overridden later?
//        redComboBox.setVisible(false);
//        greenComboBox.setVisible(false);
//        blueComboBox.setVisible(false);
//
//        ComboBoxModel redModel = getRedComboBoxModel();
//        ComboBoxModel greenModel = getGreenComboBoxModel();
//        ComboBoxModel blueModel = getBlueComboBoxModel();
//        if (redModel != null && greenModel != null && blueModel != null)
//        {
//            redComboBox.setModel(redModel);
//            greenComboBox.setModel(greenModel);
//            blueComboBox.setModel(blueModel);
//
//            redComboBox.setVisible(true);
//            greenComboBox.setVisible(true);
//            blueComboBox.setVisible(true);
//
//            redButton.setVisible(false);
//            greenButton.setVisible(false);
//            blueButton.setVisible(false);
//        }


    }

    public SpectralImageSearchParametersPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public SpectralImageSearchParametersPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public SpectralImageSearchParametersPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

//    public JCheckBox[] getFilterCheckBoxes()
//    {
//        return filterCheckBoxes;
//    }
//
//    public JCheckBox[] getUserDefinedCheckBoxes()
//    {
//        return userDefinedCheckBoxes;
//    }

    public CheckBoxTree getCheckBoxTree()
    {
        return checkBoxTree;
    }

    public void setCheckBoxTree(CheckBoxTree checkBoxTree)
    {
        this.checkBoxTree = checkBoxTree;
    }


//    public JCheckBox getUserDefined1CheckBox()
//    {
//        return userDefined1CheckBox;
//    }
//
//    public JCheckBox getUserDefined2CheckBox()
//    {
//        return userDefined2CheckBox;
//    }
//
//    public JCheckBox getUserDefined3CheckBox()
//    {
//        return userDefined3CheckBox;
//    }
//
//    public JCheckBox getUserDefined4CheckBox()
//    {
//        return userDefined4CheckBox;
//    }
//
//    public JCheckBox getUserDefined5CheckBox()
//    {
//        return userDefined5CheckBox;
//    }
//
//    public JCheckBox getUserDefined6CheckBox()
//    {
//        return userDefined6CheckBox;
//    }
//
//    public JCheckBox getUserDefined7CheckBox()
//    {
//        return userDefined7CheckBox;
//    }
//
//    public JCheckBox getUserDefined8CheckBox()
//    {
//        return userDefined8CheckBox;
//    }
//
//    public JPanel getUserDefinedCheckBoxPanel()
//    {
//        return userDefinedCheckBoxPanel;
//    }

//    public JCheckBox getFilter10CheckBox()
//    {
//        return filter10CheckBox;
//    }
//
//    public JCheckBox getFilter11CheckBox()
//    {
//        return filter11CheckBox;
//    }
//
//    public JCheckBox getFilter12CheckBox()
//    {
//        return filter12CheckBox;
//    }
//
//    public JCheckBox getFilter13CheckBox()
//    {
//        return filter13CheckBox;
//    }
//
//    public JCheckBox getFilter14CheckBox()
//    {
//        return filter14CheckBox;
//    }
//
//    public JCheckBox getFilter15CheckBox()
//    {
//        return filter15CheckBox;
//    }
//
//    public JCheckBox getFilter16CheckBox()
//    {
//        return filter16CheckBox;
//    }
//
//    public JCheckBox getFilter17CheckBox()
//    {
//        return filter17CheckBox;
//    }
//
//    public JCheckBox getFilter18CheckBox()
//    {
//        return filter18CheckBox;
//    }
//
//    public JCheckBox getFilter19CheckBox()
//    {
//        return filter19CheckBox;
//    }
//
//    public JCheckBox getFilter1CheckBox()
//    {
//        return filter1CheckBox;
//    }
//
//    public JCheckBox getFilter20CheckBox()
//    {
//        return filter20CheckBox;
//    }
//
//    public JCheckBox getFilter21CheckBox()
//    {
//        return filter21CheckBox;
//    }
//
//    public JCheckBox getFilter22CheckBox()
//    {
//        return filter22CheckBox;
//    }
//
//    public JCheckBox getFilter2CheckBox()
//    {
//        return filter2CheckBox;
//    }
//
//    public JCheckBox getFilter3CheckBox()
//    {
//        return filter3CheckBox;
//    }
//
//    public JCheckBox getFilter4CheckBox()
//    {
//        return filter4CheckBox;
//    }
//
//    public JCheckBox getFilter5CheckBox()
//    {
//        return filter5CheckBox;
//    }
//
//    public JCheckBox getFilter6CheckBox()
//    {
//        return filter6CheckBox;
//    }
//
//    public JCheckBox getFilter7CheckBox()
//    {
//        return filter7CheckBox;
//    }
//
//    public JCheckBox getFilter8CheckBox()
//    {
//        return filter8CheckBox;
//    }
//
//    public JCheckBox getFilter9CheckBox()
//    {
//        return filter9CheckBox;
//    }



//    public void enableFilenameSearch(boolean enable)
//    {
//        super.enableFilenameSearch(enable);
//        filter1CheckBox.setEnabled(!enable);
//        filter2CheckBox.setEnabled(!enable);
//        filter3CheckBox.setEnabled(!enable);
//        filter4CheckBox.setEnabled(!enable);
//        filter5CheckBox.setEnabled(!enable);
//        filter6CheckBox.setEnabled(!enable);
//        filter7CheckBox.setEnabled(!enable);
//        filter8CheckBox.setEnabled(!enable);
//        filter9CheckBox.setEnabled(!enable);
//        filter10CheckBox.setEnabled(!enable);
//        filter11CheckBox.setEnabled(!enable);
//        filter12CheckBox.setEnabled(!enable);
//        filter13CheckBox.setEnabled(!enable);
//        filter14CheckBox.setEnabled(!enable);
//        filter15CheckBox.setEnabled(!enable);
//        filter16CheckBox.setEnabled(!enable);
//        filter17CheckBox.setEnabled(!enable);
//        filter18CheckBox.setEnabled(!enable);
//        filter19CheckBox.setEnabled(!enable);
//        filter20CheckBox.setEnabled(!enable);
//        filter21CheckBox.setEnabled(!enable);
//        filter22CheckBox.setEnabled(!enable);
//        userDefined1CheckBox.setEnabled(!enable);
//        userDefined2CheckBox.setEnabled(!enable);
//        userDefined3CheckBox.setEnabled(!enable);
//        userDefined4CheckBox.setEnabled(!enable);
//        userDefined5CheckBox.setEnabled(!enable);
//        userDefined6CheckBox.setEnabled(!enable);
//        userDefined7CheckBox.setEnabled(!enable);
//        userDefined8CheckBox.setEnabled(!enable);

//    }


    public JTable getFilterTable()
    {
        return filterTable;
    }


    public JTable getUserParamTable()
    {
        return userParamTable;
    }


    public class FilterTableModel extends DefaultTableModel
    {
        public FilterTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 0) return true;
            return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex == 0)
                return Boolean.class;
            else
                return String.class;
        }
    }

}
