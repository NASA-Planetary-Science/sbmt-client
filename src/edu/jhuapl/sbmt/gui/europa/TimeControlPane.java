package edu.jhuapl.sbmt.gui.europa;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.europa.SimulationRunCollection;

public class TimeControlPane extends JPanel implements ItemListener
{
    private SimulationRunCollection simulationRunCollection;

    private JCheckBox flybyCheckBox;

    public TimeControlPane(ModelManager modelManager)
    {
        simulationRunCollection = (SimulationRunCollection)modelManager.getModel(ModelNames.SIMULATION_RUN_COLLECTION);

        TimeChanger radialChanger = new TimeChanger();
        radialChanger.setModel(simulationRunCollection);

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
                simulationRunCollection.setShowTrajectories(false);
            }
            else
            {
                simulationRunCollection.setShowTrajectories(true);
            }
        }
    }
}
