package edu.jhuapl.sbmt.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vtk.vtkFunctionParser;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.spectrum.model.core.AbstractSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumColoringPanel;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumMathPanel;

public class SpectrumColoringController
{
    SpectrumColoringPanel panel;
    AbstractSpectrumSearchModel model;

    public SpectrumColoringController(AbstractSpectrumSearchModel model)
    {
        this.panel = new SpectrumColoringPanel();
        this.model = model;
        init();

    }

    private void init()
    {
        setupComboBoxes();
        setColoringComboBox();

        panel.getColoringComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                coloringComboBoxActionPerformed(evt);
            }
        });

        panel.getRedComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                redComboBoxActionPerformed(evt);
            }
        });

        panel.getRedMaxSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                redMaxSpinnerStateChanged(evt);
            }
        });

        panel.getRedMinSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                redMinSpinnerStateChanged(evt);
            }
        });

        panel.getGreenComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                greenComboBoxActionPerformed(evt);
            }
        });

        panel.getGreenMaxSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                greenMaxSpinnerStateChanged(evt);
            }
        });

        panel.getGreenMinSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                greenMinSpinnerStateChanged(evt);
            }
        });

        panel.getBlueComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                blueComboBoxActionPerformed(evt);
            }
        });

        panel.getBlueMaxSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                blueMaxSpinnerStateChanged(evt);
            }
        });

        panel.getBlueMinSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                blueMinSpinnerStateChanged(evt);
            }
        });

        panel.getGrayscaleCheckBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                grayscaleCheckBoxActionPerformed(evt);
            }
        });

        panel.getCustomFunctionsButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customFunctionsButtonActionPerformed(evt);
            }
        });

        model.addColoringChangedListener(new SpectrumColoringChangedListener()
        {

            @Override
            public void coloringChanged()
            {
                panel.getColoringComboBox().setSelectedItem(SpectrumColoringStyle.getStyleForName(model.getSpectrumColoringStyleName()));
            }
        });

    }

    private void customFunctionsButtonActionPerformed(ActionEvent evt) {
        SpectrumMathPanel customFunctionsPanel = new SpectrumMathPanel(
                JOptionPane.getFrameForComponent(panel),
                new JComboBox[]{panel.getRedComboBox(), panel.getGreenComboBox(), panel.getBlueComboBox()}, model.getInstrument());
        model.setCurrentlyEditingUserDefinedFunction(true);
        customFunctionsPanel.setVisible(true);
        model.setCurrentlyEditingUserDefinedFunction(false);
        model.updateColoring();
    }

    protected void setColoringComboBox()
    {
        for (SpectrumColoringStyle style : SpectrumColoringStyle.values())
        {
            panel.getColoringComboBox().addItem(style);
        }
    }

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
        panel.getRedComboBox().setSelectedIndex(model.getRedIndex());
        panel.getGreenComboBox().setSelectedIndex(model.getGreenIndex());
        panel.getBlueComboBox().setSelectedIndex(model.getBlueIndex());

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

        panel.getRedMaxSpinner().setValue(model.getRedMaxVal());
        panel.getGreenMaxSpinner().setValue(model.getGreenMaxVal());
        panel.getBlueMaxSpinner().setValue(model.getBlueMaxVal());
    }

    private void coloringComboBoxActionPerformed(ActionEvent evt)
    {
        JComboBox<SpectrumColoringStyle> box = (JComboBox<SpectrumColoringStyle>)evt.getSource();
        String coloringName = box.getSelectedItem().toString();
        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(coloringName);
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.setColoringStyleForInstrument(style, model.getInstrument());

        boolean isEmissionSelected = (style == SpectrumColoringStyle.EMISSION_ANGLE);
        panel.getRgbColoringPanel().setVisible(!isEmissionSelected);
        panel.getEmissionAngleColoringPanel().setVisible(isEmissionSelected);
        model.setSpectrumColoringStyleName(coloringName);
        model.coloringOptionChanged();
    }

    private void redComboBoxActionPerformed(ActionEvent evt) {
    	model.setRedIndex(panel.getRedComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void greenComboBoxActionPerformed(ActionEvent evt) {
    	model.setGreenIndex(panel.getGreenComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void blueComboBoxActionPerformed(ActionEvent evt) {
    	model.setBlueIndex(panel.getBlueComboBox().getSelectedIndex());
        model.updateColoring();
    }

    private void redMinSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(0, true);
        model.updateColoring();
    }

    private void greenMinSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(1, true);
        model.updateColoring();
    }

    private void blueMinSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(2, true);
        model.updateColoring();
    }

    private void redMaxSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(0, false);
        model.updateColoring();
    }

    private void greenMaxSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(1, false);
        model.updateColoring();
    }

    private void blueMaxSpinnerStateChanged(ChangeEvent evt) {
        checkValidMinMax(2, false);
        model.updateColoring();
    }

    private void grayscaleCheckBoxActionPerformed(ActionEvent evt) {
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
        model.setGreyScaleSelected(panel.getGrayscaleCheckBox().isSelected());
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
            model.setRedMinVal(minVal);
            model.setRedMaxVal(maxVal);
        }
        else if (channel == 1)
        {
            model.setGreenMinVal(minVal);
            model.setGreenMaxVal(maxVal);
        }
        else if (channel == 2)
        {
            model.setBlueMinVal(minVal);
            model.setBlueMaxVal(maxVal);
        }
        if (minVal > maxVal)
        {
            if (minimunStateChange)
                minSpinner.setValue(maxSpinner.getValue());
            else
                maxSpinner.setValue(minSpinner.getValue());
        }
    }

    public SpectrumColoringPanel getPanel()
    {
        return panel;
    }
}