//package edu.jhuapl.sbmt.stateHistory.ui.state.color;
//
//import java.awt.Color;
//import java.awt.Component;
//
//import javax.swing.BorderFactory;
//import javax.swing.JLabel;
//import javax.swing.JTable;
//import javax.swing.border.Border;
//import javax.swing.table.DefaultTableCellRenderer;
//
//import edu.jhuapl.saavtk.color.provider.ColorBarColorProvider;
//import edu.jhuapl.saavtk.color.provider.ColorProvider;
//import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
//import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
//import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
//
//public class StateHistoryColorProviderCellRenderer extends DefaultTableCellRenderer
//{
//	// Constants
//	private static final long serialVersionUID = 1L;
//
//	// Attributes
//	private final boolean showToolTips;
//
//	// Cache vars
//	private Border cSelectedBorder;
//	private Border cUnselectedBorder;
//
//	/** Standard Constructor */
//	public StateHistoryColorProviderCellRenderer(boolean aShowToolTips)
//	{
//		showToolTips = aShowToolTips;
//
//		cSelectedBorder = null;
//		cUnselectedBorder = null;
//	}
//
//	@Override
//	public Component getTableCellRendererComponent(JTable aTable, Object aObject, boolean isSelected, boolean hasFocus,
//			int aRow, int aColumn)
//	{
//		JLabel retComp = (JLabel) super.getTableCellRendererComponent(aTable, aObject, isSelected, hasFocus, aRow,
//				aColumn);
//
////		GroupColorProvider tmpCP = (GroupColorProvider) aObject;
//		String tmpMsg = null;
//		Color tmpColor = null;
//		if (aObject instanceof ConstColorProvider || aObject instanceof SimpleColorProvider)
//		{
//			ColorProvider tmpCP = (ColorProvider) aObject;
//			if (tmpCP != null)
//			{
//				tmpColor = tmpCP.getBaseColor();
//				if (tmpColor == null)
//					tmpMsg = "---";
//			}
//		}
//		else if (((ConstGroupColorProvider ) aObject).getColorProviderFor(null, 0, 0) instanceof SimpleColorProvider)
//		{
//			ColorProvider tmpCP = (ColorProvider) ((ConstGroupColorProvider ) aObject).getColorProviderFor(null, 0, 0);
//			if (tmpCP != null)
//			{
//				tmpColor = tmpCP.getBaseColor();
//				if (tmpColor == null)
//					tmpMsg = "---";
//			}
//		}
//		else if (aObject instanceof ColorBarColorProvider)
//		{
//			ColorBarColorProvider tmpCP = (ColorBarColorProvider) aObject;
//			tmpMsg = "By " + tmpCP.getFeatureType().getName();
//		}
//		else if (aObject instanceof ConstGroupColorProvider )
//		{
//			ConstGroupColorProvider  tmpCP = (ConstGroupColorProvider ) aObject;
//			ColorBarColorProvider colorProviderFor = (ColorBarColorProvider)tmpCP.getColorProviderFor(null, 0, 0);
//			tmpMsg = "By " + colorProviderFor.getFeatureType().getName();
//		}
//
//		retComp.setBackground(tmpColor);
//		retComp.setForeground(null);
//		retComp.setHorizontalAlignment(JLabel.CENTER);
//		retComp.setText(tmpMsg);
//
//		boolean isOpaque = tmpColor != null;
//		retComp.setOpaque(isOpaque);
//
//		Border tmpBorder = null;
//		if (isSelected == true)
//		{
//			if (cSelectedBorder == null)
//				cSelectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, aTable.getSelectionBackground());
//			tmpBorder = cSelectedBorder;
//		}
//		else
//		{
//			if (cUnselectedBorder == null)
//				cUnselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, aTable.getBackground());
//			tmpBorder = cUnselectedBorder;
//		}
//		retComp.setBorder(tmpBorder);
//
//		String toolTipStr = null;
//		if (showToolTips == true && tmpColor != null)
//			toolTipStr = "RGB value: " + tmpColor.getRed() + ", " + tmpColor.getGreen() + ", " + tmpColor.getBlue();
//		retComp.setToolTipText(toolTipStr);
//
//		return retComp;
//	}
//
//}
