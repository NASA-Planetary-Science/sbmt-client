package edu.jhuapl.sbmt.stateHistory.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import glum.item.BaseItemManager;

public class KernelManagementModel extends BaseItemManager<KernelInfo>
{
	private String loadedKernelsDirectory;

	public KernelManagementModel(String loadedKernelsDirectory) throws IOException
	{
		File[] loadedKernelSets = new File(loadedKernelsDirectory).listFiles();
		if (loadedKernelSets == null) return;
		List<KernelInfo> allSets = new ArrayList<KernelInfo>();
		for (File directory : loadedKernelSets)
		{
			List<String> allFiles = Files.walk(Paths.get(directory.toURI())).filter(Files::isRegularFile).map(f -> f.toString()).collect(Collectors.toList());
			allSets.add(new KernelInfo(directory.getName(), directory.getAbsolutePath(), allFiles));
		}
		setAllItems(allSets);
	}
}


class KernelInfo
{
	String kernelName;
	String kernelDirectory;
	List<String> kernelListing;

	public KernelInfo(String name, String directory, List<String> listing)
	{
		this.kernelName = name;
		this.kernelDirectory = directory;
		this.kernelListing = listing;
	}
}