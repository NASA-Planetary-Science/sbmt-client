package edu.jhuapl.sbmt.gui.dtm;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dtm.DemMapAndShowTable;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class DtmCreationPanel extends JPanel
{
    final DEMCollection dems;
    final DEMBoundaryCollection boundaries;
    DemMapAndShowTable table;

    public DtmCreationPanel(DEMCollection dems, DEMBoundaryCollection boundaries) {
        this.dems=dems;
        this.boundaries=boundaries;
        table=new DemMapAndShowTable(dems);

        setLayout(new MigLayout("", "[1px][grow]", "[1px][288.00]"));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, "cell 1 1,grow");

        scrollPane.setViewportView(DemMapAndShowTable.createSwingWrapper(table).getComponent());

        populateWithDummyData();

    }

    protected void populateWithDummyData()
    {
        for (int i=0; i<10; i++)
        {
            table.appendRow(new DummyDtmMetaData());
        }
    }

}
