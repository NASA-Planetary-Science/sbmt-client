package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.ProgressMonitor;

import com.jidesoft.utils.SwingWorker;

import vtk.vtkFunctionParser;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectraColoringProgressListener;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.ui.color.SpectrumColoringPanel;
import edu.jhuapl.sbmt.spectrum.ui.math.SpectrumMathPanel;

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
        this.panel = new SpectrumColoringPanel();
        this.model = model;
        this.collection = collection;
        init();
    }

    /**
     * Sets up the combo boxes, including adding action listeners, and also adds a listenter to coloring model so this can respond to changes
     */
    private void init()
    {
        setupComboBoxes();
        setColoringComboBox();

        panel.getColoringComboBox().addActionListener(evt -> coloringComboBoxActionPerformed(evt));
        panel.getRedComboBox().addActionListener(evt -> redComboBoxActionPerformed(evt));
        panel.getRedMaxSpinner().addChangeListener(evt -> redMaxSpinnerStateChanged());
        panel.getRedMinSpinner().addChangeListener(evt -> redMinSpinnerStateChanged());
        panel.getGreenComboBox().addActionListener(evt -> greenComboBoxActionPerformed(evt));
        panel.getGreenMaxSpinner().addChangeListener(evt -> greenMaxSpinnerStateChanged());
        panel.getGreenMinSpinner().addChangeListener(evt -> greenMinSpinnerStateChanged());
        panel.getBlueComboBox().addActionListener(evt -> blueComboBoxActionPerformed(evt));
        panel.getBlueMaxSpinner().addChangeListener(evt -> blueMaxSpinnerStateChanged());
        panel.getBlueMinSpinner().addChangeListener(evt -> blueMinSpinnerStateChanged());
        panel.getGrayscaleCheckBox().addActionListener(evt -> grayscaleCheckBoxActionPerformed());
        panel.getCustomFunctionsButton().addActionListener(evt -> customFunctionsButtonActionPerformed());

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
     * Generates and displays the custom function panel, and updates the coloring as requested by that panel's input.
     */
    private void customFunctionsButtonActionPerformed() {
        SpectrumMathPanel customFunctionsPanel = new SpectrumMathPanel(
                JOptionPane.getFrameForComponent(panel),
                new JComboBox[]{panel.getRedComboBox(), panel.getGreenComboBox(), panel.getBlueComboBox()}, model.getInstrument());
        model.setCurrentlyEditingUserDefinedFunction(true);
        customFunctionsPanel.setVisible(true);
        model.setCurrentlyEditingUserDefinedFunction(false);
        model.updateColoring();
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
     * Sets up the RGB coloring combo boxes
     */
    protected void setupComboBoxes()
    {
        ISpectralInstrument instrument = model.getInstrument();
        for (int i=1; i<=instrument.getBandCenters().length; ++i)
        {
            String channel = new String("(" + i + ") " + instrument.getBandCenters()[i-1] + " " + instrument.getBandCenterUnit());
            panel.getRedComboBox().addItem(channel);
            panel.getGreenComboBox().addItem(channel);
            panel.getBlueComboBox().addItem(channel);
        }
        panel.getRedComboBox().setSelectedIndex(model.getColoringModel().getRedIndex());
        panel.getGreenComboBox().setSelectedIndex(model.getColoringModel().getGreenIndex());
        panel.getBlueComboBox().setSelectedIndex(model.getColoringModel().getBlueIndex());

        String[] derivedParameters = instrument.getSpectrumMath().getDerivedParameters();
        for (int i=0; i<derivedParameters.length; ++i)
        {
            panel.getRedComboBox().addItem(derivedParameters[i]);
            panel.getGreenComboBox().addItem(derivedParameters[i]);
            panel.getBlueComboBox().addItem(derivedParameters[i]);
        }

        for (vtkFunctionParser fp: instrument.getSpectrumMath().getAllUserDefinedDerivedParameters())
        {
            panel.getRedComboBox().addItem(fp.GetFunction());
            panel.getGreenComboBox().addItem(fp.GetFunction());
            panel.getBlueComboBox().addItem(fp.GetFunction());
        }

        panel.getRedMaxSpinner().setValue(model.getColoringModel().getRedMaxVal());
        panel.getGreenMaxSpinner().setValue(model.getColoringModel().getGreenMaxVal());
        panel.getBlueMaxSpinner().setValue(model.getColoringModel().getBlueMaxVal());
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
		        panel.getRgbColoringPanel().setVisible(!isEmissionSelected);
		        panel.getEmissionAngleColoringPanel().setVisible(isEmissionSelected);
		        model.getColoringModel().setSpectrumColoringStyle(coloringStyle);

		        return null;
			}
		};
		task.execute();
    }

    //Helper methods related to actions for the various spinners and combo boxes

    private void redComboBoxActionPerformed(ActionEvent evt) {
    	model.getColoringModel().setRedIndex(panel.getRedComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void greenComboBoxActionPerformed(ActionEvent evt) {
    	model.getColoringModel().setGreenIndex(panel.getGreenComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void blueComboBoxActionPerformed(ActionEvent evt) {
    	model.getColoringModel().setBlueIndex(panel.getBlueComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void redMinSpinnerStateChanged() {
        checkValidMinMax(0, true);
    }

    private void greenMinSpinnerStateChanged() {
        checkValidMinMax(1, true);
    }

    private void blueMinSpinnerStateChanged() {
        checkValidMinMax(2, true);
    }

    private void redMaxSpinnerStateChanged() {
        checkValidMinMax(0, false);
    }

    private void greenMaxSpinnerStateChanged() {
        checkValidMinMax(1, false);
    }

    private void blueMaxSpinnerStateChanged() {
        checkValidMinMax(2, false);
    }

    private void grayscaleCheckBoxActionPerformed() {
        boolean enableColor = !panel.getGrayscaleCheckBox().isSelected();

        panel.getRedLabel().setVisible(enableColor);
        panel.getGreenMinLabel().setVisible(enableColor);
        panel.getGreenMaxLabel().setVisible(enableColor);
        panel.getGreenLabel().setVisible(enableColor);
        panel.getGreenComboBox().setVisible(enableColor);
        panel.getGreenMinSpinner().setVisible(enableColor);
        panel.getGreenMaxSpinner().setVisible(enableColor);
        panel.getBlueComboBox().setVisible(enableColor);
        panel.getBlueMinSpinner().setVisible(enableColor);
        panel.getBlueMinLabel().setVisible(enableColor);
        panel.getBlueMaxLabel().setVisible(enableColor);
        panel.getBlueLabel().setVisible(enableColor);
        panel.getBlueMaxSpinner().setVisible(enableColor);
        model.getColoringModel().setGreyScaleSelected(panel.getGrayscaleCheckBox().isSelected());
        model.updateColoring();
    }

    private void checkValidMinMax(int channel, boolean minimunStateChange)
    {
        JSpinner minSpinner = null;
        JSpinner maxSpinner = null;

        if (channel == 0)
        {
            minSpinner = panel.getRedMinSpinner();
            maxSpinner = panel.getRedMaxSpinner();
        }
        else if (channel == 1)
        {
            minSpinner = panel.getGreenMinSpinner();
            maxSpinner = panel.getGreenMaxSpinner();
        }
        else if (channel == 2)
        {
            minSpinner = panel.getBlueMinSpinner();
            maxSpinner = panel.getBlueMaxSpinner();
        }

        Double minVal = (Double)minSpinner.getValue();
        Double maxVal = (Double)maxSpinner.getValue();
        if (channel == 0)
        {
            model.getColoringModel().setRedMinVal(minVal);
            model.getColoringModel().setRedMaxVal(maxVal);
        }
        else if (channel == 1)
        {
            model.getColoringModel().setGreenMinVal(minVal);
            model.getColoringModel().setGreenMaxVal(maxVal);
        }
        else if (channel == 2)
        {
            model.getColoringModel().setBlueMinVal(minVal);
            model.getColoringModel().setBlueMaxVal(maxVal);
        }
        if (minVal > maxVal)
        {
            if (minimunStateChange)
                minSpinner.setValue(maxSpinner.getValue());
            else
                maxSpinner.setValue(minSpinner.getValue());
        }
        model.updateColoring();
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