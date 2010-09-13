package edu.jhuapl.near.gui.eros;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;

public class TopoPanel extends JPanel implements ActionListener
{
	private ErosModelManager modelManager;
	private PickManager pickManager;
	private JToggleButton selectRegionButton;
    private JFormattedTextField nameTextField;
    
	public TopoPanel(final ErosModelManager modelManager,
			final PickManager pickManager)
	{
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));

    	this.modelManager = modelManager;
    	this.pickManager = pickManager;

		this.addComponentListener(new ComponentAdapter() 
		{
			public void componentHidden(ComponentEvent e)
			{
		    	selectRegionButton.setSelected(false);
				pickManager.setPickMode(PickMode.DEFAULT);
			}
		});

        JPanel pane = new JPanel();
        pane.setLayout(new MigLayout("wrap 1"));

        JPanel selectRegionPanel = new JPanel();
        //selectRegionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        selectRegionButton = new JToggleButton("Select Region");
        selectRegionButton.setEnabled(true);
        selectRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (selectRegionButton.isSelected())
                    pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
                else
                    pickManager.setPickMode(PickMode.DEFAULT);
            }
        });
        selectRegionPanel.add(selectRegionButton);

        final JButton clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ErosModelManager.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
            }
        });
        selectRegionPanel.add(clearRegionButton);

        final JLabel nameLabel = new JLabel("Name");
        nameTextField = new JFormattedTextField();
        nameTextField.setPreferredSize(new Dimension(125, 24));

        JPanel namePanel = new JPanel();
        namePanel.add(nameLabel);
        namePanel.add(nameTextField);

        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Submit");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);
        pane.add(submitPanel, "align center");

        submitPanel.add(submitButton);

        pane.add(selectRegionPanel, "align center");
        pane.add(namePanel);
        pane.add(submitPanel, "align center");

        add(pane);

	}

	public void actionPerformed(ActionEvent e)
	{
		// Run Bob Gaskell's map maker fortran program
		
		// First get the center point and radius of the selection circle
		double [] centerPoint = null;
		double radius = 0.0;
		
        RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ErosModelManager.CIRCLE_SELECTION);
		SmallBodyModel erosModel = (SmallBodyModel)modelManager.getModel(ErosModelManager.EROS);
		if (selectionModel.getNumberOfStructures() > 0)
		{
			RegularPolygonModel.RegularPolygon region = (RegularPolygonModel.RegularPolygon)selectionModel.getStructure(0);
			
			centerPoint = region.center;
			radius = region.radius;
		}
		else
		{
			JOptionPane.showMessageDialog(this,
					"Please select a region on the asteroid.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			return;
		}
		
		String name = this.nameTextField.getText();
		if (name == null || name.length() == 0)
		{
			JOptionPane.showMessageDialog(this,
					"Please enter a name.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
		
		// Next download the entire map maker suite to the users computer 
		// if it has never been downloaded before.
		// Ask the user beforhand if it's okay to continue.
		
		String executablePath;
		String outputFolder;
		
		// Next run the mapmaker tool
		
		
	}
}
