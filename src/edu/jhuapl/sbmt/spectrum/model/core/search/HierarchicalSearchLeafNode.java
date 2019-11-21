package edu.jhuapl.sbmt.spectrum.model.core.search;

/**
 * Helper class for storing data at TreeModel leaf nodes
 */
public class HierarchicalSearchLeafNode<S>
{
    public String name;
    public int cameraCheckbox;

    public HierarchicalSearchLeafNode(String name, int cameraCheckbox, int filterCheckbox)
    {
        this.name = name;
        this.cameraCheckbox = cameraCheckbox;
    }

    // This method must return what we want to be displayed for the leaf node in the GUI
    @Override
    public String toString()
    {
        return name;
    }
}