package edu.jhuapl.sbmt.spectrum.ui.color;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.color.EmissionSpectrumColorer;

public class EmissionAngleColoringPanel<S extends BasicSpectrum> extends JPanel implements ISpectrumColoringPanel, ChangeListener, ActionListener
{
	private final JComboBox<Colormap> colormapComboBox;
//	private SpectrumColoringModel<S> model;
	private EmissionSpectrumColorer<S> model;

	public EmissionAngleColoringPanel(EmissionSpectrumColorer<S> model)
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.model = model;
		JLabel lblNewLabel_15 = new JLabel("Coloring by Avg Emission Angle (OREX Scalar Ramp, 0 to 90)");
		add(lblNewLabel_15);
		colormapComboBox = new JComboBox<>();
		ListCellRenderer<Colormap> tmpRenderer = ColormapUtil.getFancyColormapRender();
		((Component) tmpRenderer).setEnabled(true);
		colormapComboBox.setRenderer(tmpRenderer);
		for (String aStr : Colormaps.getAllBuiltInColormapNames())
		{
			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
			colormapComboBox.addItem(cmap);
			if (cmap.getName().equals(Colormaps.getCurrentColormapName()))
				colormapComboBox.setSelectedItem(cmap);
		}
		colormapComboBox.addActionListener(this);
		colormapComboBox.setEnabled(true);
//		colormapComboBox.setSelectedItem(Colormaps.getDefaultColormapName());
		add(colormapComboBox);
	}

	public JPanel getJPanel()
	{
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		model.setCurrentColormap((Colormap)colormapComboBox.getSelectedItem());
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		Object source = aEvent.getSource();

//		// NumTicks UI
//		if (source == numTicksSpinner)
//		{
//			cColormap.setNumberOfLabels((Integer) numTicksSpinner.getValue());
//			firePropertyChange(EVT_ColormapChanged, null, null);
//		}
	}


}