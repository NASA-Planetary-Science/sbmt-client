/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NISSearchPanel.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.sbmt.spectrum.ui.oldWay;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Ranges;
import com.jidesoft.swing.CheckBoxTree;

import vtk.vtkFunctionParser;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.query.database.DatabaseQueryBase;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumListIO;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
import edu.jhuapl.sbmt.spectrum.ui.math.SpectrumMathPanel;


public abstract class AbstractSpectrumSearchPanel extends JPanel implements MouseListener, PropertyChangeListener, KeyListener
{
    protected final ModelManager modelManager;
    protected final PickManager pickManager;
    protected java.util.Date startDate = new GregorianCalendar(2000, 0, 11, 0, 0, 0).getTime();
    protected java.util.Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();

    protected SpectrumPopupMenu spectrumPopupMenu;
    protected List<BasicSpectrum> spectrumRawResults = new ArrayList<BasicSpectrum>();
    protected String spectrumResultsLabelText = " ";
    protected IdPair resultIntervalCurrentlyShown = null;
    protected boolean currentlyEditingUserDefinedFunction = false;
    Renderer renderer;
    protected CheckBoxTree checkBoxTree;

    JButton saveImageListButton = new JButton("Save Spectrum List");
    JButton saveSelectedImageListButton = new JButton();
    JButton loadImageListButton = new JButton("Load Spectrum List");

    protected final BasicSpectrumInstrument instrument;
    protected JScrollPane hierarchicalSearchScrollPane = new JScrollPane();
    private boolean hasHierarchicalSpectraSearch;
    private SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification;


    public AbstractSpectrumSearchPanel(boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification,
    		final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            final PickManager pickManager, final Renderer renderer, BasicSpectrumInstrument instrument)
    {
        this.modelManager = modelManager;
        this.pickManager = pickManager;
        this.instrument=instrument;
        this.hierarchicalSpectraSearchSpecification = hierarchicalSpectraSearchSpecification;
        this.hasHierarchicalSpectraSearch = hasHierarchicalSpectraSearch;
		SpectrumBoundaryCollection spectrumBoundaryCollection = (SpectrumBoundaryCollection)modelManager.getModel(ModelNames.SPECTRA_BOUNDARIES);

        spectrumPopupMenu = new SpectrumPopupMenu((SpectraCollection)modelManager.getModel(ModelNames.SPECTRA), spectrumBoundaryCollection, this.modelManager, infoPanelManager, renderer);
        spectrumPopupMenu.addPropertyChangeListener(this);

        // Setup hierarchical image search
        initHierarchicalImageSearch();

        initComponents();

        postInitComponents();
        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        renderer.addKeyListener(this);
        this.renderer=renderer;

        resultList.setCellRenderer(new MyListCellRenderer());

        resultList.addListSelectionListener(new ListSelectionListener()
        {

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                    return;
                //TODO update when converting this to the new panel UI
//                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//                for (int i=0; i<resultList.getModel().getSize(); i++)
//                {
//                	r
//                    IBasicSpectrumRenderer spectrum = model.getSpectrum(createSpectrumName((String)resultList.getModel().getElementAt(i)));
//                    if (spectrum == null)
//                        continue;
//                    if (resultList.isSelectedIndex(i))
//                        model.select(spectrum);
//                    else
//                        model.deselect(spectrum);
//                }
            }
        });
    }

 // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(hasHierarchicalSpectraSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
            // Create the tree
            checkBoxTree = new CheckBoxTree(hierarchicalSpectraSearchSpecification.getTreeModel());

            // Place the tree in the panel
            this.hierarchicalSearchScrollPane.setViewportView(checkBoxTree);
        }
        else
        {
            // No hierarchical search capabilities, hide the scroll pane
            hierarchicalSearchScrollPane.setVisible(false);
        }
    }


