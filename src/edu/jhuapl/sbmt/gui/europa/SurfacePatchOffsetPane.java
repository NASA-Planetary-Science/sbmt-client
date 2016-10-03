package edu.jhuapl.sbmt.gui.europa;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.europa.SimulationRunCollection;

public class SurfacePatchOffsetPane extends JPanel implements ItemListener
{
    private SimulationRunCollection simulationRunCollection;

    private JCheckBox offsetCheckBox;

    public SurfacePatchOffsetPane(ModelManager modelManager)
    {
        simulationRunCollection = (SimulationRunCollection)modelManager.getModel(ModelNames.SIMULATION_RUN_COLLECTION);

        SurfacePatchOffsetChanger radialChanger = new SurfacePatchOffsetChanger(0.02, 540);
        radialChanger.setModel(simulationRunCollection);

        offsetCheckBox = new JCheckBox();

//        offsetCheckBox.setText("Show Trajectory");
//        offsetCheckBox.setSelected(false);
//        offsetCheckBox.addItemListener(this);
//
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

//        add(Box.createVerticalStrut(10));
//        add(offsetCheckBox);
//        add(Box.createVerticalStrut(15));
        add(radialChanger);
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getItemSelectable() == this.offsetCheckBox)
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
