package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.serialization.Serializers;
import edu.jhuapl.saavtk.model.BasicColoringDataManager;
import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.file.DataFileInfo;
import edu.jhuapl.saavtk.util.file.DataFileReader;
import edu.jhuapl.saavtk.util.file.DataObjectInfo;
import edu.jhuapl.saavtk.util.file.DataObjectInfo.InfoRow;
import edu.jhuapl.saavtk.util.file.TableInfo;
import edu.jhuapl.saavtk.util.file.TableInfo.ColumnInfo;

public class DiscoverPlateColorings
{
	private static final String MAP_NAME = "map_name";
	private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

	private final File topDirectory;
	private final String coloringDirectory;
	private final BasicColoringDataManager coloringDataManager;
	private final File txtFile;
	private final File metadataFile;

	protected DiscoverPlateColorings(String[] args)
	{
		Preconditions.checkNotNull(args);
		Preconditions.checkArgument(args.length > 2, "Too few arguments");

		File topDirectory = new File(args[0]);
		Preconditions.checkArgument(topDirectory.isDirectory(), "Not a directory " + topDirectory);

		String coloringDirectory = SAFE_URL_PATHS.getString(args[1]).replace("\\", "/");

		String dataId = args[2];

		File txtFile = SAFE_URL_PATHS.get(topDirectory.getPath(), args.length > 3 ? args[3] : "coloringlist.txt").toFile();
		Preconditions.checkArgument(txtFile.isFile(), "Not a file " + txtFile);

		String defaultColoringMetadataFileName = BasicColoringDataManager.getMetadataFileName(Serializers.of().getVersion());
		File metadataFile = SAFE_URL_PATHS.get(topDirectory.getPath(), args.length > 4 ? args[4] : defaultColoringMetadataFileName).toFile();

		this.topDirectory = topDirectory;
		this.coloringDirectory = coloringDirectory;
		this.coloringDataManager = BasicColoringDataManager.of(dataId);
		this.txtFile = txtFile;
		this.metadataFile = metadataFile;
	}

