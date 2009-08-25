package edu.jhuapl.near;

import java.util.*;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class ImageBrowser extends QWidget
{
	public ImageBrowser()
	{
		QGridLayout gridLayout = new QGridLayout(this);
        //gridLayout.setSpacing(6);
        //gridLayout.setMargin(9);
        gridLayout.setObjectName("gridLayout");
        QTreeView dirView = new QTreeView(this);
        dirView.setObjectName("dirView");

        gridLayout.addWidget(dirView, 0, 0, 1, 1);

        QDirModel dirModel = new QDirModel(this);
        dirModel.setLazyChildCount(true);
        List<String> filterList = new ArrayList<String>();
        filterList.add("*.FIT");
        filterList.add("*.fit");
        dirModel.setNameFilters(filterList);
        dirModel.setFilter(new QDir.Filters(QDir.Filter.AllDirs, QDir.Filter.Files, QDir.Filter.Drives, QDir.Filter.NoDotAndDotDot));

        dirView.setModel(dirModel);

        for (int i=1; i<dirView.header().count(); ++i)
            dirView.hideColumn(i);
        dirView.header().hide();

        dirView.header().setStretchLastSection(false);
        dirView.header().setResizeMode(QHeaderView.ResizeMode.ResizeToContents);
	}
}
