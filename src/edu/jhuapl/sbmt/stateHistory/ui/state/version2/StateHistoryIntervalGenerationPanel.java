package edu.jhuapl.sbmt.stateHistory.ui.state.version2;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelIngestor;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.ui.DateTimeSpinner;

/**
 * @author steelrj1
 *
 */
public class StateHistoryIntervalGenerationPanel extends JPanel
{
	private static String PREGENDATASTRING = "Pregenerated Pointing";

	private static String SPICEDATASTRING = "SPICE Pointing";

	/**
	 *
	 */
	private JRadioButton pregenDataRadioButton;

	/**
	 *
	 */
	private JRadioButton spiceDataRadioButton;

	/**
	 *
	 */
	private ButtonGroup dataSourceButtonGroup;

	/**
	 *
	 */
	private JPanel dataSourceCards;

	private JPanel timeRangePanel;

	private String metakernelToLoad;

	private StateHistorySourceType stateHistorySourceType = StateHistorySourceType.PREGEN;

    /**
     * JLabel showing the available time to choose from
     */
    private JTextPane availableTimeLabel;

    /**
     * DateTimeSpinner to select the start time of the interval being generated
     */
    private DateTimeSpinner startTimeSpinner;

    /**
     * DateTimeSpinner to select the stop time of the interval being generated
     */
    private DateTimeSpinner stopTimeSpinner;

    /**
     * JButton that causes the interval to be generated
     */
    private JButton getIntervalButton;

    /**
     * Internal value for spinner size
     */
    private Dimension spinnerSize = new Dimension(400, 28);

    private String buttonText = "Get Interval";

    private boolean editMode;

    private StateHistory history;

    private boolean hasSpiceInfo;

    private SpiceKernelIngestor kernelIngestor;

	/**
	 * Constructor.
	 */
	public StateHistoryIntervalGenerationPanel(StateHistoryModel historyModel)
	{
		editMode = false;
		this.hasSpiceInfo = historyModel.getViewConfig().getSpiceInfo() != null;
		this.kernelIngestor = new SpiceKernelIngestor(historyModel.getCustomDataFolder());
		initUI();
	}

	public StateHistoryIntervalGenerationPanel(StateHistory history)
	{
		editMode = true;
		this.history = history;
		buttonText = "Update Interval";
		initUI();
		stateHistorySourceType = history.getType();
		if (stateHistorySourceType == StateHistorySourceType.SPICE) spiceDataRadioButton.setSelected(true);
		else pregenDataRadioButton.setSelected(true);
		Date startDate = StateHistoryTimeModel.getDateForET(history.getStartTime());
		SpinnerDateModel spinnerDateModel = new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH);
        startTimeSpinner.setModel(spinnerDateModel);

		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(startTimeSpinner, "yyyy-MMM-dd HH:mm:ss.SSS");
        startTimeSpinner.setEditor(dateEditor);
        System.out.println("StateHistoryIntervalGenerationPanel: StateHistoryIntervalGenerationPanel: history start time " + new Date(history.getStartTime().longValue()));
		startTimeSpinner.getModel().setValue(startDate);

		Date stopDate = StateHistoryTimeModel.getDateForET(history.getEndTime());
		spinnerDateModel = new SpinnerDateModel(stopDate, null, null, Calendar.DAY_OF_MONTH);
        stopTimeSpinner.setModel(spinnerDateModel);

