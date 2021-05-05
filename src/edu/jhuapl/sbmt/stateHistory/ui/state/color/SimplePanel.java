//package edu.jhuapl.sbmt.stateHistory.ui.state.color;
//
//import java.awt.Color;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.Icon;
//import javax.swing.JButton;
//import javax.swing.JColorChooser;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
//import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
//import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
//import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
//import edu.jhuapl.saavtk.util.ColorIcon;
//
//import glum.gui.GuiUtil;
//import net.miginfocom.swing.MigLayout;
//
///**
// * {@link StateHistoryColorConfigPanel} that provides simplistic configuration of state history
// * <P>
// * The configuration options are:
// * <UL>
// * <LI>A single color for all source points (spacecraft).
// * </UL>
// *
// * Originally made for Lidar by lopeznr1
// *
// * @author steelrj1
// */
//public class SimplePanel extends JPanel implements ActionListener, EditGroupColorPanel
//{
//	// Reference vars
//	private final ActionListener refListener;
//
//	// Gui vars
//	private JLabel srcColorL;
//	private JButton srcColorB;
//
//	// State vars
//	private Color srcColor;
//
//	/**
//	 * Standard Constructor
//	 */
//	public SimplePanel(ActionListener aListener, Color aSrcColor)
//	{
//		refListener = aListener;
//
//		// Setup the GUI
//		setLayout(new MigLayout("", "[]", ""));
//
//		srcColorL = new JLabel("Spacecraft:", JLabel.RIGHT);
//		srcColorB = GuiUtil.formButton(this, "");
//		add(srcColorL, "sg g1");
//		add(srcColorB, "sg g2,wrap");
//
//		srcColor = aSrcColor;
//
//		updateGui();
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent aEvent)
//	{
//		// Process the event
//		Object source = aEvent.getSource();
//		if (source == srcColorB)
//			doUpdateSourceColor();
//
//		// Notify our refListener
//		refListener.actionPerformed(new ActionEvent(this, 0, ""));
//	}
//
//	@Override
//	public void activate(boolean aIsActive)
//	{
//		updateGui();
//	}
//
//	/**
//	 * Sets in the installed colors.
//	 */
//	public void setInstalledColors(Color aSrcColor, Color aTgtColor)
//	{
//		srcColor = aSrcColor;
//
//		updateGui();
//	}
//
//	@Override
//	public GroupColorProvider getGroupColorProvider()
//	{
//		return new ConstGroupColorProvider(new SimpleColorProvider(srcColor));
//	}
//
//	/**
//	 * Helper method that handles the action for srcColorB
//	 */
//	private void doUpdateSourceColor()
//	{
//		// Prompt the user for a color
//		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", srcColor);
//		if (tmpColor == null)
//			return;
//		srcColor = tmpColor;
//
//		updateGui();
//	}
//
//	/**
//	 * Helper method that will update the UI to reflect the user selected colors.
//	 */
//	private void updateGui()
//	{
//		int iconW = (int) (srcColorL.getWidth() * 0.60);
//		int iconH = (int) (srcColorL.getHeight() * 0.80);
//
//		// Update the source / target icons
//		Icon srcIcon = new ColorIcon(srcColor, Color.BLACK, iconW, iconH);
//		srcColorB.setIcon(srcIcon);
//	}
//
//}
