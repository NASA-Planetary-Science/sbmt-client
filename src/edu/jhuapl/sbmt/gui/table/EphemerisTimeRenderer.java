package edu.jhuapl.sbmt.gui.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.util.TimeUtil;

/**
 * TableCellRenderer used to display ephemeris time.
 *
 * @author lopeznr1
 */
public class EphemerisTimeRenderer extends DefaultTableCellRenderer
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Attributes
	private final boolean isShortMode;
	private final String nanStr;

	/**
	 * Standard Constructor
	 *
	 * @param aIsShortMode Defines if the time display will show milliseconds or
	 * microseconds.
	 */
	public EphemerisTimeRenderer(boolean aIsShortMode)
	{
		isShortMode = aIsShortMode;
		nanStr = "---";
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
	{
		String tmpStr = "" + value;
		if (value instanceof Double)
		{
			double tmpVal = (Double) value;

			tmpStr = nanStr;
			if (Double.isNaN(tmpVal) == false)
				tmpStr = TimeUtil.et2str(tmpVal);
		}

		if (isShortMode == true && tmpStr.length() > 23)
			tmpStr = tmpStr.substring(0, 23);

		return super.getTableCellRendererComponent(table, tmpStr, isSelected, hasFocus, row, column);
	}

}
