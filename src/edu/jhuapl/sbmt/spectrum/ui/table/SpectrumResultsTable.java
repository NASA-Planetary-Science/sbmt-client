package edu.jhuapl.sbmt.spectrum.ui.table;

import javax.swing.JTable;

public class SpectrumResultsTable extends JTable
{
    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;

    public SpectrumResultsTable()
    {
        mapColumnIndex=0;
        showFootprintColumnIndex=1;
        frusColumnIndex=2;
        bndrColumnIndex=3;
        idColumnIndex=4;
        filenameColumnIndex=5;
        dateColumnIndex=6;
        setAutoCreateRowSorter(true);
    }

    public int getMapColumnIndex()
    {
        return mapColumnIndex;
    }

    public int getShowFootprintColumnIndex()
    {
        return showFootprintColumnIndex;
    }

    public int getFrusColumnIndex()
    {
        return frusColumnIndex;
    }

    public int getBndrColumnIndex()
    {
        return bndrColumnIndex;
    }

    public int getDateColumnIndex()
    {
        return dateColumnIndex;
    }

    public int getIdColumnIndex()
    {
        return idColumnIndex;
    }

    public int getFilenameColumnIndex()
    {
        return filenameColumnIndex;
    }
}
