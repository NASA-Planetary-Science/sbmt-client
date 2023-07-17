package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
//import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.core.pointing.PointingSource;

public class DistributedPerspectiveImagePreRenderer2
{

    public DistributedPerspectiveImagePreRenderer2(String inputDir, String outputPath, PointingSource pointingSource, int instrumentIndex, ShapeModelBody body, ShapeModelType type, boolean reprocess)
    {
    	 File outputPathDir = new File(outputPath);
    	 if (outputPathDir.exists() == false) outputPathDir.mkdirs();
    	 SessionFactory factory = SessionFactory.getFactory();
         Session session = factory.getSession();
         {
             try
             {
                 session.init("");
                 JobTemplate jt = session.createJobTemplate();
                 String outputLogPath = "/clusterOutputFiles/";
                 File outputDir = new File(System.getProperty("user.home")+outputLogPath);
                 if (!outputDir.exists()) outputDir.mkdirs();
                 jt.setOutputPath(":" + outputDir);
                 jt.setErrorPath(":" + outputDir);

                 //imageFileName GASKELL RQ36 ALTWG-SPC-v20181109b 0 $baseDir/support $reprocess
                 jt.setRemoteCommand("/homes/sbmt/dartWorkspace/preRenderImage.sh");
                 List<String> argList = new ArrayList<String>();

                 List<File> fileList = Lists.newArrayList(new File(inputDir).listFiles());

                 int i=0;
                 for (; i<fileList.size();)
                 {
                	 int nextBatchLength = Math.min(100, fileList.size() - i);
	                 for (int j=0; j<nextBatchLength; j++)
	                 {
              		 	argList.clear();

	                     argList.add(fileList.get(i+j).getAbsolutePath());
	                     argList.add(pointingSource.name());
	                     argList.add(body.name());
	                     argList.add(type.name());
	                     argList.add("" + instrumentIndex);
	                     argList.add(outputPath);
	                     argList.add(""+reprocess);
	                     jt.setArgs(argList);
	                     String id = session.runJob(jt);
	                     System.out.println("Your job has been submitted with id " + id + " for image pre-rendering for image " + fileList.get(i+j).getAbsolutePath() + " to ouput dir " + outputPath);
	                 }
//	                 //wait for this batch of 100 to finish
	                 session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
	                 i += nextBatchLength;
	                 System.out.println(
							"DistributedPerspectiveImagePreRenderer: DistributedPerspectiveImagePreRenderer: processed through " + i + " of " + fileList.size());
                 }
                 System.out.println ("Number of jobs completed = " + i);

                 session.deleteJobTemplate(jt);
                 session.exit();

             }
             catch (DrmaaException e)
             {
                 System.out.println("Error: "  + e.getMessage());
             }
         }
         java.awt.Toolkit.getDefaultToolkit().beep();
         System.out.println("Done");

    }

    //String inputDirectory, ImageSource, ShapeModelBody, ShapeModelType, int imagerIndex, String outputDir = args[5] + args[1], reproecess

    public static void main(String[] args)
    {
    	//$baseDir/images GASKELL RQ36 ALTWG-SPC-v20181109b 0 $baseDir/support $reprocess
        String inputDirectory = args[0];
        final PointingSource source = PointingSource.valueOf(args[1]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[2]);
        ShapeModelType type = ShapeModelType.provide(args[3]);
        int imagerIndex = Integer.parseInt(args[4]);
        String outputDirectory = args[5] + "/" + args[1];
        boolean reprocess = Boolean.parseBoolean(args[6]);

        new DistributedPerspectiveImagePreRenderer2(inputDirectory, outputDirectory, source, imagerIndex, body, type, reprocess);
        System.out.println("Done");
    }
}
