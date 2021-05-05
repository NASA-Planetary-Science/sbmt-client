package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.border.TitledBorder;

import org.jfree.ui.FontChooserDialog;

import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.displayItems.table.DisplayOptionsTableView;

/**
 * Controllers that governs the view which contains controls for which items to display in the renderer
 * @author steelrj1
 *
 */
public class StateHistoryDisplayItemsController
{
	/**
	 * The view that this controller governs
	 */
	DisplayOptionsTableView view;

	/**
	 * Constructor.  Sets state properties and initializes view control panel
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryDisplayItemsController(StateHistoryRendererManager rendererManager)
	{
		view = new DisplayOptionsTableView(rendererManager);
		view.setup();
		view.setBorder(new TitledBorder(null, "Display Items", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		view.getFontButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				//show a font dialog
				JFrame fontFrame = new JFrame();
	        	FontChooserDialog fontDialog = new FontChooserDialog(fontFrame, "Choose Font", true, rendererManager.getSpacecraftTextFont());
	        	fontDialog.setSize(400, 300);
	        	fontDialog.setVisible(true);
	        	if (fontDialog.getSelectedFont() != null)
	        	{
	        		Font font = fontDialog.getSelectedFont();
	        		rendererManager.setDistanceTextFont(font);
	        		rendererManager.setEarthTextFont(font);
	        		rendererManager.setSunTextFont(font);
	        		rendererManager.setSpacecraftTextFont(font);
	        		rendererManager.setSpacecraftLabelTextFont(font);
	        	}
			}
		});

	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public DisplayOptionsTableView getView()
	{
		return view;
	}
}
