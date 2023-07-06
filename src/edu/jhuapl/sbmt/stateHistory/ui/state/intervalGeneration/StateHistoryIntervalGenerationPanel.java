package edu.jhuapl.sbmt.stateHistory.ui.state.intervalGeneration;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.ui.SBMTDateSpinner;
import edu.jhuapl.sbmt.stateHistory.controllers.kernel.KernelManagementController;
import edu.jhuapl.sbmt.stateHistory.controllers.kernel.KernelSetChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.SpiceKernelLoadStatusListener;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelIngestor;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;

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
    private SBMTDateSpinner startTimeSpinner;

    /**
     * DateTimeSpinner to select the stop time of the interval being generated
     */
    private SBMTDateSpinner stopTimeSpinner;

    /**
     * JButton that causes the interval to be generated
     */
    private JButton getIntervalButton;

    private JButton cancelButton;

    /**
     * Internal value for spinner size
     */
    private Dimension spinnerSize = new Dimension(400, 28);

    private String buttonText = "Get Interval";

    private boolean editMode;

    private StateHistory history;

    private boolean hasSpiceInfo;

    private boolean hasPregenInfo;

    private SpiceKernelIngestor kernelIngestor;

    private KernelIngestor ingestor;

    private KernelManagementController kernelManagementController;

    private JComboBox<String> kernelComboBox;


	/**
	 * Constructor.
	 */
	public StateHistoryIntervalGenerationPanel(StateHistoryModel historyModel)
	{
		editMode = false;
		this.hasSpiceInfo = historyModel.getViewConfig().getSpiceInfo() != null;
		this.hasPregenInfo = historyModel.getViewConfig().getTimeHistoryFile() != null;
		this.kernelIngestor = new SpiceKernelIngestor(historyModel.getCustomDataFolder());
		kernelManagementController = new KernelManagementController(kernelIngestor.getLoadedKernelsDirectory(), new KernelSetChangedListener()
		{

			@Override
			public void kernelDeleted(String kernelName)
			{
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(getAvailableKernels());
				kernelComboBox.setModel(model);
			}
		});
		initUI();
	}

	public StateHistoryIntervalGenerationPanel(StateHistoryModel historyModel, StateHistory history)
	{
		editMode = true;
		this.history = history;
		buttonText = "Update Interval";
		this.hasSpiceInfo = historyModel.getViewConfig().getSpiceInfo() != null;
		this.hasPregenInfo = historyModel.getViewConfig().getTimeHistoryFile() != null;
		this.kernelIngestor = new SpiceKernelIngestor(historyModel.getCustomDataFolder());
		kernelManagementController = new KernelManagementController(kernelIngestor.getLoadedKernelsDirectory(), new KernelSetChangedListener()
		{

			@Override
			public void kernelDeleted(String kernelName)
			{
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(getAvailableKernels());
				kernelComboBox.setModel(model);
			}
		});
		initUI();
		IStateHistoryMetadata metadata = history.getMetadata();
		stateHistorySourceType = metadata.getType();
		if (stateHistorySourceType == StateHistorySourceType.SPICE && hasPregenInfo) spiceDataRadioButton.setSelected(true);
		else if (hasPregenInfo) pregenDataRadioButton.setSelected(true);
		Date startDate = StateHistoryTimeModel.getDateForET(metadata.getStartTime());
		startTimeSpinner.setDate(startDate);

		Date stopDate = StateHistoryTimeModel.getDateForET(metadata.getEndTime());
		stopTimeSpinner.setDate(stopDate);
		getIntervalButton.setEnabled(true);
	}

	/**
	 * Initializes the user interface elements
	 */
	private void initUI()
	{
        setBorder(new TitledBorder(null, "Interval Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        timeRangePanel = getTimeRangePanel();

        if (hasPregenInfo == false)
        {
        	add(getSpiceTimeRangePanel());
        	stateHistorySourceType = StateHistorySourceType.SPICE;
        }
        else
        {
        	add(getRadioButtonPanel());

        	add(getDataSouceCardsPanel());
        }
        add(timeRangePanel);
        repaint();
	}

	public void updateStateHistory()
	{
		IStateHistoryMetadata metadata = history.getMetadata();
		metadata.setType(stateHistorySourceType);
//		DateTime startTime = startTimeSpinner.getISOFormattedTime();
//        DateTime endTime = stopTimeSpinner.getISOFormattedTime();
		metadata.setStartTime(StateHistoryTimeModel.getETForDate(startTimeSpinner.getDate()));
		metadata.setEndTime(StateHistoryTimeModel.getETForDate(stopTimeSpinner.getDate()));
		history.getTrajectoryMetadata().getTrajectory().setStartTime(StateHistoryTimeModel.getETForDate(startTimeSpinner.getDate()));
		history.getTrajectoryMetadata().getTrajectory().setStopTime(StateHistoryTimeModel.getETForDate(stopTimeSpinner.getDate()));
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

		if (hasPregenInfo)
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

	public void refreshKernels()
	{
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(getAvailableKernels());
		kernelComboBox.setModel(model);
	}

	private String[] getAvailableKernels()
	{
		List<String> loadedKernels = new ArrayList<String>();
		if (kernelIngestor.getLoadedKernelsDirectory().listFiles() != null)
			loadedKernels = Stream.of(kernelIngestor.getLoadedKernelsDirectory().listFiles()).filter(file -> file.isDirectory()).map(File::getName).collect(Collectors.toList());
		loadedKernels.add(0, "Load new kernel...");
		String[] loadedKernelNamesArray = new String[loadedKernels.size()];
		loadedKernels.toArray(loadedKernelNamesArray);
		return loadedKernelNamesArray;
	}

	private JPanel getSpiceTimeRangePanel()
	{
		JPanel panel = new JPanel();
		JLabel metaKernelNameLabel = new JLabel();
		JProgressBar progressBar = new JProgressBar();
		cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(false);


		kernelComboBox = new JComboBox<String>(getAvailableKernels());

		kernelComboBox.addActionListener(e -> {


			String selectedItem = (String)kernelComboBox.getSelectedItem();
			File loadedKernelsDirectory = kernelIngestor.getLoadedKernelsDirectory();
			if (selectedItem.equals("Load new kernel..."))
			{
				File file = CustomFileChooser.showOpenDialog(this, "Select Metakernel");
				if (file == null)
				{
					getIntervalButton.setEnabled(false);
					return;
				}
				metakernelToLoad = file.getAbsolutePath();
				ingestor  = new KernelIngestor(progressBar, kernelComboBox);
				ingestor.execute();
			}
			else
			{
				File selectedKernelDirectory = new File(loadedKernelsDirectory, selectedItem);
				metakernelToLoad = new File(selectedKernelDirectory, selectedItem + ".mk").getAbsolutePath();
			}
			getIntervalButton.setEnabled(!selectedItem.equals("Load new kernel..."));

		});

		cancelButton.addActionListener( l -> {
			ingestor.cancel(true);
			File loadedKernelsDirectory = kernelIngestor.getLoadedKernelsDirectory();
			File selectedKernelDirectory = new File(loadedKernelsDirectory, FilenameUtils.getBaseName(metakernelToLoad));
			try {
				FileUtils.deleteDirectory(selectedKernelDirectory);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		});

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		if (editMode == false)
		{
			panel.add(new JLabel("Select Metakernel"));
			panel.add(kernelComboBox);

			panel.add(metaKernelNameLabel);
			panel.add(progressBar);
			panel.add(cancelButton);
		}
		return panel;
	}

	public JToolBar getToolbar()
	{
		JToolBar toolbar = new JToolBar();
    	JButton manageKernels = new JButton("Manage Kernels");
    	toolbar.add(manageKernels);
    	manageKernels.addActionListener(l -> {
    		JFrame frame2 = new JFrame("Manage Kernels");
    		frame2.add(kernelManagementController.getView());
    		frame2.setMinimumSize(new Dimension(800, frame2.getHeight()));
        	frame2.pack();
        	frame2.setVisible(true);
    	});
    	return toolbar;
	}

	class KernelIngestor extends SwingWorker<Void, Void>
	{
		JProgressBar progressBar;
		JComboBox<String> kernelComboBox;

		public KernelIngestor(JProgressBar progressBar, JComboBox<String> kernelComboBox)
		{
			this.progressBar = progressBar;
			this.kernelComboBox = kernelComboBox;
		}

		@Override
		protected Void doInBackground() throws Exception
		{
			try
			{
				cancelButton.setEnabled(true);
				metakernelToLoad = kernelIngestor.ingestMetaKernelToCache(metakernelToLoad, new SpiceKernelLoadStatusListener() {

					@Override
					public void percentageLoaded(double percentage) {
						progressBar.setValue((int)percentage);
					}
				});
			}
			catch (StateHistoryIOException | IOException e1)
			{
				if (!isCancelled())
				{
					JOptionPane.showMessageDialog(StateHistoryIntervalGenerationPanel.this, "Problem ingesting SPICE kernel.  Please check the file for correctness.",
												"Ingestion Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
			finally {
				progressBar.setValue(0);
				cancelButton.setEnabled(false);
			}
			return null;
		}

		@Override
		protected void done()
		{
			if (!isCancelled())
			{
				String newComboItemName = FilenameUtils.getBaseName(new File(metakernelToLoad).getName());

				kernelComboBox.addItem(newComboItemName);
				kernelComboBox.setSelectedItem(newComboItemName);
			}
			super.done();
		}

	}

	private JPanel getTimeRangePanel()
	{
		JPanel timeRangePanel = new JPanel();
		timeRangePanel.setLayout(new BoxLayout(timeRangePanel, BoxLayout.Y_AXIS));
		JPanel panel_1 = new JPanel();
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
        panel_2.setBorder(null);
        timeRangePanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Start Time:");
        panel_2.add(lblNewLabel_2);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_1);

        startTimeSpinner = new SBMTDateSpinner();
        startTimeSpinner.setMinimumSize(spinnerSize);
        startTimeSpinner.setMaximumSize(spinnerSize);
        startTimeSpinner.setPreferredSize(spinnerSize);
        panel_2.add(startTimeSpinner);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(null);
        timeRangePanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        JLabel lblNewLabel_3 = new JLabel("Stop Time:");
        panel_3.add(lblNewLabel_3);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_2);

        stopTimeSpinner = new SBMTDateSpinner();
        stopTimeSpinner.setMinimumSize(spinnerSize);
        stopTimeSpinner.setMaximumSize(spinnerSize);
        stopTimeSpinner.setPreferredSize(spinnerSize);
        panel_3.add(stopTimeSpinner);

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        timeRangePanel.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        getIntervalButton = new JButton(buttonText);
        getIntervalButton.setEnabled(false);
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
    public SBMTDateSpinner getStartTimeSpinner()
    {
        return startTimeSpinner;
    }

    /**
     * Returns the stop time spinner
     * @return the stop time spinner
     */
    public SBMTDateSpinner getStopTimeSpinner()
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