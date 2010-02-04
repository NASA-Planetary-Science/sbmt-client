package edu.jhuapl.near.gui;

import javax.swing.*;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.Properties;
import net.miginfocom.swing.MigLayout;

public class StructureMapperControlPanel extends JPanel implements 
	ItemListener, 
	ActionListener, 
	PropertyChangeListener 
{
    private ModelManager modelManager;
    private PickManager pickManager;
    private JButton loadStructuresButton;
    private JLabel structuresFileTextField;
    private JToggleButton mapLineButton;
    private JToggleButton mapCircleButton;
    private JButton saveStructuresButton;
    private JButton saveAsStructuresButton;
    //private JButton stopDrawingButton;
    private File structuresFile;
    
    public StructureMapperControlPanel(ModelManager modelManager, final PickManager pickManager) 
    {
		this.modelManager = modelManager;
		this.pickManager = pickManager;
		
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
        
        mapLineButton = new JToggleButton();
        mapLineButton.setToolTipText("Draw a lineament");
        mapLineButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (mapLineButton.isSelected())
				{
					pickManager.setPickMode(PickManager.PickMode.LINEAMENT_MAPPER);
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
	}

}
