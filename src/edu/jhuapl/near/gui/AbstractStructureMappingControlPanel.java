package edu.jhuapl.near.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.util.Properties;

public abstract class AbstractStructureMappingControlPanel extends JPanel implements
    ActionListener,
    PropertyChangeListener,
    TableModelListener, ListSelectionListener
{
    private ModelManager modelManager;
    //private PickManager pickManager;
    private JButton loadStructuresButton;
    private JLabel structuresFileTextField;
    private JButton saveStructuresButton;
    private JButton saveAsStructuresButton;
    //private JList structuresList;
    private JTable structuresTable;
    private File structuresFile;
    //private StructuresPopupMenu structuresPopupMenu;
    private JToggleButton editButton;
    //private JComboBox structureTypeComboBox;
    //private int selectedIndex = -1;
    private StructureModel structureModel;
    private PickManager pickManager;
    private PickManager.PickMode pickMode;

    public AbstractStructureMappingControlPanel(
            final ModelManager modelManager,
            final StructureModel structureModel,
            final PickManager pickManager,
            final PickManager.PickMode pickMode,
            boolean compactMode)
    {
        this.modelManager = modelManager;
        //this.pickManager = pickManager;
        this.structureModel = structureModel;
        this.pickManager = pickManager;
        this.pickMode = pickMode;

        structureModel.addPropertyChangeListener(this);
        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                setEditingEnabled(false);
            }
        });

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        setLayout(new MigLayout("wrap 3, insets 0"));

        this.loadStructuresButton = new JButton("Load...");
        this.loadStructuresButton.setEnabled(true);
        this.loadStructuresButton.addActionListener(this);

        this.saveStructuresButton= new JButton("Save");
        this.saveStructuresButton.setEnabled(true);
        this.saveStructuresButton.addActionListener(this);

        this.saveAsStructuresButton= new JButton("Save As...");
        this.saveAsStructuresButton.setEnabled(true);
        this.saveAsStructuresButton.addActionListener(this);

        this.structuresFileTextField = new JLabel("<no file loaded>");
        this.structuresFileTextField.setEnabled(true);
        this.structuresFileTextField.setPreferredSize(new java.awt.Dimension(150, 22));

        if (!compactMode)
        {
            add(this.structuresFileTextField, "span");

            add(this.loadStructuresButton, "w 100!");
            add(this.saveStructuresButton, "w 100!");
            add(this.saveAsStructuresButton, "w 100!, wrap 15px");
        }

        JLabel structureTypeText = new JLabel(" Structures");
        if (!compactMode)
            add(structureTypeText, "span");

        //String[] options = {LineModel.LINES, CircleModel.CIRCLES};
        //structureTypeComboBox = new JComboBox(options);

        //add(structureTypeComboBox, "wrap");


        String[] columnNames = {"Id",
                "Type",
                "Name",
                "Details",
                "Color"};

        /*
        structuresList = new JList();
        structuresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structuresList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(structuresList);
        */

        Object[][] data = new Object[0][5];

        structuresTable = new JTable(new StructuresTableModel(data, columnNames));
        structuresTable.setBorder(BorderFactory.createTitledBorder(""));
        //table.setPreferredScrollableViewportSize(new Dimension(500, 130));
        structuresTable.setColumnSelectionAllowed(false);
        structuresTable.setRowSelectionAllowed(true);
        structuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        structuresTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structuresTable.setDefaultRenderer(Color.class, new ColorRenderer());
