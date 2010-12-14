package edu.jhuapl.near.util;

public class NativeLibraryLoader
{
	/**
	 * Loads in the vtk shared libraries. This needs to be called before any vtk classes
	 * are used. This has been tested with vtk version 5.4.2 only.
	 *
	 */
    static public void loadVtkLibraries()
    {
    	// Before loading the native vtk libraries, we want to make sure the
    	// awt/swing subsystem is loaded and initialized as much as possible (i.e.
    	// has already done some usefull work) since by doing this, we
    	// ensure that other java-internal shared libraries which vtk depends on are
    	// already loaded in. Failure to do so may result in linking errors when
    	// loading in the vtk shared libraries (especially vtkRenderingJava).
    	// The following dummy thread seems to do the trick.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
        	public void run()
        	{
        		// do nothing
        	}
        });

    	String name = System.getProperty("os.name");
    	if (name.toLowerCase().startsWith("windows"))
    	{
        	// On windows, just load all the libraries
    		// manually rather than dealing with how to find dependent libraries.
    		// Note they must be loaded in the following order.
        
    		System.loadLibrary("jawt"); // For some reason this is not loaded automatically on Windows
    		System.loadLibrary("vtkzlib");
    		System.loadLibrary("vtkNetCDF");
    		System.loadLibrary("vtksys");
    		System.loadLibrary("vtkalglib");
    		System.loadLibrary("vtkexoIIc");
    		System.loadLibrary("vtkexpat");
    		System.loadLibrary("vtkfreetype");
    		System.loadLibrary("vtkftgl");
    		System.loadLibrary("vtkjpeg");
    		System.loadLibrary("vtklibxml2");
    		System.loadLibrary("vtkmetaio");
    		System.loadLibrary("vtkpng");
    		System.loadLibrary("vtkproj4");
    		System.loadLibrary("vtktiff");
    		System.loadLibrary("vtkverdict");
    		System.loadLibrary("vtkCommon");
    		System.loadLibrary("vtkCommonJava");
    		System.loadLibrary("vtkDICOMParser");
    		System.loadLibrary("vtkFiltering");
    		System.loadLibrary("vtkFilteringJava");
    		System.loadLibrary("vtkGraphics");
    		System.loadLibrary("vtkGraphicsJava");
    		System.loadLibrary("vtkGenericFiltering");
    		System.loadLibrary("vtkGenericFilteringJava");
    		System.loadLibrary("vtkIO");
    		System.loadLibrary("vtkIOJava");
    		System.loadLibrary("vtkImaging");
    		System.loadLibrary("vtkImagingJava");
    		System.loadLibrary("vtkRendering");
    		System.loadLibrary("vtkRenderingJava");
    		System.loadLibrary("vtkHybrid");
    		System.loadLibrary("vtkHybridJava");
    		System.loadLibrary("vtkWidgets");
    		System.loadLibrary("vtkWidgetsJava");
    		System.loadLibrary("vtkInfovis");
    		System.loadLibrary("vtkInfovisJava");
    		System.loadLibrary("vtkViews");
    		System.loadLibrary("vtkViewsJava");
    		System.loadLibrary("vtkGeovis");
    		System.loadLibrary("vtkGeovisJava");
    		System.loadLibrary("vtkVolumeRendering");
    		System.loadLibrary("vtkVolumeRenderingJava");
    		System.loadLibrary("vtksbUnsorted");
    		System.loadLibrary("vtksbUnsortedJava");
    	}
    	else
    	{
    		// On linux or mac the shared libraries must have
    		// $ORIGIN or @loader_path embedded in them so that
    		// the dependent libraries are found.
    
    		System.loadLibrary("vtkCommonJava");
    		System.loadLibrary("vtkFilteringJava");
    		System.loadLibrary("vtkGraphicsJava");
    		System.loadLibrary("vtkGenericFilteringJava");
    		System.loadLibrary("vtkIOJava");
    		System.loadLibrary("vtkImagingJava");
    		System.loadLibrary("vtkRenderingJava");
    		System.loadLibrary("vtkHybridJava");
    		System.loadLibrary("vtkWidgetsJava");
    		System.loadLibrary("vtkInfovisJava");
    		System.loadLibrary("vtkViewsJava");
    		System.loadLibrary("vtkGeovisJava");
    		System.loadLibrary("vtkVolumeRenderingJava");
    		System.loadLibrary("vtksbUnsortedJava");
    	}
    }

    /**
     * For batch processing jobs on linux, we don't need any rendering,
     * so don't load the rendering related libraries.
     */
    static public void loadVtkLibrariesLinuxNoX11()
    {
		System.loadLibrary("vtkCommonJava");
		System.loadLibrary("vtkFilteringJava");
		System.loadLibrary("vtkGraphicsJava");
		System.loadLibrary("vtkGenericFilteringJava");
		System.loadLibrary("vtkIOJava");
		System.loadLibrary("vtkImagingJava");
		System.loadLibrary("vtksbUnsortedJava");
    }
}
