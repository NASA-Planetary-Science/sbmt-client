package edu.jhuapl.sbmt.gui.image.ui.images;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

public class ImageResultsTableView extends JPanel
{
    private JButton loadImageListButton;
    private JPanel monochromePanel;
    private JButton nextButton;
    private JComboBox<Integer> numberOfBoundariesComboBox;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JButton saveImageListButton;
    private JButton saveSelectedImageListButton;
    private JButton viewResultsGalleryButton;
    private ImagePopupMenu imagePopupMenu;
    private boolean enableGallery;
    String[] columnNames;
    protected ImageResultsTable resultList;
    private JLabel resultsLabel;
    private JLabel lblNumberBoundaries;
    protected JPanel buttonPanel3;

    /**
     * @wbp.parser.constructor
     */
    public ImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection, ImagePopupMenu imagePopupMenu)
    {
        this.imagePopupMenu = imagePopupMenu;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        init();
    }

    protected void init()
    {
        resultsLabel = new JLabel("0 Results");
        resultList = new ImageResultsTable();
        lblNumberBoundaries = new JLabel("Number Boundaries:");
        numberOfBoundariesComboBox = new JComboBox<Integer>();
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        removeAllImagesButton = new JButton("Remove All Images");
        removeAllButton = new JButton("Remove All Boundaries");
        loadImageListButton = new JButton("Load...");
        saveImageListButton = new JButton("Save...");
        saveSelectedImageListButton = new JButton("Save Selected...");
        viewResultsGalleryButton = new JButton("View Results as Image Gallery");
    }

    public void setup()
    {
    	resultList.setAutoCreateRowSorter(true);
//    	resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	resultList.setDragEnabled(true);
//    	resultList.setDropMode(DropMode.INSERT_ROWS);
//    	resultList.setTransferHandler(new TableRowTransferHandler(resultList));


        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        panel_4.add(resultsLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 300));
        add(scrollPane);

        scrollPane.setViewportView(resultList);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(lblNumberBoundaries);

        panel.add(numberOfBoundariesComboBox);

        panel.add(prevButton);

        panel.add(nextButton);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        panel_1.add(removeAllImagesButton);

        panel_1.add(removeAllButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        panel_2.add(loadImageListButton);

        panel_2.add(saveImageListButton);

        panel_2.add(saveSelectedImageListButton);

        buttonPanel3 = new JPanel();
        add(buttonPanel3);
        buttonPanel3.add(viewResultsGalleryButton);
    }

    public ImageResultsTableView(LayoutManager layout)
    {
        super(layout);
    }

    public ImageResultsTableView(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    public ImageResultsTableView(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
    }

    public JTable getResultList()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    public JComboBox<Integer> getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    public JButton getLoadImageListButton()
    {
        return loadImageListButton;
    }

    public JPanel getMonochromePanel()
    {
        return monochromePanel;
    }

    public JButton getNextButton()
    {
        return nextButton;
    }

    public JButton getPrevButton()
    {
        return prevButton;
    }

    public JButton getRemoveAllButton()
    {
        return removeAllButton;
    }

    public JButton getRemoveAllImagesButton()
    {
        return removeAllImagesButton;
    }

    public JButton getSaveImageListButton()
    {
        return saveImageListButton;
    }

    public JButton getSaveSelectedImageListButton()
    {
        return saveSelectedImageListButton;
    }

    public JButton getViewResultsGalleryButton()
    {
        return viewResultsGalleryButton;
    }

    public ImagePopupMenu getImagePopupMenu()
    {
        return imagePopupMenu;
    }

    public boolean isEnableGallery()
    {
        return enableGallery;
    }

    public void setEnableGallery(boolean enableGallery)
    {
        this.enableGallery = enableGallery;
    }

    public void setNumberOfBoundariesComboBox(JComboBox<Integer> numberOfBoundariesComboBox)
    {
        this.numberOfBoundariesComboBox = numberOfBoundariesComboBox;
    }

    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }

    public int getMapColumnIndex()
    {
        return resultList.mapColumnIndex;
    }

    public int getShowFootprintColumnIndex()
    {
        return resultList.showFootprintColumnIndex;
    }

    public int getFrusColumnIndex()
    {
        return resultList.frusColumnIndex;
    }

    public int getBndrColumnIndex()
    {
        return resultList.bndrColumnIndex;
    }

    public int getDateColumnIndex()
    {
        return resultList.dateColumnIndex;
    }

    public int getIdColumnIndex()
    {
        return resultList.idColumnIndex;
    }

    public int getFilenameColumnIndex()
    {
        return resultList.filenameColumnIndex;
    }

    public JLabel getLblNumberBoundaries()
    {
        return lblNumberBoundaries;
    }

