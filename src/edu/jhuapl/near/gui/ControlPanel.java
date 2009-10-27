package edu.jhuapl.near.gui;

import java.util.*;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;



public class ControlPanel extends JTabbedPane
{
    private ImageGLWidget viewer;
	private SearchPanel searchPanel;
	private ErosControlPanel erosPanel;
	private MSIControlPanel msiPanel;
	private LineamentControlPanel lineamentPanel;
    
	public ControlPanel(ImageGLWidget viewer)
	{
		this.viewer = viewer;

		searchPanel = new SearchPanel(viewer);
		erosPanel = new ErosControlPanel(viewer);
		msiPanel = new MSIControlPanel(viewer);
		lineamentPanel = new LineamentControlPanel(viewer);
		
		addTab("Search", searchPanel);
		addTab("Eros", erosPanel);
		addTab("MSI", msiPanel);
		addTab("Lineament", lineamentPanel);
	}
}
