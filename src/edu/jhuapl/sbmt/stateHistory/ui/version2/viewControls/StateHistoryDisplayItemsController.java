package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.jfree.ui.FontChooserDialog;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

public class StateHistoryDisplayItemsController implements ItemListener
{
	StateHistoryDisplayItemsPanel view;
	private StateHistoryCollection runs;
	private StateHistoryModel historyModel;
	private Renderer renderer;

	public StateHistoryDisplayItemsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.runs = historyModel.getRuns();
		this.renderer = renderer;
		initializeViewControlPanel();
	}

	/**
	 *
	 */
	private void initializeViewControlPanel()
	{
		view = new StateHistoryDisplayItemsPanel();

		String[] distanceChoices =
		{ "Distance to Center", "Distance to Surface" };
		DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);

		view.getShowSpacecraftPanel().getDistanceOptions().setModel(comboModelDistance);
		view.getShowEarthPanel().getShowEarthPointer().addItemListener(this);
		view.getShowSunPanel().getShowSunPointer().addItemListener(this);
		view.getShowSpacecraftPointerPanel().getShowSpacecraftMarker().addItemListener(this);
		view.getShowSpacecraftPanel().getShowSpacecraft().addItemListener(this);
		view.getShowLightingPanel().getShowLighting().addItemListener(this);

		view.getShowSpacecraftPanel().getLabelCheckBox().addActionListener(e ->
		{
			view.getShowSpacecraftPanel().getDistanceOptions().setEnabled(view.getShowSpacecraftPanel().getLabelCheckBox().isSelected());
			view.getShowSpacecraftPanel().getLabelFontButton().setEnabled(view.getShowSpacecraftPanel().getLabelCheckBox().isSelected());
			runs.setDistanceTextVisiblity(view.getShowSpacecraftPanel().getLabelCheckBox().isSelected());
		});

		view.getShowSpacecraftPanel().getDistanceOptions().addActionListener(e ->
		{
			String selectedItem = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
			runs.setDistanceText(selectedItem);
		});

		view.getShowSpacecraftPanel().getLabelFontButton().addActionListener(e ->
		{
			JFrame fontFrame = new JFrame();
			FontChooserDialog fontDialog = new FontChooserDialog(fontFrame, "Choose Font", true, view.getShowSpacecraftPanel().getLabelFont());
			fontDialog.setSize(400, 300);
			fontDialog.setVisible(true);
			if (fontDialog.getSelectedFont() != null)
			{
				view.getShowSpacecraftPanel().setLabelFont(fontDialog.getSelectedFont());
				runs.setDistanceTextFont(fontDialog.getSelectedFont());
			}
		});

		view.getShowSpacecraftPanel().getSpacecraftColorButton().addActionListener(e ->
		{
			Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog",
					view.getShowSpacecraftPanel().getSpacecraftColor());
			if (tmpColor == null)
				return;
			view.getShowSpacecraftPanel().setSpacecraftColor(tmpColor);
			Icon spacecraftIcon = new ColorIcon(view.getShowSpacecraftPanel().getSpacecraftColor(), Color.BLACK, view.getShowSpacecraftPanel().getIconW(), 10);
			view.getShowSpacecraftPanel().getSpacecraftColorButton().setIcon(spacecraftIcon);
			runs.setSpacecraftColor(view.getShowSpacecraftPanel().getSpacecraftColor());
		});

		view.getShowEarthPanel().getEarthPointerColorButton().addActionListener(e ->
		{
			Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog",
					view.getShowEarthPanel().getEarthPointerColor());
			if (tmpColor == null)
				return;
			view.getShowEarthPanel().setEarthPointerColor(tmpColor);
			Icon earthIcon = new ColorIcon(view.getShowEarthPanel().getEarthPointerColor(), Color.BLACK, view.getShowEarthPanel().getIconW(), 10);
			view.getShowEarthPanel().getEarthPointerColorButton().setIcon(earthIcon);
			runs.setEarthDirectionMarkerColor(view.getShowEarthPanel().getEarthPointerColor());
		});

		view.getShowSunPanel().getSunPointerColorButton().addActionListener(e ->
		{
			Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog",
					view.getShowSunPanel().getSunPointerColor());
			if (tmpColor == null)
				return;
			view.getShowSunPanel().setSunPointerColor(tmpColor);
			Icon sunIcon = new ColorIcon(view.getShowSunPanel().getSunPointerColor(), Color.BLACK, view.getShowSunPanel().getIconW(), 10);
			view.getShowSunPanel().getSunPointerColorButton().setIcon(sunIcon);
			runs.setSunDirectionMarkerColor(view.getShowSunPanel().getSunPointerColor());
		});

		view.getShowSpacecraftPointerPanel().getScPointerColorButton().addActionListener(e ->
		{
			Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog", view.getShowSpacecraftPointerPanel().getScPointerColor());
			if (tmpColor == null)
				return;
			view.getShowSpacecraftPointerPanel().setScPointerColor(tmpColor);
			Icon scIcon = new ColorIcon(view.getShowSpacecraftPointerPanel().getScPointerColor(), Color.BLACK, view.getShowSpacecraftPointerPanel().getIconW(), 10);
			view.getShowSpacecraftPointerPanel().getScPointerColorButton().setIcon(scIcon);
			runs.setScDirectionMarkerColor(view.getShowSpacecraftPointerPanel().getScPointerColor());
		});

		view.getShowSpacecraftPanel().getScSizeSlider().addChangeListener(e ->
		{
			JSlider slider = (JSlider) e.getSource();
			runs.setSpacecraftSize(slider.getValue() * .0002);
		});

		view.getShowEarthPanel().getEarthSlider().addChangeListener(e ->
		{
			JSlider slider = (JSlider) e.getSource();
			runs.setEarthDirectionMarkerSize(slider.getValue());
		});

		view.getShowSunPanel().getSunSlider().addChangeListener(e ->
		{
			JSlider slider = (JSlider) e.getSource();
			runs.setSunDirectionMarkerSize(slider.getValue());
		});

		view.getShowSpacecraftPointerPanel().getSpacecraftSlider().addChangeListener(e ->
		{
			JSlider slider = (JSlider) e.getSource();
			runs.setSpacecraftDirectionMarkerSize(slider.getValue());
		});


	}

	@Override
	public void itemStateChanged(ItemEvent e) throws NullPointerException
	{
		Object source = e.getItemSelectable();
		StateHistory currentRun = runs.getCurrentRun();
		JCheckBox showEarthMarker = view.getShowEarthPanel().getShowEarthPointer();
		JCheckBox showSunMarker = view.getShowSunPanel().getShowSunPointer();
		JCheckBox showSpacecraft = view.getShowSpacecraftPanel().getShowSpacecraft();
		JCheckBox showSpacecraftMarker = view.getShowSpacecraftPointerPanel().getShowSpacecraftMarker();
		JComboBox distanceOptions = view.getShowSpacecraftPanel().getDistanceOptions();
		JCheckBox showLighting = view.getShowLightingPanel().getShowLighting();
		JSlider earthSlider = view.getShowEarthPanel().getEarthSlider();
		JSlider sunSlider = view.getShowSunPanel().getSunSlider();
		JSlider spacecraftSlider = view.getShowSpacecraftPointerPanel().getSpacecraftSlider();
		JLabel earthText = view.getShowEarthPanel().getEarthText();
		JLabel sunText = view.getShowSunPanel().getSunText();
		JLabel spacecraftText = view.getShowSpacecraftPointerPanel().getSpacecraftText();
		//
		// handles changes in options to show/hide different parts of the model.
		// Ex. pointers, lighting, trajectory
		//
		boolean selected = (e.getStateChange() == ItemEvent.SELECTED);

		if (source == showEarthMarker)
		{
			runs.setEarthDirectionMarkerVisibility(selected);
			earthSlider.setEnabled(selected);
			earthText.setEnabled(selected);
			System.out.println("StateHistoryViewControlsController: itemStateChanged: earth pointer color "
					+ view.getShowEarthPanel().getEarthPointerColor());
			runs.setEarthDirectionMarkerColor(view.getShowEarthPanel().getEarthPointerColor());
		} else if (source == showSunMarker)
		{
			runs.setSunDirectionMarkerVisibility(selected);
			sunSlider.setEnabled(selected);
			sunText.setEnabled(selected);
			runs.setSunDirectionMarkerColor(view.getShowSunPanel().getSunPointerColor());
		} else if (source == showSpacecraftMarker)
		{
			runs.setSpacecraftDirectionMarkerVisibility(selected);
			spacecraftSlider.setEnabled(selected);
			spacecraftText.setEnabled(selected);
			runs.setScDirectionMarkerColor(view.getShowSpacecraftPointerPanel().getScPointerColor());
		} else if (source == showSpacecraft)
		{
			// distanceOptions.setEnabled(selected);
			if (selected)
				historyModel.setDistanceText(distanceOptions.getSelectedItem().toString());
			// runs.setSpacecraftLabelVisibility(selected);
			runs.setSpacecraftVisibility(selected);
			view.getShowSpacecraftPanel().getScSizeSlider().setEnabled(selected);
		} else if (source == showLighting)
		{
			if (selected)
			{
				renderer.setFixedLightDirection(currentRun.getSunPosition());
				renderer.setLighting(LightingType.FIXEDLIGHT);
			} else
			{
				renderer.setLighting(LightingType.LIGHT_KIT);
			}
		}
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryDisplayItemsPanel getView()
	{
		return view;
	}
}
