package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.Picker;

public class SmallBodyControlPanel extends JPanel implements ItemListener, ChangeListener
{
    private JCheckBox modelCheckBox;
    private ModelManager modelManager;
    private JCheckBox showColoringCheckBox;
    private ArrayList<JRadioButton> coloringButtons = new ArrayList<JRadioButton>();
    private JComboBox customColorRedComboBox;
    private JComboBox customColorGreenComboBox;
    private JComboBox customColorBlueComboBox;
    private JLabel customColorRedLabel;
    private JLabel customColorGreenLabel;
    private JLabel customColorBlueLabel;
    private JRadioButton flatShadingButton;
    private JRadioButton smoothShadingButton;
    private JRadioButton lowResModelButton;
    private JRadioButton medResModelButton;
    private JRadioButton highResModelButton;
    private JRadioButton veryHighResModelButton;
    private ButtonGroup coloringButtonGroup;
    private ButtonGroup shadingButtonGroup;
    private ButtonGroup resolutionButtonGroup;
    private JCheckBox gridCheckBox;
    private JCheckBox axesCheckBox;
    private JCheckBox imageMapCheckBox;
    private JLabel opacityLabel;
    private JSpinner imageMapOpacitySpinner;
    private JButton scaleColoringButton;
    private JRadioButton customColorButton;
    private JLabel statisticsLabel;


