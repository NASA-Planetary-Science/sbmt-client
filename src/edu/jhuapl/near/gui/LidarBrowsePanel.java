package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.near.model.LidarBrowseDataCollection;
import edu.jhuapl.near.model.LidarDataPerUnit;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.DoublePair;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;


abstract public class LidarBrowsePanel extends JPanel implements ListSelectionListener
{
    private final String LIDAR_REMOVE_ALL_BUTTON_TEXT = "Remove All Lidar Data";

    private LidarBrowseDataCollection lidarModel;
    private JList resultList;
    private DefaultListModel lidarResultListModel;
    private ArrayList<String> lidarRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private JButton showHideButton;
    private JButton removeAllButton;
    private JButton saveButton;
    private LidarTimeIntervalChanger timeIntervalChanger;
    private RadialOffsetChanger radialOffsetChanger;


    public class LidarTimeIntervalChanger extends JPanel implements ChangeListener
    {
        private RangeSlider slider;

        private LidarDataPerUnit lidarData;

        public LidarTimeIntervalChanger()
        {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            setBorder(BorderFactory.createTitledBorder("Displayed Lidar Data"));

            slider = new RangeSlider(0, 255, 0, 255);
            slider.setPaintTicks(true);
            slider.setMajorTickSpacing(10);
            slider.setPaintTrack(true);
            slider.addChangeListener(this);
            slider.setEnabled(false);
            add(slider);
        }

        void setLidarData(LidarDataPerUnit data)
        {
            if (data != null)
            {
                lidarData = data;
                DoublePair pair = data.getPercentageShown();
                slider.setLowValue((int)(pair.d1*slider.getMaximum()));
                slider.setHighValue((int)(pair.d2*slider.getMaximum()));
                slider.setEnabled(true);
            }
            else
            {
                slider.setEnabled(false);
            }
        }

        public void stateChanged(ChangeEvent e)
        {
            double lowVal = (double)slider.getLowValue()/(double)slider.getMaximum();
            double highVal = (double)slider.getHighValue()/(double)slider.getMaximum();
            if (lidarData != null)
                lidarData.setPercentageShown(lowVal, highVal);
        }
    }

    public LidarBrowsePanel(
            final ModelManager modelManager)
    {
        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        //this.modelManager = modelManager;
        this.lidarModel = (LidarBrowseDataCollection)modelManager.getModel(getModelName());


        JPanel resultsPanel = new JPanel(new BorderLayout());

        resultsLabel = new JLabel("Available Files");

        lidarResultListModel = new DefaultListModel();

        lidarRawResults = lidarModel.getAllLidarPaths();
        for (String str : lidarRawResults)
        {
            if (str.toLowerCase().endsWith(".gz"))
                str = str.substring(0, str.length()-3);
            lidarResultListModel.addElement(new File(str).getName());
        }

        //Create the list and put it in a scroll pane.
        resultList = new JList(lidarResultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(listScrollPane, BorderLayout.CENTER);

        final JPanel resultControlsPanel = new JPanel(new BorderLayout());

        final JPanel resultSub1ControlsPanel = new JPanel();

        resultSub1ControlsPanel.setLayout(new BoxLayout(resultSub1ControlsPanel,
                BoxLayout.PAGE_AXIS));


        showHideButton = new JButton("Show");
        showHideButton.setActionCommand("Show");
        showHideButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    try
                    {
                        if (showHideButton.getText().startsWith("Show"))
                        {
                            lidarModel.addLidarData(lidarRawResults.get(index));

                            showHideButton.setText("Remove");
                            timeIntervalChanger.setLidarData(lidarModel.getLidarData(lidarRawResults.get(index)));
                        }
                        else
                        {
                            lidarModel.removeLidarData(lidarRawResults.get(index));

                            showHideButton.setText("Show");
                            timeIntervalChanger.setLidarData(null);
                        }
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        });
        showHideButton.setEnabled(false);


        saveButton = new JButton("Save...");
        saveButton.setActionCommand("Save...");
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    File tmp = new File(lidarRawResults.get(index));
                    File file = CustomFileChooser.showSaveDialog(
                            saveButton.getParent(),
                            "Save Lidar data",
                            tmp.getName().substring(0, tmp.getName().length()-3));

                    try
                    {
                        if (file != null)
                        {
                            File lidarFile = FileCache.getFileFromServer(lidarRawResults.get(index));

                            FileUtil.copyFile(lidarFile, file);
                        }
                    }
                    catch(Exception ex)
                    {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(saveButton),
                                "Unable to save file to " + file.getAbsolutePath(),
                                "Error Saving File",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }

                }
            }
        });
        saveButton.setEnabled(false);


        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
                BoxLayout.LINE_AXIS));
        removeAllButton = new JButton(LIDAR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(LIDAR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                LidarBrowseDataCollection model = (LidarBrowseDataCollection)modelManager.getModel(getModelName());
                model.removeAllLidarData();

                showHideButton.setText("Show");
                timeIntervalChanger.setLidarData(null);
            }
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        resultSub2ControlsPanel.add(showHideButton);
        resultSub2ControlsPanel.add(removeAllButton);
        resultSub2ControlsPanel.add(saveButton);

        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);

        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        timeIntervalChanger = new LidarTimeIntervalChanger();

        radialOffsetChanger = new RadialOffsetChanger();
        radialOffsetChanger.setModel(lidarModel);

        add(resultsPanel);
        add(timeIntervalChanger);
        add(radialOffsetChanger);
    }


    public void valueChanged(ListSelectionEvent arg0)
    {
        int[] idx = {arg0.getFirstIndex(), arg0.getLastIndex()};
        for (int index : idx)
        {
            if (index >= 0 && resultList.isSelectedIndex(index))
            {
                showHideButton.setEnabled(true);
                saveButton.setEnabled(true);

                //resultList.setSelectedIndex(index);
                if (lidarModel.containsLidarData(lidarRawResults.get(index)))
                {
                    showHideButton.setText("Remove");
                    timeIntervalChanger.setLidarData(lidarModel.getLidarData(lidarRawResults.get(index)));
                }
                else
                {
                    showHideButton.setText("Show");
                    timeIntervalChanger.setLidarData(null);
                }
                break;
            }
            else
            {
                showHideButton.setEnabled(false);
                saveButton.setEnabled(false);
            }
        }
    }

    abstract protected String getModelName();
}
