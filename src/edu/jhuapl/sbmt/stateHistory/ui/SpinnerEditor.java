package edu.jhuapl.sbmt.stateHistory.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor
{
	final JSpinner spinner = new JSpinner();

	public SpinnerEditor(Double[] items)
	{
		SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1.0, 0.1);
		spinner.setModel(model);
	}

	@Override
	public Object getCellEditorValue()
	{
		System.out.println("SpinnerEditor: getCellEditorValue: returning " + spinner.getValue());
		return spinner.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		System.out.println("SpinnerEditor: getTableCellEditorComponent: setting value " + value);
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