		dateEditor = new JSpinner.DateEditor(stopTimeSpinner, "yyyy-MMM-dd HH:mm:ss.SSS");
        stopTimeSpinner.setEditor(dateEditor);
		stopTimeSpinner.getModel().setValue(stopDate);
	}

	/**
	 * Initializes the user interface elements
	 */
	private void initUI()
	{
        setBorder(new TitledBorder(null, "Interval Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        timeRangePanel = getTimeRangePanel();
        add(getRadioButtonPanel());

        add(getDataSouceCardsPanel());

        add(timeRangePanel);
        repaint();
	}

	public void updateStateHistory()
	{
		history.setType(stateHistorySourceType);
		DateTime startTime = startTimeSpinner.getISOFormattedTime();
        DateTime endTime = stopTimeSpinner.getISOFormattedTime();
		history.setStartTime(StateHistoryTimeModel.getETForDate(startTime.toDate()));
		history.setEndTime(StateHistoryTimeModel.getETForDate(endTime.toDate()));
	}

	@Override
	public void repaint()
	{
		super.repaint();
		if (dataSourceCards == null) return;
		dataSourceCards.setMinimumSize(new Dimension(this.getWidth(), 40));
		dataSourceCards.setPreferredSize(new Dimension(this.getWidth(), 40));
		dataSourceCards.setMaximumSize(new Dimension(this.getWidth(), 40));
	}

	private JPanel getDataSouceCardsPanel()
	{
		dataSourceCards = new JPanel(new CardLayout());

		dataSourceCards.add(getPregenTimeRangePanel(), PREGENDATASTRING);
		if (hasSpiceInfo)
			dataSourceCards.add(getSpiceTimeRangePanel(), SPICEDATASTRING);
		dataSourceCards.setMinimumSize(new Dimension(this.getWidth(), 40));
		dataSourceCards.setPreferredSize(new Dimension(this.getWidth(), 40));
		dataSourceCards.setMaximumSize(new Dimension(this.getWidth(), 40));
		return dataSourceCards;
	}

	private JPanel getRadioButtonPanel()
	{
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.X_AXIS));
		pregenDataRadioButton = new JRadioButton(PREGENDATASTRING);
		radioButtonPanel.add(pregenDataRadioButton);
		dataSourceButtonGroup = new ButtonGroup();
		dataSourceButtonGroup.add(pregenDataRadioButton);
		if (hasSpiceInfo)
		{
			spiceDataRadioButton = new JRadioButton(SPICEDATASTRING);
			radioButtonPanel.add(spiceDataRadioButton);
			dataSourceButtonGroup.add(spiceDataRadioButton);
			spiceDataRadioButton.addActionListener(e -> {
				CardLayout cl = (CardLayout)(dataSourceCards.getLayout());
				cl.show(dataSourceCards, SPICEDATASTRING);
				stateHistorySourceType = StateHistorySourceType.SPICE;
			});
		}

		pregenDataRadioButton.setSelected(true);
		pregenDataRadioButton.addActionListener(e -> {
			CardLayout cl = (CardLayout)(dataSourceCards.getLayout());
			cl.show(dataSourceCards, PREGENDATASTRING);
			stateHistorySourceType = StateHistorySourceType.PREGEN;
		});

		return radioButtonPanel;
	}

	private JPanel getPregenTimeRangePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalGlue());
		panel.add(new JLabel("Using pregenerated data on the server.  Please choose a time range below."));
		panel.add(Box.createVerticalGlue());
		return panel;
	}

	private JPanel getSpiceTimeRangePanel()
	{
		JPanel panel = new JPanel();
		JLabel metaKernelNameLabel = new JLabel();
//		JButton fileLoadButton = new JButton("Metakernel to load");
		List<String> loadedKernels = new ArrayList<String>();
		if (kernelIngestor.getLoadedKernelsDirectory().listFiles() != null)
			loadedKernels = Stream.of(kernelIngestor.getLoadedKernelsDirectory().listFiles()).filter(file -> file.isDirectory()).map(File::getName).collect(Collectors.toList());
		loadedKernels.add("Load new kernel...");
		String[] loadedKernelNamesArray = new String[loadedKernels.size()];
		loadedKernels.toArray(loadedKernelNamesArray);
		JComboBox<String> kernelComboBox = new JComboBox<String>(loadedKernelNamesArray);

		kernelComboBox.addActionListener(e -> {

			String selectedItem = (String)kernelComboBox.getSelectedItem();
			File loadedKernelsDirectory = kernelIngestor.getLoadedKernelsDirectory();
			if (selectedItem.equals("Load new kernel..."))
			{
				JFileChooser fileChooser = new JFileChooser();
				int showOpenDialogResult = fileChooser.showOpenDialog(StateHistoryIntervalGenerationPanel.this);
				if (showOpenDialogResult == JFileChooser.APPROVE_OPTION)
				{
					metakernelToLoad = fileChooser.getSelectedFile().getAbsolutePath();
					try
					{
						kernelIngestor.ingestMetaKernelToCache(metakernelToLoad);
					}
					catch (StateHistoryIOException | IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String newComboItemName = FilenameUtils.getBaseName(new File(metakernelToLoad).getName());
					kernelComboBox.addItem(newComboItemName);
					kernelComboBox.setSelectedItem(newComboItemName);
				}
			}
			else
			{
				File selectedKernelDirectory = new File(loadedKernelsDirectory, selectedItem);
				metakernelToLoad = new File(selectedKernelDirectory, selectedItem + ".mk").getAbsolutePath();
			}

		});

//		fileLoadButton.addActionListener(new ActionListener()
//		{
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				JFileChooser fileChooser = new JFileChooser();
//				int showOpenDialogResult = fileChooser.showOpenDialog(StateHistoryIntervalGenerationPanel.this);
//				if (showOpenDialogResult == JFileChooser.APPROVE_OPTION)
//				{
//					metakernelToLoad = fileChooser.getSelectedFile().getAbsolutePath();
//					metaKernelNameLabel.setText(metakernelToLoad);
//				}
//
//			}
//		});
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//		panel.add(fileLoadButton);
		panel.add(new JLabel("Select Metakernel"));
		panel.add(kernelComboBox);
		panel.add(metaKernelNameLabel);
		return panel;
	}

	private JPanel getTimeRangePanel()
	{
		JPanel timeRangePanel = new JPanel();
		timeRangePanel.setLayout(new BoxLayout(timeRangePanel, BoxLayout.Y_AXIS));
		JPanel panel_1 = new JPanel();
//		panel_1.setBackground(Color.red);
        panel_1.setBorder(null);
        timeRangePanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Available Time Range");
        panel_1.add(lblNewLabel);

        panel_1.add(Box.createRigidArea(new Dimension(40,0)));


        availableTimeLabel = new JTextPane();
        availableTimeLabel.setMaximumSize( new Dimension(Integer.MAX_VALUE, availableTimeLabel.getPreferredSize().height*2) );
        availableTimeLabel.setEditable(false);
        availableTimeLabel.setBackground(null);
        availableTimeLabel.setBorder(null);

        panel_1.add(availableTimeLabel);

        JPanel panel_2 = new JPanel();
//        panel_2.setBackground(Color.orange);
        panel_2.setBorder(null);
        timeRangePanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Start Time:");
        panel_2.add(lblNewLabel_2);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_1);

        startTimeSpinner = new DateTimeSpinner();
        startTimeSpinner.setMinimumSize(spinnerSize);
        startTimeSpinner.setMaximumSize(spinnerSize);
        startTimeSpinner.setPreferredSize(spinnerSize);
        panel_2.add(startTimeSpinner);

        JPanel panel_3 = new JPanel();
//        panel_3.setBackground(Color.yellow);
        panel_3.setBorder(null);
        timeRangePanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        JLabel lblNewLabel_3 = new JLabel("Stop Time:");
        panel_3.add(lblNewLabel_3);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_2);

        stopTimeSpinner = new DateTimeSpinner();
        stopTimeSpinner.setMinimumSize(spinnerSize);
        stopTimeSpinner.setMaximumSize(spinnerSize);
        stopTimeSpinner.setPreferredSize(spinnerSize);
        panel_3.add(stopTimeSpinner);

        JPanel panel_4 = new JPanel();
