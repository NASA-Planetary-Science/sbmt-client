package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.image.ImageSource;

public class DistributedPerspectiveImagePreRenderer
{

    public DistributedPerspectiveImagePreRenderer(String inputDir, String outputPath, ImageSource pointingSource, int instrumentIndex, ShapeModelBody body, ShapeModelType type)
    {
        // TODO Auto-generated constructor stub

//
//
//        SessionFactory factory = SessionFactory.getFactory();
//        Session session = factory.getSession();
//        {
//            try
//            {
//                session.init("");
//                JobTemplate jt = session.createJobTemplate();
//                String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
//                File outputDir = new File(outputPath);
//                if (!outputDir.exists()) outputDir.mkdirs();
//                jt.setOutputPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//                jt.setErrorPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//
////                for (int i=-1; i<20; i++)
//////                    if ((i!=6) && (i!=7))
////                        runReport(i, jt, session);
////                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
//////                runReport(6, jt, session);
//////                runReport(7, jt, session);
//////                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
////                jt.setRemoteCommand(scriptDir + "/report2.sh");           //script directory
//                List<String> list = new ArrayList<String>();
//                list.add(e)
////                list.add(jarDir);                                      //jar Directory
////                list.add(workDir);                                      //workDirectory
////                list.add(runName);
////                list.add(resName);                                      //resName
//                jt.setArgs(list);
//                String id = session.runJob(jt);
//                System.out.println("Your job has been submitted with id " + id + " for summary reports");
//                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
//                session.deleteJobTemplate(jt);
//                session.exit();
//
//            }
//            catch (DrmaaException e)
//            {
//                System.out.println("Error: "  + e.getMessage());
//                session.exit();
//            }
//        }
    }

    //String inputDirectory, ImageSource, ShapeModelBody, ShapeModelType, int imagerIndex, String outputDir = args[5] + args[1], reproecess

    public static void main(String[] args) throws Exception
    {
        String inputDirectory = args[0];
//        final ImageSource source = ImageSource.valueOf(args[1]);
//        ShapeModelBody body = ShapeModelBody.valueOf(args[2]);
//        ShapeModelType type = ShapeModelType.valueOf(args[3]);
//        int imagerIndex = Integer.parseInt(args[4]);
//        String outputDirectory = args[5] + "/" + args[1];
//        boolean reprocess = Boolean.parseBoolean(args[6]);

        SessionFactory factory = SessionFactory.getFactory();
        Session session = factory.getSession();
        {
//            try
//            {
                session.init("");
                JobTemplate jt = session.createJobTemplate();
//                String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
//                File outputDir = new File(outputPath);
//                if (!outputDir.exists()) outputDir.mkdirs();
//                jt.setOutputPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//                jt.setErrorPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
                File[] fileList;
                fileList = new File(inputDirectory).listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return FilenameUtils.getExtension(name).contains("fit");
                    }
                });
                for (File file : fileList)
                {
                    String[] tempArgs = args;
                    tempArgs[0] = file.getAbsolutePath();
                    DistributedPerspectiveImagePreRenderer.processImage(tempArgs, jt, session);
                }

                session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);

                session.deleteJobTemplate(jt);
                session.exit();

//            }
//            catch (DrmaaException e)
//            {
//                System.out.println("Error: "  + e.getMessage());
//            }
        }
    }

    private static void processImage(String[] args, JobTemplate jt, Session session) throws DrmaaException
    {
        System.out.println(
                "DistributedPerspectiveImagePreRenderer: processImage: processing file " + args[0]);
        jt.setRemoteCommand("/homes/sbmt/workspace2/preRenderImage.sh");        //script Directory
        List<String> list = new ArrayList<String>();

        jt.setArgs(list);
        String id = session.runJob(jt);
        System.out.println("Your job has been submitted with id " + id + " for image " + args[0]);
    }

}
