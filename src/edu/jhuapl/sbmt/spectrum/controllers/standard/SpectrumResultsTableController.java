package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.ImmutableSet;
import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumSearchResultsListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumStringRenderer;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
import edu.jhuapl.sbmt.spectrum.ui.table.SpectrumResultsTableView;

/**
 * Controller for displaying the Spectrum Search Results table.  Establishes connections between the UI elements and the model methods
 * @author steelrj1
 *
 */
public class SpectrumResultsTableController<S extends BasicSpectrum>
{
    protected SpectrumResultsTableView<S> panel;
    protected BaseSpectrumSearchModel<S> model;
    protected List<S> spectrumRawResults;
    protected BasicSpectrumInstrument instrument;
    protected SpectrumStringRenderer stringRenderer;
    protected SpectraCollection<S> spectrumCollection;
    protected SpectrumPopupMenu spectrumPopupMenu;
    protected SpectrumBoundaryCollection<S> boundaries;
    private SpectrumSearchResultsListener<S> tableResultsChangedListener;
    private ProgressMonitor progressMonitor;


    /**
     * @param instrument			The spectrum instrument
     * @param spectrumCollection	The spectrum collection
     * @param modelManager			The system model manager
     * @param boundaries			The spectrum boundary collection
     * @param model					The spectrum model
     * @param renderer				The system renderer
     * @param infoPanelManager		The system info panel manager
     */
    public SpectrumResultsTableController(BasicSpectrumInstrument instrument, SpectraCollection<S> spectrumCollection, ModelManager modelManager, SpectrumBoundaryCollection<S> boundaries, BaseSpectrumSearchModel<S> model, Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        spectrumPopupMenu = new SpectrumPopupMenu(spectrumCollection, boundaries, modelManager,infoPanelManager, renderer);
        spectrumPopupMenu.setInstrument(instrument);
        this.spectrumCollection = spectrumCollection;

        this.spectrumCollection.setActiveInstrument(instrument);
        panel = new SpectrumResultsTableView<S>(spectrumCollection, boundaries, spectrumPopupMenu);
        panel.setup();
        this.boundaries = boundaries;
        spectrumRawResults = model.getSpectrumRawResults();
        this.model = model;
        this.instrument = instrument;
	    model.setCustomDataFolder(spectrumCollection.getShapeModel().getCustomDataFolder());

        this.tableResultsChangedListener = new SpectrumSearchResultsListener<S>()
        {
            @Override
            public void resultsChanged(List<S> results)
            {
                setSpectrumResults(results);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                panel.getResultsLabel().setText(count + " Spectra Found");
            }

			@Override
			public void resultsRemoved()
			{
				spectrumCollection.removeAllSpectraForInstrument(instrument);
		        spectrumCollection.deselectAll();
			}
        };

        this.spectrumCollection.addPropertyChangeListener(evt -> panel.repaint());
        this.boundaries.addPropertyChangeListener(evt -> panel.repaint());
    }


    /**
     * Helper method to re-add the listener if it needs to be temporarily disabled
     */
    public void addResultListener()
    {
    	model.addResultsChangedListener(tableResultsChangedListener);
    }

    /**
     * Helper method to remove the listener if it needs to be temporarily disabled
     */
    public void removeResultListener()
    {
    	model.removeResultsChangedListener(tableResultsChangedListener);
    }

    /**
     * Does on demand setup of the widgets for the UI
     */
    public void setSpectrumResultsPanel()
    {
        setupWidgets();
    }

