package edu.jhuapl.near.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;
import altwg.tools.DistributedGravity;
import altwg.tools.Maplet2FITS;

import edu.jhuapl.near.model.SmallBodyModel;

public class Bigmap
{
    public static final int MAX_WIDTH = 1027;
    public static final int MAX_HEIGHT = 1027;
    public static final int MAX_PLANES = 6;

    private ProcessBuilder processBuilder;
    private String bigmapRootDir;
    private String name;
    private double latitude;
    private double longitude;
    private int halfSize = 512;
    private double pixelSize;
    private File outputFolder;
    private File mapletFitsFile;
    private SmallBodyModel smallBodyModel;

    public Bigmap(String bigmapRootDir) throws IOException
    {
        this.bigmapRootDir = bigmapRootDir;

        String execDir = bigmapRootDir;

        ArrayList<String> processCommand = new ArrayList<String>();

        processBuilder = new ProcessBuilder(processCommand);

        processBuilder.directory(new File(bigmapRootDir));

        Map<String, String> env = processBuilder.environment();

        String processName = null;
        if (Configuration.isLinux())
        {
            if (System.getProperty("sun.arch.data.model").equals("64"))
                processName = execDir + File.separator + "BIGMAPO.linux64";
            else
                processName = execDir + File.separator + "BIGMAPO.linux32";

            env.put("LD_LIBRARY_PATH", execDir);
        }
        else if (Configuration.isMac())
        {
            processName = execDir + File.separator + "BIGMAPO.macosx";

            env.put("DYLD_LIBRARY_PATH", execDir);
        }
        else
        {
            processName = execDir + File.separator + "BIGMAPO.win32.exe";
            //throw new IOException("Operating system not supported");
        }

        new File(processName).setExecutable(true);
        processCommand.add(processName);
    }

    public Process runBigmap() throws IOException, InterruptedException
    {
        Process process = processBuilder.start();
        OutputStream stdin = process.getOutputStream();

        /** Hardcoded values for now, check with Olivier B. on these **/
        int seed = 1234; // (value from example in BIGMAPO.F comments)
        double maxMapletRes = 0.2; // km (value recommended by Ray Espiritu)
        double fractionHeights = .005; // fraction of heights for conditioning (value from example in BIGMAPO.F comments)
        double conditioningWeight = .025; // conditioning weight (value from example in BIGMAPO.F comments)
        int slopeIntegration = 1; // (value from example in BIGMAPO.F comments)
        int numIterations = 6; // Not sure how many iterations is appropriate

        // Also, should we be using bigmap.f, bigmapo.f, or bigmapof.f (grotesque) ???

        // Create arguments for bigmap, see header comment of BIGMAPO.F for example
        String arguments = "l" + "\n"               // specify by lat/lon
                + latitude + ", " + longitude + "\n" // lat/lon values
                + pixelSize + ", " + halfSize + ", " + seed + ", " + maxMapletRes + "\n" // scale, qsx, seed, max maplet res
                + name + "\n"                       // bigmap name
                + slopeIntegration + "\n"           // choose slope integration
                + fractionHeights + "\n"            // fraction of heights for conditioning
                + conditioningWeight + "\n";        // conditioning weight
        for(int i=0; i<numIterations; i++)
        {
            arguments += "1" + "\n";
        }
        arguments += "0" + "\n"                     // stop iteration
                + "0" + "\n";                       // no template

        System.out.println(arguments);

        stdin.write(arguments.getBytes());
        stdin.flush();
        stdin.close();

        return process;
    }

