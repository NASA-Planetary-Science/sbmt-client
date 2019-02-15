package edu.jhuapl.sbmt.gui.image.controllers.images;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.images.OfflimbImageResultsTableView;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class OfflimbImageResultsTableController extends ImageResultsTableController
{
    private OfflimbImageResultsTableView offlimbTableView;

    public OfflimbImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, ImageSearchModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        super(instrument, imageCollection, model, renderer, infoPanelManager, spectrumPanelManager);
    }

    @Override
    public void setImageResultsPanel()
    {
        offlimbTableView = new OfflimbImageResultsTableView(instrument, imageCollection, imagePopupMenu);
        offlimbTableView.setup();
        this.imageResultsTableView = offlimbTableView;
        setupWidgets();
        setupTable();
    }

    @Override
    protected void setupWidgets()
    {
        super.setupWidgets();
        offlimbTableView.getOfflimbControlsButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String name = imageRawResults.get(offlimbTableView.getResultList().getSelectedRow()).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(key);
                OfflimbControlsController controller = new OfflimbControlsController(image, imageSearchModel.getCurrentSlice());
                controller.getControlsFrame().setVisible(true);
            }
        });
    }

    @Override
    public void setupTable()
    {
        String[] columnNames = new String[]{
                "Map",
                "Show",
                "Offlimb",
                "Frus",
                "Bndr",
                "Id",
                "Filename",
                "Date"
        };
        offlimbTableView.getResultList().setModel(new OfflimbImagesTableModel(new Object[0][8], columnNames));

        offlimbTableView.getResultList().getTableHeader().setReorderingAllowed(false);
        offlimbTableView.getResultList().getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        offlimbTableView.getResultList().getColumnModel().getColumn(offlimbTableView.getOffLimbIndex()).setPreferredWidth(60);
        offlimbTableView.getResultList().getColumnModel().getColumn(offlimbTableView.getOffLimbIndex()).setResizable(true);

        tableModelListener = new OfflimbImageResultsTableModeListener();

        this.imageResultsTableView.addComponentListener(new ComponentListener()
		{

			@Override
			public void componentShown(ComponentEvent e)
			{
		        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);

			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
		        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);

			}
		});



        imageResultsTableView.getResultList().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }
        });


        imageResultsTableView.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
                }
            }
        });

        stringRenderer = new StringRenderer(imageSearchModel, imageRawResults);
        imageResultsTableView.getResultList().setDefaultRenderer(String.class, stringRenderer);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setPreferredWidth(35);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setResizable(true);
    }

    protected JTable getResultList()
    {
        return imageResultsTableView.getResultList();
    }

    @Override
    public void setImageResults(List<List<String>> results)
    {
        super.setImageResults(results);
        int i=0;
        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        for (List<String> str : results)
        {
            String name = imageRawResults.get(i).get(0);
            ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
            if (imageCollection.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                image.setOffLimbFootprintVisibility(false);   // hide off limb footprint by default
                getResultList().setValueAt(false, i, offlimbTableView.getOffLimbIndex());   // hide off limb footprint by default
            }
            else
            {
                getResultList().setValueAt(false, i, offlimbTableView.getOffLimbIndex());   // hide off limb footprint by default
            }

            ++i;
        }
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
    }

    class OfflimbImageResultsTableModeListener extends ImageResultsTableModeListener
    {
        public void tableChanged(TableModelEvent e)
        {

            if (e.getColumn() == offlimbTableView.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                String namePrefix = name.substring(0, name.length()-4);
                super.tableChanged(e);
                offlimbTableView.getResultList().setValueAt(false, row, offlimbTableView.getOffLimbIndex());
                setOffLimbFootprintVisibility(namePrefix, false);   // set visibility to false if we are mapping or unmapping the image
            }
            else if (e.getColumn() == offlimbTableView.getOffLimbIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                String namePrefix = name.substring(0, name.length()-4);
                boolean visible = (Boolean)getResultList().getValueAt(row, offlimbTableView.getOffLimbIndex());
                setOffLimbFootprintVisibility(namePrefix, visible);
                ((OfflimbImageResultsTableView) imageResultsTableView).getOfflimbControlsButton().setEnabled(visible);
            }
            super.tableChanged(e);

        }
    }

    protected void setOffLimbFootprintVisibility(String name, boolean visible)
    {
        List<ImageKeyInterface> keys = imageSearchModel.createImageKeys(name, imageSearchModel.getImageSourceOfLastQuery(), instrument);
        ImageCollection images = (ImageCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageCollectionModelName());
        for (ImageKeyInterface key : keys)
        {
            if (images.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                image.setOffLimbFootprintVisibility(visible);
            }
        }
    }


    public class OfflimbImagesTableModel extends DefaultTableModel
    {
        public OfflimbImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == offlimbTableView.getShowFootprintColumnIndex() || column == offlimbTableView.getOffLimbIndex() || column == offlimbTableView.getFrusColumnIndex())
            {
                String name = imageRawResults.get(row).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
                ImageCollection images = (ImageCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageCollectionModelName());
                return images.containsImage(key);
            }
            else
            {
                return column == offlimbTableView.getMapColumnIndex() || column == offlimbTableView.getBndrColumnIndex();
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= offlimbTableView.getBndrColumnIndex())
                return Boolean.class;
            else
                return String.class;
        }
    }
}
