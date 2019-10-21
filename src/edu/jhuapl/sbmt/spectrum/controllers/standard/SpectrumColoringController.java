package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.ProgressMonitor;

import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.color.SpectrumColoringModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumCollectionChangedListener;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.ui.color.SpectrumColoringPanel;

/**
 * Controller for handling the Spectrum Coloring UI.  Brings in the spectrum model, collection, and sets up connections with the panel elements and the model actions
 * @author steelrj1
 *
 */
public class SpectrumColoringController<S extends BasicSpectrum>
{
    private SpectrumColoringPanel<S> panel;
    private BaseSpectrumSearchModel<S> model;
    private SpectraCollection<S> collection;
    private ProgressMonitor progressMonitor;
    private SpectrumColoringModel<S> coloringModel;

    public SpectrumColoringController(BaseSpectrumSearchModel<S> model, SpectraCollection<S> collection, double[] rgbMaxvals, int[] rgbIndices)
    {
        this.coloringModel = new SpectrumColoringModel<>();
        coloringModel.setRedMaxVal(rgbMaxvals[0]);
        coloringModel.setGreenMaxVal(rgbMaxvals[1]);
        coloringModel.setBlueMaxVal(rgbMaxvals[2]);
        coloringModel.setRedIndex(rgbIndices[0]);
        coloringModel.setGreenIndex(rgbIndices[1]);
        coloringModel.setBlueIndex(rgbIndices[2]);
        this.panel = new SpectrumColoringPanel<S>(coloringModel, model.getInstrument());
        this.model = model;
        this.collection = collection;
        init();
    }

    /**
     * Sets up the combo boxes, including adding action listeners, and also adds a listenter to coloring model so this can respond to changes
     */
    private void init()
    {
        setColoringComboBox();

        panel.getColoringComboBox().addActionListener(evt -> coloringComboBoxActionPerformed(evt));

        coloringModel.addColoringChangedListener(new SpectrumColoringChangedListener()
        {
            @Override
            public void coloringChanged()
            {
            	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
        		{
        			@Override
        			protected Void doInBackground() throws Exception
        			{
		            	if (!coloringModel.getSpectrumColoringStyle().equals(panel.getColoringComboBox().getSelectedItem()))
		            		panel.getColoringComboBox().setSelectedItem(coloringModel.getSpectrumColoringStyle());
		            	 Set<IBasicSpectrumRenderer<S>> renderers = collection.getSpectra();
		            	 Iterator<IBasicSpectrumRenderer<S>> iterator = renderers.iterator();
		            	 progressMonitor = new ProgressMonitor(null, "Updating coloring...", "", 0, 100);
						 progressMonitor.setProgress(0);
						 int i=0;
						 int numToRender = renderers.size();
		            	 while (iterator.hasNext())
		                 {
		                 	IBasicSpectrumRenderer<S> spectrumRenderer = iterator.next();
		                 	if (spectrumRenderer == null) continue;
		                 	if (spectrumRenderer.getSpectrum().getInstrument() != model.getInstrument()) continue;
		                 	double[] color = coloringModel.getSpectrumColoringForCurrentStyle(spectrumRenderer);
		                 	spectrumRenderer.setColor(color);
		                 	spectrumRenderer.updateChannelColoring();
		                 	progressMonitor.setProgress(((int)(100*(double)i/(double)numToRender)));
		                 	i++;
		                 }
		            	progressMonitor.setProgress(100);
		            	return null;
        			}
	     		};
	     		task.execute();
            }
        });

        collection.addSpectrumCollectionChangedListener(new SpectrumCollectionChangedListener<S>()
		{

			@Override
			public void spectraRendered(IBasicSpectrumRenderer<S> renderer)
			{
				if (renderer.getSpectrum().getInstrument() != model.getInstrument()) return;
				renderer.setColor(coloringModel.getSpectrumColoringForCurrentStyle(renderer));
				renderer.updateChannelColoring();
			}
		});

        panel.getColoringComboBox().setSelectedItem(SpectrumColoringStyle.RGB);


    }

    /**
     * Sets up the coloring style combo box with the available styles
     */
    protected void setColoringComboBox()
    {
        for (SpectrumColoringStyle style : SpectrumColoringStyle.values())
        {
            panel.getColoringComboBox().addItem(style);
        }
    }

    /**
     * On an update of the coloring combo box, kicks off a background process to update the spectrum coloring currently on screen.  This not only keeps the UI responsive, but also
     * updates the spectrum as they get updated, which makes for a nice UX animation
     * @param evt
     */
    private void coloringComboBoxActionPerformed(ActionEvent evt)
    {
    	JComboBox<SpectrumColoringStyle> box = (JComboBox<SpectrumColoringStyle>)evt.getSource();
        SpectrumColoringStyle coloringStyle = (SpectrumColoringStyle)box.getSelectedItem();
        panel.switchToPanelForColoringStyle(coloringStyle);
    	coloringModel.setSpectrumColoringStyle(coloringStyle);
    }

    /**
     * Returns the panel components so it can be embedded in a container view
     * @return
     */
    public SpectrumColoringPanel<S> getPanel()
    {
        return panel;
    }

}