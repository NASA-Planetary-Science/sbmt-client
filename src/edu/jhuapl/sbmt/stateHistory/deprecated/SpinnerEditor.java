package edu.jhuapl.sbmt.stateHistory.deprecated;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor
{
	final JSpinner spinner = new JSpinner();

	public SpinnerEditor()
	{
		SpinnerListModel model = new SpinnerListModel();
		spinner.setModel(model);
	}

	public void setItems(Object[] items)
	{
		if (items instanceof Double[])
		{
			SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1.0, 0.1);
			spinner.setModel(model);
		}
		else
		{
			SpinnerListModel model = new SpinnerListModel(items);
			spinner.setModel(model);
		}
	}

	public SpinnerEditor(Double[] items)
	{
		SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1.0, 0.1);
		spinner.setModel(model);
	}

	public SpinnerEditor(String[] items)
	{
		SpinnerListModel model = new SpinnerListModel(items);
		spinner.setModel(model);
	}

	@Override
	public Object getCellEditorValue()
	{
		return spinner.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}

	@Override
	public boolean isCellEditable(EventObject e)
	{
		if (e instanceof MouseEvent)
		{
	      return ((MouseEvent) e).getClickCount() >= 2;
	    }
	    return true;
	}

}
