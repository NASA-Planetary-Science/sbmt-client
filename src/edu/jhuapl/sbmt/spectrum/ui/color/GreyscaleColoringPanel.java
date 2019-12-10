package edu.jhuapl.sbmt.spectrum.ui.color;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;

import com.google.common.collect.Lists;

import vtk.vtkFunctionParser;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.color.GreyscaleSpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.ui.math.SpectrumMathPanel;

public class GreyscaleColoringPanel<S extends BasicSpectrum> extends JPanel implements ISpectrumColoringPanel
{
	private JButton customFunctionsButton;
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
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Dimension preferredSpinnerSize = new Dimension(100, 28);
        Dimension minSpinnerSize = new Dimension(36, 22);
        Dimension maxSpinnerSize = new Dimension(100, 22);

        JPanel panel_10 = new JPanel();
        add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));
        customFunctionsButton = new JButton("Custom Formulas");
        panel_10.add(customFunctionsButton);
        panel_10.add(Box.createHorizontalGlue());

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
        greyMinSpinner.setPreferredSize(preferredSpinnerSize);
        greyMinSpinner.setMinimumSize(minSpinnerSize);
        greyMinSpinner.setMaximumSize(maxSpinnerSize);
        panel_12.add(greyMinSpinner);

        greyMaxLabel = new JLabel("Max");
        panel_12.add(greyMaxLabel);

        greyMaxSpinner = new JSpinner();
        greyMaxSpinner.setPreferredSize(preferredSpinnerSize);
        greyMaxSpinner.setMinimumSize(minSpinnerSize);
        greyMaxSpinner.setMaximumSize(maxSpinnerSize);
        panel_12.add(greyMaxSpinner);

        List<JSpinner> spinners=Lists.newArrayList(greyMaxSpinner, greyMinSpinner);

        for (JSpinner spinner : spinners)
        {
            spinner.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.0000001d)));
            NumberEditor editor = (NumberEditor)spinner.getEditor();
            DecimalFormat format = editor.getFormat();
            format.setMinimumFractionDigits(8);
        }

        setupComboBoxes();

        greyComboBox.addActionListener(evt -> greyComboBoxActionPerformed(evt));
        greyMaxSpinner.addChangeListener(evt -> greyMaxSpinnerStateChanged());
        greyMinSpinner.addChangeListener(evt -> greyMinSpinnerStateChanged());
        getCustomFunctionsButton().addActionListener(evt -> customFunctionsButtonActionPerformed());
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

	private void greyComboBoxActionPerformed(ActionEvent evt) {
    	model.setGreyScaleIndex(greyComboBox.getSelectedIndex());
    	model.updateColoring();
    }

	private void greyMinSpinnerStateChanged() {
        checkValidMinMax(0, true);
    }

	private void greyMaxSpinnerStateChanged() {
        checkValidMinMax(0, false);

    }

	private void checkValidMinMax(int channel, boolean minimumStateChange)
    {
        Double minVal = (Double)greyMinSpinner.getValue();
        Double maxVal = (Double)greyMaxSpinner.getValue();

        model.setGreyMinVal(minVal);
        model.setGreyMaxVal(maxVal);

        if (minVal > maxVal)
        {
            if (minimumStateChange)
                greyMinSpinner.setValue(greyMaxSpinner.getValue());
            else
                greyMaxSpinner.setValue(greyMinSpinner.getValue());
        }
        model.updateColoring();
    }

	public JPanel getJPanel()
	{
		return this;
	}

	public JButton getCustomFunctionsButton()
    {
        return customFunctionsButton;
    }

    public JComboBox<String> getGreyComboBox()
    {
        return greyComboBox;
    }

    /**
     * Generates and displays the custom function panel, and updates the coloring as requested by that panel's input.
     */
    private void customFunctionsButtonActionPerformed() {
        SpectrumMathPanel customFunctionsPanel = new SpectrumMathPanel(
                JOptionPane.getFrameForComponent(this),
                new JComboBox[]{getGreyComboBox(), getGreyComboBox(), getGreyComboBox()}, instrument);
        model.setCurrentlyEditingUserDefinedFunction(true);
        customFunctionsPanel.setVisible(true);
        model.setCurrentlyEditingUserDefinedFunction(false);
        model.updateColoring();
    }
}
