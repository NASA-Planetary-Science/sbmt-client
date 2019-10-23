package edu.jhuapl.sbmt.spectrum.ui.color;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import vtk.vtkFunctionParser;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.color.GreyscaleSpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

public class GreyscaleColoringPanel<S extends BasicSpectrum> extends JPanel implements ISpectrumColoringPanel
{
	private JComboBox<String> greyComboBox;
	private JSpinner greyMinSpinner;
	private JSpinner greyMaxSpinner;
	private JLabel greyLabel;
    private JLabel greyMinLabel;
    private JLabel greyMaxLabel;
    private ISpectralInstrument instrument;
    private GreyscaleSpectrumColorer<S> model;


	public GreyscaleColoringPanel(GreyscaleSpectrumColorer<S> model, ISpectralInstrument instrument)
	{
		this.model = model;
		this.instrument = instrument;

		initialize();
	}

	private void initialize()
	{
		SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d));
        Dimension preferredSpinnerSize = new Dimension(100, 28);
        Dimension minSpinnerSize = new Dimension(36, 22);
        Dimension maxSpinnerSize = new Dimension(100, 22);

		JPanel panel_12 = new JPanel();
        add(panel_12);
        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));

        greyLabel = new JLabel("Greyscale");
        panel_12.add(greyLabel);

        greyComboBox = new JComboBox<String>();
        panel_12.add(greyComboBox);

        greyMinLabel = new JLabel("Min");
        panel_12.add(greyMinLabel);

        greyMinSpinner = new JSpinner();
        greyMinSpinner.setModel(spinnerNumberModel);
        greyMinSpinner.setPreferredSize(preferredSpinnerSize);
        greyMinSpinner.setMinimumSize(minSpinnerSize);
        greyMinSpinner.setMaximumSize(maxSpinnerSize);
        panel_12.add(greyMinSpinner);

        greyMaxLabel = new JLabel("Max");
        panel_12.add(greyMaxLabel);

        greyMaxSpinner = new JSpinner();
        greyMaxSpinner.setModel(spinnerNumberModel);
        greyMaxSpinner.setPreferredSize(preferredSpinnerSize);
        greyMaxSpinner.setMinimumSize(minSpinnerSize);
        greyMaxSpinner.setMaximumSize(maxSpinnerSize);
        panel_12.add(greyMaxSpinner);

        setupComboBoxes();
        model.updateColoring();
	}

	protected void setupComboBoxes()
    {
        for (int i=1; i<=instrument.getBandCenters().length; ++i)
        {
            String channel = new String("(" + i + ") " + instrument.getBandCenters()[i-1] + " " + instrument.getBandCenterUnit());
            greyComboBox.addItem(channel);
        }
        greyComboBox.setSelectedIndex(model.getGreyScaleIndex());

        String[] derivedParameters = instrument.getSpectrumMath().getDerivedParameters();
        for (int i=0; i<derivedParameters.length; ++i)
        {
            greyComboBox.addItem(derivedParameters[i]);
        }

        for (vtkFunctionParser fp: instrument.getSpectrumMath().getAllUserDefinedDerivedParameters())
        {
            greyComboBox.addItem(fp.GetFunction());
        }

        greyMaxSpinner.setValue(model.getGreyMaxVal());
    }

	public JPanel getJPanel()
	{
		return this;
	}
}
