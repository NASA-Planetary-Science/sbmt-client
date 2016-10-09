package edu.jhuapl.sbmt.gui.time;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.time.StateHistoryCollection;

public class TimeControlPane extends JPanel implements ItemListener
{
    private StateHistoryCollection stateHistoryCollection;

    private JCheckBox flybyCheckBox;

    public TimeControlPane(ModelManager modelManager)
    {
        stateHistoryCollection = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        TimeChanger radialChanger = new TimeChanger();
        radialChanger.setModel(stateHistoryCollection);

        flybyCheckBox = new JCheckBox();
//        flybyCheckBox.setText("Show Trajectory");
//        flybyCheckBox.setSelected(false);
//        flybyCheckBox.addItemListener(this);
//
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

//        add(Box.createVerticalStrut(10));
//        add(flybyCheckBox);
//        add(Box.createVerticalStrut(15));
        add(radialChanger);
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getItemSelectable() == this.flybyCheckBox)
        {
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
                stateHistoryCollection.setShowTrajectories(false);
            }
            else
            {
                stateHistoryCollection.setShowTrajectories(true);
            }
        }
    }
}
