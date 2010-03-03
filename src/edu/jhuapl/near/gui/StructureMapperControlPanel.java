package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.gui.pick.PickManager;
//import edu.jhuapl.near.gui.popupmenus.StructuresPopupMenu;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.Properties;
import net.miginfocom.swing.MigLayout;

public class StructureMapperControlPanel extends JPanel implements 
	ActionListener, 
	PropertyChangeListener 
{
    //private ModelManager modelManager;
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
    
    public StructureMapperControlPanel(
    		final ModelManager modelManager,
    		final StructureModel structureModel,
    		final PickManager pickManager,
    		final PickManager.PickMode pickMode) 
    {
		//this.modelManager = modelManager;
		//this.pickManager = pickManager;
		this.structureModel = structureModel;
		
		structureModel.addPropertyChangeListener(this);
		
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
        
        add(this.structuresFileTextField, "span");
        
        add(this.loadStructuresButton, "w 100!");
        add(this.saveStructuresButton, "w 100!");
        add(this.saveAsStructuresButton, "w 100!, wrap 15px");

        JLabel structureTypeText = new JLabel(" Structures");
        add(structureTypeText, "span");
        
        //String[] options = {LineModel.LINES, CircleModel.CIRCLES};
        //structureTypeComboBox = new JComboBox(options);
        
        //add(structureTypeComboBox, "wrap");
        
        
        String[] columnNames = {"Id",
                "Type",
                "Name",
                "Details"};

        /*
        structuresList = new JList();
        structuresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structuresList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(structuresList);
        */

        Object[][] data = new Object[0][4];

		structuresTable = new JTable(new DefaultTableModel(data, columnNames)
		{
			public boolean isCellEditable(int row, int column)
			{
				if (column == 2)
					return true;
				else
					return false;
			}
		});

		structuresTable.setBorder(BorderFactory.createTitledBorder(""));
		//table.setPreferredScrollableViewportSize(new Dimension(500, 130));
        structuresTable.setColumnSelectionAllowed(false);
        structuresTable.setRowSelectionAllowed(true);
        structuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        structuresTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        
        
        JScrollPane tableScrollPane = new JScrollPane(structuresTable);
        tableScrollPane.setPreferredSize(new Dimension(10000, 10000));
        
        //structuresPopupMenu = new StructuresPopupMenu(this.modelManager, this.pickManager, this);

        add(tableScrollPane, "span");



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
				structuresTable.setRowSelectionInterval(numStructures-1, numStructures-1);
			}
        });

        add(newButton, "w 100!");
        
        editButton = new JToggleButton("Edit");
        editButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				int idx = structuresTable.getSelectedRow();
				
				if (editButton.isSelected())
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
        add(deleteButton, "w 100!");
	}

    public void actionPerformed(ActionEvent actionEvent)
    {
        Object source = actionEvent.getSource();

        if (source == this.loadStructuresButton)
        {
        	structuresFile = AnyFileChooser.showOpenDialog(this, "Select File");
        	if (structuresFile != null)
        		this.structuresFileTextField.setText(structuresFile.getAbsolutePath());
        	/*
        	try {
				((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).loadModel(structuresFile);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
        }
        else if (source == this.saveStructuresButton || source == this.saveAsStructuresButton)
        {
        	if (structuresFile == null || source == this.saveAsStructuresButton)
        	{
            	structuresFile = AnyFileChooser.showSaveDialog(this, "Select File");
            	if (structuresFile != null)
            		this.structuresFileTextField.setText(structuresFile.getAbsolutePath());
        	}
        	
        	if (structuresFile != null)
        	{
        		/*
				try {
					((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).saveModel(structuresFile);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
        	}
        }
    }

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			updateStructureTable();
		}
	}
	
	private void updateStructureTable()
	{
		int numStructures = structureModel.getNumberOfStructures();

		((DefaultTableModel)structuresTable.getModel()).setRowCount(numStructures);
		for (int i=0; i<numStructures; ++i)
		{
			StructureModel.Structure structure = structureModel.getStructure(i);
			structuresTable.setValueAt(structure.getId(), i, 0);
			structuresTable.setValueAt(structure.getType(), i, 1);
			structuresTable.setValueAt(structure.getName(), i, 2);
			structuresTable.setValueAt(structure.getInfo(), i, 3);
		}
	}
}