// TODO make this class abstract with these abstract functions. Subclasses will
// need to redefine them
//    abstract protected java.util.Date getDefaultStartDate();
//    abstract protected java.util.Date getDefaultEndDate();
//    abstract protected QueryBase getQuery();
//    abstract protected double getDefaultMaxSpacecraftDistance();
//    abstract protected double[] getBandCenters();
//    private String getSpectraCollectionModelName()
//    {
//        return ModelNames.NIS_SPECTRA;
//    }

    private void postInitComponents()
    {
        ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
        ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);
        polygonType3CheckBox.setVisible(false);
    }

    protected void setupComboBoxes()
    {
        for (int i=1; i<=instrument.getBandCenters().length; ++i)
        {
            String channel = new String("(" + i + ") " + instrument.getBandCenters()[i-1] + " nm");
            redComboBox.addItem(channel);
            greenComboBox.addItem(channel);
            blueComboBox.addItem(channel);
        }

        String[] derivedParameters = instrument.getSpectrumMath().getDerivedParameters();
        for (int i=0; i<derivedParameters.length; ++i)
        {
            redComboBox.addItem(derivedParameters[i]);
            greenComboBox.addItem(derivedParameters[i]);
            blueComboBox.addItem(derivedParameters[i]);
        }

        for (vtkFunctionParser fp: instrument.getSpectrumMath().getAllUserDefinedDerivedParameters())
        {
            redComboBox.addItem(fp.GetFunction());
            greenComboBox.addItem(fp.GetFunction());
            blueComboBox.addItem(fp.GetFunction());
        }
    }

    private void saveSpectrumListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(this, "Select File", "spectrumlist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Spectrum_Name Image_Time_UTC"  + nl);
                int size = resultList.getModel().getSize();
                for (int i=0; i<size; ++i)
                {
                    String result = spectrumRawResults.get(i).getFullPath();
                    String spectrum  = result;
                    out.write(spectrum + ".spect " + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    private void loadSpectrumListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showOpenDialog(this, "Select File");

        try
		{
        	List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
			SpectrumListIO.loadSpectrumListButtonActionPerformed(file, results, instrument, new Runnable()
			{

				@Override
				public void run()
				{
					setSpectrumSearchResults(results);

				}
			});
		}

        catch (Exception e)
		{
        	 JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                     "There was an error reading the file.",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

//        if (file != null)
//        {
//            try
//            {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//                List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
//                List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
//                for (int i=0; i<lines.size(); ++i)
//                {
//                    if (lines.get(i).startsWith("#")) continue;
//                    String[] words = lines.get(i).trim().split("\\s+");
//                    List<String> result = new ArrayList<String>();
//                    result.add(words[0]);
//                    results.add(result);
//                }
//                setSpectrumSearchResults(results);
//            }
//            catch (Exception e)
//            {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
//                        "There was an error reading the file.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//
//                e.printStackTrace();
//            }
//        }
    }

    protected abstract void setSpectrumSearchResults(List<BasicSpectrum> results);

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.locationToIndex(e.getPoint());

            if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
            {
                resultList.setSelectedIndex(index);
                spectrumPopupMenu.setCurrentSpectrum(createSpectrumName(spectrumRawResults.get(index).getFullPath()));
                spectrumPopupMenu.setInstrument(instrument);
                spectrumPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // 2018-02-08 JP. Turn this method into a no-op for now. The reason is that
        // currently all listeners respond to all key strokes, and VTK keyboard events
        // do not have a means to determine their source, so there is no way for listeners
        // to be more selective. The result is, e.g., if one types "s", statistics windows show
        // up even if we're not looking at a spectrum tab.
        //
        // Leave it in the code (don't comment it out) so Eclipse can find references to this,
        // and so that we don't unknowingly break this code.
        boolean disableKeyResponses = true;
        if (disableKeyResponses) return;

        if (e.getKeyChar()=='a')
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            renderer.removeKeyListener(this);
            model.toggleSelectAll();
            renderer.addKeyListener(this);
        }
        else if (e.getKeyChar()=='s')
        {
            spectrumPopupMenu.showStatisticsWindow();
        }
        else if (e.getKeyChar()=='i' || e.getKeyChar()=='v')    // 'i' sets the lighting direction based on time of a single NIS spectrum, and 'v' looks from just above the footprint toward the sun
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            List<IBasicSpectrumRenderer> selection=model.getSelectedSpectra();
            if (selection.size()!=1)
            {
                JOptionPane.showMessageDialog(this, "Please select only one spectrum to specify lighting or viewpoint");
                return;
            }
            IBasicSpectrumRenderer spectrum=selection.get(0);
            renderer.setLighting(LightingType.FIXEDLIGHT);
            Path fullPath=Paths.get(spectrum.getSpectrum().getFullPath());
            Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
            //Vector3D toSunVector=getToSunUnitVector(relativePath.toString());
            renderer.setFixedLightDirection(spectrum.getSpectrum().getToSunUnitVector()); // the fixed light direction points to the light
            if (e.getKeyChar()=='v')
            {
                Vector3D footprintCenter=new Vector3D(spectrum.getShiftedFootprint().GetCenter());
                SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
                //
                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
                normalsFilter.SetInputData(spectrum.getUnshiftedFootprint());
                normalsFilter.SetComputeCellNormals(0);
                normalsFilter.SetComputePointNormals(1);
                normalsFilter.SplittingOff();
                normalsFilter.Update();
                Vector3D upVector=new Vector3D(PolyDataUtil.computePolyDataNormal(normalsFilter.GetOutput())).normalize();  // TODO: fix this for degenerate cases, i.e. normal parallel to to-sun direction
                double viewHeight=0.01; // km
                Vector3D cameraPosition=footprintCenter.add(upVector.scalarMultiply(viewHeight));
                double lookLength=footprintCenter.subtract(cameraPosition).getNorm();
                Vector3D focalPoint=cameraPosition.add((new Vector3D(spectrum.getSpectrum().getToSunUnitVector())).scalarMultiply(lookLength));
                //
                renderer.setCameraOrientation(cameraPosition.toArray(), focalPoint.toArray(), renderer.getRenderWindowPanel().getActiveCamera().GetViewUp(), renderer.getCameraViewAngle());
            }
        }
        else if (e.getKeyChar()=='h')
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            model.decreaseFootprintSeparation(0.001);
        }
        else if (e.getKeyChar()=='H')
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            model.increaseFootprintSeparation(0.001);
        }
        else if (e.getKeyChar()=='+')
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            model.setOffset(model.getOffset()+body.getBoundingBoxDiagonalLength()/50);
        }
        else if (e.getKeyChar()=='-')
        {
            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            model.setOffset(model.getOffset()-body.getBoundingBoxDiagonalLength()/50);
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // TODO Auto-generated method stub

    }

    protected void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//        model.removeAllSpectra();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= spectrumRawResults.size())
                break;

