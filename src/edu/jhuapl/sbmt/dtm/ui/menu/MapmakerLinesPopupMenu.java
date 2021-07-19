package edu.jhuapl.sbmt.dtm.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.sbmt.util.gravity.Gravity;

/**
 * Popup menu used by the mapmaker view for profiles. It is meant to replace
 * LinesPopupMenu which is used in the regular views.
 */
public class MapmakerLinesPopupMenu extends PopupMenu
{
	// Ref vars
	private final LineModel<PolyLine> refLineManager;
	private final PolyhedralModel refPolyhedralModel;

	// Gui vars
	private final JMenuItem saveProfileAction;

	// State vars
	private int pickedCellId = -1;

	public MapmakerLinesPopupMenu(ModelManager aModelManager, PolyhedralModel aPolyhedralModel, Renderer aRenderer)
	{
		refLineManager = (LineModel<PolyLine>) aModelManager.getModel(ModelNames.LINE_STRUCTURES).get(0);
		refPolyhedralModel = aPolyhedralModel;

		saveProfileAction = new JMenuItem(new SaveProfileAction());
		saveProfileAction.setText("Save Profile...");
		saveProfileAction.setEnabled(true);
		add(saveProfileAction);
	}

	@Override
	public void showPopup(MouseEvent e, vtkProp pickedProp, int aPickedCellId, double[] aPickedPosition)
	{
		pickedCellId = aPickedCellId;
		show(e.getComponent(), e.getX(), e.getY());
	}

	private class SaveProfileAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (pickedCellId < 0 || pickedCellId >= refLineManager.getNumItems())
				return;
			PolyLine tmpLine = refLineManager.getItem(pickedCellId);

			// Prompt the user for the file to save to
			File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
			if (file == null)
				return;

			try
			{
				// Require at least two control points
				if (tmpLine.getControlPoints().size() != 2)
					throw new Exception("Line must contain exactly 2 control points.");

				// Delegate
				List<Vector3D> xyzPointL = refLineManager.getXyzPointsFor(tmpLine);
				Gravity.saveProfileUsingGravityProgram(xyzPointL, file, refPolyhedralModel);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(getInvoker(),
						e1.getMessage() != null ? e1.getMessage() : "An error occurred saving the profile.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
}
