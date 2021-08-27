package edu.jhuapl.sbmt.stateHistory.controllers.kernel;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.sbmt.stateHistory.model.kernel.KernelManagementModel;
import edu.jhuapl.sbmt.stateHistory.ui.state.kernel.KernelManagementPanel;

public class KernelManagementController
{
	private KernelManagementPanel view;
	private KernelManagementModel model;

	public KernelManagementController(File loadedKernelsDirectory)
	{
		try
		{
			model = new KernelManagementModel(loadedKernelsDirectory.getAbsolutePath());
			view = new KernelManagementPanel(model);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		view.getDeleteKernelButton().addActionListener(e -> {
			model.getSelectedItems().forEach( kernelSet ->
				{
					model.deleteKernelSet(kernelSet.getKernelDirectory(), true);
				}
			);
		});

		view.getItemEditB().addActionListener(e -> {

		});
	}


	/**
	 * Returns the view associated with this controller
	 * @return
	 */
	public KernelManagementPanel getView()
	{
		try
		{
			model.refreshModel();
			view.setKernelSet(model);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}

}