    public SmallBodyControlPanel(ModelManager modelManager, String bodyName)
    {
        super(new BorderLayout());
        this.modelManager = modelManager;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

        modelCheckBox = new JCheckBox();
        modelCheckBox.setText("Show " + bodyName);
        modelCheckBox.setSelected(true);
        modelCheckBox.addItemListener(this);

        JLabel resolutionLabel = new JLabel("Resolution");

        lowResModelButton = new JRadioButton(SmallBodyModel.LowResModelStr);
        lowResModelButton.setActionCommand(SmallBodyModel.LowResModelStr);
        lowResModelButton.addItemListener(this);
        lowResModelButton.setEnabled(true);
        lowResModelButton.setToolTipText(
                "<html>Click here to show a low resolution model of " + bodyName + " <br />" +
                "containing 49152 plates or triangles</html>");

        medResModelButton = new JRadioButton(SmallBodyModel.MedResModelStr);
        medResModelButton.setActionCommand(SmallBodyModel.MedResModelStr);
        medResModelButton.addItemListener(this);
        medResModelButton.setEnabled(true);
        medResModelButton.setToolTipText(
                "<html>Click here to show a medium resolution model of " + bodyName + " <br />" +
                "containing 196608 plates or triangles</html>");

        highResModelButton = new JRadioButton(SmallBodyModel.HighResModelStr);
        highResModelButton.setActionCommand(SmallBodyModel.HighResModelStr);
        highResModelButton.addItemListener(this);
        highResModelButton.setEnabled(true);
        highResModelButton.setToolTipText(
                "<html>Click here to show a high resolution model of " + bodyName + " <br />" +
                "containing 786432 plates or triangles</html>");

        veryHighResModelButton = new JRadioButton(SmallBodyModel.VeryHighResModelStr);
        veryHighResModelButton.setActionCommand(SmallBodyModel.VeryHighResModelStr);
        veryHighResModelButton.addItemListener(this);
        veryHighResModelButton.setEnabled(true);
        veryHighResModelButton.setToolTipText(
                "<html>Click here to show a very high resolution model of " + bodyName + " <br />" +
                "containing 3145728 plates or triangles </html>");

        statisticsLabel = new JLabel();
        statisticsLabel.setBorder(null);
        statisticsLabel.setOpaque(false);
        setStatisticsLabel();


        resolutionButtonGroup = new ButtonGroup();
        resolutionButtonGroup.add(lowResModelButton);
        resolutionButtonGroup.add(medResModelButton);
        resolutionButtonGroup.add(highResModelButton);
        resolutionButtonGroup.add(veryHighResModelButton);
        resolutionButtonGroup.setSelected(lowResModelButton.getModel(), true);

        showColoringCheckBox = new JCheckBox();
        showColoringCheckBox.setText("Color " + bodyName + " by");
        showColoringCheckBox.setSelected(false);
        showColoringCheckBox.addItemListener(this);

        final SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();
        for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
        {
            JRadioButton button = new JRadioButton(smallBodyModel.getColoringName(i));
            button.setActionCommand(smallBodyModel.getColoringName(i));
            button.addItemListener(this);
            button.setEnabled(false);
            coloringButtons.add(button);
        }

        if (smallBodyModel.isFalseColoringSupported())
        {
            final String customColor = "Custom";
            customColorButton = new JRadioButton(customColor);
            customColorButton.setActionCommand(customColor);
            customColorButton.addItemListener(this);
            customColorButton.setEnabled(false);
            coloringButtons.add(customColorButton);

            customColorRedLabel = new JLabel("Red: ");
            customColorGreenLabel = new JLabel("Green: ");
            customColorBlueLabel = new JLabel("Blue: ");

            Object[] coloringOptions = new Object[smallBodyModel.getNumberOfColors()];
            for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
                coloringOptions[i] = smallBodyModel.getColoringName(i);

            customColorRedComboBox = new JComboBox(coloringOptions);
            customColorRedComboBox.setMaximumSize(new Dimension(175, 23));
            customColorRedComboBox.addItemListener(this);
            customColorGreenComboBox = new JComboBox(coloringOptions);
            customColorGreenComboBox.setMaximumSize(new Dimension(175, 23));
            customColorGreenComboBox.addItemListener(this);
            customColorBlueComboBox = new JComboBox(coloringOptions);
            customColorBlueComboBox.setMaximumSize(new Dimension(175, 23));
            customColorBlueComboBox.addItemListener(this);

            customColorRedComboBox.setEnabled(false);
            customColorGreenComboBox.setEnabled(false);
            customColorBlueComboBox.setEnabled(false);
            customColorRedLabel.setEnabled(false);
            customColorGreenLabel.setEnabled(false);
            customColorBlueLabel.setEnabled(false);
        }

        coloringButtonGroup = new ButtonGroup();
        for (int i=0; i<coloringButtons.size(); ++i)
            coloringButtonGroup.add(coloringButtons.get(i));
        if (coloringButtons.size() > 0)
            coloringButtonGroup.setSelected(coloringButtons.get(0).getModel(), true);

        scaleColoringButton = new JButton("Rescale Data Range");
        scaleColoringButton.setEnabled(false);
        scaleColoringButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ScaleDataRangeDialog scaleDataDialog = new ScaleDataRangeDialog(smallBodyModel);
                scaleDataDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(scaleColoringButton));
                scaleDataDialog.setVisible(true);
            }
        });

        JLabel shadingLabel = new JLabel("Shading");

        flatShadingButton = new JRadioButton(SmallBodyModel.FlatShadingStr);
        flatShadingButton.setActionCommand(SmallBodyModel.FlatShadingStr);
        flatShadingButton.addItemListener(this);
        flatShadingButton.setEnabled(true);

        smoothShadingButton = new JRadioButton(SmallBodyModel.SmoothShadingStr);
        smoothShadingButton.setActionCommand(SmallBodyModel.SmoothShadingStr);
        smoothShadingButton.addItemListener(this);
        smoothShadingButton.setEnabled(true);

        shadingButtonGroup = new ButtonGroup();
        shadingButtonGroup.add(flatShadingButton);
        shadingButtonGroup.add(smoothShadingButton);
        shadingButtonGroup.setSelected(smoothShadingButton.getModel(), true);

        gridCheckBox = new JCheckBox();
        gridCheckBox.setText("Show Coordinate Grid");
        gridCheckBox.setSelected(false);
        gridCheckBox.addItemListener(this);

        axesCheckBox = new JCheckBox();
        axesCheckBox.setText("Show Orientation Axes");
        axesCheckBox.setSelected(true);
        axesCheckBox.addItemListener(this);

        imageMapCheckBox = new JCheckBox();
        imageMapCheckBox.setText("Show Image Map");
        imageMapCheckBox.setSelected(false);
        imageMapCheckBox.addItemListener(this);

        opacityLabel = new JLabel("Opacity");
        imageMapOpacitySpinner = new JSpinner(new SpinnerNumberModel(0.50, 0.0, 1.0, 0.1));
        imageMapOpacitySpinner.setEditor(new JSpinner.NumberEditor(imageMapOpacitySpinner, "0.00"));
        imageMapOpacitySpinner.setPreferredSize(new Dimension(80, 21));
        imageMapOpacitySpinner.addChangeListener(this);
        opacityLabel.setEnabled(false);
        imageMapOpacitySpinner.setEnabled(false);


        panel.add(modelCheckBox, "wrap");
        if (modelManager.getSmallBodyModel().getNumberResolutionLevels() > 1)
        {
            panel.add(resolutionLabel, "wrap");
            panel.add(lowResModelButton, "wrap, gapleft 25");
            panel.add(medResModelButton, "wrap, gapleft 25");
            panel.add(highResModelButton, "wrap, gapleft 25");
            panel.add(veryHighResModelButton, "wrap, gapleft 25");
        }
        if (modelManager.getSmallBodyModel().isColoringDataAvailable())
        {
            panel.add(showColoringCheckBox, "wrap");
            for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
                panel.add(coloringButtons.get(i), "wrap, gapleft 25");
            if (smallBodyModel.isFalseColoringSupported())
            {
                panel.add(coloringButtons.get(coloringButtons.size()-1), "wrap, gapleft 25");

                panel.add(customColorRedLabel, "gapleft 50, split 2, align right");
                panel.add(customColorRedComboBox, "wrap");
                panel.add(customColorGreenLabel, "gapleft 50, split 2, align right");
                panel.add(customColorGreenComboBox, "wrap");
                panel.add(customColorBlueLabel, "gapleft 50, split 2, align right");
                panel.add(customColorBlueComboBox, "wrap");
            }
            panel.add(scaleColoringButton, "wrap, gapleft 25");
        }
        if (modelManager.getSmallBodyModel().isImageMapAvailable())
        {
            panel.add(imageMapCheckBox, "wrap");
            panel.add(opacityLabel, "gapleft 25, split 2");
            panel.add(imageMapOpacitySpinner, "wrap");
        }
        panel.add(gridCheckBox, "wrap");
        //panel.add(axesCheckBox, "wrap");
        panel.add(shadingLabel, "wrap");
        panel.add(flatShadingButton, "wrap, gapleft 25");
        panel.add(smoothShadingButton, "wrap, gapleft 25");
        panel.add(statisticsLabel, "gaptop 15");

        add(panel, BorderLayout.CENTER);
    }

    public void itemStateChanged(ItemEvent e)
    {
        Picker.setPickingEnabled(false);

        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

        if (e.getItemSelectable() == this.modelCheckBox)
        {
            // In the following we ensure that the graticule is shown
            // only if the shape model is shown
            Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                smallBodyModel.setShowSmallBody(true);
                if (gridCheckBox.isSelected())
                    graticule.setShowGraticule(true);
            }
            else
            {
                smallBodyModel.setShowSmallBody(false);
                if (gridCheckBox.isSelected())
                    graticule.setShowGraticule(false);
            }
        }
        else if (e.getItemSelectable() == this.gridCheckBox)
        {
            Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
            if (e.getStateChange() == ItemEvent.SELECTED)
                graticule.setShowGraticule(true);
            else
                graticule.setShowGraticule(false);
        }
        else if (e.getItemSelectable() == this.imageMapCheckBox)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                smallBodyModel.setShowImageMap(true);
                if (this.showColoringCheckBox.isSelected())
                {
                    opacityLabel.setEnabled(true);
                    imageMapOpacitySpinner.setEnabled(true);
                }
            }
            else
            {
                smallBodyModel.setShowImageMap(false);
                opacityLabel.setEnabled(false);
                imageMapOpacitySpinner.setEnabled(false);
            }
        }
        else if (e.getItemSelectable() == this.flatShadingButton)
        {
            if (this.flatShadingButton.isSelected())
                smallBodyModel.setShadingToFlat();
        }
        else if (e.getItemSelectable() == this.smoothShadingButton)
        {
            if (this.smoothShadingButton.isSelected())
                smallBodyModel.setShadingToSmooth();
        }
        else if (e.getItemSelectable() == this.lowResModelButton)
        {
            if (this.lowResModelButton.isSelected())
                try {
                    smallBodyModel.setModelResolution(0);
                    setStatisticsLabel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.medResModelButton)
        {
            if (this.medResModelButton.isSelected())
                try {
                    smallBodyModel.setModelResolution(1);
                    setStatisticsLabel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.highResModelButton)
        {
            if (this.highResModelButton.isSelected())
                try {
                    smallBodyModel.setModelResolution(2);
                    setStatisticsLabel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.veryHighResModelButton)
        {
            if (this.veryHighResModelButton.isSelected())
                try {
                    smallBodyModel.setModelResolution(3);
                    setStatisticsLabel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.showColoringCheckBox)
        {
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
                for (int i=0; i<coloringButtons.size(); ++i)
                    coloringButtons.get(i).setEnabled(false);
                if (smallBodyModel.isFalseColoringSupported())
                {
                    customColorRedComboBox.setEnabled(false);
                    customColorGreenComboBox.setEnabled(false);
                    customColorBlueComboBox.setEnabled(false);
                    customColorRedLabel.setEnabled(false);
                    customColorGreenLabel.setEnabled(false);
                    customColorBlueLabel.setEnabled(false);
                }

                try
                {
                    smallBodyModel.setColoringIndex(-1);
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }

                scaleColoringButton.setEnabled(false);
                opacityLabel.setEnabled(false);
                imageMapOpacitySpinner.setEnabled(false);
            }
            else
            {
                for (int i=0; i<coloringButtons.size(); ++i)
                    coloringButtons.get(i).setEnabled(true);

                scaleColoringButton.setEnabled(true);

                if (smallBodyModel.isFalseColoringSupported())
                {
                    customColorRedComboBox.setEnabled(true);
                    customColorGreenComboBox.setEnabled(true);
                    customColorBlueComboBox.setEnabled(true);
                    customColorRedLabel.setEnabled(true);
                    customColorGreenLabel.setEnabled(true);
                    customColorBlueLabel.setEnabled(true);

                    if (customColorButton.isSelected())
                        scaleColoringButton.setEnabled(false);
                }

                setColoring();

                if (imageMapCheckBox.isSelected())
                {
                    opacityLabel.setEnabled(true);
                    imageMapOpacitySpinner.setEnabled(true);
                }
            }
        }
        else
        {
            if (smallBodyModel.isFalseColoringSupported() &&
                    this.showColoringCheckBox.isSelected())
            {
                if (customColorButton.isSelected())
                    scaleColoringButton.setEnabled(false);
                else
                    scaleColoringButton.setEnabled(true);
            }

            if (this.showColoringCheckBox.isSelected())
                setColoring();
        }

        Picker.setPickingEnabled(true);
    }

    private void setColoring()
    {
        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

        try
        {
            if (this.showColoringCheckBox.isSelected())
            {
                for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
                {
                    if (coloringButtonGroup.getSelection() == this.coloringButtons.get(i).getModel())
                    {
                        smallBodyModel.setColoringIndex(i);
                        return;
                    }
                }

                // If the false coloring option is selected (which is the last radio button
                // and is not included in the previous for loop)
                if (coloringButtonGroup.getSelection() ==
                    this.coloringButtons.get(smallBodyModel.getNumberOfColors()).getModel())
                {
                    smallBodyModel.setFalseColoring(
                            customColorRedComboBox.getSelectedIndex(),
                            customColorGreenComboBox.getSelectedIndex(),
                            customColorBlueComboBox.getSelectedIndex());
                }
            }

        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    public void stateChanged(ChangeEvent e)
    {
        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

        double val = (Double)imageMapOpacitySpinner.getValue();

        smallBodyModel.setImageMapOpacity(val);
    }

    private void setStatisticsLabel()
    {
        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

        DecimalFormat df = new DecimalFormat("#.####");

        String text = "<html>Statistics:<br>"
            + "&nbsp;&nbsp;&nbsp;Number of plates: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfCells() + "<br>"
            + "&nbsp;&nbsp;&nbsp;Number of vertices: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints() + "<br>"
            + "&nbsp;&nbsp;&nbsp;Surface Area: " + df.format(smallBodyModel.getSurfaceArea()) + " km^2<br>"
            + "&nbsp;&nbsp;&nbsp;Volume: " + df.format(smallBodyModel.getVolume()) + " km^3<br>"
            + "&nbsp;&nbsp;&nbsp;Average plate area: " + df.format(1.0e6 * smallBodyModel.getMeanCellArea()) + " m^2<br>"
            + "&nbsp;&nbsp;&nbsp;Minimum plate area: " + df.format(1.0e6 * smallBodyModel.getMinCellArea()) + " m^2<br>"
            + "&nbsp;&nbsp;&nbsp;Maximum plate area: " + df.format(1.0e6 * smallBodyModel.getMaxCellArea()) + " m^2<br>"
            + "</html>";

        text += "</table>";
        statisticsLabel.setText(text);
    }
}
