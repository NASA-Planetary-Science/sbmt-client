package edu.jhuapl.sbmt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NonexistentRemoteFile;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.file.ZipFileUnzipper;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;

public abstract class ImageGalleryGenerator
{
    public static class ImageGalleryEntry
    {
        private final String caption;
        private final String imageFilename;
        private final String previewFilename;

        public ImageGalleryEntry(String caption, String imageFilename, String previewFilename)
        {
            this.caption = caption;
            this.imageFilename = imageFilename;
            this.previewFilename = previewFilename;
        }

        @Override
        public String toString()
        {
            return caption + " (" + previewFilename + " -> " + imageFilename + ")";
        }
    }

    protected static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static ImageGalleryGenerator of(IImagingInstrument instrument)
    {
        if (instrument == null)
        {
            return null;
        }

        String rootPath = instrument.getSearchQuery().getRootPath();

        AtomicReference<String> galleryTopReference = new AtomicReference<>();

        String galleryListFile = SAFE_URL_PATHS.getString(rootPath, "gallery-list.txt");

        File file;
        try
        {
            file = FileCache.getFileFromServer(galleryListFile);
        }
        catch (NonexistentRemoteFile e)
        {
            // Ignore this -- this file is a newer resource, not present
            // in legacy models. It was added when DART models were added.
            file = null;
        }

        String dataPath = instrument.getSearchQuery().getDataPath();
        String galleryPath = instrument.getSearchQuery().getGalleryPath();

        ImageGalleryGenerator nonFinalGenerator;
        if (file == null || !file.isFile())
        {
            // Legacy behavior.
            nonFinalGenerator = new ImageGalleryGenerator() {

                @Override
                protected String getPreviewImageFile(String imageFileName)
                {
                    return "/" + imageFileName.replace(dataPath, galleryPath) + "-small.jpeg";
                }

                @Override
                protected String getGalleryImageFile(String imageFileName)
                {
                    return "/" + imageFileName.replace(dataPath, galleryPath) + ".jpeg";
                }

                @Override
                protected String getPreviewTopUrl()
                {
                    galleryTopReference.compareAndSet(null, Configuration.getDataRootURL().toString());

                    return galleryTopReference.get();
                }

                @Override
                protected void setPreviewTopUrl(String previewTopUrl)
                {
                    galleryTopReference.set(previewTopUrl != null ? previewTopUrl : Configuration.getDataRootURL().toString());
                }
            };
        }
        else
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(file)))
            {

                LinkedHashMap<String, String> previewImages = new LinkedHashMap<>();
                LinkedHashMap<String, String> galleryImages = new LinkedHashMap<>();

                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null)
                {
                    ++lineNumber;

                    String[] files = line.split("\\s+");

                    if (files.length != 3)
                    {
                        throw new IOException("Expected three fields in gallery list file " + file + " on line " + lineNumber + ": " + line);
                    }

                    String imageFilePath = SAFE_URL_PATHS.getString(dataPath, files[0]);
                    previewImages.put(imageFilePath, SAFE_URL_PATHS.getString(galleryPath, files[1]));
                    galleryImages.put(imageFilePath, SAFE_URL_PATHS.getString(galleryPath, files[2]));
                }

                nonFinalGenerator = new ImageGalleryGenerator() {

                    @Override
                    protected String getPreviewImageFile(String imageFileName)
                    {
                        return previewImages.get(imageFileName);
                    }

                    @Override
                    protected String getGalleryImageFile(String imageFileName)
                    {
                        return galleryImages.get(imageFileName);
                    }

                    @Override
                    protected String getPreviewTopUrl()
                    {
                        galleryTopReference.compareAndSet(null, Configuration.getDataRootURL().toString());

                        return galleryTopReference.get();
                    }

                    @Override
                    protected void setPreviewTopUrl(String previewTopUrl)
                    {
                        galleryTopReference.set(previewTopUrl != null ? previewTopUrl : Configuration.getDataRootURL().toString());
                    }

                };
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to show gallery view", e);
            }

        }

        ImageGalleryGenerator galleryGenerator = nonFinalGenerator;

        String galleryZipFile = SAFE_URL_PATHS.getString(rootPath, "gallery.zip");
        if (!FileCache.instance().getFile(galleryZipFile).isFile())
        {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception
                {
                    File zipFile = null;
                    try
                    {
                        System.err.println("Trying to download and unzip again the file " + galleryZipFile);
                        zipFile = FileCache.getFileFromServer(galleryZipFile);
                        if (zipFile.isFile())
                        {
                            ZipFileUnzipper unzipper = ZipFileUnzipper.of(zipFile);
                            unzipper.unzip();
                            galleryGenerator.setPreviewTopUrl(".");
                        }
                    }
                    catch (Exception e)
                    {
                        // Ignore this -- this file is a newer resource, not present
                        // in legacy models. It was added when DART models were
                        // added.
                        if (zipFile != null && !Debug.isEnabled())
                        {
                            zipFile.delete();
                        }
                    }
                    return null;
                }

            };
            worker.execute();
        }
        else
        {
            // The gallery zip file exists -- assume it was unzipped in the cache already.
            galleryGenerator.setPreviewTopUrl(".");
        }


        return galleryGenerator;
  }

    protected ImageGalleryGenerator()
    {
        super();
    }

    // Generates all the required image gallery files and returns the location of the HTML file
    public String generateGallery(List<ImageGalleryEntry> entries)
    {
        // Define location and name of gallery file
        String galleryURL = SAFE_URL_PATHS.getString(Configuration.getCacheDir(), "gallery.html");

//        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//
//            @Override
//            protected Void doInBackground() throws Exception
//            {
//                for (ImageGalleryEntry entry : entries) {
//                    try
//                    {
//                        FileCache.getFileFromServer(entry.previewFilename);
//                    }
//                    catch (Exception e)
//                    {
//
//                    }
//                    try
//                    {
//                        FileCache.getFileFromServer(entry.imageFilename);
//                    }
//                    catch (Exception e)
//                    {
//
//                    }
//                }
//                return null;
//            }
//
//        };
//        worker.execute();

        // Generate the image gallery
        try
        {
            String galleryName = "Search Results Image Gallery (Auto-Generated on " +
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ")";
            generateGalleryHTML(galleryURL,galleryName,entries);
        }
        catch(Exception e)
        {
            System.err.println(e);
            return null;
        }

        // Copy over required javascript files
        ConvertResourceToFile.convertResourceToRealFile(
                galleryURL,
                "/edu/jhuapl/sbmt/data/main.js",
                Configuration.getCustomGalleriesDir());
        ConvertResourceToFile.convertResourceToRealFile(
                galleryURL,
                "/edu/jhuapl/sbmt/data/jquery.js",
                Configuration.getCustomGalleriesDir());

        // Return to user to be opened
        return galleryURL;
    }

    public ImageGalleryEntry getEntry(String imageFileName)
    {
        String imageFileUrl = locateGalleryFile(getGalleryImageFile(imageFileName));
        String previewFileUrl = locateGalleryFile(getPreviewImageFile(imageFileName));

        return new ImageGalleryEntry(imageFileName.substring(imageFileName.lastIndexOf("/") + 1), imageFileUrl, previewFileUrl);
    }

    protected abstract String getPreviewImageFile(String imageFileName);

    protected abstract String getGalleryImageFile(String imageFileName);

    protected abstract String getPreviewTopUrl();

    protected abstract void setPreviewTopUrl(String previewToUrl);

    protected String locateGalleryFile(String fileName)
    {
        return SAFE_URL_PATHS.getString(getPreviewTopUrl(), fileName);
    }

    // Creates equivalent of gallery.html at specified location and containing entries in argument
    private void generateGalleryHTML(String galleryURL, String galleryName, List<ImageGalleryEntry> entries) throws FileNotFoundException
    {
        // Setup writer
        PrintWriter writer = new PrintWriter(galleryURL);

        // Write header
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        writer.println("<head>");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        writer.println("<title>" + galleryName + "</title>");
        writer.println("<meta name=\"description\" content=\"Easiest jQuery Tooltip Ever\">");
        writer.println("<script src=\"jquery.js\" type=\"text/javascript\"></script>");
        writer.println("<script src=\"main.js\" type=\"text/javascript\"></script>");
        writer.println("</meta>");

        // Write style
        writer.println("<style>");
        writer.println("body {");
        writer.println("    margin:0;");
        writer.println("    padding:40px;");
        writer.println("    background:#fff;");
        writer.println("    font:80% Arial, Helvetica, sans-serif;");
        writer.println("    color:#555;");
        writer.println("    line-height:180%;");
        writer.println("}");
        writer.println("h1{");
        writer.println("    font-size:180%;");
        writer.println("    font-weight:normal;");
        writer.println("    color:#555;");
        writer.println("}");
        writer.println("h2{");
        writer.println("    clear:both;");
        writer.println("    font-size:160%;");
        writer.println("    font-weight:normal;");
        writer.println("    color:#555;");
        writer.println("    margin:0;");
        writer.println("    padding:.5em 0;");
        writer.println("}");
        writer.println("a{");
        writer.println("    text-decoration:none;");
        writer.println("    color:#f30;");
        writer.println("}");
        writer.println("p{");
        writer.println("    clear:both;");
        writer.println("    margin:0;");
        writer.println("    padding:.5em 0;");
        writer.println("}");
        writer.println("pre{");
        writer.println("    display:block;");
        writer.println("    font:100% \"Courier New\", Courier, monospace;");
        writer.println("    padding:10px;");
        writer.println("    border:1px solid #bae2f0;");
        writer.println("    background:#e3f4f9;");
        writer.println("    margin:.5em 0;");
        writer.println("    overflow:auto;");
        writer.println("    width:800px;");
        writer.println("}");
        writer.println("img{border:none;}");
        writer.println("ul,li{");
        writer.println("    margin:0;");
        writer.println("    padding:0;");
        writer.println("}");
        writer.println("li{");
        writer.println("    list-style:none;");
        writer.println("    float:left;");
        writer.println("    display:inline;");
        writer.println("    margin-right:10px;");
        writer.println("}");
        writer.println("#preview{");
        writer.println("    position:absolute;");
        writer.println("    border:1px solid #ccc;");
        writer.println("    background:#333;");
        writer.println("    padding:5px;");
        writer.println("    display:none;");
        writer.println("    color:#fff;");
        writer.println("    }");
        writer.println("</style>");
        writer.println("</head>");

        // Write entries
        writer.println("<body>");
        writer.println("<h1>" + galleryName + "</h1>");
        writer.println("<ul>");

        for(ImageGalleryEntry entry : entries)
        {
            writer.println("<li><a href=\"" +
                entry.imageFilename +
                "\" class=\"preview\" title=\"" + entry.caption + "\"><img src=\"" +
                entry.previewFilename +
                "\" alt=\"" + entry.caption + "\" /></a></li>");
        }
        writer.println("</ul>");
        writer.println("</body>");

        // End of document
        writer.println("</html>");

        // Close writer
        writer.close();
    }
}
