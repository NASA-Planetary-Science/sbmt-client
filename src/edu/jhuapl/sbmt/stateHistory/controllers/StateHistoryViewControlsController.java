package edu.jhuapl.sbmt.stateHistory.controllers;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import edu.cmu.relativelayout.Binding;
import edu.cmu.relativelayout.BindingFactory;
import edu.cmu.relativelayout.Direction;
import edu.cmu.relativelayout.Edge;
import edu.cmu.relativelayout.RelativeConstraints;
import edu.cmu.relativelayout.RelativeLayout;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryColoringOptionsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryDisplayItemsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryFOVController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryViewOptionsController;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.item.ItemEventType;

/**
 * Controller that governs the "View Controls" panel in the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsController
{

	/**
	 * The controller that governs the "Display Items" sub panel
	 */
	private StateHistoryDisplayItemsController displayItemsControls;

	/**
	 * The controller that governs the "Display Items" sub panel
	 */
	private StateHistoryFOVController fovControls;

	/**
	 * The controller that governs the "View Options" sub panel
	 */
	private StateHistoryViewOptionsController viewControls;

	private StateHistoryColoringOptionsController coloringControls;

	private StateHistoryRendererManager rendererManager;

	private JPanel view = new JPanel();

	/**
	 * Constructor.  Takes in a <pre>historyModel</pre> and <pre>renderer</pre> object to
	 * help populate the user interface
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryViewControlsController(StateHistoryModel historyModel, StateHistoryRendererManager rendererManager, ColoringDataManager coloringDataManager)
	{
		this.rendererManager = rendererManager;
		initUI(historyModel, coloringDataManager);
	}

	/**
	 * Initializes the user interface using the given <pre>historyModel</pre> and <pre>renderer</pre>
	 */
	private void initUI(StateHistoryModel historyModel, ColoringDataManager coloringDataManager)
	{
        viewControls = new StateHistoryViewOptionsController(rendererManager);
        displayItemsControls = new StateHistoryDisplayItemsController(rendererManager);
        fovControls = new StateHistoryFOVController(rendererManager, coloringDataManager);
        coloringControls = new StateHistoryColoringOptionsController(rendererManager);

        //this is a cross panel listener action, so set it up here, above the 3 controllers
        viewControls.getView().getViewOptions().addActionListener(e ->
        {
        	// toggle the ability to show the spacecraft depending on what
			// mode we're in
        	RendererLookDirection selectedView = (RendererLookDirection) viewControls.getView().getViewOptions().getSelectedItem();
			boolean scSelected = (selectedView == RendererLookDirection.SPACECRAFT);
        });

        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsChanged) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (rendererManager.getNumMappedTrajectories() == 0) fovControls.getView().clearTable();
					fovControls.getView().repaint();
					fovControls.getView().validate();
					view.repaint();
					view.validate();
				}
			});

		});
        renderView();
	}

	public void setEnabled(boolean enabled)
	{
		viewControls.getView().setEnabled(enabled);
		displayItemsControls.getView().setEnabled(enabled);
		fovControls.getView().setEnabled(enabled);
		coloringControls.getView().setEnabled(enabled);
	}

	private void renderView()
	{
		view.setLayout(new RelativeLayout());
		BindingFactory factory = new BindingFactory();
		factory.setLeftMargin(0);
		factory.setRightMargin(0);
        view.add(displayItemsControls.getView(), new RelativeConstraints(factory.leftEdge(), factory.leftOf(coloringControls.getView()),
        																 factory.topEdge(), new Binding(Edge.BOTTOM, 200, Direction.BELOW, Edge.TOP, view)));
    	view.add(coloringControls.getView(), new RelativeConstraints(factory.rightEdge(), factory.topEdge(), new Binding(Edge.LEFT, 275, Direction.LEFT, Edge.RIGHT, view),
    																 new Binding(Edge.BOTTOM, 200, Direction.BELOW, Edge.TOP, view)));
        view.add(viewControls.getView(), new RelativeConstraints(factory.leftEdge(), factory.rightEdge(), factory.below(displayItemsControls.getView())));

        JScrollPane fovScroll = new JScrollPane();
        fovScroll.setViewportView(fovControls.getView());
        view.add(fovScroll, new RelativeConstraints(factory.leftEdge(), factory.rightEdge(), factory.bottomEdge(), factory.below(viewControls.getView())));
//        														new Binding(Edge.BOTTOM, 250, Direction.BELOW, Edge.BOTTOM, viewControls.getView()), factory.below(viewControls.getView())));
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public JPanel getView()
	{
		return view;
	}
}