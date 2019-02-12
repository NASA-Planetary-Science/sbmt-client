package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.gui.lidar.v2.LidarSearchModel;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used to provide a search for lidar tracks capability.
 */
public class LidarSearchPanel extends JPanel implements ActionListener, ChangeListener, PickManagerListener
{
	// Reference vars
	private ModelManager refModelManager;
	private LidarSearchModel refModel;
	private PickManager refPickManager;
	private Picker refPicker;

	// GUI vars
	private LidarListPanel listPanel;
	private JComboBox<String> sourceBox;
	private JButton manageSourcesB;
	private JSpinner begDateSpinner;
	private JSpinner endDateSpinner;
	private JFormattedTextField minTrackSizeTF;
	private JFormattedTextField trackSeparationTF;
	private JToggleButton selectRegionB;
	private JButton clearRegionB;
	private JButton searchB;

	protected JPanel searchPropertiesPanel;

	/**
	 * Constructor
	 */
	public LidarSearchPanel(ModelManager aModelManager, LidarSearchModel aModel, PickManager aPickManager,
			Renderer aRenderer)
	{
		refModelManager = aModelManager;
		refModel = aModel;
		refPickManager = aPickManager;
		refPicker = refPickManager.getPickerForPickMode(PickMode.CIRCLE_SELECTION);

		setLayout(new MigLayout("", "0[]0", "0[]0"));

		JPanel searchPanel = formSearchPanel();
		add(searchPanel, "growx,span,wrap");

		listPanel = new LidarListPanel(aModelManager, refModel.getLidarModel(), aPickManager, aRenderer);
		add(listPanel, "growx,growy,pushx,pushy");

		// Initialize the begDate, endDate spinners
		if (refModel.getSmallBodyConfig().hasLidarData == true)
		{
			Date begDate = refModel.getSmallBodyConfig().lidarSearchDefaultStartDate;
			Date endDate = refModel.getSmallBodyConfig().lidarSearchDefaultEndDate;
			refModel.setStartDate(begDate);
			((SpinnerDateModel) begDateSpinner.getModel()).setValue(begDate);
			refModel.setEndDate(endDate);
			((SpinnerDateModel) endDateSpinner.getModel()).setValue(endDate);
		}

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, refPicker, this);
		refPickManager.addListener(this);
		begDateSpinner.addChangeListener(this);
		endDateSpinner.addChangeListener(this);
	}

	public JComboBox<String> getSourceComboBox()
	{
		return sourceBox;
	}

	public JButton getManageSourcesButton()
	{
		return manageSourcesB;
	}

	public JFormattedTextField getMinTrackSizeTextField()
	{
		return minTrackSizeTF;
	}

	public JFormattedTextField getTrackSeparationTextField()
	{
		return trackSeparationTF;
	}

	public JButton getSearchButton()
	{
		return searchB;
	}

	public JSpinner getStartDateSpinner()
	{
		return begDateSpinner;
	}

	public JSpinner getEndDateSpinner()
	{
		return endDateSpinner;
	}

	/**
	 * See {@link LidarListPanel#injectNewLidarModel}
	 */
	@Deprecated
	public void injectNewLidarModel(LidarSearchDataCollection aLidarModel, Renderer aRenderer)
	{
		listPanel.injectNewLidarModel(aLidarModel, aRenderer);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == selectRegionB)
			doActionSelectRegion();
		else if (source == clearRegionB)
			doActionClearRegion();
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = refPicker == refPickManager.getActivePicker();
		selectRegionB.setSelected(tmpBool);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == begDateSpinner)
		{
			Date date = ((SpinnerDateModel) begDateSpinner.getModel()).getDate();
			if (date != null)
				refModel.setStartDate(date);
		}
		else if (source == begDateSpinner)
		{
			Date date = ((SpinnerDateModel) endDateSpinner.getModel()).getDate();
			if (date != null)
				refModel.setEndDate(date);
		}
	}

	/**
	 * Helper method that handles the clear region action.
	 */
	private void doActionClearRegion()
	{
		AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) refModelManager
				.getModel(ModelNames.CIRCLE_SELECTION);
		selectionModel.removeAllStructures();
	}

	/**
	 * Helper method that handles the select region action.
	 */
	private void doActionSelectRegion()
	{
		Picker targPicker = null;
		if (selectRegionB.isSelected() == true)
			targPicker = refPicker;

		refPickManager.setActivePicker(targPicker);
	}

	/**
	 * Helper method that forms the "Search" panel.
	 */
	private JPanel formSearchPanel()
	{
		JPanel retPanel;

		retPanel = new JPanel();
		retPanel.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		retPanel.setLayout(new BoxLayout(retPanel, BoxLayout.Y_AXIS));

		JPanel sourcePanel = new JPanel();
		retPanel.add(sourcePanel);
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));

		JLabel lblNewLabel = new JLabel("Source:");
		sourcePanel.add(lblNewLabel);

		sourceBox = new JComboBox<>();
		sourceBox.setModel(new DefaultComboBoxModel<>(new String[] { "Default" }));
		sourcePanel.add(sourceBox);

		manageSourcesB = new JButton("Manage");
		sourcePanel.add(manageSourcesB);

		JPanel timeRangePanel = new JPanel();
		timeRangePanel.setBorder(null);
		retPanel.add(timeRangePanel);
		timeRangePanel.setLayout(new BoxLayout(timeRangePanel, BoxLayout.X_AXIS));

		JLabel lblStart = new JLabel("Start:");
		timeRangePanel.add(lblStart);

		begDateSpinner = new JSpinner();
		begDateSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(951714000000L), null, null,
				java.util.Calendar.DAY_OF_MONTH));
		begDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(begDateSpinner, "yyyy-MMM-dd HH:mm:ss"));
		begDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, begDateSpinner.getPreferredSize().height));
		timeRangePanel.add(begDateSpinner);

		JLabel lblEnd = new JLabel("End:");
		timeRangePanel.add(lblEnd);

		endDateSpinner = new JSpinner();
		endDateSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(982040400000L), null, null,
				java.util.Calendar.DAY_OF_MONTH));
		endDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endDateSpinner, "yyyy-MMM-dd HH:mm:ss"));
		endDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, endDateSpinner.getPreferredSize().height));
		timeRangePanel.add(endDateSpinner);

		Component verticalStrut = Box.createVerticalStrut(20);
		retPanel.add(verticalStrut);

		searchPropertiesPanel = new JPanel();
		searchPropertiesPanel.setBorder(null);
		retPanel.add(searchPropertiesPanel);
		GridBagLayout gbl_searchPropertiesPanel = new GridBagLayout();
		gbl_searchPropertiesPanel.columnWidths = new int[] { 95, 30, 30, 0, 75, 0, 0, 30, 30 };
		gbl_searchPropertiesPanel.rowHeights = new int[] { 26, 0, 0, 0, 0 };
		gbl_searchPropertiesPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_searchPropertiesPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		searchPropertiesPanel.setLayout(gbl_searchPropertiesPanel);

		JLabel lblMinTrackSize = new JLabel("Min Track Size:");
		GridBagConstraints gbc_lblMinTrackSize = new GridBagConstraints();
		gbc_lblMinTrackSize.anchor = GridBagConstraints.WEST;
		gbc_lblMinTrackSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinTrackSize.gridx = 0;
		gbc_lblMinTrackSize.gridy = 0;
		searchPropertiesPanel.add(lblMinTrackSize, gbc_lblMinTrackSize);

		minTrackSizeTF = new JFormattedTextField();
		minTrackSizeTF.setText("10");
		minTrackSizeTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, minTrackSizeTF.getPreferredSize().height));

		GridBagConstraints gbc_minTrackSizeTextField = new GridBagConstraints();
		gbc_minTrackSizeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minTrackSizeTextField.gridwidth = 2;
		gbc_minTrackSizeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minTrackSizeTextField.gridx = 1;
		gbc_minTrackSizeTextField.gridy = 0;
		searchPropertiesPanel.add(minTrackSizeTF, gbc_minTrackSizeTextField);

		JLabel lblNewLabel_1 = new JLabel("Track Separation (sec):");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 3;
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 4;
		gbc_lblNewLabel_1.gridy = 0;
		searchPropertiesPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		trackSeparationTF = new JFormattedTextField();
		trackSeparationTF.setText("10");
		trackSeparationTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, trackSeparationTF.getPreferredSize().height));

		GridBagConstraints gbc_trackSeparationTextField = new GridBagConstraints();
		gbc_trackSeparationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_trackSeparationTextField.gridwidth = 2;
		gbc_trackSeparationTextField.insets = new Insets(0, 0, 5, 0);
		gbc_trackSeparationTextField.gridx = 7;
		gbc_trackSeparationTextField.gridy = 0;
		searchPropertiesPanel.add(trackSeparationTF, gbc_trackSeparationTextField);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		retPanel.add(verticalStrut_1);

		JPanel searchButtonsPanel = new JPanel();
		retPanel.add(searchButtonsPanel);

		searchButtonsPanel.setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.X_AXIS));

		selectRegionB = new JToggleButton("Select Region");
		selectRegionB.addActionListener(this);
		searchButtonsPanel.add(selectRegionB);

		clearRegionB = new JButton("Clear Region");
		clearRegionB.addActionListener(this);
		searchButtonsPanel.add(clearRegionB);

		searchB = new JButton("Search");
		searchButtonsPanel.add(searchB);

		return retPanel;
	}

}
