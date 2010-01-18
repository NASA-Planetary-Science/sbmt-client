package edu.jhuapl.near.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.model.*;
import net.miginfocom.swing.MigLayout;

public class StructureMapperControlPanel extends JPanel implements ItemListener, ActionListener 
{
    private ModelManager modelManager;
    private JButton loadStructuresButton;
    private JLabel structuresFileTextField;
    private JToggleButton mapLineButton;
    private JToggleButton mapCircleButton;
    private JButton saveStructuresButton;
    //private JButton stopDrawingButton;
    private File structuresFile;
    
    public StructureMapperControlPanel(ModelManager modelManager, final PickManager pickManager) 
    {
		this.modelManager = modelManager;

		setLayout(new MigLayout("wrap 2, insets 0"));

        this.loadStructuresButton = new JButton("Load Structures File...");
        this.loadStructuresButton.setEnabled(true);
        this.loadStructuresButton.addActionListener(this);

        this.structuresFileTextField = new JLabel();
        this.structuresFileTextField.setEnabled(true);
        this.structuresFileTextField.setPreferredSize(new java.awt.Dimension(150, 22));
        
        add(this.loadStructuresButton);
        add(this.structuresFileTextField);
        
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

        this.saveStructuresButton= new JButton("Save Structures");
        this.saveStructuresButton.setEnabled(true);
        this.saveStructuresButton.addActionListener(this);

        add(this.saveStructuresButton);
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
        }
        else if (source == this.saveStructuresButton)
        {
        	if (structuresFile == null)
        	{
            	structuresFile = AnyFileChooser.showSaveDialog(this, "Select File");
            	if (structuresFile != null)
            		this.structuresFileTextField.setText(structuresFile.getAbsolutePath());
        		
        	}
        }
    }

}
