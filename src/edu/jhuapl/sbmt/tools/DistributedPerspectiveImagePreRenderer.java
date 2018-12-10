package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.image.ImageSource;s

public class DistributedPerspectiveImagePreRenderer
{

    public DistributedPerspectiveImagePreRenderer(String inputDir, String outputDir, ImageSource pointingSource, int instrumentIndex, ShapeModelBody body, ShapeModelType type)
    {
        // TODO Auto-generated constructor stub



        SessionFactory factory = SessionFactory.getFactory();
        Session session = factory.getSession();
        {
            try
            {
                session.init("");
                JobTemplate jt = session.createJobTemplate();
                String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String outputPath = "/clusterOutputFiles/"+dateString+"/";
                File outputDir = new File(System.getProperty("user.home")+outputPath);
                if (!outputDir.exists()) outputDir.mkdirs();
                jt.setOutputPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
                jt.setErrorPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);

                for (int i=-1; i<20; i++)
//                    if ((i!=6) && (i!=7))
                        runReport(i, jt, session);
                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
//                runReport(6, jt, session);
//                runReport(7, jt, session);
//                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
                jt.setRemoteCommand(scriptDir + "/report2.sh");           //script directory
                List<String> list = new ArrayList<String>();
                list.add(jarDir);                                      //jar Directory
                list.add(workDir);                                      //workDirectory
                list.add(runName);
                list.add(resName);                                      //resName
                jt.setArgs(list);
                String id = session.runJob(jt);
                System.out.println("Your job has been submitted with id " + id + " for summary reports");
                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
                session.deleteJobTemplate(jt);
                session.exit();

            }
            catch (DrmaaException e)
            {
                System.out.println("Error: "  + e.getMessage());
                session.exit();
            }
        }
    }

}
