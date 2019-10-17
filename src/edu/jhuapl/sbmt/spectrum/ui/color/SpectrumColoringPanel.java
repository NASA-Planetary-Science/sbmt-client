package edu.jhuapl.sbmt.spectrum.ui.color;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;

/**
 * Panel that shows the coloring controls for a spectrum
 * @author steelrj1
 *
 */
public class SpectrumColoringPanel extends JPanel
{
    private JPanel coloringDetailPanel;
    private JComboBox<SpectrumColoringStyle> coloringComboBox;
    private EmissionAngleColoringPanel emissionAngleColoringPanel;
    private RGBColoringPanel rgbColoringPanel;
    private GreyscaleColoringPanel greyScaleColoringPanel;
    private BaseSpectrumSearchModel model;

    public SpectrumColoringPanel(BaseSpectrumSearchModel model)
    {
    	this.model = model;
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
        //emission panel was here
        emissionAngleColoringPanel = new EmissionAngleColoringPanel();
        coloringDetailPanel.add(emissionAngleColoringPanel);


        rgbColoringPanel = new RGBColoringPanel(model);
        coloringDetailPanel.add(rgbColoringPanel);

        greyScaleColoringPanel = new GreyscaleColoringPanel(model);
        coloringDetailPanel.add(greyScaleColoringPanel);

//        rgbColoringPanel.setLayout(new BoxLayout(rgbColoringPanel, BoxLayout.Y_AXIS));

        JPanel panel_10 = new JPanel();
        rgbColoringPanel.add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        getEmissionAngleColoringPanel().setVisible(false);
        getGreyscaleColoringPanel().setVisible(false);
        getRgbColoringPanel().setVisible(true);

    }

    public JPanel getColoringDetailPanel()
    {
        return coloringDetailPanel;
    }

    public JComboBox<SpectrumColoringStyle> getColoringComboBox()
    {
        return coloringComboBox;
    }

    public JPanel getEmissionAngleColoringPanel()
    {
        return emissionAngleColoringPanel;
    }

    public JPanel getGreyscaleColoringPanel()
    {
        return greyScaleColoringPanel;
    }

    public JPanel getRgbColoringPanel()
    {
        return rgbColoringPanel;
    }


}
