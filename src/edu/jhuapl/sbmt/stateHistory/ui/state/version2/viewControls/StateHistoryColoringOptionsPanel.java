package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;

public class StateHistoryColoringOptionsPanel extends JPanel
{

	/**
	 *
	 */
	private JLabel colorFunctionLabel;

	/**
	 *
	 */
	private JLabel colorRampLabel;

	/**
	 *
	 */
	private JComboBox<StateHistoryColoringFunctions> colorFunctionComboBox;

	/**
	 *
	 */
	private JComboBox<Colormap> colormapComboBox;

	public StateHistoryColoringOptionsPanel()
	{
		initUI();
	}

	private void initUI()
	{
		configureColoringPanel();
	}

	/**
	 *
	 */
	private void configureColoringPanel()
	{
		setBorder(new TitledBorder(null, "Coloring Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		colorFunctionLabel = new JLabel("Function:");
		colorFunctionComboBox = new JComboBox<StateHistoryColoringFunctions>(StateHistoryColoringFunctions.values());

		JPanel horizPanel1 = new JPanel();
		horizPanel1.setLayout(new BoxLayout(horizPanel1, BoxLayout.X_AXIS));

		horizPanel1.add(colorFunctionLabel);
		horizPanel1.add(colorFunctionComboBox);

		colorRampLabel = new JLabel("Color:");

		colormapComboBox = new JComboBox<Colormap>();
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
		colormapComboBox.setEnabled(true);

		colormapComboBox.setPreferredSize(new Dimension(300, 30));
		colormapComboBox.setMaximumSize(new Dimension(300, 30));

		colormapComboBox.setEnabled(!(((StateHistoryColoringFunctions) colorFunctionComboBox
				.getSelectedItem()) == StateHistoryColoringFunctions.PER_TABLE));

		JPanel horizPanel2 = new JPanel();
		horizPanel2.setLayout(new BoxLayout(horizPanel2, BoxLayout.X_AXIS));

		horizPanel2.add(colorRampLabel);
		horizPanel2.add(colormapComboBox);

		horizPanel1.add(Box.createHorizontalGlue());
		horizPanel2.add(Box.createHorizontalGlue());

		add(horizPanel1);
		add(horizPanel2);
	}

	/**
	 * @return
	 */
	public JComboBox<StateHistoryColoringFunctions> getColorFunctionComboBox()
	{
		return colorFunctionComboBox;
	}

	/**
	 * @return
	 */
	public JComboBox<Colormap> getColormapComboBox()
	{
		return colormapComboBox;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		colorFunctionComboBox.setEnabled(enabled);
		colorFunctionLabel.setEnabled(enabled);
		colormapComboBox.setEnabled(!(((StateHistoryColoringFunctions) colorFunctionComboBox
				.getSelectedItem()) == StateHistoryColoringFunctions.PER_TABLE));
		colorRampLabel.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
