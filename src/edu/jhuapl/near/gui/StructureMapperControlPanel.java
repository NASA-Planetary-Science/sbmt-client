package edu.jhuapl.near.gui;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.gui.pick.LinePicker;
import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.gui.popupmenus.StructuresPopupMenu;
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
    private JList structuresList;
    private File structuresFile;
    private StructuresPopupMenu structuresPopupMenu;
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

        this.structuresFileTextField = new JLabel("File: <none>");
        this.structuresFileTextField.setEnabled(true);
        this.structuresFileTextField.setPreferredSize(new java.awt.Dimension(150, 22));
        
        add(this.loadStructuresButton);
        add(this.saveStructuresButton);
        add(this.saveAsStructuresButton);

        add(this.structuresFileTextField, "wrap");
        

        final JButton newButton = new JButton("New Path");

        newButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				lineModel.addNewLine();
				pickManager.setPickMode(PickManager.PickMode.LINE_DRAW);
				pickManager.getLineamentPicker().setEditMode(LinePicker.EditMode.VERTEX_ADD);
				editButton.setSelected(true);
				updateStructureList();
			}
        });

        add(newButton);
        

        editButton = new JToggleButton("Edit Path");
        editButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				int selectedIndex = structuresList.getSelectedIndex();
				if (editButton.isSelected())
				{
					if (selectedIndex >= 0)
					{
						pickManager.setPickMode(PickManager.PickMode.LINE_DRAW);
						pickManager.getLineamentPicker().setEditMode(LinePicker.EditMode.VERTEX_DRAG);
						lineModel.selectLine(selectedIndex);
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
				if (selectedIndex >= 0)
					structuresList.setSelectedIndex(selectedIndex);
			}
        });
        add(editButton);
        
        JButton deleteButton = new JButton("Delete Path");
        deleteButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				editButton.setSelected(false);
				
				int selectedIndex = structuresList.getSelectedIndex();
				if (selectedIndex >= 0 && selectedIndex < lineModel.getNumberOfLines())
				{
					lineModel.removeLine(selectedIndex);
					updateStructureList();
				}
			}
        });
        add(deleteButton);

        
        //JLabel structureTypeText = new JLabel("Structure Type ");
        //add(structureTypeText);
        
        //String[] options = {LineModel.LINES};
        //structureTypeComboBox = new JComboBox(options);
        
        //add(structureTypeComboBox, "wrap");
        
        structuresList = new JList();
        structuresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structuresList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(structuresList);
        listScrollPane.setPreferredSize(new Dimension(10000, 10000));
        
        structuresPopupMenu = new StructuresPopupMenu(this.modelManager, this.pickManager, this);

        add(listScrollPane, "span");

        
        /*
        mapLineButton = new JToggleButton();
        mapLineButton.setToolTipText("Draw a lineament");
        mapLineButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (mapLineButton.isSelected())
				{
					pickManager.setPickMode(PickManager.PickMode.LINE_DRAW);
					mapCircleButton.setSelected(false);
				}
				else
				{
					pickManager.setPickMode(PickManager.PickMode.DEFAULT);
				}
			}
        });
        //URL imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/point.png");
        //mapLineButton.setIcon(new ImageIcon(imageURL));
        mapLineButton.setText("Draw Line");

        add(mapLineButton);

        mapCircleButton = new JToggleButton();
        mapCircleButton.setToolTipText("Draw a circle");
        mapCircleButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (mapCircleButton.isSelected())
				{
					pickManager.setPickMode(PickManager.PickMode.CIRCLE_MAPPER);
					mapLineButton.setSelected(false);
				}
				else
				{
					pickManager.setPickMode(PickManager.PickMode.DEFAULT);
				}
			}
        });
        //imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/rectangle.png");
        //mapCircleButton.setIcon(new ImageIcon(imageURL));
        mapCircleButton.setText("Draw Circle");

        add(mapCircleButton);

        //stopDrawingButton = new JButton();
        //stopDrawingButton.setToolTipText("Stop Drawing a line or circle and return to default mouse interaction");
        //stopDrawingButton.addActionListener(new ActionListener()
        //{
		//	public void actionPerformed(ActionEvent e) 
		//	{
		//		pickManager.setPickMode(PickManager.PickMode.DEFAULT);
		//	}
        //});
        //stopDrawingButton.setText("Stop Drawing");

        //add(stopDrawingButton, "wrap");
         */
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
        else if (source == this.saveStructuresButton)
        {
        	if (structuresFile == null)
        	{
            	structuresFile = AnyFileChooser.showSaveDialog(this, "Select File");
            	if (structuresFile != null)
            		this.structuresFileTextField.setText(structuresFile.getAbsolutePath());

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
    }
	
	private void updateStructureList()
	{
		//String item = (String)structureTypeComboBox.getSelectedItem();
		//if (LineModel.LINES.equals(item))
		{
			LineModel lineModel = 
				((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).getLineModel();
			
			int numLines = lineModel.getNumberOfLines();
			
	    	String[] formattedResults = new String[numLines];

			for (int i=0; i<numLines; ++i)
			{
				LineModel.Line line = lineModel.getLine(i);
	    		formattedResults[i] = new String(
	    				"Id: " + line.id + ", Number of vertices: " + line.controlPointIds.size());
			}
			
			structuresList.setListData(formattedResults);
			
			if (lineModel.getSelectedLineIndex() >= 0)
				structuresList.setSelectedIndex(lineModel.getSelectedLineIndex());
		}
	}
}
