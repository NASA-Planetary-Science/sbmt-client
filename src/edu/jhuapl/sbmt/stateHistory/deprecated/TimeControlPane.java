//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.util.Date;
//
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JSpinner;
//
//import org.joda.time.DateTime;
//import org.joda.time.format.ISODateTimeFormat;
//
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryCollection;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
//
//public class TimeControlPane extends JPanel implements ItemListener
//{
//    private StateHistoryCollection stateHistoryCollection;
//
//    private JCheckBox flybyCheckBox;
//    private boolean playChecked = false;
//    TimeChanger radialChanger;
//
//    private JLabel timeText;
//
//    private JPanel UTCtimePanel;
//
//    private JSpinner timeBox;
//
//    private JButton timeButton;
//
//    public TimeControlPane(ModelManager modelManager, final StateHistoryPanel panel)
//    {
//        stateHistoryCollection = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
//
//        radialChanger = new TimeChanger();
//        playChecked = radialChanger.playButtonChecked();
//        radialChanger.setModel(stateHistoryCollection);
//
//
//        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//
//        // time changer
//        add(radialChanger);
//
//
//
//        // time selecter
//        UTCtimePanel = new JPanel();
//
//        GridBagConstraints gbc_UTCtimePanel = new GridBagConstraints();
//        gbc_UTCtimePanel.fill = GridBagConstraints.BOTH;
//        gbc_UTCtimePanel.insets = new Insets(0, 0, 5, 0);
//        gbc_UTCtimePanel.gridx = 2;
//        gbc_UTCtimePanel.gridy = 4;
//        add(UTCtimePanel, gbc_UTCtimePanel);
//        GridBagLayout gbl_UTCtimePanel = new GridBagLayout();
//        gbl_UTCtimePanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        gbl_UTCtimePanel.rowHeights = new int[]{0, 0};
//        gbl_UTCtimePanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
//        gbl_UTCtimePanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
//        UTCtimePanel.setLayout(gbl_UTCtimePanel);
//        timeText = new JLabel("Enter UTC Time:");
//        UTCtimePanel.add(timeText);
//        GridBagConstraints gbc_timeText = new GridBagConstraints();
//        gbc_timeText.insets = new Insets(0, 0, 0, 5);
//        gbc_timeText.gridx = 0;
//        gbc_timeText.gridy = 0;
//
//        timeBox = new JSpinner();
//        GridBagConstraints gbc_timeBox = new GridBagConstraints();
//        gbc_timeBox.gridwidth = 7;
//        gbc_timeBox.fill = GridBagConstraints.HORIZONTAL;
//        gbc_timeBox.insets = new Insets(0, 0, 0, 5);
//        gbc_timeBox.gridx = 1;
//        gbc_timeBox.gridy = 0;
//        UTCtimePanel.add(timeBox, gbc_timeBox);
//        timeBox.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
//        timeBox.setEditor(new javax.swing.JSpinner.DateEditor(timeBox, "yyyy-MMM-dd HH:mm:ss.SSS"));
//        timeBox.setMinimumSize(new java.awt.Dimension(36, 22));
//        timeBox.setPreferredSize(new java.awt.Dimension(200, 28));
//        timeButton = new JButton("Set Time");
//        GridBagConstraints gbc_timeButton = new GridBagConstraints();
//        gbc_timeButton.insets = new Insets(0, 0, 0, 5);
//        gbc_timeButton.gridx = 8;
//        gbc_timeButton.gridy = 0;
//        UTCtimePanel.add(timeButton, gbc_timeButton);
//        timeButton.setEnabled(true);
//
//        timeButton.addActionListener(new ActionListener()
//        {
//
//
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                StateHistoryModel currentRun = stateHistoryCollection.getCurrentRun();
//                if (currentRun != null)
//                {
//                    Date enteredTime = (Date) timeBox.getModel().getValue();
//                    DateTime dt = new DateTime(enteredTime);
//                    DateTime dt1 = ISODateTimeFormat.dateTimeParser()
//                            .parseDateTime(dt.toString());
//                    boolean success = currentRun.setInputTime(dt1, panel);
//                    if (success) // only call again if the first call was a success
//                        currentRun.setInputTime(dt1, panel); //The method needs to run twice because running once gets it close to the input but not exact. Twice shows the exact time. I don't know why.
//                }else{
//                    JOptionPane.showMessageDialog(null, "No Time Interval selected.", "Error",
//                            JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//            }
//        });
//
//
//
//    }
//
//    public void itemStateChanged(ItemEvent e)
//    {
//        if (e.getItemSelectable() == this.flybyCheckBox)
//        {
//            if (e.getStateChange() == ItemEvent.DESELECTED)
//            {
//                stateHistoryCollection.setShowTrajectories(false);
//            }
//            else
//            {
//                stateHistoryCollection.setShowTrajectories(true);
//            }
//        }
//    }
//
//    public boolean getPlayButtonChecked(){
//        return playChecked;
//    }
//
//    public TimeChanger getTimeChanger(){
//        return radialChanger;
//    }
//}
