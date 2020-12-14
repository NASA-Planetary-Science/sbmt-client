package edu.jhuapl.sbmt.stateHistory.ui.state.color;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

public class StateHistoryColorProviderCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Gui vars
	private StateHistoryColorProviderCellRenderer dispComp;

	// State vars
	private GroupColorProvider currCP;

	private StateHistory stateHistory;

	private StateHistoryRendererManager rendererManager;

	private StateHistoryColorConfigPanel colorConfigPanel;

	private JTable aTable;

	private boolean aIsSelected;

	private int aRow, aCol;

	/**
	 * Standard Constructor
	 */
	public StateHistoryColorProviderCellEditor(StateHistoryRendererManager rendererManager)
	{
		dispComp = new StateHistoryColorProviderCellRenderer(false);
		currCP = null;
		this.rendererManager = rendererManager;
	}

	@Override
	public Object getCellEditorValue()
	{
		return currCP;
	}

	@Override
	public Component getTableCellEditorComponent(JTable aTable, Object aValue, boolean aIsSelected, int aRow, int aCol)
	{
		System.out.println("StateHistoryColorProviderCellEditor: getTableCellEditorComponent: getting table components");
		// Bail if we are not selected
		if (aIsSelected == false)
			return null;

		// Color editing will only be allowed if 1 item is selected
		if (aTable.getSelectedRows().length != 1)
			return null;

		this.aTable = aTable;
		this.aIsSelected = aIsSelected;
		this.aRow = aRow;
		this.aCol = aCol;

		if (colorConfigPanel == null)
			colorConfigPanel = new StateHistoryColorConfigPanel(this, rendererManager);
		JFrame frame = new JFrame("Choose Trajectory Color");
		frame.add(colorConfigPanel);
		frame.pack();
		frame.setVisible(true);

//		// Prompt the user to select a color
//		Color oldColor = ((ColorProvider) aValue).getBaseColor();
//		if (oldColor == null)
//			oldColor = Color.BLACK;
//		Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(aTable), oldColor);
//		if (tmpColor == null)
//			return null;
//
//		// Update our internal renderer to display the user's selection
//		currCP = new ConstColorProvider(tmpColor);
//		dispComp.getTableCellRendererComponent(aTable, currCP, aIsSelected, false, aRow, aCol);
//
//		// There is no further editing since it occurs within the popup dialog
//		// Note, stopCellEditing must be called after all pending AWT events
//		SwingUtilities.invokeLater(() ->
//		{
//			stopCellEditing();
//		});

		return dispComp;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		rendererManager.installGroupColorProviders(srcGCP);
		// Update our internal renderer to display the user's selection
		currCP = srcGCP;
		dispComp.getTableCellRendererComponent(aTable, currCP, aIsSelected, false, aRow, aCol);
	}

}
