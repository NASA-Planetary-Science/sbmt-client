package edu.jhuapl.sbmt.image2.modules.search;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

public class LoadImageListOperator extends BasePipelineOperator<Pair<String, IImagingInstrument>, Pair<List<List<String>>, IImagingInstrument>>
{


	public LoadImageListOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		String filename = inputs.get(0).getLeft();
		ImagingInstrument instrument = (ImagingInstrument)inputs.get(0).getRight();

		List<List<String>> fixedList = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<String> namesOnly = new ArrayList<String>();
        List<List<String>> results = new ArrayList<List<String>>();
        List<String> lines = FileUtil.getFileLinesAsStringList(filename);
        System.out.println("ImageResultsTableController: loadImageListButtonActionPerformed: lines size " + lines.size());

        for (int i = 0; i < lines.size(); ++i)
        {
            if (lines.get(i).startsWith("#"))
                continue;
            String[] words = lines.get(i).trim().split("\\s+");
//            ImageSource imageSource = ImageSource.valueFor(words[2].replace("_", " "));
//            if (fixedList == null) { fixedList = imageSearchModel.getFixedList(imageSource); System.out.println("ImageResultsTableController: loadImageListButtonActionPerformed: first entry " + fixedList.get(fixedList.size()-1).get(0));}
//            System.out.println("ImageResultsTableController: loadImageListButtonActionPerformed: words 0 " + words[0]);
            List<String> result = new ArrayList<String>();
            String name = instrument.searchQuery.getDataPath() + "/" + words[0];
            result.add(name);
            Date dt = sdf.parse(words[1]);
            result.add(String.valueOf(dt.getTime()));
            result.add(words[2]);
            results.add(result);
        }


		outputs = List.of(Pair.of(results, instrument));





	}
}