	public void run() throws IOException
	{
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(txtFile)))
		{
			int numberColoringFiles = 0;
			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				File colorFile = SAFE_URL_PATHS.get(topDirectory.getAbsolutePath(), line).toFile();

				if (colorFile.isFile())
				{
					try
					{
						DataFileInfo fileInfo = DataFileReader.of().readFileInfo(colorFile);
						System.out.println(fileInfo);
						System.out.flush();
						extractColorings(fileInfo);
						++numberColoringFiles;
					}
					catch (Exception e)
					{
						reportThrowable(e);
						System.err.println("Skipping file " + colorFile);
					}
				}
			}

			Serializers.serialize("Coloring Metadata", coloringDataManager.getMetadataManager(), metadataFile);
			System.out.println("Wrote metadata for " + numberColoringFiles + " file(s) to the coloring metadata file " + metadataFile);
		}
	}

	public ColoringDataManager getColoringDataManager()
	{
		return coloringDataManager.copy();
	}

	protected void extractColorings(DataFileInfo fileInfo)
	{
		String mapName = null;
		for (DataObjectInfo objectInfo : fileInfo.getDataObjectInfo())
		{
			if (mapName == null)
			{
				mapName = getMapName(objectInfo);
			}
			if (objectInfo instanceof TableInfo)
			{
				TableInfo tableInfo = (TableInfo) objectInfo;
				int numberColumns = tableInfo.getNumberColumns();
				File file = fileInfo.getFile();

				// This has got to go, obviously.
				if (numberColumns == 1)
				{
					// One scalar coloring, presumably from text file.
					addScalarColoring(mapName, file, tableInfo, 0);
				}
				else if (numberColumns == 3)
				{
					// One vector coloring, presumably from text file.
					throw new UnsupportedOperationException("Code up this case");
				}
				else if (numberColumns == 6)
				{
					// While this could be a text file with vector + vector error, the more likely scenario is
					// a FITS file with scalar coloring in column 4, sigma in column 5. Assume that to be true.
					addScalarColoring(mapName, file, tableInfo, 4);
					addScalarColoring(mapName, file, tableInfo, 5);
				}
				else if (numberColumns == 10)
				{
					// Assume FITS file with vector coloring in 4, 6, 8, vector sigma in 5, 7, 9.
					addVectorColoring(mapName, file, tableInfo, 4, 6, 8);
					addVectorColoring(mapName, file, tableInfo, 5, 7, 9);
				}
				else
				{
					throw new UnsupportedOperationException("Don't know how to handle a table with " + numberColumns + " columns in data object " + objectInfo.getTitle() + " in file " + fileInfo.getFile());
				}
			}
		}
	}

	protected String getMapName(DataObjectInfo objectInfo)
	{
        for (InfoRow info : objectInfo.getDescription().get())
		{
			List<String> stringList = info.get();
			if (MAP_NAME.equalsIgnoreCase(stringList.get(0)))
			{
				return stringList.get(1);
			}
		}
		return null;
	}

	protected void addScalarColoring(String mapName, File file, TableInfo tableInfo, int columnNumber)
	{
		String name = null;
		try
		{
			name = getColoringName(mapName, file, tableInfo, columnNumber);
			if (name == null)
			{
				throw new IllegalArgumentException("Cannot deduce scalar coloring name for column " + tableInfo.getColumnInfo(columnNumber));
			}

			String units = getUnits(name, tableInfo, columnNumber);

			file = new File(file.getAbsolutePath().replace(topDirectory.getAbsolutePath(), coloringDirectory));
			coloringDataManager.add(ColoringData.of(name, file.toString(), ImmutableList.of(name), units, tableInfo.getNumberRows(), false));
		}
		catch (Exception e)
		{
			reportThrowable(e);
			System.err.println("Skipping scalar coloring " + name);
		}
	}

	protected void addVectorColoring(String mapName, File file, TableInfo tableInfo, int xColumn, int yColumn, int zColumn)
	{
		String name = null;
		try
		{
			name = getColoringName(mapName, file, tableInfo, xColumn);
			if (name == null)
			{
				throw new IllegalArgumentException("Cannot deduce vector coloring name for table " + tableInfo);
			}

			String units = getUnits(name, tableInfo, xColumn);
			if (units == null ? getUnits(name, tableInfo, yColumn) != null : !units.equalsIgnoreCase(getUnits(name, tableInfo, yColumn)))
			{
				throw new IllegalArgumentException("Units of vector coloring must be the same for all elements in table " + tableInfo);
			}
			if (units == null ? getUnits(name, tableInfo, zColumn) != null : !units.equalsIgnoreCase(getUnits(name, tableInfo, zColumn)))
			{
				throw new IllegalArgumentException("Units of vector coloring must be the same for all elements in table " + tableInfo);
			}

			if (units == null)
			{
				units = "";
			}
			file = new File(file.getAbsolutePath().replace(topDirectory.getAbsolutePath(), coloringDirectory));
			coloringDataManager.add(ColoringData.of(name, file.toString(), ImmutableList.of(name + " X", name + " Y", name + " Z"), units, tableInfo.getNumberRows(), false));
		}
		catch (Exception e)
		{
			reportThrowable(e);
			System.err.println("Skipping vector coloring " + name);
		}
	}

	private String getColoringName(String mapName, File file, TableInfo tableInfo, int columnNumber)
	{
		ColumnInfo info = tableInfo.getColumnInfo(columnNumber);
		final String columnName = info.getName();

		String name = mapName != null ? mapName : guessColoringName(file.getName());
		if (name != null)
		{
			String lowerCaseColumnName = columnName.toLowerCase();
			if (lowerCaseColumnName.contains("err") || lowerCaseColumnName.contains("sig"))
			{
				name = name + " Error";
			}
		}
		else
		{
			name = guessColoringName(columnName);
			if (name == null)
			{
				name = validateString(columnName, info).replaceAll("[\\s_]*[XxYyZz]", "");
			}
		}
		return fixCase(name);
	}

	private String getUnits(String name, TableInfo tableInfo, int columnNumber)
	{
		String units = null;
		try
		{
			ColumnInfo columnInfo = tableInfo.getColumnInfo(columnNumber);
			units = validateString(columnInfo.getUnits(), columnInfo);
		}
		catch (@SuppressWarnings("unused") IllegalArgumentException e)
		{
			units = getUnits(name);
		}
		// Except for Joules, all units are lower case.
		return units != null ? units.toLowerCase().replace('j', 'J') : null;
	}

	private String guessColoringName(String string)
	{
		string = string.toLowerCase();
		if (string.matches(".*slo?p.*"))
		{
			return GenericPolyhedralModel.SlopeStr;
		}
		else if (string.matches(".*ele?v.*"))
		{
			return GenericPolyhedralModel.ElevStr;
		}
		else if (string.matches(".*acc.*"))
		{
			return GenericPolyhedralModel.GravAccStr;
		}
		else if (string.matches(".*pot.*"))
		{
			return GenericPolyhedralModel.GravPotStr;
		}
		return null;
	}

	private String validateString(String string, ColumnInfo info)
	{
		try
		{
			if (string != null && string.matches(".*\\S.*"))
			{
				Double.parseDouble(string);
			}
			// Success is bad in this case. It means the name looks like a number.
			throw new IllegalArgumentException("String " + string + " looks like a number in column " + info);
		}
		catch (@SuppressWarnings("unused") NumberFormatException e)
		{
			// That's good actually!
			return string;
		}

	}

	private String getUnits(String name)
	{
		if (GenericPolyhedralModel.SlopeStr.equals(name))
		{
			return GenericPolyhedralModel.SlopeUnitsStr;
		}
		else if (GenericPolyhedralModel.ElevStr.equals(name))
		{
			return GenericPolyhedralModel.ElevUnitsStr;
		}
		else if (GenericPolyhedralModel.GravAccStr.equals(name))
		{
			return GenericPolyhedralModel.GravAccUnitsStr;
		}
		else if (GenericPolyhedralModel.GravPotStr.equals(name))
		{
			return GenericPolyhedralModel.GravPotUnitsStr;
		}
		return null;
	}

	private static void reportThrowable(Throwable t)
	{
		String message = t.getLocalizedMessage();
		if (message != null)
		{
			System.err.println(message);
		}
		else
		{
			t.printStackTrace();
		}
	}

	private static String fixCase(String string)
	{
		if (string != null)
		{
			List<String> words = new ArrayList<>();
			for (String word : string.split("\\s+"))
			{
				if (word.length() > 1)
				{
					word = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
				}
				else if (word.length() == 1)
				{
					word = word.toUpperCase();
				}
				words.add(word);
			}
			string = String.join(" ", words);
		}
		return string;
	}

	private static void usage()
	{
		System.err.println("Usage:\tdiscovery full-path-to-coloring-directory hierarchical-coloring-path unique-model-id [ coloring-list-file-name ] [ coloring-metadata-file-name ]");
	}

	public static void main(String[] args)
	{
		try
		{
			DiscoverPlateColorings discovery = new DiscoverPlateColorings(args);
			discovery.run();
			System.out.println("Done");
		}
		catch (IllegalArgumentException e)
		{
			reportThrowable(e);
			usage();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

}
