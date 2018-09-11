package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.rosetta.OsirisImagingSearchPanel.StructuresTableModel;

public class ImageResultsTableView extends JPanel implements TableModelListener, MouseListener, ListSelectionListener
{
    private javax.swing.JButton loadImageListButton;
    private javax.swing.JPanel monochromePanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JComboBox numberOfBoundariesComboBox;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeAllImagesButton;
    private javax.swing.JButton saveImageListButton;
    private javax.swing.JButton saveSelectedImageListButton;
    private javax.swing.JButton viewResultsGalleryButton;
    private javax.swing.JButton clearRegionButton;

    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;
    String[] columnNames;
    protected List<List<String>> imageRawResults = new ArrayList<List<String>>();
    ImageCollection imageCollection;
    protected ImagingInstrument instrument;
    protected ImageSource imageSourceOfLastQuery = ImageSource.SPICE;

    private JTable resultList;
    private JLabel resultsLabel;

    public ImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection)
    {
        // TODO Auto-generated constructor stub
        columnNames = new String[]{
                "Map",
                "Show",
                "Frus",
                "Bndr",
                "Id",
                "Filename",
                "Date"
        };
        mapColumnIndex=0;
        showFootprintColumnIndex=1;
        frusColumnIndex=2;
        bndrColumnIndex=3;
        idColumnIndex=4;
        filenameColumnIndex=5;
        dateColumnIndex=6;

        resultList = new JTable();
        resultsLabel = new JLabel("0 images matched");

        Object[][] data = new Object[0][7];
        resultList.setModel(new StructuresTableModel(data, columnNames));
        resultList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultList.setDefaultRenderer(String.class, new StringRenderer());
        resultList.getColumnModel().getColumn(mapColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(showFootprintColumnIndex).setPreferredWidth(35);
        resultList.getColumnModel().getColumn(frusColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(bndrColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(mapColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(showFootprintColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(frusColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(bndrColumnIndex).setResizable(true);
        resultList.addMouseListener(this);
        resultList.getModel().addTableModelListener(this);
        resultList.getSelectionModel().addListSelectionListener(this);

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel5.add(clearRegionButton, gridBagConstraints);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel5.add(submitButton, gridBagConstraints);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });

        numberOfBoundariesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        numberOfBoundariesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfBoundariesComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(numberOfBoundariesComboBox, gridBagConstraints);

        prevButton.setText("<");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(prevButton, gridBagConstraints);

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        removeAllButton.setText("Remove All Boundaries");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel13.add(removeAllButton, gridBagConstraints);

        removeAllImagesButton.setText("Remove All Images");
        removeAllImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllImagesButtonActionPerformed(evt);
            }
        });

        saveImageListButton.setText("Save List...");
        saveImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImageListButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel15.add(saveImageListButton, gridBagConstraints);

        saveSelectedImageListButton.setText("Save Selected List...");
        saveSelectedImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSelectedImageListButtonActionPerformed(evt);
            }
        });
        jPanel15.add(saveSelectedImageListButton, new java.awt.GridBagConstraints());

        loadImageListButton.setText("Load List...");
        loadImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageListButtonActionPerformed(evt);
            }
        });
        jPanel15.add(loadImageListButton, new java.awt.GridBagConstraints());

        viewResultsGalleryButton.setText("View Search Results as Image Gallery");
        viewResultsGalleryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewResultsGalleryButtonActionPerformed(evt);
            }
        });

        resultList.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(imageResultsTableView.getResultList());
        resultList.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    }

    public ImageResultsTableView(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public ImageResultsTableView(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public ImageResultsTableView(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public List<ImageKey> createImageKeys(String boundaryName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        List<ImageKey> result = new ArrayList<ImageKey>();
        result.add(createImageKey(boundaryName, sourceOfLastQuery, instrument));
        return result;
    }

    public ImageKey createImageKey(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        int slice = this.getCurrentSlice();
        String band = this.getCurrentBand();
        return new ImageKey(imagePathName, sourceOfLastQuery, null, null, instrument, band, slice);
    }

    public int getCurrentSlice() { return 0; }

    public String getCurrentBand() { return "0"; }

    public void setImageSourceOfLastQuery(ImageSource imageSourceOfLastQuery)
    {
        this.imageSourceOfLastQuery = imageSourceOfLastQuery;
    }

    public void setImageCollection(ImageCollection imageCollection)
    {
        this.imageCollection = imageCollection;
    }

    public void setInstrument(ImagingInstrument instrument)
    {
        this.instrument = instrument;
    }

    public void setImageResults(List<List<String>> results)
    {
        resultsLabel.setText(results.size() + " images matched");
        imageRawResults = results;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

        resultList.getModel().removeTableModelListener(this);
        images.removePropertyChangeListener(this);
        boundaries.removePropertyChangeListener(this);

        try
        {
            int[] widths = new int[resultList.getColumnCount()];
            int[] columnsNeedingARenderer=new int[]{idColumnIndex,filenameColumnIndex,dateColumnIndex};

            // add the results to the list
            ((DefaultTableModel)resultList.getModel()).setRowCount(results.size());
            int i=0;
            for (List<String> str : results)
            {
                Date dt = new Date(Long.parseLong(str.get(1)));

                String name = imageRawResults.get(i).get(0);
//            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                if (images.containsImage(key))
                {
                    resultList.setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    resultList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    resultList.setValueAt(!image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    resultList.setValueAt(false, i, mapColumnIndex);
                    resultList.setValueAt(false, i, showFootprintColumnIndex);
                    resultList.setValueAt(false, i, frusColumnIndex);
                }


                if (boundaries.containsBoundary(key))
                    resultList.setValueAt(true, i, bndrColumnIndex);
                else
                    resultList.setValueAt(false, i, bndrColumnIndex);

                resultList.setValueAt(i+1, i, idColumnIndex);
                resultList.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
                resultList.setValueAt(sdf.format(dt), i, dateColumnIndex);

                for (int j : columnsNeedingARenderer)
                {
                    TableCellRenderer renderer = resultList.getCellRenderer(i, j);
                    Component comp = resultList.prepareRenderer(renderer, i, j);
                    widths[j] = Math.max (comp.getPreferredSize().width, widths[j]);
                }

                ++i;
            }

            for (int j : columnsNeedingARenderer)
                resultList.getColumnModel().getColumn(j).setPreferredWidth(widths[j] + 5);

            boolean enablePostSearchButtons = resultList.getModel().getRowCount() > 0;
            saveImageListButton.setEnabled(enablePostSearchButtons);
            saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
            viewResultsGalleryButton.setEnabled(enableGallery && enablePostSearchButtons);
        }
        finally
        {
            resultList.getModel().addTableModelListener(this);
            images.addPropertyChangeListener(this);
            boundaries.addPropertyChangeListener(this);
        }


        // Show the first set of boundaries
        this.resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
        this.showImageBoundaries(resultIntervalCurrentlyShown);

        // Enable or disable the image gallery button
        viewResultsGalleryButton.setEnabled(enableGallery && !results.isEmpty());
    }

    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!resultList.isRowSelected(index))
                {
                    resultList.clearSelection();
                    resultList.setRowSelectionInterval(index, index);
                }

                int[] selectedIndices = resultList.getSelectedRows();
                List<ImageKey> imageKeys = new ArrayList<ImageKey>();
                for (int selectedIndex : selectedIndices)
                {
                    String name = imageRawResults.get(selectedIndex).get(0);
//                    ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                    ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                    imageKeys.add(key);
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public class ImagesTableModel extends DefaultTableModel
    {
        public ImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == showFootprintColumnIndex || column == frusColumnIndex)
            {
                String name = imageRawResults.get(row).get(0);
                ImageKey key = createImageKey(name.substring(0, name.length()-4), imageSourceOfLastQuery, instrument);
//                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
                return imageCollection.containsImage(key);
            }
            else
            {
                return column == mapColumnIndex || column == bndrColumnIndex;
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= bndrColumnIndex)
                return Boolean.class;
            else
                return String.class;
        }
    }

    public void mousePressed(MouseEvent e)
    {
        resultsListMaybeShowPopup(e);
        saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
    }

    public void mouseReleased(MouseEvent e)
    {
        resultsListMaybeShowPopup(e);
        saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        if (e.getColumn() == mapColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            String namePrefix = name.substring(0, name.length()-4);
            if ((Boolean)resultList.getValueAt(row, mapColumnIndex))
                loadImages(namePrefix);
            else
            {
                unloadImages(namePrefix);
                renderer.setLighting(LightingType.LIGHT_KIT);
            }
        }
        else if (e.getColumn() == showFootprintColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            String namePrefix = name.substring(0, name.length()-4);
            boolean visible = (Boolean)resultList.getValueAt(row, showFootprintColumnIndex);
            setImageVisibility(namePrefix, visible);
        }
        else if (e.getColumn() == frusColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            if (images.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                image.setShowFrustum(!image.isFrustumShowing());
            }
        }
        else if (e.getColumn() == bndrColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            try
            {
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
                if (!boundaries.containsBoundary(key))
                    boundaries.addBoundary(key);
                else
                    boundaries.removeBoundary(key);
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }
        }
    }

    public JTable getResultList()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    protected JComboBox getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    private void numberOfBoundariesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfBoundariesComboBoxActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only update if there's been a change in what is selected
            int newMaxId = resultIntervalCurrentlyShown.id1 + Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem());
            if (newMaxId != resultIntervalCurrentlyShown.id2)
            {
                resultIntervalCurrentlyShown.id2 = newMaxId;
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
    }//GEN-LAST:event_numberOfBoundariesComboBoxActionPerformed

    private void saveImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImageListButtonActionPerformed
        File file = CustomFileChooser.showSaveDialog(this, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing"  + nl);
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    String image = new File(imageRawResults.get(i).get(0)).getName();
                    String dtStr = imageRawResults.get(i).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + " " + sourceOfLastQuery.toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_saveImageListButtonActionPerformed

    private void loadImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImageListButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select File");

        if (file != null)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                List<List<String>> results = new ArrayList<List<String>>();
                List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
                for (int i=0; i<lines.size(); ++i)
                {
                    if (lines.get(i).startsWith("#")) continue;
                    String[] words = lines.get(i).trim().split("\\s+");
                    List<String> result = new ArrayList<String>();
                    String name = instrument.searchQuery.getDataPath() + "/" + words[0];
                    result.add(name);
                    Date dt = sdf.parse(words[1]);
                    result.add(String.valueOf(dt.getTime()));
                    results.add(result);
                }

                sourceOfLastQuery = ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name());

                setImageResults(processResults(results));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error reading the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }//GEN-LAST:event_loadImageListButtonActionPerformed


    private void saveSelectedImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSelectedImageListButtonActionPerformed
        File file = CustomFileChooser.showSaveDialog(this, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing"  + nl);
                int[] selectedIndices = imageResultsTableView.getResultList().getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                    String image = new File(imageRawResults.get(selectedIndex).get(0)).getName();
                    String dtStr = imageRawResults.get(selectedIndex).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + " " + sourceOfLastQuery.toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_saveSelectedImageListButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();
        resultIntervalCurrentlyShown = null;
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeAllImagesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllImagesButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllImagesButtonActionPerformed
        ImageCollection model = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        model.removeImages(ImageSource.GASKELL);
        model.removeImages(ImageSource.GASKELL_UPDATED);
        model.removeImages(ImageSource.SPICE);
        model.removeImages(ImageSource.CORRECTED_SPICE);
        model.removeImages(ImageSource.CORRECTED);
    }//GEN-LAST:event_removeAllImagesButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }

    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < imageResultsTableView.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    protected void showImageBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= imageRawResults.size())
                break;

            try
            {
                String currentImage = imageRawResults.get(i).get(0);
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_DDR.LBL";
                String boundaryName = currentImage.substring(0,currentImage.length()-4);

//                ImageKey key = new ImageKey(boundaryName, sourceOfLastQuery, instrument);

                ImageKey key = imageResultsTableView.createImageKey(boundaryName, sourceOfLastQuery, instrument);
                model.addBoundary(key);

//                List<ImageKey> keys = createImageKeys(boundaryName, sourceOfLastQuery, instrument);
//                for (ImageKey key : keys)
//                {
//                    key.instrument = this.instrument;
//                    model.addBoundary(key);
//                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
                break;
            }
        }
    }

    public class StringRenderer extends DefaultTableCellRenderer
    {
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String name = imageRawResults.get(row).get(0);
//            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            ImageKey key = imageResultsTableView.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            if (model.containsBoundary(key))
            {
                int[] c = model.getBoundary(key).getBoundaryColor();
                if (isSelected)
                {
                    co.setForeground(new Color(c[0], c[1], c[2]));
                    co.setBackground(table.getSelectionBackground());
                }
                else
                {
                    co.setForeground(new Color(c[0], c[1], c[2]));
                    co.setBackground(table.getBackground());
                }
            }
            else
            {
                if (isSelected)
                {
                    co.setForeground(table.getSelectionForeground());
                    co.setBackground(table.getSelectionBackground());
                }
                else
                {
                    co.setForeground(table.getForeground());
                    co.setBackground(table.getBackground());
                }
            }

            return co;
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            resultList.getModel().removeTableModelListener(this);
            int size = imageRawResults.size();
//            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
            for (int i=0; i<size; ++i)
            {
                String name = imageRawResults.get(i).get(0);
//                ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKey key = imageResultsTableView.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                if (images.containsImage(key))
                {
                    imageResultsTableView.getResultList().setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    imageResultsTableView.getResultList().setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    imageResultsTableView.getResultList().setValueAt(image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    imageResultsTableView.getResultList().setValueAt(false, i, mapColumnIndex);
                    imageResultsTableView.getResultList().setValueAt(false, i, showFootprintColumnIndex);
                    imageResultsTableView.getResultList().setValueAt(false, i, frusColumnIndex);
                }
                if (boundaries.containsBoundary(key))
                    imageResultsTableView.getResultList().setValueAt(true, i, bndrColumnIndex);
                else
                    imageResultsTableView.getResultList().setValueAt(false, i, bndrColumnIndex);
            }
            imageResultsTableView.getResultList().getModel().addTableModelListener(this);
            // Repaint the list in case the boundary colors has changed
            imageResultsTableView.getResultList().repaint();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {
            viewResultsGalleryButton.setEnabled(enableGallery && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
        }
    }
}
