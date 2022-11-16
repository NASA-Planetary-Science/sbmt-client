package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.pointing.spice.KernelProviderFromLocalMetakernel;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

public class KernelFileLister implements Callable<Integer>
{

    @Parameters(index = "0", description = "The path to the metakernel file")
    private String metaKernelFileName;

    protected KernelFileLister()
    {

    }

    @Override
    public Integer call() throws Exception
    {
        int result = 1;

        Preconditions.checkState(metaKernelFileName != null);
        Preconditions.checkState(!metaKernelFileName.matches("^\\s*$"));

        Path metaKernelPath = Paths.get(metaKernelFileName);

        KernelProviderFromLocalMetakernel kernelProvider = KernelProviderFromLocalMetakernel.of(metaKernelPath);
        List<File> kernels = kernelProvider.get();

        for (File kernel : kernels)
        {
            System.out.println(kernel);
        }

        result = 0;

        return result;
    }

    public static void main(String[] args)
    {
        int exitCode = 1;
        try
        {
            System.setProperty("java.awt.headless", "true");
            exitCode = new CommandLine(new KernelFileLister()).execute(args);
        }
        finally
        {
            System.exit(exitCode);
        }
    }

}