//        structuresTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        structuresTable.getModel().addTableModelListener(this);
        structuresTable.getSelectionModel().addListSelectionListener(this);
        structuresTable.addMouseListener(new TableMouseHandler());

        JScrollPane tableScrollPane = new JScrollPane(structuresTable);
        tableScrollPane.setPreferredSize(new Dimension(10000, 10000));

        //structuresPopupMenu = new StructuresPopupMenu(this.modelManager, this.pickManager, this);

        if (!compactMode)
            add(tableScrollPane, "span");


        if (structureModel.supportsSelection())
        {
            final JButton newButton = new JButton("New");
            newButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    structureModel.addNewStructure();
                    pickManager.setPickMode(pickMode);
                    editButton.setSelected(true);
                    updateStructureTable();

                    int numStructures = structuresTable.getRowCount();
                    if (numStructures > 0)
                        structuresTable.setRowSelectionInterval(numStructures-1, numStructures-1);
                }
            });
            add(newButton, "w 100!");
        }

        editButton = new JToggleButton("Edit");
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setEditingEnabled(editButton.isSelected());
            }
        });
        add(editButton, "w 100!");

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editButton.setSelected(false);

                int numStructures = structuresTable.getRowCount();
                int idx = structuresTable.getSelectedRow();
                if (idx >= 0 && idx < numStructures)
                {
                    structureModel.removeStructure(idx);
                    pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                    structureModel.selectStructure(-1);
                    updateStructureTable();

                    numStructures = structuresTable.getRowCount();
                    if (numStructures > 0)
                    {
                        if (idx > numStructures-1)
                            structuresTable.setRowSelectionInterval(numStructures-1, numStructures-1);
                        else
                            structuresTable.setRowSelectionInterval(idx, idx);
                    }
                }
            }
        });
        add(deleteButton, "w 100!, wrap");
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        Object source = actionEvent.getSource();

        if (source == this.loadStructuresButton)
        {
            structuresFile = CustomFileChooser.showOpenDialog(this, "Select File");
            if (structuresFile != null)
                this.structuresFileTextField.setText(structuresFile.getAbsolutePath());

            if (structuresFile != null)
            {
                try
                {
                    structureModel.loadModel(structuresFile);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(null,
                            "There was an error reading the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }
            }
        }
        else if (source == this.saveStructuresButton || source == this.saveAsStructuresButton)
        {
            if (structuresFile == null || source == this.saveAsStructuresButton)
            {
                structuresFile = CustomFileChooser.showSaveDialog(this, "Select File");
                if (structuresFile != null)
                    this.structuresFileTextField.setText(structuresFile.getAbsolutePath());
            }

            if (structuresFile != null)
            {
                try
                {
                    structureModel.saveModel(structuresFile);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(null,
                            "There was an error saving the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            updateStructureTable();

            if (structureModel.supportsSelection())
            {
                int idx = structureModel.getSelectedStructureIndex();
                if (idx >= 0)
                {
                    pickManager.setPickMode(pickMode);
                    if (!editButton.isSelected())
                        editButton.setSelected(true);
                    structuresTable.setRowSelectionInterval(idx, idx);
                    structuresTable.setEnabled(false);
                }
                else
                {
                    // Don't change the picker if this tab is not in view since
                    // it's possible we could be in the middle of drawing other
                    // objects.
                    if (isVisible())
                        pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                    if (editButton.isSelected())
                        editButton.setSelected(false);
                    structuresTable.setEnabled(true);
                }
            }
        }
        else if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            // If we're editing, say, a path, return immediately.
            if (structureModel.supportsSelection() &&
                    editButton.isSelected())
            {
                return;
            }

            PickEvent e = (PickEvent)evt.getNewValue();
            if (modelManager.getModel(e.getPickedProp()) == structureModel)
            {
                int idx = structureModel.getStructureIndexFromCellId(e.getPickedCellId(), e.getPickedProp());

                structuresTable.setRowSelectionInterval(idx, idx);
                structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
            }
            else
            {
                int count = structuresTable.getRowCount();
                if (count > 0)
                    structuresTable.removeRowSelectionInterval(0, count-1);
            }
        }
        else if (Properties.STRUCTURE_ADDED.equals(evt.getPropertyName()))
        {
            int idx = structureModel.getNumberOfStructures() - 1;
            structuresTable.setRowSelectionInterval(idx, idx);
            structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
        }
    }

    private void updateStructureTable()
    {
        int numStructures = structureModel.getNumberOfStructures();

        ((DefaultTableModel)structuresTable.getModel()).setRowCount(numStructures);
        for (int i=0; i<numStructures; ++i)
        {
            StructureModel.Structure structure = structureModel.getStructure(i);
            int[] c = structure.getColor();
            structuresTable.setValueAt(String.valueOf(structure.getId()), i, 0);
            structuresTable.setValueAt(structure.getType(), i, 1);
            structuresTable.setValueAt(structure.getName(), i, 2);
            structuresTable.setValueAt(structure.getInfo(), i, 3);
            structuresTable.setValueAt(new Color(c[0], c[1], c[2]), i, 4);
        }
    }

    public void tableChanged(TableModelEvent e)
    {
        if (e.getColumn() == 2)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            String name = (String)structuresTable.getValueAt(row, col);
            if (name != null && !name.equals(structure.getName()))
            {
                structure.setName(name);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() == false)
        {
            structureModel.highlightStructure(structuresTable.getSelectedRow());
        }
    }

    public void setEditingEnabled(boolean enable)
    {
        if (enable)
        {
            if (!editButton.isSelected())
                editButton.setSelected(true);
        }
        else
        {
            if (editButton.isSelected())
                editButton.setSelected(false);
        }

        if (structureModel.supportsSelection())
        {
            int idx = structuresTable.getSelectedRow();

            if (enable)
            {
                if (idx >= 0)
                {
                    pickManager.setPickMode(pickMode);
                    structureModel.selectStructure(idx);
                }
                else
                {
                    editButton.setSelected(false);
                }
            }
            else
            {
                pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                structureModel.selectStructure(-1);
            }

            // The item in the table might get deselected so select it again here.
            int numStructures = structuresTable.getRowCount();
            if (idx >= 0 && idx < numStructures)
                structuresTable.setRowSelectionInterval(idx, idx);
        }
        else
        {
            if (enable)
            {
                pickManager.setPickMode(pickMode);
            }
            else
            {
                pickManager.setPickMode(PickManager.PickMode.DEFAULT);
            }
        }
    }

    class ColorRenderer extends JLabel implements TableCellRenderer
    {
        private Border unselectedBorder = null;
        private Border selectedBorder = null;

        public ColorRenderer()
        {
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Color newColor = (Color)color;
            setBackground(newColor);

            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            }
            else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getBackground());
                }
                setBorder(unselectedBorder);
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());

            return this;
        }
    }

    class StructuresTableModel extends DefaultTableModel
    {
        public StructuresTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 2)
                return true;
            else
                return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex == 4)
                return Color.class;
            else
                return String.class;
        }
    }

    class TableMouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            int row = structuresTable.rowAtPoint(e.getPoint());
            int col = structuresTable.columnAtPoint(e.getPoint());

            if (e.getClickCount() == 2 && row >= 0 && col == 4)
            {
                Color color = ColorChooser.showColorChooser(
                        JOptionPane.getFrameForComponent(structuresTable),
                        structureModel.getStructure(row).getColor());

                if (color == null)
                    return;

                int[] c = new int[4];
                c[0] = color.getRed();
                c[1] = color.getGreen();
                c[2] = color.getBlue();
                c[3] = color.getAlpha();

                structureModel.setStructureColor(row, c);
            }
        }
    }

}