    /**
     * Sets text and action listeners of the UI widgets
     */
    protected void setupWidgets()
    {
        // setup Image Results Table view components
        panel.getNumberOfBoundariesComboBox().setModel(new DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        panel.getNumberOfBoundariesComboBox().addActionListener(evt -> numberOfBoundariesComboBoxActionPerformed());
        model.setNumberOfBoundariesToShow(10);

        panel.getPrevButton().setText("<");
        panel.getPrevButton().addActionListener(evt -> prevButtonActionPerformed());

        panel.getNextButton().setText(">");
        panel.getNextButton().addActionListener(evt -> nextButtonActionPerformed());

        panel.getShowSpectraButton().setText("Show Spectra");
        panel.getShowSpectraButton().addActionListener(evt -> showSpectraButtonActionPerformed());

        panel.getShowBoundariesButton().setText("Show Boundaries");
        panel.getShowBoundariesButton().addActionListener(evt -> showBoundariesButtonActionPerformed());

        panel.getRemoveBoundariesButton().setText("Hide Boundaries");
        panel.getRemoveBoundariesButton().addActionListener(evt -> removeBoundariesButtonActionPerformed());

        panel.getRemoveSpectraButton().setText("Hide Spectra");
        panel.getRemoveSpectraButton().addActionListener(evt -> removeFootprintsButtonActionPerformed());

        panel.getSaveSpectraListButton().setText("Save List...");
        panel.getSaveSpectraListButton().addActionListener(evt -> saveSpectrumListButtonActionPerformed());

        panel.getSaveSelectedSpectraListButton().setText("Save Selected List...");
        panel.getSaveSelectedSpectraListButton().addActionListener(evt -> saveSelectedSpectrumListButtonActionPerformed());

        panel.getLoadSpectraListButton().setText("Load List...");
        panel.getLoadSpectraListButton().addActionListener(evt -> loadSpectrumListButtonActionPerformed());

		panel.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    model.setSelectedSpectraIndices(panel.getResultList().getSelectedRows());
                }
            }
        });
    }

    /**
     * Handles action for Load Spectrum button. On an exception, displays an error dialog to the user
     */
    protected void loadSpectrumListButtonActionPerformed()
    {
    	 try
         {
    		 File file = CustomFileChooser.showOpenDialog(null, "Select File");
    	     if (file == null) return;
    	     model.clearSpectraFromDisplay();
             model.loadSpectrumListFromFile(file);
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                     "There was an error reading the file.  See the console for details",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);

             e.printStackTrace();
         }
    }

    /**
     * Handles action for Save Spectrum button. On an exception, displays an error dialog to the user
     */
    protected void saveSpectrumListButtonActionPerformed()
    {
        try
        {
        	File file = CustomFileChooser.showSaveDialog(panel, "Select File", "spectrumlist.txt");
        	if (file == null) return;
            model.saveSpectrumListToFile(file);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error saving the file.  See the console for details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
    }

    /**
     * Handles action for Save Selected Spectrum button. On an exception, displays an error dialog to the user
     */
    protected void saveSelectedSpectrumListButtonActionPerformed()
    {
        try
        {
        	File file = CustomFileChooser.showSaveDialog(panel, "Select File", "spectrumlist.txt");
        	if (file == null) return;
            model.saveSelectedSpectrumListToFile(file, panel.getResultList().getSelectedRows());
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error saving the file.  See the console for details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
    }

    /**
     * Returns the panel component so it can be displayed in a container view
     * @return
     */
    public SpectrumResultsTableView<S> getPanel()
    {
        return panel;
    }

    /**
     * Handles action for the "Previous" button, which jumps to the previous set of spectra being displayed
     */
    private void prevButtonActionPerformed()
    {
    	boundaries.removeAllBoundaries();
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        spectrumCollection.deselectAll();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(model.getNumberOfBoundariesToShow());
            }
            else
            {
                resultIntervalCurrentlyShown = new IdPair(panel.getResultList().getModel().getRowCount() - model.getNumberOfBoundariesToShow(), panel.getResultList().getModel().getRowCount());
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(panel.getResultList().getModel().getRowCount() - model.getNumberOfBoundariesToShow(), panel.getResultList().getModel().getRowCount());
        }
        showSpectrumBoundaries(resultIntervalCurrentlyShown);
        model.setResultIntervalCurrentlyShown(resultIntervalCurrentlyShown);
    }

    /**
     * Handles action for the "next" button, which jumps to the next set of spectra being displayed
     */
    private void nextButtonActionPerformed()
    {
    	boundaries.removeAllBoundaries();
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        spectrumCollection.deselectAll();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < panel.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(model.getNumberOfBoundariesToShow());
            }
            else
            {
                resultIntervalCurrentlyShown = new IdPair(0, model.getNumberOfBoundariesToShow());
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, model.getNumberOfBoundariesToShow());
        }
        showSpectrumBoundaries(resultIntervalCurrentlyShown);
        model.setResultIntervalCurrentlyShown(resultIntervalCurrentlyShown);
    }

    /**
     * Handles the action of the "Show Spectra" button.  The adding of the spectra happens on a background thread and a progress
     * monitor is presented to the user for a better UX.
     */
    private void showSpectraButtonActionPerformed()
    {
    	progressMonitor = new ProgressMonitor(null, "Showing Footprints...", "", 0, 100);
		progressMonitor.setProgress(0);
    	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
		    	ImmutableSet<S> selectedItems = spectrumCollection.getSelectedItems();
		    	int i=0;
		    	int firstIndex = -1, lastIndex = -1;
		    	for (S spectrum: selectedItems)
		    	{
		    		if (firstIndex == -1) firstIndex = spectrumCollection.getAllItems().asList().indexOf(spectrum);
		    		spectrumCollection.addSpectrum(spectrum, false);
		            boundaries.addBoundary(spectrum);
		            progressMonitor.setProgress((int)(100*(double)i/(double)selectedItems.size()));
		            lastIndex = spectrumCollection.getAllItems().asList().indexOf(spectrum);
		            i++;
		        }
		    	progressMonitor.setProgress(100);
		        model.setResultIntervalCurrentlyShown(new IdPair(firstIndex, lastIndex));
		        return null;
			}
		};
		task.execute();
    }

    /**
     * Handles the action of the "Show Boundaries" button.  The adding of the boundaries happens on a background thread and a progress
     * monitor is presented to the user for a better UX.
     */
    private void showBoundariesButtonActionPerformed()
    {
    	progressMonitor = new ProgressMonitor(null, "Showing Boundaries...", "", 0, 100);
		progressMonitor.setProgress(0);
    	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				ImmutableSet<S> selectedItems = spectrumCollection.getSelectedItems();
				int i=0;
		    	for (BasicSpectrum spectrum: selectedItems)
		    	{
		            boundaries.addBoundary(spectrum);
		            progressMonitor.setProgress((int)(100*(double)i/(double)selectedItems.size()));
		            i++;
		        }
		    	progressMonitor.setProgress(100);
		        model.setResultIntervalCurrentlyShown(null);
		        return null;
			}
		};
		task.execute();

    }

    /**
     * Handles the action of the "Remove Spectra" button.  The removing of the spectra happens on a background thread and a progress
     * monitor is presented to the user for a better UX.
     */
    private void removeFootprintsButtonActionPerformed()
    {
    	spectrumCollection.getSelectedItems().forEach(spectrum ->
    	{
    		spectrumCollection.removeSpectrum(spectrum);
            boundaries.removeBoundary(spectrum);
    	});
        model.setResultIntervalCurrentlyShown(null);
    }

    /**
     * Handles the action of the "Remove Spectra" button.  The removing of the spectra happens on a background thread and a progress
     * monitor is presented to the user for a better UX.
     */
    protected void removeFootprintsForAllInstrumentsButtonActionPerformed()
    {
    	spectrumCollection.getSelectedItems().forEach(spectrum ->
    	{
    		spectrumCollection.removeSpectrum(spectrum);
            boundaries.removeBoundary(spectrum);
    	});

        model.setResultIntervalCurrentlyShown(null);
    }

    /**
     * Handles the action of the "Remove Boundaries" button.  The removing of the boundaries happens on a background thread and a progress
     * monitor is presented to the user for a better UX.
     */
    private void removeBoundariesButtonActionPerformed()
    {
		spectrumCollection.getSelectedItems().forEach(spectrum -> boundaries.removeBoundary(spectrum));
	    model.setResultIntervalCurrentlyShown(null);
    }

    /**
     * Handles the number of boundaries combo box action.  Changes the number of displayed spectra when the prev/next buttons are pressed
     */
    private void numberOfBoundariesComboBoxActionPerformed() {
        IdPair shown = model.getResultIntervalCurrentlyShown();
        if (shown == null) return;
        model.setNumberOfBoundariesToShow(Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem()));
        // Only update if there's been a change in what is selected
        int newMaxId = shown.id1 + model.getNumberOfBoundariesToShow();
        if (newMaxId == shown.id2) return;
        shown.id2 = newMaxId;
        showSpectrumBoundaries(shown);
    }

    /**
     * Helper method to show the request set of spectra boundaries defined by IdPair (ranging from idPair.id1 to idPair.id2)
     *
     * Can display an error dialog on an exception when loading in the spectra
     *
     * @param idPair
     */
    private void showSpectrumBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;
        boundaries.removeAllBoundaries();
        progressMonitor = new ProgressMonitor(null, "Showing Footprints...", "", 0, 100);
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToDecideToPopup(0);
		progressMonitor.setMillisToPopup(0);
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				for (int i=startId; i<endId; ++i)
		        {
		        	if (progressMonitor.isCanceled()) break;
		            if (i < 0)
		                continue;
		            else if(i >= spectrumRawResults.size())
		                break;

		            try
		            {
		                S currentSpectrum = spectrumRawResults.get(i);
		                progressMonitor.setProgress((int)(100*(double)(i-startId)/(double)(endId - startId)));
		                spectrumCollection.addSpectrum(currentSpectrum, currentSpectrum.isCustomSpectra);
		                boundaries.addBoundary(currentSpectrum);
		            }
		            catch (Exception e1) {
		                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
		                        "There was an error mapping the boundary.",
		                        "Error",
		                        JOptionPane.ERROR_MESSAGE);

		                e1.printStackTrace();
		                break;
		            }
		        }
		        progressMonitor.setProgress(100);
		        return null;
			}
		};
		task.execute();
    }

    /**
     * Sets the list of spectra to display on the table
     * @param results
     */
    public void setSpectrumResults(List<S> results)
    {
        panel.getResultsLabel().setText(results.size() + " spectra found");
        spectrumRawResults = results; //.parallelStream().filter(spec -> spec.getInstrument() == instrument).collect(Collectors.toList());
        spectrumCollection.setAllItems(spectrumRawResults);
        showSpectrumBoundaries(new IdPair(0, Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem())));
    }
}



