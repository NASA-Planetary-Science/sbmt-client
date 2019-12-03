package edu.jhuapl.sbmt.spectrum.ui.color;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
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
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.model = model;

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		JLabel lblNewLabel_15 = new JLabel("Coloring by Avg Emission Angle (OREX Scalar Ramp, 0 to 90)");
		lblNewLabel_15.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		labelPanel.add(lblNewLabel_15);
		labelPanel.add(Box.createHorizontalGlue());
		add(labelPanel);
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

		JPanel comboPanel = new JPanel();
		comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
		comboPanel.add(colormapComboBox);
		comboPanel.add(Box.createHorizontalGlue());
		add(comboPanel);
		setBackground(Color.red);
		colormapComboBox.setPreferredSize(new Dimension(450, 30));
		colormapComboBox.setMaximumSize(new Dimension(450, 30));
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