package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import edu.jhuapl.sbmt.core.InstrumentMetadataIO;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

public abstract class SpectraHierarchicalSearchSpecification<S extends SearchSpec> implements InstrumentMetadataIO<S>
{
    private TreeModel treeModel;
    protected List<Integer> selectedDatasets;
    protected String rootName;

    public SpectraHierarchicalSearchSpecification(String rootName)
    {
        // Create a tree model with just the root
    	this.rootName = rootName;
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(rootName));
        // Initialize container objects
        selectedDatasets = new LinkedList<Integer>();
    }

    public abstract SpectraHierarchicalSearchSpecification<S> clone();

    // Method used to get the tree model
    public TreeModel getTreeModel()
    {
        return treeModel;
    }

    public void setRootName(String name)
    {
        DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)treeModel.getRoot();
        currNode.setUserObject(name);
    }

    public void clearTreeLeaves()
    {
        DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)treeModel.getRoot();
        currNode.removeAllChildren();
    }

    // Adds nodes to tree as necessary to create the path
    protected void addHierarchicalSearchPath(String[] path, int cameraCheckbox, int filterCheckbox)
    {
        // Get the root node
        DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)treeModel.getRoot();
        // Go through each level of path before child and make sure that it exists
        for(int i=0; i<path.length-1; i++)
        {
            // See if node has a child called path[i]
            Enumeration e = currNode.children();
            boolean childFound = false;
            while(e.hasMoreElements())
            {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();
                if(childNode.toString().equals(path[i]))
                {
                    childFound = true;
                    currNode = childNode;
                    break;
                }
            }

            // If child was not found then create one and insert
            if(!childFound)
            {
                // Add the new node
                DefaultMutableTreeNode newChildNode = new DefaultMutableTreeNode(path[i]);
                currNode.add(newChildNode);

                // Set current node to that child
                currNode = newChildNode;
            }
        }

        // Always insert the child node
        DefaultMutableTreeNode newLeafNode = new DefaultMutableTreeNode(
                new HierarchicalSearchLeafNode<S>(path[path.length-1],cameraCheckbox,filterCheckbox));
        currNode.add(newLeafNode);
    }

    // Method for processing tree selections
    public void processTreeSelections(TreePath[] selectedPaths)
    {
        // Clear storage for selected (camera,filter) pairs
        selectedDatasets.clear();

        // Iterate through the selected paths
        for(TreePath tp : selectedPaths)
        {
            // Note: This is a common source of confusion for a lot of users of CheckboxTree, each selected path
            //       last component is actually the deepest selected node for which all its children are selected
            //       as opposed to one path for each selected leaf
            DefaultMutableTreeNode selectedParentNode = (DefaultMutableTreeNode)tp.getLastPathComponent();
            // Get all leaves from the selected parent node
            Enumeration en = selectedParentNode.depthFirstEnumeration();
            while(en.hasMoreElements())
            {
                // Information that we want is located at the leaf nodes
                DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode)en.nextElement();
                if(tempNode.isLeaf())
                {
                    // Extract the saved object at the leaf node containing camera and filter checkbox numbers
                    HierarchicalSearchLeafNode ln = (HierarchicalSearchLeafNode)tempNode.getUserObject();
                    selectedDatasets.add(ln.cameraCheckbox);
                }
            }
        }
    }

    // Get camera portion of selected (camera,filter) pairs
    public List<Integer> getSelectedDatasets()
    {
        return new LinkedList<Integer>(selectedDatasets);
    }
}