//        panel_4.setBackground(Color.green);
        panel_4.setBorder(null);
        timeRangePanel.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        getIntervalButton = new JButton(buttonText);
        panel_4.add(getIntervalButton);

        return timeRangePanel;
	}

    /**
     * Returns the available time label
     * @return the available time label
     */
    public JTextPane getAvailableTimeLabel()
    {
        return availableTimeLabel;
    }

    /**
     * Returns the start time spinner
     * @return the start time spinner
     */
    public DateTimeSpinner getStartTimeSpinner()
    {
        return startTimeSpinner;
    }

    /**
     * Returns the stop time spinner
     * @return the stop time spinner
     */
    public DateTimeSpinner getStopTimeSpinner()
    {
        return stopTimeSpinner;
    }

    /**
     * Returns the "Get Interval" button
     * @return the "Get Interval" button
     */
    public JButton getGetIntervalButton()
    {
        return getIntervalButton;
    }

	/**
	 * @return the metakernelToLoad
	 */
	public String getMetakernelToLoad()
	{
		return metakernelToLoad;
	}

	/**
	 * @return the stateHistorySourceType
	 */
	public StateHistorySourceType getStateHistorySourceType()
	{
		return stateHistorySourceType;
	}

	/**
	 * @return the editMode
	 */
	public boolean isEditMode()
	{
		return editMode;
	}
}