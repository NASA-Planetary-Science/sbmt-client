package edu.jhuapl.near.lidar.hyperoctree.app;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class FileListPane extends JScrollPane
{

    JList<String> list=new JList<String>();
    DefaultListModel<String> listModel=new DefaultListModel<>();

    public FileListPane()
    {
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setModel(listModel);
        this.add(list);
    }

    public void addFile(String path)
    {
        if (!listModel.contains(path))
            listModel.addElement(path);
    }

    public void removeFile(String path)
    {
        listModel.removeElement(path);
    }



}
