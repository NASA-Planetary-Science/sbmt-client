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
    private JButton mapLineButton;
    private JButton mapCircleButton;
    private JButton saveStructuresButton;
    private File structuresFile;
    
    public StructureMapperControlPanel(ModelManager modelManager, final PickManager pickManager) 
    {
		this.modelManager = modelManager;

		setLayout(new MigLayout("wrap 2, insets 0"));

        this.loadStructuresButton = new JButton("Load Structure File...");
        this.loadStructuresButton.setEnabled(true);
        this.loadStructuresButton.addActionListener(this);

        this.structuresFileTextField = new JLabel();
        this.structuresFileTextField.setEnabled(true);
        this.structuresFileTextField.setPreferredSize(new java.awt.Dimension(150, 22));
        
        add(this.loadStructuresButton);
        add(this.structuresFileTextField);
        
        mapLineButton.setToolTipText("Draw a lineament");
        mapLineButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				pickManager.setPickMode(PickManager.PickMode.LINEAMENT_MAPPER);
			}
        });
        URL imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/point.png");
        mapLineButton.setIcon(new ImageIcon(imageURL));

        add(mapLineButton);

        mapCircleButton = new JButton();
        mapCircleButton.setToolTipText("Draw a circle");
        mapCircleButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				pickManager.setPickMode(PickManager.PickMode.CIRCLE_MAPPER);
			}
        });
        imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/rectangle.png");
        mapCircleButton.setIcon(new ImageIcon(imageURL));

        add(mapCircleButton);

        this.saveStructuresButton= new JButton("Load Structure File...");
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