    public void convertMapletToFitsAndSaveInOutputFolder(boolean deleteMaplet)
    {
        try
        {
            // Convert the maplet produced by bigmap to FITs format with 10 planes:
            // 0. lat
            // 1. lon
            // 2. radius
            // 3. x position
            // 4. y position
            // 5. z position
            // 6. height
            // 7. albedo
            // 8. sigma
            // 9. quality
            File origMapletFile = new File(bigmapRootDir + File.separator + "BIGMAP" + File.separator + name + ".MAP");
            File bigmapToFitsFile = new File(outputFolder + File.separator + name + "_m2f_" + ".FIT");
            Maplet2FITS.main(new String[] {origMapletFile.getPath(), bigmapToFitsFile.getPath()});

            // Assemble options for calling DistributedGravity
            File dgFitsFile = new File(outputFolder + File.separator + name + "_dg_" + ".FIT");
            File objShapeFile = new File(bigmapRootDir + File.separator + "SHAPEFILES" + File.separator + "SHAPE_LOWEST_RES.OBJ");
            List<String> dgOptionList = new LinkedList<String>();
            dgOptionList.add("-d");
            dgOptionList.add(Double.toString(smallBodyModel.getDensity()));
            dgOptionList.add("-r");
            dgOptionList.add(Double.toString(smallBodyModel.getRotationRate()));
            dgOptionList.add("--fits-local");
            dgOptionList.add(bigmapToFitsFile.getPath());
            dgOptionList.add("--ref-potential");
            dgOptionList.add(Double.toString(smallBodyModel.getReferencePotential()));
            dgOptionList.add("--output-folder");
            dgOptionList.add(outputFolder.getPath());
            dgOptionList.add(objShapeFile.getPath()); // Global shape model file, Olivier suggests lowest res .OBJ **/
            dgOptionList.add(dgFitsFile.getPath()); // Path to output file that will contain all results

            // Debug output
            System.out.println("Bigmap.java: Calling Distributed Gravity with...");
            System.out.println("  density = " + smallBodyModel.getDensity() + " g/cm^3");
            System.out.println("  rotation rate = " + smallBodyModel.getRotationRate() + " rad/s");
            System.out.println("  reference potential = " + smallBodyModel.getReferencePotential() + " J/kg");

            // Convert argument list to array and call DistributedGravity
            // Resulting FITS file has original Maplet2FITS planes plus the following:
            // 10. Add gravity information by appending the following planes to the FITs file
            // 11. normal vector x component
            // 12. normal vector y component
            // 13. normal vector z component
            // 14. gravacc x component
            // 15. gravacc y component
            // 16. gravacc z component
            // 17. magnitude of grav acc
            // 18. grav potential
            // 19. elevation
            // 20. slope
            // 21. Tilt
            // 22. Tilt direction
            // 23. Tilt mean
            // 24. Tilt standard deviation
            // 25. Distance to plane
            // 26. Shaded relief
            String[] dgOptionArray = dgOptionList.toArray(new String[dgOptionList.size()]);
            DistributedGravity.main(dgOptionArray);

            // Open the FITs file produced by DistributedGravity
            Fits dgFits = new Fits(dgFitsFile.getPath());
            BasicHDU dgHdu = dgFits.getHDU(0);
            float[][][] dgData = (float[][][])dgHdu.getData().getData();

            // Extract only the planes that SBMT is interested in
            int liveSize = 2 * halfSize + 1;
            float[][][] outdata = new float[MAX_PLANES][liveSize][liveSize];
            for (int p=0; p<MAX_PLANES; ++p)
            {
                for (int m=0; m<liveSize; ++m)
                {
                    for (int n=0; n<liveSize; ++n)
                    {
                        outdata[p][m][n] = dgData[mapletFitsToDgFitsPlane(p)][m][n];
                    }
                }
            }

            // Write the output FITS file
            mapletFitsFile = new File(outputFolder + File.separator + name + ".FIT");

            Fits f = new Fits();
            BasicHDU hdu = FitsFactory.HDUFactory(outdata);

            hdu.getHeader().addValue("PLANE1", "Elevation Relative to Gravity (kilometers)", null);
            hdu.getHeader().addValue("PLANE2", "Elevation Relative to Normal Plane (kilometers)", null);
            hdu.getHeader().addValue("PLANE3", "Slope (radians)", null);
            hdu.getHeader().addValue("PLANE4", "X coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("PLANE5", "Y coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("PLANE6", "Z coordinate of maplet vertices (kilometers)", null);
            hdu.getHeader().addValue("HALFSIZE", halfSize, "Half Size (pixels)");
            hdu.getHeader().addValue("SCALE", pixelSize, "Horizontal Scale (meters per pixel)");
            hdu.getHeader().addValue("LATITUDE", latitude, "Latitude of Maplet Center (degrees)");
            hdu.getHeader().addValue("LONGTUDE", longitude, "Longitude of Maplet Center (degrees)");

            f.addHDU(hdu);
            BufferedFile bf = new BufferedFile(mapletFitsFile, "rw");
            f.write(bf);
            bf.close();

            // Delete the files that are no longer needed
            bigmapToFitsFile.delete();
            dgFitsFile.delete();
            if(deleteMaplet)
            {
                origMapletFile.delete();
            }
        }
        catch(FitsException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static int mapletFitsToDgFitsPlane(int i) throws Exception
    {
        switch(i)
        {
        case 0:
            return 19; // Confirmed with Olivier
        case 1:
            return 7; // Confirmed with Olivier
        case 2:
            return 20; // Confirmed with Olivier
        case 3:
            return 3;
        case 4:
            return 4;
        case 5:
            return 5;
        default:
            throw new Exception("Unrecognized FITS plane index " + i);
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    /**
     * set the latitude in degrees
     * @param latitude
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    /**
     * set the longitude in degrees and as West Longitude (not east as is shown in the status bar)
     * @param longitude
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
        this.longitude = 360.0 - this.longitude;
        if (this.longitude < 0.0)
            this.longitude += 360.0;
    }

    public int getHalfSize()
    {
        return halfSize;
    }

    public void setHalfSize(int halfSize)
    {
        this.halfSize = halfSize;
    }

    public double getPixelSize()
    {
        return pixelSize;
    }

    public void setPixelSize(double pixelSize)
    {
        this.pixelSize = pixelSize;
    }

    public File getMapletFile()
    {
        return mapletFitsFile;
    }

    public File getOutputFolder()
    {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }

    public void setSmallBodyModel(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    public SmallBodyModel getSmallBodyModel()
    {
        return smallBodyModel;
    }
}
