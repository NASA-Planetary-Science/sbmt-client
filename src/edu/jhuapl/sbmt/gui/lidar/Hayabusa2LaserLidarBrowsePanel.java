package edu.jhuapl.sbmt.gui.lidar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.lidar.LidarBrowseDataCollection.LidarDataFileSpec;
import edu.jhuapl.sbmt.util.TimeUtil;

public class Hayabusa2LaserLidarBrowsePanel extends LidarBrowsePanel
{

    public Hayabusa2LaserLidarBrowsePanel(ModelManager modelManager, SmallBodyViewConfig smallBodyConfig)
    {
        super(modelManager);
        String datasourceName="Hayabusa2";
        String browseFileList=smallBodyConfig.lidarBrowseDataSourceMap.get(datasourceName);
        repopulate(browseFileList, datasourceName);
    }

    public void repopulate(String browseFileList, String datasourceName)
    {
        System.out.println("Hayabusa2LaserLidarBrowsePanel: repopulate: repopulating");
        lidarResultListModel.clear();

        File listFile=FileCache.getFileFromServer(browseFileList);
        try
        {
            Scanner scanner=new Scanner(new FileInputStream(listFile));
            while (scanner.hasNext())
            {
                String filename=scanner.next();
                double startTime=Double.valueOf(scanner.next());
                double endTime=Double.valueOf(scanner.next());
                LidarDataFileSpec spec=new LidarDataFileSpec();
                spec.path=filename;
                spec.name=Paths.get(filename).getFileName().toString();
                System.out.println(
                        "Hayabusa2LaserLidarBrowsePanel: repopulate: start time is " + startTime + " and utc time is " + TimeUtil.et2str(startTime));
                spec.comment=TimeUtil.et2str(startTime)+" - "+TimeUtil.et2str(endTime);
                //System.out.println(spec.comment);
                lidarResultListModel.addElement(spec);
                //System.out.println(filename);
            }
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Create the list and put it in a scroll pane.
        //resultList = new JList(lidarResultListModel);
        //resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //resultList.addListSelectionListener(this);

        resultsLabel.setText("Available Files: "+datasourceName);

    }

}
