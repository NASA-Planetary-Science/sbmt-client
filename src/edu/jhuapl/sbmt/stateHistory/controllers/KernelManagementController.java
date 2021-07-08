package edu.jhuapl.sbmt.stateHistory.controllers;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.sbmt.stateHistory.model.KernelManagementModel;
import edu.jhuapl.sbmt.stateHistory.ui.state.KernelManagementPanel;

public class KernelManagementController
{
	private KernelManagementPanel view;
	private KernelManagementModel model;

	public KernelManagementController(File loadedKernelsDirectory)
	{
		try
		{
			model = new KernelManagementModel(loadedKernelsDirectory.getAbsolutePath());
			view = new KernelManagementPanel();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public KernelManagementPanel getView()
	{
		return view;
	}

}
