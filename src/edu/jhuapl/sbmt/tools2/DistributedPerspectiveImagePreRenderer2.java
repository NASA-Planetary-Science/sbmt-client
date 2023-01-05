package edu.jhuapl.sbmt.tools2;

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
import edu.jhuapl.sbmt.core.image.ImageSource;

public class DistributedPerspectiveImagePreRenderer2
{

    public DistributedPerspectiveImagePreRenderer2(String inputDir, String outputPath, ImageSource pointingSource, int instrumentIndex, ShapeModelBody body, ShapeModelType type, boolean reprocess)
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
//                 String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
                 String outputLogPath = "/clusterOutputFiles/";
                 File outputDir = new File(System.getProperty("user.home")+outputLogPath);
                 if (!outputDir.exists()) outputDir.mkdirs();
//                 jt.setOutputPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//                 jt.setErrorPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
                 jt.setOutputPath(":" + outputDir);
                 jt.setErrorPath(":" + outputDir);

                 //imageFileName GASKELL RQ36 ALTWG-SPC-v20181109b 0 $baseDir/support $reprocess
                 jt.setRemoteCommand("/homes/sbmt/dartWorkspace/preRenderImage.sh");
                 List<String> argList = new ArrayList<String>();

                 List<File> fileList = Lists.newArrayList(new File(inputDir).listFiles());
//                 System.out.println(
//						"DistributedPerspectiveImagePreRenderer: DistributedPerspectiveImagePreRenderer: number of files " + fileList.length);

                 //instead of grabbing all of the images (the old way above), grab the ones for the appropriate pointing file list
//                 File inputParent = new File(inputDir).getParentFile();
//                 File files = null;
//                 if (pointingSource == ImageSource.SPICE)
//                 {
//                	 files = new File(inputParent, "imagelist-fullpath-info.txt");
//                 }
//                 else
//                 {
//                	 files = new File(inputParent, "imagelist-fullpath-sum.txt");
//
//                 }
//                 BufferedReader reader = new BufferedReader(new FileReader(files));
//                 List<File> fileList = new ArrayList<File>();
//                 String line;
//                 while((line = reader.readLine()) != null) {
//                     fileList.add(new File(line));
//                 }
//                 reader.close();


//                 Arrays.sort(fileList);
                 int i=0;
                 for (; i<fileList.size();)
                 {
                	 int nextBatchLength = Math.min(100, fileList.size() - i);
	                 for (int j=0; j<nextBatchLength; j++)
	                 {
              		 	argList.clear();

	                     argList.add(fileList.get(i+j).getAbsolutePath());
//	                     argList.add(fileList[i].getAbsolutePath());
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
//	                 i++;
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
//                 session.exit();
             }
//             catch (FileNotFoundException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
         }
         java.awt.Toolkit.getDefaultToolkit().beep();
         System.out.println("Done");

    }

    //String inputDirectory, ImageSource, ShapeModelBody, ShapeModelType, int imagerIndex, String outputDir = args[5] + args[1], reproecess

    public static void main(String[] args)
    {
    	//$baseDir/images GASKELL RQ36 ALTWG-SPC-v20181109b 0 $baseDir/support $reprocess
        String inputDirectory = args[0];
        final ImageSource source = ImageSource.valueOf(args[1]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[2]);
        ShapeModelType type = ShapeModelType.provide(args[3]);
        int imagerIndex = Integer.parseInt(args[4]);
        String outputDirectory = args[5] + "/" + args[1];
        boolean reprocess = Boolean.parseBoolean(args[6]);

//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//
//        URL[] urls = ((URLClassLoader)cl).getURLs();
//
//        for(URL url: urls){
//        	System.out.println(url.getFile());
//        }


        new DistributedPerspectiveImagePreRenderer2(inputDirectory, outputDirectory, source, imagerIndex, body, type, reprocess);
        System.out.println("Done");
    }

//    private static void processImage(String[] args, JobTemplate jt, Session session) throws DrmaaException
//    {
//        System.out.println(
//                "DistributedPerspectiveImagePreRenderer: processImage: processing file " + args[0]);
//        jt.setRemoteCommand("/homes/sbmt/workspace2/preRenderImage.sh");        //script Directory
//        List<String> list = new ArrayList<String>();
//
//        jt.setArgs(list);
//        String id = session.runJob(jt);
//        System.out.println("Your job has been submitted with id " + id + " for image " + args[0]);
//    }

}





// TODO Auto-generated constructor stub

//
//
//SessionFactory factory = SessionFactory.getFactory();
//Session session = factory.getSession();
//{
//    try
//    {
//        session.init("");
//        JobTemplate jt = session.createJobTemplate();
//        String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        File outputDir = new File(outputPath);
//        if (!outputDir.exists()) outputDir.mkdirs();
//        jt.setOutputPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//        jt.setErrorPath(":" + JobTemplate.HOME_DIRECTORY + outputPath);
//
////        for (int i=-1; i<20; i++)
//////            if ((i!=6) && (i!=7))
////                runReport(i, jt, session);
////        session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
//////        runReport(6, jt, session);
//////        runReport(7, jt, session);
//////        session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
////        jt.setRemoteCommand(scriptDir + "/report2.sh");           //script directory
//        List<String> list = new ArrayList<String>();
//        list.add(e)
////        list.add(jarDir);                                      //jar Directory
////        list.add(workDir);                                      //workDirectory
////        list.add(runName);
////        list.add(resName);                                      //resName
//        jt.setArgs(list);
//        String id = session.runJob(jt);
//        System.out.println("Your job has been submitted with id " + id + " for summary reports");
//        session.synchronize(Collections.singletonList(Session.JOB_IDS_SESSION_ALL), Session.TIMEOUT_WAIT_FOREVER, false);
//        session.deleteJobTemplate(jt);
//        session.exit();
//
//    }
//    catch (DrmaaException e)
//    {
//        System.out.println("Error: "  + e.getMessage());
//        session.exit();
//    }
//}