//@Override
//public void keyTyped(KeyEvent e)
//{
//
//}

//@Override
//public void keyPressed(KeyEvent e)
//{
//  // 2018-02-08 JP. Turn this method into a no-op for now. The reason is that
//  // currently all listeners respond to all key strokes, and VTK keyboard events
//  // do not have a means to determine their source, so there is no way for listeners
//  // to be more selective. The result is, e.g., if one types "s", statistics windows show
//  // up even if we're not looking at a spectrum tab.
//  //
//  // Leave it in the code (don't comment it out) so Eclipse can find references to this,
//  // and so that we don't unknowingly break this code.
//  boolean disableKeyResponses = true;
//  if (disableKeyResponses) return;
//  ModelManager modelManager = model.getModelManager();
//  Renderer renderer = model.getRenderer();
//
//  if (e.getKeyChar()=='a')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      renderer.removeKeyListener(this);
//      model.toggleSelectAll();
//      renderer.addKeyListener(this);
//  }
//  else if (e.getKeyChar()=='s')
//  {
//      view.getSpectrumPopupMenu().showStatisticsWindow();
//  }
//  else if (e.getKeyChar()=='i' || e.getKeyChar()=='v')    // 'i' sets the lighting direction based on time of a single NIS spectrum, and 'v' looks from just above the footprint toward the sun
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      List<Spectrum> selection=model.getSelectedSpectra();
//      if (selection.size()!=1)
//      {
//          JOptionPane.showMessageDialog(panel, "Please select only one spectrum to specify lighting or viewpoint");
//          return;
//      }
//      Spectrum spectrum=selection.get(0);
//      renderer.setLighting(LightingType.FIXEDLIGHT);
//      Path fullPath=Paths.get(spectrum.getFullPath());
//      Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
//      //Vector3D toSunVector=getToSunUnitVector(relativePath.toString());
//      renderer.setFixedLightDirection(spectrum.getToSunUnitVector()); // the fixed light direction points to the light
//      if (e.getKeyChar()=='v')
//      {
//          Vector3D footprintCenter=new Vector3D(spectrum.getShiftedFootprint().GetCenter());
//          SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//          //
//          vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
//          normalsFilter.SetInputData(spectrum.getUnshiftedFootprint());
//          normalsFilter.SetComputeCellNormals(0);
//          normalsFilter.SetComputePointNormals(1);
//          normalsFilter.SplittingOff();
//          normalsFilter.Update();
//          Vector3D upVector=new Vector3D(PolyDataUtil.computePolyDataNormal(normalsFilter.GetOutput())).normalize();  // TODO: fix this for degenerate cases, i.e. normal parallel to to-sun direction
//          double viewHeight=0.01; // km
//          Vector3D cameraPosition=footprintCenter.add(upVector.scalarMultiply(viewHeight));
//          double lookLength=footprintCenter.subtract(cameraPosition).getNorm();
//          Vector3D focalPoint=cameraPosition.add((new Vector3D(spectrum.getToSunUnitVector())).scalarMultiply(lookLength));
//          //
//          renderer.setCameraOrientation(cameraPosition.toArray(), focalPoint.toArray(), renderer.getRenderWindowPanel().getActiveCamera().GetViewUp(), renderer.getCameraViewAngle());
//      }
//  }
//  else if (e.getKeyChar()=='h')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      model.decreaseFootprintSeparation(0.001);
//  }
//  else if (e.getKeyChar()=='H')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      model.increaseFootprintSeparation(0.001);
//  }
//  else if (e.getKeyChar()=='+')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//      model.setOffset(model.getOffset()+body.getBoundingBoxDiagonalLength()/50);
//  }
//  else if (e.getKeyChar()=='-')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//      model.setOffset(model.getOffset()-body.getBoundingBoxDiagonalLength()/50);
//  }
//}
//
//@Override
//public void keyReleased(KeyEvent e)
//{
//  // TODO Auto-generated method stub
//
//}
