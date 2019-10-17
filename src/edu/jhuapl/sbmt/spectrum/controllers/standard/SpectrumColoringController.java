package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.ProgressMonitor;

import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectraColoringProgressListener;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.ui.color.SpectrumColoringPanel;

/**
 * Controller for handling the Spectrum Coloring UI.  Brings in the spectrum model, collection, and sets up connections with the panel elements and the model actions
 * @author steelrj1
 *
 */
public class SpectrumColoringController
{
    private SpectrumColoringPanel panel;
    private BaseSpectrumSearchModel model;
    private SpectraCollection collection;
    private ProgressMonitor progressMonitor;

    public SpectrumColoringController(BaseSpectrumSearchModel model, SpectraCollection collection)
    {
        this.panel = new SpectrumColoringPanel(model);
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

        model.getColoringModel().addColoringChangedListener(new SpectrumColoringChangedListener()
        {
            @Override
            public void coloringChanged()
            {
            	if (!model.getColoringModel().getSpectrumColoringStyle().equals(panel.getColoringComboBox().getSelectedItem()))
            		panel.getColoringComboBox().setSelectedItem(model.getColoringModel().getSpectrumColoringStyle());
                collection.setChannelColoring(model.getColoringModel().getChannels(), model.getColoringModel().getMins(), model.getColoringModel().getMaxs(), model.getInstrument());
            }
        });

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
    	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
		        JComboBox<SpectrumColoringStyle> box = (JComboBox<SpectrumColoringStyle>)evt.getSource();
		        SpectrumColoringStyle coloringStyle = (SpectrumColoringStyle)box.getSelectedItem();
		        collection.setColoringStyleForInstrument(coloringStyle, model.getInstrument());
		        collection.setColoringStyle(coloringStyle, new SpectraColoringProgressListener()
				{

					@Override
					public void coloringUpdateStarted()
					{
						progressMonitor = new ProgressMonitor(null, "Updating coloring...", "", 0, 100);
						progressMonitor.setProgress(0);
					}

					@Override
					public void coloringUpdateProgressChanged(int percentComplete)
					{
						progressMonitor.setProgress(percentComplete);
					}

					@Override
					public void coloringUpdateEnded()
					{
						progressMonitor.setProgress(100);
					}
				});


		        boolean isEmissionSelected = (coloringStyle == SpectrumColoringStyle.EMISSION_ANGLE);
		        boolean isGreyscaleSelected = (coloringStyle == SpectrumColoringStyle.GREYSCALE);
		        boolean isRGBSelected = (coloringStyle == SpectrumColoringStyle.RGB);
		        panel.getRgbColoringPanel().setVisible(isRGBSelected);
		        panel.getEmissionAngleColoringPanel().setVisible(isEmissionSelected);
		        panel.getGreyscaleColoringPanel().setVisible(isGreyscaleSelected);
		        model.getColoringModel().setSpectrumColoringStyle(coloringStyle);

		        return null;
			}
		};
		task.execute();
    }

    /**
     * Returns the panel components so it can be embedded in a container view
     * @return
     */
    public SpectrumColoringPanel getPanel()
    {
        return panel;
    }
}