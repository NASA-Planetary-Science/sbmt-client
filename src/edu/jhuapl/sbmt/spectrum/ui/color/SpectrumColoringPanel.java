package edu.jhuapl.sbmt.spectrum.ui.color;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.color.SpectrumColoringModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;

/**
 * Panel that shows the coloring controls for a spectrum
 * @author steelrj1
 *
 */
public class SpectrumColoringPanel<S extends BasicSpectrum> extends JPanel
{
    private JPanel coloringDetailPanel;
    private JComboBox<SpectrumColoringStyle> coloringComboBox;

    public SpectrumColoringPanel(SpectrumColoringModel<S> coloringModel, ISpectralInstrument instrument)
    {
    	SpectrumColoringFactory.registerColoringPanel(SpectrumColoringStyle.EMISSION_ANGLE, new EmissionAngleColoringPanel<S>(coloringModel));
    	SpectrumColoringFactory.registerColoringPanel(SpectrumColoringStyle.GREYSCALE, new GreyscaleColoringPanel<S>(coloringModel, instrument));
    	SpectrumColoringFactory.registerColoringPanel(SpectrumColoringStyle.RGB, new RGBColoringPanel<S>(coloringModel, instrument));
        initialize();
    }

    private void initialize()
    {
        setBorder(new TitledBorder(null, "Coloring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        coloringComboBox = new JComboBox<SpectrumColoringStyle>();

        add(coloringComboBox);

        coloringDetailPanel = new JPanel();
        add(coloringDetailPanel);
        coloringDetailPanel.setLayout(new BoxLayout(coloringDetailPanel, BoxLayout.Y_AXIS));
    }

    public void switchToPanelForColoringStyle(SpectrumColoringStyle style)
    {
    	coloringDetailPanel.removeAll();
    	JPanel panel = SpectrumColoringFactory.getColoringPanelForStyle(style).getJPanel();
    	coloringDetailPanel.add(panel);
    	revalidate();
    }

    public JPanel getColoringDetailPanel()
    {
        return coloringDetailPanel;
    }

    public JComboBox<SpectrumColoringStyle> getColoringComboBox()
    {
        return coloringComboBox;
    }

}