//    class DragDropRowTableUI extends BasicTableUI {
//
//        private boolean draggingRow = false;
//        private int startDragPoint;
//        private int dyOffset;
//
//       protected MouseInputListener createMouseInputListener() {
//           return new DragDropRowMouseInputHandler();
//       }
//
//       public void paint(Graphics g, JComponent c) {
//            super.paint(g, c);
//
//            if (draggingRow) {
//                 g.setColor(table.getParent().getBackground());
//                  Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
//                 g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);
//
//                 if (dyOffset < 0) {
//                      g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
//                 } else {
//                      g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
//                 }
//            }
//       }
//
//       class DragDropRowMouseInputHandler extends MouseInputHandler {
//
//           public void mousePressed(MouseEvent e) {
//                super.mousePressed(e);
//                startDragPoint = (int)e.getPoint().getY();
//           }
//
////           public void mouseDragged(MouseEvent e) {
////               // Only do special handling if we are drag enabled with multiple selection
////               if (table.getDragEnabled() &&
////                         table.getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
////                    table.getTransferHandler().exportAsDrag(table, e,DnDConstants.ACTION_COPY);
////               } else {
////                    super.mouseDragged(e);
////               }
////          }
//
//           public void mouseDragged(MouseEvent e) {
//                int fromRow = table.getSelectedRow();
//
//                if (fromRow >= 0) {
//                     draggingRow = true;
//
//                     int rowHeight = table.getRowHeight();
//                     int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
//
//                     int toRow = -1;
//                     int yMousePoint = (int)e.getPoint().getY();
//
//                     if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
//                          // Move row up
//                          toRow = fromRow - 1;
//                     } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
//                          // Move row down
//                          toRow = fromRow + 1;
//                     }
//
//
//
//
//                     if (toRow >= 0 && toRow < table.getRowCount()) {
//                          TableModel model = table.getModel();
//
//                        //capture checkbox state before and after
//                          boolean fromMap = (Boolean)model.getValueAt(fromRow, 0);
//                          boolean fromShow = (Boolean)model.getValueAt(fromRow, 1);
//                          boolean fromFrus = (Boolean)model.getValueAt(fromRow, 2);
//                          boolean fromBndr = (Boolean)model.getValueAt(fromRow, 3);
//
//                          boolean toMap = (Boolean)model.getValueAt(toRow, 0);
//                          boolean toShow = (Boolean)model.getValueAt(toRow, 1);
//                          boolean toFrus = (Boolean)model.getValueAt(toRow, 2);
//                          boolean toBndr = (Boolean)model.getValueAt(toRow, 3);
//
//                          System.out.println(
//								"ImageResultsTableView.DragDropRowTableUI.DragDropRowMouseInputHandler: mouseDragged: from frust " + fromFrus);
//                           for (int i = 0; i < model.getColumnCount(); i++) {
//                                Object fromValue = model.getValueAt(fromRow, i);
//                                Object toValue = model.getValueAt(toRow, i);
////                                System.out.println(
////										"ImageResultsTableView.DragDropRowTableUI.DragDropRowMouseInputHandler: mouseDragged: from value is " + fromValue);
//                                model.setValueAt(toValue, fromRow, i);
//                                model.setValueAt(fromValue, toRow, i);
//                           }
//
//                           model.setValueAt(toMap, fromRow, 0);
//                           model.setValueAt(toShow, fromRow, 1);
//                           model.setValueAt(toFrus, fromRow, 2);
//                           model.setValueAt(toBndr, fromRow, 3);
//
//                           model.setValueAt(fromMap, toRow, 0);
//                           model.setValueAt(fromShow, toRow, 1);
//                           model.setValueAt(fromFrus, toRow, 2);
//                           model.setValueAt(fromBndr, toRow, 3);
//                           table.setRowSelectionInterval(toRow, toRow);
//                           startDragPoint = yMousePoint;
//                     }
//
//                     dyOffset = (startDragPoint - yMousePoint) * -1;
//                     table.repaint();
//                }
//           }
//
//           public void mouseReleased(MouseEvent e){
//                super.mouseReleased(e);
//
//                draggingRow = false;
//                table.repaint();
//           }
//       }
//   }

//    /**
//     * Handles drag & drop row reordering
//     */
//    public class TableRowTransferHandler extends TransferHandler {
//       private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "application/x-java-Integer;class=java.lang.Integer", "Integer Row Index");
//       private JTable           table             = null;
//
//       public TableRowTransferHandler(JTable table) {
//          this.table = table;
//       }
//
//       @Override
//       protected Transferable createTransferable(JComponent c) {
//          assert (c == table);
//          return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
//       }
//
//       @Override
//       public boolean canImport(TransferHandler.TransferSupport info) {
//          boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
//          table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
//          return b;
//       }
//
//       @Override
//       public int getSourceActions(JComponent c) {
//          return TransferHandler.COPY_OR_MOVE;
//       }
//
//       @Override
//       public boolean importData(TransferHandler.TransferSupport info) {
//          JTable target = (JTable) info.getComponent();
//          JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
//          int index = dl.getRow();
//          int max = table.getModel().getRowCount();
//          if (index < 0 || index > max)
//             index = max;
//          target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//          try {
//             Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
//             if (rowFrom != -1 && rowFrom != index) {
//                ((Reorderable)table.getModel()).reorder(rowFrom, index);
//                if (index > rowFrom)
//                   index--;
//                target.getSelectionModel().addSelectionInterval(index, index);
//                return true;
//             }
//          } catch (Exception e) {
//             e.printStackTrace();
//          }
//          return false;
//       }
//
//       @Override
//       protected void exportDone(JComponent c, Transferable t, int act) {
//          if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
//             table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//          }
//       }
//
//    }
//
//    public interface Reorderable {
//    	   public void reorder(int fromIndex, int toIndex);
//    	}

}
