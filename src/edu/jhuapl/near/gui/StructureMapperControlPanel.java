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
	ItemListener, 
	ActionListener, 
	PropertyChangeListener, MouseListener 
{
    private ModelManager modelManager;
    private PickManager pickManager;
    private JButton loadStructuresButton;
    private JLabel structuresFileTextField;
    private JToggleButton mapLineButton;
    private JToggleButton mapCircleButton;
    private JButton saveStructuresButton;
    private JButton saveAsStructuresButton;
    //private JList structuresList;
    private JTable structuresTable;
    private File structuresFile;
    //private StructuresPopupMenu structuresPopupMenu;
    private JToggleButton editButton;
    //private JComboBox structureTypeComboBox;
    //private int selectedIndex = -1;
    
    public StructureMapperControlPanel(final ModelManager modelManager, final PickManager pickManager) 
    {
		this.modelManager = modelManager;
		this.pickManager = pickManager;

		final LineModel lineModel = 
			((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).getLineModel();

		lineModel.addPropertyChangeListener(this);
		
		pickManager.getLineamentPicker().addPropertyChangeListener(this);
		
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
        
        //String[] options = {LineModel.LINES};
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
        
        
        JScrollPane listScrollPane = new JScrollPane(structuresTable);
        listScrollPane.setPreferredSize(new Dimension(10000, 10000));
        
        //structuresPopupMenu = new StructuresPopupMenu(this.modelManager, this.pickManager, this);

        add(listScrollPane, "span");



        final JButton newButton = new JButton("New");

        newButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				lineModel.addNewLine();
				pickManager.setPickMode(PickManager.PickMode.LINE_DRAW);
				editButton.setSelected(true);
				updateStructureList();

				int numLines = structuresTable.getRowCount();
				structuresTable.setRowSelectionInterval(numLines-1, numLines-1);
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
						pickManager.setPickMode(PickManager.PickMode.LINE_DRAW);
						lineModel.selectLine(idx);
					}
					else
					{
						editButton.setSelected(false);
					}
				}
				else
				{
					pickManager.setPickMode(PickManager.PickMode.DEFAULT);
					lineModel.selectLine(-1);
				}
				
				// The item in the list might get deselected so select it again here.
				int numLines = structuresTable.getRowCount();
				if (idx >= 0 && idx < numLines)
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
				
				int numLines = lineModel.getNumberOfLines();
				int idx = structuresTable.getSelectedRow();
				if (idx >= 0 && idx < numLines)
				{
					lineModel.removeLine(idx);
					pickManager.setPickMode(PickManager.PickMode.DEFAULT);
					lineModel.selectLine(-1);
					updateStructureList();

					numLines = lineModel.getNumberOfLines();
					if (numLines > 0)
					{
						if (idx < 0)
							structuresTable.setRowSelectionInterval(0, 0);
						else if (idx > numLines-1)
							structuresTable.setRowSelectionInterval(numLines-1, numLines-1);
						else
							structuresTable.setRowSelectionInterval(idx, idx);
					}
				}
			}
        });
        add(deleteButton, "w 100!");
	}

	public void itemStateChanged(ItemEvent e) 
	{
	}

    public void actionPerformed(ActionEvent actionEvent)
    {
        Object source = actionEvent.getSource();

        if (source == this.loadStructuresButton)
        {
        	structuresFile = AnyFileChooser.showOpenDialog(this, "Select File");
        	if (structuresFile != null)
        		this.structuresFileTextField.setText(structuresFile.getAbsolutePath());
        	
        	try {
				((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).loadModel(structuresFile);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				try {
					((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).saveModel(structuresFile);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (Properties.FINISHED_DRAWING_LINE.equals(evt.getPropertyName()))
		{
			pickManager.setPickMode(PickManager.PickMode.DEFAULT);
			mapCircleButton.setSelected(false);
			mapLineButton.setSelected(false);
		}
		else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			updateStructureList();
		}
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) 
	{
		/*
		int selectedIndex = structuresList.locationToIndex(e.getPoint());

		if (selectedIndex >= 0 && structuresList.getCellBounds(selectedIndex, selectedIndex).contains(e.getPoint()))
		{
			structuresList.setSelectedIndex(selectedIndex);
			//structuresPopupMenu.setCurrentStructure(msiRawResults.get(index));

			LineModel lineModel = 
				((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).getLineModel();

			if (e.isPopupTrigger()) 
			{
				structuresPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
        */
    }
	
	private void updateStructureList()
	{
		//String item = (String)structureTypeComboBox.getSelectedItem();
		//if (LineModel.LINES.equals(item))
		{
			LineModel lineModel = 
				((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).getLineModel();
			
			int numLines = lineModel.getNumberOfLines();
			
			((DefaultTableModel)structuresTable.getModel()).setRowCount(numLines);
			for (int i=0; i<numLines; ++i)
			{
				LineModel.Line line = lineModel.getLine(i);
				structuresTable.setValueAt(line.id, i, 0);
				structuresTable.setValueAt("Polyline", i, 1);
				structuresTable.setValueAt(line.name, i, 2);
				structuresTable.setValueAt(line.controlPointIds.size() + " vertices", i, 3);
			}
		}
	}
}
