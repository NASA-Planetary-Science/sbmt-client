package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.gui.image.model.custom.CustomImagesModel;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImagesControlPanel;

public class CustomImagesControlController
{
    CustomImagesControlPanel panel;
    CustomImagesModel model;
    List<ImageInfo> customImages;

    public CustomImagesControlController(CustomImagesModel model)
    {
        panel = new CustomImagesControlPanel();
        this.model = model;
        this.customImages = model.getCustomImages();
        init();
    }

    public CustomImagesControlPanel getPanel()
    {
        return panel;
    }

    private void init()
    {
        panel.getNewButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                newButtonActionPerformed(e);
            }
        });

        panel.getEditButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editButtonActionPerformed(e);
            }
        });

        panel.getDeleteButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteButtonActionPerformed(e);
            }
        });
    }

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageInfo imageInfo = new ImageInfo();
        CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, model.getInstrument());
        dialog.setImageInfo(imageInfo, model.getModelManager().getPolyhedralModel().isEllipsoid());
        dialog.setLocationRelativeTo(getPanel());
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            imageInfo = dialog.getImageInfo();
            try
            {
                saveImage(model.getImageResults().size(), null, imageInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        model.editButtonActionPerformed(evt);
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        model.deleteButtonActionPerformed(evt);
    }

    private void saveImage(int index, ImageInfo oldImageInfo, ImageInfo newImageInfo) throws IOException
    {
        model.saveImage(index, oldImageInfo, newImageInfo);
    }
}
