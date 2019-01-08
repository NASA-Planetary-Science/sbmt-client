package edu.jhuapl.sbmt.gui.image.ui.images;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableModel;

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
//    	resultList.setAutoCreateRowSorter(true);
    	resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	resultList.setDragEnabled(true);
    	resultList.setUI(new DragDropRowTableUI());

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

    class DragDropRowTableUI extends BasicTableUI {

        private boolean draggingRow = false;
        private int startDragPoint;
        private int dyOffset;

       protected MouseInputListener createMouseInputListener() {
           return new DragDropRowMouseInputHandler();
       }

       public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            if (draggingRow) {
                 g.setColor(table.getParent().getBackground());
                  Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
                 g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);

                 if (dyOffset < 0) {
                      g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
                 } else {
                      g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
                 }
            }
       }

       class DragDropRowMouseInputHandler extends MouseInputHandler {

           public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                startDragPoint = (int)e.getPoint().getY();
           }

//           public void mouseDragged(MouseEvent e) {
//               // Only do special handling if we are drag enabled with multiple selection
//               if (table.getDragEnabled() &&
//                         table.getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
//                    table.getTransferHandler().exportAsDrag(table, e,DnDConstants.ACTION_COPY);
//               } else {
//                    super.mouseDragged(e);
//               }
//          }

           public void mouseDragged(MouseEvent e) {
                int fromRow = table.getSelectedRow();

                if (fromRow >= 0) {
                     draggingRow = true;

                     int rowHeight = table.getRowHeight();
                     int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);

                     int toRow = -1;
                     int yMousePoint = (int)e.getPoint().getY();

                     if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
                          // Move row up
                          toRow = fromRow - 1;
                     } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
                          // Move row down
                          toRow = fromRow + 1;
                     }

                     if (toRow >= 0 && toRow < table.getRowCount()) {
                          TableModel model = table.getModel();

                           for (int i = 0; i < model.getColumnCount(); i++) {
                                Object fromValue = model.getValueAt(fromRow, i);
                                Object toValue = model.getValueAt(toRow, i);

                                model.setValueAt(toValue, fromRow, i);
                                model.setValueAt(fromValue, toRow, i);
                           }
                           table.setRowSelectionInterval(toRow, toRow);
                           startDragPoint = yMousePoint;
                     }

                     dyOffset = (startDragPoint - yMousePoint) * -1;
                     table.repaint();
                }
           }

           public void mouseReleased(MouseEvent e){
                super.mouseReleased(e);

                draggingRow = false;
                table.repaint();
           }
       }
   }

}
