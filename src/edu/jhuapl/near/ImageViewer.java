package edu.jhuapl.near;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class ImageViewer extends QWidget
{
	private QTabWidget tabWidget;
	
	public ImageViewer(QWidget parent)
	{
		super(parent);
		QGridLayout gridLayout = new QGridLayout(this);
        //gridLayout.setSpacing(4);
        //gridLayout.setMargin(4);
        gridLayout.setObjectName("gridLayout");
        tabWidget = new QTabWidget(this);
        tabWidget.setTabsClosable(true);
        tabWidget.tabCloseRequested.connect(this, "removeTab(int)");
        
        gridLayout.addWidget(tabWidget, 0, 0, 1, 1);
	}
	
	public void addNewTab(String filename)
	{
		QFileInfo fi = new QFileInfo(filename);
		
		try
		{
			tabWidget.addTab(new ImageGLWidget(this, filename), fi.baseName());
			int index = tabWidget.count()-1;
			tabWidget.setTabToolTip(index, filename);
			tabWidget.setCurrentIndex(index);
		}
		catch (Exception e)
		{
            QMessageBox.critical(null, "Problem reading file", "The file could not be loaded. Sorry.");
            e.printStackTrace();
            return;
		}
	}
	
	public void removeTab(int index)
	{
		tabWidget.removeTab(index);
	}
}