//            try
//            {
//                String currentSpectrum = spectrumRawResults.get(i).getFullPath();
//                model.addSpectrum(createSpectrumName(currentSpectrum), instrument, false);
            	model.addSpectrum(spectrumRawResults.get(i), false);
//            }
//            catch (IOException e1) {
//                e1.printStackTrace();
//            }
        }
        updateColoring();
    }


    public abstract String createSpectrumName(String currentSpectrumRaw);
//    {
//        return currentSpectrumRaw.substring(0,currentSpectrumRaw.length()-4) + ".NIS";
//    }


    private void checkValidMinMax(int channel, boolean minimunStateChange)
    {
        JSpinner minSpinner = null;
        JSpinner maxSpinner = null;

        if (channel == 0)
        {
            minSpinner = redMinSpinner;
            maxSpinner = redMaxSpinner;
        }
        else if (channel == 1)
        {
            minSpinner = greenMinSpinner;
            maxSpinner = greenMaxSpinner;
        }
        else if (channel == 2)
        {
            minSpinner = blueMinSpinner;
            maxSpinner = blueMaxSpinner;
        }

        Double minVal = (Double)minSpinner.getValue();
        Double maxVal = (Double)maxSpinner.getValue();
        if (minVal > maxVal)
        {
            if (minimunStateChange)
                minSpinner.setValue(maxSpinner.getValue());
            else
                maxSpinner.setValue(minSpinner.getValue());
        }
    }

    protected void updateColoring()
    {
        // If we are currently editing user defined functions
        // (i.e. the dialog is open), do not update the coloring
        // since we may be in an inconsistent state.
        if (currentlyEditingUserDefinedFunction)
            return;


        Double redMinVal = (Double)redMinSpinner.getValue();
        Double redMaxVal = (Double)redMaxSpinner.getValue();

        Double greenMinVal = (Double)greenMinSpinner.getValue();
        Double greenMaxVal = (Double)greenMaxSpinner.getValue();

        Double blueMinVal = (Double)blueMinSpinner.getValue();
        Double blueMaxVal = (Double)blueMaxSpinner.getValue();

        SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//        if (grayscaleCheckBox.isSelected())
//        {
//            model.setChannelColoring(
//                    new int[]{redComboBox.getSelectedIndex(), redComboBox.getSelectedIndex(), redComboBox.getSelectedIndex()},
//                    new double[]{redMinVal, redMinVal, redMinVal},
//                    new double[]{redMaxVal, redMaxVal, redMaxVal},
//                    instrument);
//        }
//        else
//        {
//            model.setChannelColoring(
//                    new int[]{redComboBox.getSelectedIndex(), greenComboBox.getSelectedIndex(), blueComboBox.getSelectedIndex()},
//                    new double[]{redMinVal, greenMinVal, blueMinVal},
//                    new double[]{redMaxVal, greenMaxVal, blueMaxVal},
//                    instrument);
//        }



    }
    PickEvent lastPickEvent=null;

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();


            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof SpectraCollection)
            {
            	//TODO fix this when going to the new way of doing the UI
//                SpectraCollection coll=(SpectraCollection)model;
//                String name = coll.getSpectrumName((vtkActor)e.getPickedProp());
//                IBasicSpectrumRenderer spectrum=coll.getSpectrum(name);
//                if (spectrum==null)
//                    return;
//
//                resultList.getSelectionModel().clearSelection();
//                for (int i=0; i<resultList.getModel().getSize(); i++)
//                {
//                    if (FilenameUtils.getBaseName(name).equals(resultList.getModel().getElementAt(i)))
//                    {
//                        resultList.getSelectionModel().setSelectionInterval(i, i);
//                        resultList.ensureIndexIsVisible(i);
//                        coll.select(coll.getSpectrum(name));//.setShowOutline(true);
//                    }
//                }
//
//                for (int i=0; i<resultList.getModel().getSize(); i++)
//                {
//                    if (!resultList.getSelectionModel().isSelectedIndex(i))
//                    {
//                        IBasicSpectrumRenderer spectrum_=coll.getSpectrum(createSpectrumName((String)resultList.getModel().getElementAt(i)));
//                        if (spectrum_ != null)
//                            coll.deselect(spectrum_);
//                    }
//                }
//                resultList.repaint();
            }
       }
        else
        {
            resultList.repaint();
        }

    }


    class MyListCellRenderer extends DefaultListCellRenderer  {

        public MyListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            //setText(value.toString());
            JLabel label = (JLabel) super.getListCellRendererComponent(paramlist, value, index, isSelected, cellHasFocus);
            label.setOpaque(true /* was isSelected */); // Highlight only when selected
            if(isSelected) { // I faked a match for the second index, put you matching condition here.
                label.setBackground(Color.YELLOW);
                label.setEnabled(false);
            }
            else
            {
            	//TODO fix when updating to the new UI way
//	            String spectrumFile=createSpectrumName(value.toString());
//	            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//	            IBasicSpectrumRenderer spectrum=model.getSpectrum(spectrumFile);
//	            setBackground(Color.LIGHT_GRAY);
//	            if (spectrum==null)
//	                setForeground(Color.black);
//	            else
//	            {
//	                double[] color=spectrum.getChannelColor();
//	                for (int i=0; i<3; i++)
//	                {
//	                    if (color[i]>1)
//	                        color[i]=1;
//	                    if (color[i]<0)
//	                        color[i]=0;
//	                }
//	                setForeground(new Color((float)color[0],(float)color[1],(float)color[2]));
//
//	//                setBackground(paramlist.getBackground());
//	            }
            }

            return label;
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        startDateLabel = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endDateLabel = new javax.swing.JLabel();
        endSpinner = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        fromDistanceLabel = new javax.swing.JLabel();
        fromDistanceTextField = new javax.swing.JFormattedTextField();
        toDistanceLabel = new javax.swing.JLabel();
        toDistanceTextField = new javax.swing.JFormattedTextField();
        endDistanceLabel = new javax.swing.JLabel();
        fromIncidenceLabel = new javax.swing.JLabel();
        fromIncidenceTextField = new javax.swing.JFormattedTextField();
        toIncidenceLabel = new javax.swing.JLabel();
        toIncidenceTextField = new javax.swing.JFormattedTextField();
        endIncidenceLabel = new javax.swing.JLabel();
        fromEmissionLabel = new javax.swing.JLabel();
        fromEmissionTextField = new javax.swing.JFormattedTextField();
        toEmissionLabel = new javax.swing.JLabel();
        toEmissionTextField = new javax.swing.JFormattedTextField();
        endEmissionLabel = new javax.swing.JLabel();
        fromPhaseLabel = new javax.swing.JLabel();
        fromPhaseTextField = new javax.swing.JFormattedTextField();
        toPhaseLabel = new javax.swing.JLabel();
        toPhaseTextField = new javax.swing.JFormattedTextField();
        endPhaseLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        clearRegionButton = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();
        selectRegionButton = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        numberOfFootprintsComboBox = new javax.swing.JComboBox();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        removeAllFootprintsButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        redLabel = new javax.swing.JLabel();
        redComboBox = new javax.swing.JComboBox();
        jPanel13 = new javax.swing.JPanel();
        redMaxSpinner = new javax.swing.JSpinner();
        redMinLabel = new javax.swing.JLabel();
        redMaxLabel = new javax.swing.JLabel();
        redMinSpinner = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        greenLabel = new javax.swing.JLabel();
        greenComboBox = new javax.swing.JComboBox();
        jPanel15 = new javax.swing.JPanel();
        greenMinLabel = new javax.swing.JLabel();
        greenMaxSpinner = new javax.swing.JSpinner();
        greenMaxLabel = new javax.swing.JLabel();
        greenMinSpinner = new javax.swing.JSpinner();
        jPanel10 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        blueComboBox = new javax.swing.JComboBox();
        blueLabel = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        blueMaxLabel = new javax.swing.JLabel();
        blueMaxSpinner = new javax.swing.JSpinner();
        blueMinLabel = new javax.swing.JLabel();
        blueMinSpinner = new javax.swing.JSpinner();
        grayscaleCheckBox = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        polygonType0CheckBox = new javax.swing.JCheckBox();
        polygonType1CheckBox = new javax.swing.JCheckBox();
        polygonType2CheckBox = new javax.swing.JCheckBox();
        polygonType3CheckBox = new javax.swing.JCheckBox();
        customFunctionsButton = new javax.swing.JButton();

        saveImageListButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveSpectrumListButtonActionPerformed(e);
            }
        });

        loadImageListButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadSpectrumListButtonActionPerformed(e);

            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        startDateLabel.setText("Start Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(startDateLabel, gridBagConstraints);

        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setPreferredSize(new java.awt.Dimension(200, 28));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel1.add(startSpinner, gridBagConstraints);

        endDateLabel.setText("End Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(endDateLabel, gridBagConstraints);

        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1132462800000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(200, 28));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel1.add(endSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel8.add(jPanel1, gridBagConstraints);

        if(hasHierarchicalSpectraSearch)
        {
            hierarchicalSearchScrollPane.setPreferredSize(new java.awt.Dimension(300, 200));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
            jPanel8.add(hierarchicalSearchScrollPane, gridBagConstraints);

            hierarchicalSpectraSearchSpecification.processTreeSelections(
                    checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());
        }

        jPanel3.setLayout(new java.awt.GridBagLayout());

        fromDistanceLabel.setText("S/C Distance from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromDistanceLabel, gridBagConstraints);

        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromDistanceTextField, gridBagConstraints);

        toDistanceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toDistanceLabel, gridBagConstraints);

        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("100");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toDistanceTextField, gridBagConstraints);

        endDistanceLabel.setText("km");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endDistanceLabel, gridBagConstraints);

        fromIncidenceLabel.setText("Incidence from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromIncidenceLabel, gridBagConstraints);

        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromIncidenceTextField, gridBagConstraints);

        toIncidenceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toIncidenceLabel, gridBagConstraints);

        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toIncidenceTextField, gridBagConstraints);

        endIncidenceLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endIncidenceLabel, gridBagConstraints);

        fromEmissionLabel.setText("Emission from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromEmissionLabel, gridBagConstraints);

        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromEmissionTextField, gridBagConstraints);

        toEmissionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toEmissionLabel, gridBagConstraints);

        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toEmissionTextField, gridBagConstraints);

        endEmissionLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endEmissionLabel, gridBagConstraints);

        fromPhaseLabel.setText("Phase from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromPhaseLabel, gridBagConstraints);

        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromPhaseTextField, gridBagConstraints);

        toPhaseLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toPhaseLabel, gridBagConstraints);

        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toPhaseTextField, gridBagConstraints);

        endPhaseLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endPhaseLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        jPanel5.add(clearRegionButton, gridBagConstraints);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                submitButtonActionPerformed(evt);
                setCursor(Cursor.getDefaultCursor());
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 40);
        jPanel5.add(submitButton, gridBagConstraints);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel5.add(selectRegionButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel8.add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        resultsLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(resultsLabel, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 200));

        resultList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                resultListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(resultList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel6.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(jPanel6, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Number Footprints:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(jLabel6, gridBagConstraints);

        numberOfFootprintsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        numberOfFootprintsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfFootprintsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(numberOfFootprintsComboBox, gridBagConstraints);

        prevButton.setText("<");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(prevButton, gridBagConstraints);

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(jPanel7, gridBagConstraints);

        removeAllFootprintsButton.setText("Remove All Footprints");
        removeAllFootprintsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllFootprintsButtonActionPerformed(evt);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new java.awt.GridBagLayout());


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(removeAllFootprintsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(saveImageListButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(loadImageListButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        jPanel8.add(buttonPanel, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        jLabel20.setText("Coloring");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel9.add(jLabel20, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel9.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel8.add(jPanel9, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel12.setLayout(new java.awt.GridBagLayout());

        redLabel.setText("Red");
        redLabel.setPreferredSize(new java.awt.Dimension(40, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel12.add(redLabel, gridBagConstraints);

        redComboBox.setPreferredSize(new java.awt.Dimension(150, 22));
        redComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel12.add(redComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jPanel12, gridBagConstraints);

        jPanel13.setLayout(new java.awt.GridBagLayout());

        redMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        redMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        redMaxSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redMaxSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel13.add(redMaxSpinner, gridBagConstraints);

        redMinLabel.setText("Min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel13.add(redMinLabel, gridBagConstraints);

        redMaxLabel.setText("Max");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel13.add(redMaxLabel, gridBagConstraints);

        redMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.01d)));
        redMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        redMinSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redMinSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel13.add(redMinSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel13, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel2, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel14.setLayout(new java.awt.GridBagLayout());

        greenLabel.setText("Green");
        greenLabel.setPreferredSize(new java.awt.Dimension(40, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel14.add(greenLabel, gridBagConstraints);

        greenComboBox.setPreferredSize(new java.awt.Dimension(150, 22));
        greenComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel14.add(greenComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel4.add(jPanel14, gridBagConstraints);

        jPanel15.setLayout(new java.awt.GridBagLayout());

        greenMinLabel.setText("Min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel15.add(greenMinLabel, gridBagConstraints);

        greenMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        greenMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        greenMaxSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenMaxSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel15.add(greenMaxSpinner, gridBagConstraints);

        greenMaxLabel.setText("Max");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel15.add(greenMaxLabel, gridBagConstraints);

        greenMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.01d)));
        greenMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        greenMinSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenMinSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel15.add(greenMinSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jPanel15, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel4, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        jPanel16.setLayout(new java.awt.GridBagLayout());

        blueComboBox.setPreferredSize(new java.awt.Dimension(150, 22));
        blueComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel16.add(blueComboBox, gridBagConstraints);

        blueLabel.setText("Blue");
        blueLabel.setPreferredSize(new java.awt.Dimension(40, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel16.add(blueLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel10.add(jPanel16, gridBagConstraints);

        jPanel17.setLayout(new java.awt.GridBagLayout());

        blueMaxLabel.setText("Max");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel17.add(blueMaxLabel, gridBagConstraints);

        blueMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        blueMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        blueMaxSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueMaxSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel17.add(blueMaxSpinner, gridBagConstraints);

        blueMinLabel.setText("Min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel17.add(blueMinLabel, gridBagConstraints);

        blueMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.01d)));
        blueMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        blueMinSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueMinSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel17.add(blueMinSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(jPanel17, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel10, gridBagConstraints);

        grayscaleCheckBox.setText("Grayscale");
        grayscaleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grayscaleCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel8.add(grayscaleCheckBox, gridBagConstraints);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Field-of-View-Polygon Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel11.add(jLabel1, gridBagConstraints);

        polygonType0CheckBox.setSelected(true);
        polygonType0CheckBox.setText("Full");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel11.add(polygonType0CheckBox, gridBagConstraints);

        polygonType1CheckBox.setText("Partial");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel11.add(polygonType1CheckBox, gridBagConstraints);

        polygonType2CheckBox.setText("Degenerate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel11.add(polygonType2CheckBox, gridBagConstraints);

        polygonType3CheckBox.setText("Empty");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel11.add(polygonType3CheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel11, gridBagConstraints);

        customFunctionsButton.setText("Custom Formulas...");
        customFunctionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customFunctionsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel8.add(customFunctionsButton, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel8);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentHidden(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentHidden
    {//GEN-HEADEREND:event_formComponentHidden
        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_formComponentHidden

    private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_startSpinnerStateChanged
    {//GEN-HEADEREND:event_startSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)startSpinner.getModel()).getDate();
        if (date != null)
            startDate = date;
    }//GEN-LAST:event_startSpinnerStateChanged

    private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_endSpinnerStateChanged
    {//GEN-HEADEREND:event_endSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)endSpinner.getModel()).getDate();
        if (date != null)
            endDate = date;

    }//GEN-LAST:event_endSpinnerStateChanged

    private void selectRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectRegionButtonActionPerformed
    {//GEN-HEADEREND:event_selectRegionButtonActionPerformed
        if (selectRegionButton.isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_selectRegionButtonActionPerformed

    private void clearRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearRegionButtonActionPerformed
    {//GEN-HEADEREND:event_clearRegionButtonActionPerformed
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }//GEN-LAST:event_clearRegionButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String)numberOfFootprintsComboBox.getSelectedItem()));
                showFootprints(resultIntervalCurrentlyShown);
            }
        }
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getSize())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)numberOfFootprintsComboBox.getSelectedItem()));
                showFootprints(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)numberOfFootprintsComboBox.getSelectedItem()));
            showFootprints(resultIntervalCurrentlyShown);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void removeAllFootprintsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllFootprintsButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllFootprintsButtonActionPerformed
        SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
        model.removeAllSpectra();
        resultIntervalCurrentlyShown = null;
    }//GEN-LAST:event_removeAllFootprintsButtonActionPerformed

    protected TreeSet<Integer> cubeList = null;

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        try
        {
            selectRegionButton.setSelected(false);
            pickManager.setPickMode(PickMode.DEFAULT);

            List<Integer> polygonTypesChecked = new ArrayList<Integer>();

            if (polygonType0CheckBox.isSelected())
                polygonTypesChecked.add(0);
            if (polygonType1CheckBox.isSelected())
                polygonTypesChecked.add(1);
            if (polygonType2CheckBox.isSelected())
                polygonTypesChecked.add(2);
            if (polygonType3CheckBox.isSelected())
                polygonTypesChecked.add(3);

            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(startDate);
            endDateGreg.setTime(endDate);
            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH)+1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH)+1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

//            TreeSet<Integer> cubeList = null;
            if (cubeList != null)
                cubeList.clear();
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel erosModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            if (selectionModel.getAllItems().size() > 0)
            {
                EllipsePolygon region = (EllipsePolygon)selectionModel.getStructure(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (erosModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    erosModel.drawRegularPolygonLowRes(region.getCenter(), region.getRadius(), region.getNumberOfSides(), interiorPoly, null);
                    cubeList = erosModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = erosModel.getIntersectingCubes(region.getVtkInteriorPolyData());
                }
            }

            List<Integer> productsSelected;
            List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
            if(hasHierarchicalSpectraSearch)
            {
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
//                sumOfProductsSearch = true;

                // Process the user's selections
                hierarchicalSpectraSearchSpecification.processTreeSelections(
                        checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());

                // Get the selected (camera,filter) pairs
//                productsSelected = smallBodyConfig.hierarchicalSpectraSearchSpecification.getSelectedCameras();
//                TreeModel tree = smallBodyConfig.hierarchicalSpectraSearchSpecification.getTreeModel();
//                for (Integer selected : productsSelected)
//                {
//                    String name = tree.getChild(tree.getRoot(), selected).toString();
//                    System.out.println(
//                            "SpectrumSearchPanel: submitButtonActionPerformed: name is " + name);
////                    FixedListSearchMetadata.of(name, String filelist, String datapath, ImageSource pointingSource);
////                    results.addAll(instrument.getQueryBase().runQuery(Fix))
//                }
            }
            else
            {
                IQueryBase queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)queryType;
                    results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Ranges.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
                            "", polygonTypesChecked,
                            Ranges.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
                            Ranges.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
                            Ranges.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
                            cubeList);

                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
                    results = query.runQuery(searchMetadata).getResultlist();
                }
            }

            setSpectrumSearchResults(results);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void resultListMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultListMousePressed
    {//GEN-HEADEREND:event_resultListMousePressed
        maybeShowPopup(evt);
    }//GEN-LAST:event_resultListMousePressed

    private void resultListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultListMouseReleased
    {//GEN-HEADEREND:event_resultListMouseReleased
        maybeShowPopup(evt);
    }//GEN-LAST:event_resultListMouseReleased

    private void redComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redComboBoxActionPerformed
        updateColoring();
    }//GEN-LAST:event_redComboBoxActionPerformed

    private void greenComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenComboBoxActionPerformed
        updateColoring();
    }//GEN-LAST:event_greenComboBoxActionPerformed

    private void blueComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blueComboBoxActionPerformed
        updateColoring();
    }//GEN-LAST:event_blueComboBoxActionPerformed

    private void redMinSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redMinSpinnerStateChanged
        checkValidMinMax(0, true);
        updateColoring();
    }//GEN-LAST:event_redMinSpinnerStateChanged

    private void greenMinSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_greenMinSpinnerStateChanged
        checkValidMinMax(1, true);
        updateColoring();
    }//GEN-LAST:event_greenMinSpinnerStateChanged

    private void blueMinSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blueMinSpinnerStateChanged
        checkValidMinMax(2, true);
        updateColoring();
    }//GEN-LAST:event_blueMinSpinnerStateChanged

    private void redMaxSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redMaxSpinnerStateChanged
        checkValidMinMax(0, false);
        updateColoring();
    }//GEN-LAST:event_redMaxSpinnerStateChanged

    private void greenMaxSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_greenMaxSpinnerStateChanged
        checkValidMinMax(1, false);
        updateColoring();
    }//GEN-LAST:event_greenMaxSpinnerStateChanged

    private void blueMaxSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blueMaxSpinnerStateChanged
        checkValidMinMax(2, false);
        updateColoring();
    }//GEN-LAST:event_blueMaxSpinnerStateChanged

    private void grayscaleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grayscaleCheckBoxActionPerformed
        boolean enableColor = !grayscaleCheckBox.isSelected();

        redLabel.setVisible(enableColor);
        greenLabel.setVisible(enableColor);
        greenComboBox.setVisible(enableColor);
        greenMinLabel.setVisible(enableColor);
        greenMinSpinner.setVisible(enableColor);
        greenMaxLabel.setVisible(enableColor);
        greenMaxSpinner.setVisible(enableColor);
        blueLabel.setVisible(enableColor);
        blueComboBox.setVisible(enableColor);
        blueMinLabel.setVisible(enableColor);
        blueMinSpinner.setVisible(enableColor);
        blueMaxLabel.setVisible(enableColor);
        blueMaxSpinner.setVisible(enableColor);

        updateColoring();
    }//GEN-LAST:event_grayscaleCheckBoxActionPerformed

    private void customFunctionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customFunctionsButtonActionPerformed
        SpectrumMathPanel customFunctionsPanel = new SpectrumMathPanel(
                JOptionPane.getFrameForComponent(this),
                new JComboBox[]{redComboBox, greenComboBox, blueComboBox}, instrument);
        currentlyEditingUserDefinedFunction = true;
        customFunctionsPanel.setVisible(true);
        currentlyEditingUserDefinedFunction = false;
        updateColoring();
    }//GEN-LAST:event_customFunctionsButtonActionPerformed

    private void numberOfFootprintsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfFootprintsComboBoxActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only update if there's been a change in what is selected
            int newMaxId = resultIntervalCurrentlyShown.id1 + Integer.parseInt((String)this.numberOfFootprintsComboBox.getSelectedItem());
            if (newMaxId != resultIntervalCurrentlyShown.id2)
            {
                resultIntervalCurrentlyShown.id2 = newMaxId;
                showFootprints(resultIntervalCurrentlyShown);
            }
        }
    }//GEN-LAST:event_numberOfFootprintsComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected JComboBox blueComboBox;
    private javax.swing.JLabel blueLabel;
    private javax.swing.JLabel blueMaxLabel;
    protected javax.swing.JSpinner blueMaxSpinner;
    private javax.swing.JLabel blueMinLabel;
    protected javax.swing.JSpinner blueMinSpinner;
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JButton customFunctionsButton;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JLabel endDistanceLabel;
    private javax.swing.JLabel endEmissionLabel;
    private javax.swing.JLabel endIncidenceLabel;
    private javax.swing.JLabel endPhaseLabel;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JLabel fromDistanceLabel;
    private javax.swing.JFormattedTextField fromDistanceTextField;
    private javax.swing.JLabel fromEmissionLabel;
    private javax.swing.JFormattedTextField fromEmissionTextField;
    private javax.swing.JLabel fromIncidenceLabel;
    private javax.swing.JFormattedTextField fromIncidenceTextField;
    private javax.swing.JLabel fromPhaseLabel;
    private javax.swing.JFormattedTextField fromPhaseTextField;
    private javax.swing.JCheckBox grayscaleCheckBox;
    protected JComboBox greenComboBox;
    private javax.swing.JLabel greenLabel;
    private javax.swing.JLabel greenMaxLabel;
    protected javax.swing.JSpinner greenMaxSpinner;
    private javax.swing.JLabel greenMinLabel;
    protected javax.swing.JSpinner greenMinSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextButton;
    protected JComboBox numberOfFootprintsComboBox;
    private javax.swing.JCheckBox polygonType0CheckBox;
    private javax.swing.JCheckBox polygonType1CheckBox;
    private javax.swing.JCheckBox polygonType2CheckBox;
    private javax.swing.JCheckBox polygonType3CheckBox;
    private javax.swing.JButton prevButton;
    protected JComboBox redComboBox;
    private javax.swing.JLabel redLabel;
    private javax.swing.JLabel redMaxLabel;
    protected javax.swing.JSpinner redMaxSpinner;
    private javax.swing.JLabel redMinLabel;
    protected javax.swing.JSpinner redMinSpinner;
    private javax.swing.JButton removeAllFootprintsButton;
    protected javax.swing.JList resultList;
    protected javax.swing.JLabel resultsLabel;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel toDistanceLabel;
    private javax.swing.JFormattedTextField toDistanceTextField;
    private javax.swing.JLabel toEmissionLabel;
    private javax.swing.JFormattedTextField toEmissionTextField;
    private javax.swing.JLabel toIncidenceLabel;
    private javax.swing.JFormattedTextField toIncidenceTextField;
    private javax.swing.JLabel toPhaseLabel;
    private javax.swing.JFormattedTextField toPhaseTextField;
    // End of variables declaration//GEN-END:variables

}
