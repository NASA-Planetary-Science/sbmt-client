package edu.jhuapl.sbmt.image2.modules.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

public class SaveImageListOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<List<G1>, Void>
{
	public SaveImageListOperator()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData() throws IOException, Exception
	{
		List<G1> images = inputs.get(0);
		File file = CustomFileChooser.showSaveDialog(null, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing" + nl);
                int size = images.size();
                for (int i = 0; i < size; ++i)
                {
//                	int actualRow = imageResultsTableView.getResultList().getRowSorter().convertRowIndexToModel(i);
                	String imageName = new File(images.get(0).getFilename()).getName();
                	Date imageDate = images.get(i).getDate();
                	String pointingSource = images.get(0).getPointingSourceType().toString();
                	out.write(imageName + " " + sdf.format(imageDate) + " " + pointingSource);
//                    String image = new File(imageRawResults.get(actualRow).get(0)).getName();
//                    String dtStr = imageRawResults.get(actualRow).get(1);
//                    Date dt = new Date(Long.parseLong(dtStr));

//                    out.write(imageName + " " + sdf.format(imageDate) + " " + imageSearchModel.getImageSourceOfLastQuery().toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
	}
}
