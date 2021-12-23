package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.ui.state.color;
//
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.AbstractCellEditor;
//import javax.swing.JFrame;
//import javax.swing.JTable;
//import javax.swing.table.TableCellEditor;
//
//import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
//import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
//
///**
// * Based on original for Lidar by lopeznr1
// *
// * @author steelrj1
// *
// */
//public class StateHistoryColorProviderCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
//{
//	// Constants
//	private static final long serialVersionUID = 1L;
//
//	// Gui vars
//	private StateHistoryColorProviderCellRenderer dispComp;
//
//	// State vars
//	private GroupColorProvider currCP;
//
//	private StateHistoryRendererManager rendererManager;
//
//	private StateHistoryColorConfigPanel colorConfigPanel;
//
//	private JTable aTable;
//
//	private boolean aIsSelected;
//
//	private int aRow, aCol;
//
//	/**
//	 * Standard Constructor
//	 */
//	public StateHistoryColorProviderCellEditor(StateHistoryRendererManager rendererManager)
//	{
//		dispComp = new StateHistoryColorProviderCellRenderer(false);
//		currCP = null;
//		this.rendererManager = rendererManager;
//	}
//
//	@Override
//	public Object getCellEditorValue()
//	{
//		return currCP;
//	}
//
//	@Override
//	public Component getTableCellEditorComponent(JTable aTable, Object aValue, boolean aIsSelected, int aRow, int aCol)
//	{
//		// Bail if we are not selected
//		if (aIsSelected == false)
//			return null;
//
//		// Color editing will only be allowed if 1 item is selected
//		if (aTable.getSelectedRows().length != 1)
//			return null;
//
//		this.aTable = aTable;
//		this.aIsSelected = aIsSelected;
//		this.aRow = aRow;
//		this.aCol = aCol;
//
//		if (colorConfigPanel == null)
//			colorConfigPanel = new StateHistoryColorConfigPanel(this, rendererManager);
//		JFrame frame = new JFrame("Choose Trajectory Color");
//		frame.add(colorConfigPanel);
//		frame.pack();
//		frame.setVisible(true);
//
//		return dispComp;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e)
//	{
//		if (colorConfigPanel == null) return;
//		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
//		rendererManager.installGroupColorProviders(srcGCP);
//		// Update our internal renderer to display the user's selection
//		currCP = srcGCP;
//		dispComp.getTableCellRendererComponent(aTable, currCP, aIsSelected, false, aRow, aCol);
//	}
//}
