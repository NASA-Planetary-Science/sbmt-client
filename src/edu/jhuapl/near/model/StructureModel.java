package edu.jhuapl.near.model;

import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Model of structures drawn on Eros such as lineaments and circles.
 * 
 * @author 
 *
 */
public abstract class StructureModel extends Model
{
	public static abstract class Structure
	{
		public abstract Element toXmlDomElement(Document dom);
	    public abstract void fromXmlDomElement(Element element, ErosModel erosModel);
	    public abstract String getClickStatusBarText();
	    public abstract int getId();
	    public abstract String getName();
	    public abstract void setName(String name);
	    public abstract String getType();
	    public abstract String getInfo();
	}
	
	public abstract void addNewStructure();

	public abstract boolean supportsSelection();
	
	public abstract void selectStructure(int idx);
	
	public abstract int getSelectedStructureIndex();
	
	public abstract int getNumberOfStructures();
	
	public abstract void removeStructure(int idx);
	
	public abstract Structure getStructure(int idx);
	
	public abstract void loadModel(File file) throws Exception;

	public abstract void saveModel(File file) throws Exception;
}
/*
public class StructureModel extends Model implements PropertyChangeListener
{
	private LineModel lineModel;
	//private CircleModel circleModel;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
	static public String STRUCTURES = "structures";

	public static abstract class Structure
	{
		public abstract Element toXmlDomElement(Document dom);
	    public abstract void fromXmlDomElement(Element element, ErosModel erosModel);
	    public abstract String getClickStatusBarText();
	}
	
	public StructureModel(ErosModel erosModel)
	{
		lineModel = new LineModel(erosModel);
		//circleModel = new CircleModel();
		lineModel.addPropertyChangeListener(this);
		//circleModel.addPropertyChangeListener(this);
	}

	public void loadModel(File file) throws NumberFormatException, IOException
	{
		try 
		{
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(file);

			//get the root element
			Element docEle = dom.getDocumentElement();

			//get a nodelist of  elements
			NodeList nl = docEle.getElementsByTagName(LineModel.LINES);
			System.out.println(nl.getLength());
			if(nl != null && nl.getLength() <= 2)
			{
				Element el = (Element)nl.item(0);
				System.out.println(el);
				if (LineModel.LINES.equals(el.getTagName()))
					lineModel.fromXmlDomElement(el);
				
				//el = (Element)nl.item(1);
				//if (CircleModel.CIRCLES.equals(el.getTagName()))
				//	circleModel.fromXmlDomElement(el);
			}

		}
		catch(Exception e) 
		{
			JOptionPane.showMessageDialog(null,
					"There was an error reading the file.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}
	}

	public void saveModel(File file) throws NumberFormatException, IOException
	{
		try 
		{
			//get an instance of factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//create an instance of DOM
			Document dom = db.newDocument();

	    	Element rootEle = dom.createElement(STRUCTURES);

	    	dom.appendChild(rootEle);

	    	rootEle.appendChild(lineModel.toXmlDomElement(dom));
	    	//rootEle.appendChild(circleModel.toXmlDomElement(dom));

			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(
					new FileOutputStream(file), format);

			serializer.serialize(dom);

		} 
		catch(Exception e) 
		{
			JOptionPane.showMessageDialog(null,
					"There was an error saving the file.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}
	}
	

	public LineModel getLineModel()
	{
		return lineModel;
	}
	
	//public CircleModel getCircleModel()
	//{
	//	return circleModel;
	//}
	
	public ArrayList<vtkProp> getProps() 
	{
		actors.clear();
		actors.addAll(lineModel.getProps());
		//actors.addAll(circleModel.getProps());
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (lineModel.getProps().contains(prop))
    		return lineModel.getClickStatusBarText(prop, cellId);
    	//else if (circleModel.getProps().contains(prop))
    	//	return circleModel.getClickStatusBarText(prop, cellId);
    	else
    		return "";
    }

	public void propertyChange(PropertyChangeEvent evt) 
	{
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
}
*/