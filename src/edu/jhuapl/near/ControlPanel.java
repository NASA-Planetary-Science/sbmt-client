package edu.jhuapl.near;

import java.util.*;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


public class ControlPanel extends JPanel implements ListSelectionListener, ItemListener
{
    private ImageGLWidget viewer;
	
    private JList list;
    private DefaultListModel listModel;

    private static final String addString = "Add...";
    private static final String deleteString = "Delete";
    private static final String upString = "Up";
    private static final String downString = "Down";
    private JButton addButton;
    private JButton deleteButton;
    private JButton upButton;
    private JButton downButton;
    private ArrayList<File> fileList = new ArrayList<File>();

    private JCheckBox modelCheckBox;
    private JCheckBox lineamentCheckBox;
    private ContrastChanger contrastChanger;
    
    class AddListener implements ActionListener 
    {
    	JPanel panel;
    	AddListener(JPanel panel)
    	{
    		this.panel = panel;
    	}
        public void actionPerformed(ActionEvent e) 
        {
        	File file = FITFileChooser.showOpenDialog(panel, "Select FIT File");
        	System.out.println(file);
        	
        	if (file != null)
        	{
            	listModel.addElement(file.getName());
            	fileList.add(file);
            	
            	int index = listModel.getSize() - 1;
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);

                updateButtons();
                //updateRenderedImages();
                try {
					viewer.addImage(file);
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
                updateContrastChanger();
        	}
        }
    }
    
    class DeleteListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
            int index = list.getSelectedIndex();
            File file = fileList.get(index);
            listModel.remove(index);
            fileList.remove(index);
            
            int size = listModel.getSize();

            if (size > 0) 
            { 
                //Select an index.
                if (index == size) 
                {
                    //removed item in last position
                    index--;
                }

                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }

            updateButtons();
            //updateRenderedImages();
            viewer.removeImage(file);
            updateContrastChanger();
        }
    }

    class UpListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
            int index = list.getSelectedIndex();

            if (index > 0)
            {
            	File prev = fileList.get(index-1);
            	
            	listModel.remove(index-1);
            	listModel.add(index, prev.getName());
            	
            	fileList.remove(index-1);
            	fileList.add(index, prev);
            	
                list.setSelectedIndex(index-1);
                list.ensureIndexIsVisible(index-1);

                updateButtons();
            	updateRenderedImages();
                updateContrastChanger();
            }
        }
    }
    
    class DownListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
            int index = list.getSelectedIndex();
            int size = listModel.getSize();

            if (index < size-1)
            {
            	File curr = fileList.get(index);
            	
            	listModel.remove(index);
            	listModel.add(index+1, curr.getName());
            	
            	fileList.remove(index);
            	fileList.add(index+1, curr);
            	
                list.setSelectedIndex(index+1);
                list.ensureIndexIsVisible(index+1);

                updateButtons();
            	updateRenderedImages();
                updateContrastChanger();
            }
        }
    }
    
	public ControlPanel(ImageGLWidget viewer)
	{
		super(new BorderLayout());

		this.viewer = viewer;

        listModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(list);

        addButton = new JButton(addString);
        AddListener addListener = new AddListener(this);
        addButton.setActionCommand(addString);
        addButton.addActionListener(addListener);
        addButton.setEnabled(true);

        deleteButton = new JButton(deleteString);
        DeleteListener deleteListener = new DeleteListener();
        deleteButton.setActionCommand(deleteString);
        deleteButton.addActionListener(deleteListener);
        deleteButton.setEnabled(false);

        upButton = new JButton(upString);
        UpListener upListener = new UpListener();
        upButton.setActionCommand(upString);
        upButton.addActionListener(upListener);
        upButton.setEnabled(false);

        downButton = new JButton(downString);
        DownListener downListener = new DownListener();
        downButton.setActionCommand(downString);
        downButton.addActionListener(downListener);
        downButton.setEnabled(false);


        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(deleteButton);
        //buttonPane.add(Box.createHorizontalStrut(5));
        //buttonPane.add(upButton);
        //buttonPane.add(Box.createHorizontalStrut(5));
        //buttonPane.add(downButton);

        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(buttonPane, BorderLayout.PAGE_START);
        add(listScrollPane, BorderLayout.CENTER);

        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BorderLayout());
        bottomPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        contrastChanger = new ContrastChanger(viewer);
        
        LineamentRadialOffsetChanger radialChanger = new LineamentRadialOffsetChanger(viewer.getLineamentModel());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        modelCheckBox = new JCheckBox();
        modelCheckBox.setText("Show Model");
        modelCheckBox.setSelected(true);
        modelCheckBox.addItemListener(this);
        
        lineamentCheckBox = new JCheckBox();
        lineamentCheckBox.setText("Show Lineaments");
        lineamentCheckBox.setSelected(true);
        lineamentCheckBox.addItemListener(this);

        panel.add(modelCheckBox);
        panel.add(lineamentCheckBox);
        
        bottomPane.add(contrastChanger, BorderLayout.PAGE_START);
        bottomPane.add(radialChanger, BorderLayout.CENTER);
        bottomPane.add(panel, BorderLayout.PAGE_END);
        
        add(bottomPane, BorderLayout.PAGE_END);
	}

	public void itemStateChanged(ItemEvent e) 
	{
		if (e.getItemSelectable() == this.modelCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
				viewer.showModel(false);
			else
				viewer.showModel(true);
		}
		else if (e.getItemSelectable() == this.lineamentCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
				viewer.showLineaments(false);
			else
				viewer.showLineaments(true);
		}
	}
	
    //This method is required by ListSelectionListener.
    public void valueChanged(ListSelectionEvent e) 
    {
        if (e.getValueIsAdjusting() == false) 
        {
        	updateButtons();
        	updateContrastChanger();
        }
    }
    
    private void updateContrastChanger()
    {
        int index = list.getSelectedIndex();
        if (index >= 0)
        {
        	File file = fileList.get(index);
        	NearImage image = viewer.getImage(file);
        	contrastChanger.setNearImage(image);
        }
        else
        {
        	contrastChanger.setNearImage(null);
        }
    }

    private void updateRenderedImages()
    {
    	/*
		try {
			viewer.setImages(fileList);
		} catch (FitsException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		*/
    }
    
    private void updateButtons()
    {
        int index = list.getSelectedIndex();
        int size = listModel.getSize();

        if (size == 0 || index == -1)
        {
        	deleteButton.setEnabled(false);
    		upButton.setEnabled(false);
    		downButton.setEnabled(false);
        }
        else
        {
        	deleteButton.setEnabled(true);

        	if (index == 0)
        	{
        		upButton.setEnabled(false);
        		if (size > 1)
        			downButton.setEnabled(true);
        		else
        			downButton.setEnabled(false);
        	}
        	else if (index == size-1)
        	{
        		if (size > 1)
        			upButton.setEnabled(true);
        		else
        			upButton.setEnabled(false);
        		downButton.setEnabled(false);
        	}
        	else
        	{
        		upButton.setEnabled(true);
        		downButton.setEnabled(true);
        	}
        }
    }
}
