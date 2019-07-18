package edu.jhuapl.sbmt.spectrum.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;

public abstract class SpectrumView extends JPanel {

    private JScrollPane resultsScrollPanel;
    private JComboBox numberOfFootprintsComboBox;
    private JButton prevButton;
    private JButton nextButton;
    private JButton removeAllFootprintsButton;
    private JCheckBox grayscaleCheckBox;
    private JButton customFunctionsButton;
    private JComboBox redComboBox;
    private JSpinner redMinSpinner;
    private JSpinner redMaxSpinner;
    private JComboBox greenComboBox;
    private JSpinner greenMinSpinner;
    private JSpinner greenMaxSpinner;
    private JComboBox blueComboBox;
    private JSpinner blueMinSpinner;
    private JSpinner blueMaxSpinner;
    private JList resultList;
    protected SpectrumPopupMenu spectrumPopupMenu;
    private JLabel resultsLabel;
    private JPanel resultsLabelPanel;
    private JButton removeAllBoundariesButton;
    private JPanel dbSearchPanel;
    private JPanel coloringDetailPanel;
    private JPanel coloringPanel;
    private JComboBox coloringComboBox;
    private JPanel emissionAngleColoringPanel;
    private JPanel rgbColoringPanel;
    private JButton saveSpectraListButton;
    private JButton loadSpectraListButton;
    private PickManager pickManager;
//    private SmallBodyViewConfig smallBodyConfig;
    private ModelManager modelManager;
    private Renderer renderer;
    private ISpectralInstrument instrument;
    private JPanel panel;
       private JButton selectRegionButton;
    private JButton clearRegionButton;
    private JPanel searchButtonPanel;
    private JButton submitButton;


    public SpectrumView(/*SmallBodyViewConfig smallBodyConfig,*/ ModelManager modelManager, PickManager pickManager2, Renderer renderer, ISpectralInstrument instrument)
    {

//        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.pickManager = pickManager2;
        this.renderer = renderer;
        this.instrument = instrument;


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        resultList = new JList();
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        panel = new JPanel();
        scrollPane.setViewportView(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        saveSpectraListButton = new JButton("Save List");
        loadSpectraListButton = new JButton("Load List");
        removeAllFootprintsButton = new JButton("Remove All Footprints");
        removeAllBoundariesButton = new JButton("Remove All Boundaries");

        resultsScrollPanel = new JScrollPane();
        resultsLabelPanel = new JPanel();
        resultsLabel = new JLabel("");
        numberOfFootprintsComboBox = new JComboBox();
        prevButton = new JButton("<");
        nextButton = new JButton(">");
        coloringPanel = new JPanel();
        coloringPanel.setBorder(new TitledBorder(null, "Coloring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        coloringComboBox = new JComboBox();
        coloringDetailPanel = new JPanel();
        emissionAngleColoringPanel = new JPanel();
        rgbColoringPanel = new JPanel();
        grayscaleCheckBox = new JCheckBox("Grayscale");
        customFunctionsButton = new JButton("Custom Formulas");
        redComboBox = new JComboBox();
        redMinSpinner = new JSpinner();
        redMaxSpinner = new JSpinner();
        greenComboBox = new JComboBox();
        greenMinSpinner = new JSpinner();
        greenMaxSpinner = new JSpinner();
        blueComboBox = new JComboBox();
        blueMinSpinner = new JSpinner();
        blueMaxSpinner = new JSpinner();


        // search / region buttons
        searchButtonPanel = new JPanel();

        selectRegionButton = new JButton("Select Region");
        selectRegionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selectRegionButton.isSelected()) {
                    selectRegionButton.setSelected(true);
                    getPickManager().setPickMode(PickMode.CIRCLE_SELECTION);
                } else {
                    selectRegionButton.setSelected(false);
                    getPickManager().setPickMode(PickMode.DEFAULT);
                }
            }
        });
        searchButtonPanel.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        searchButtonPanel.add(clearRegionButton);

        submitButton = new JButton("Search");
        searchButtonPanel.add(submitButton);
    }


    public JPanel getSearchButtonPanel()
    {
        return searchButtonPanel;
    }

    public JPanel getPanel() {
        return panel;
    }
    public JScrollPane getResultsScrollPanel() {
        return resultsScrollPanel;
    }
    public JComboBox getNumberOfFootprintsComboBox() {
        return numberOfFootprintsComboBox;
    }
    public JButton getPrevButton() {
        return prevButton;
    }
    public JButton getNextButton() {
        return nextButton;
    }
    public JButton getRemoveAllFootprintsButton() {
        return removeAllFootprintsButton;
    }
    public JCheckBox getGrayscaleCheckBox() {
        return grayscaleCheckBox;
    }
    public JButton getCustomFunctionsButton() {
        return customFunctionsButton;
    }
    public JComboBox getRedComboBox() {
        return redComboBox;
    }
    public JSpinner getRedMinSpinner() {
        return redMinSpinner;
    }
    public JSpinner getRedMaxSpinner() {
        return redMaxSpinner;
    }
    public JComboBox getGreenComboBox() {
        return greenComboBox;
    }
    public JSpinner getGreenMinSpinner() {
        return greenMinSpinner;
    }
    public JSpinner getGreenMaxSpinner() {
        return greenMaxSpinner;
    }
    public JComboBox getBlueComboBox() {
        return blueComboBox;
    }
    public JSpinner getBlueMinSpinner() {
        return blueMinSpinner;
    }
    public JSpinner getBlueMaxSpinner() {
        return blueMaxSpinner;
    }


    public JList getResultList()
    {
        return resultList;
    }


    public SpectrumPopupMenu getSpectrumPopupMenu()
    {
        return spectrumPopupMenu;
    }


    public void setSpectrumPopupMenu(SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
    }
    public JLabel getResultsLabel() {
        return resultsLabel;
    }
    public JButton getRemoveAllBoundariesButton() {
        return removeAllBoundariesButton;
    }
    public JPanel getDbSearchPanel() {
        return dbSearchPanel;
    }
    public JPanel getColoringDetailPanel() {
        return coloringDetailPanel;
    }
    public JPanel getColoringPanel() {
        return coloringPanel;
    }
    public JComboBox getColoringComboBox() {
        return coloringComboBox;
    }
    public JPanel getEmissionAngleColoringPanel() {
        return emissionAngleColoringPanel;
    }
    public JPanel getRgbColoringPanel() {
        return rgbColoringPanel;
    }
    public JButton getSaveSpectraListButton() {
        return saveSpectraListButton;
    }
    public JButton getLoadSpectraListButton() {
        return loadSpectraListButton;
    }
    public JPanel getResultsLabelPanel()
    {
        return resultsLabelPanel;
    }


    public void setResultsLabelPanel(JPanel resultsLabelPanel)
    {
        this.resultsLabelPanel = resultsLabelPanel;
    }


    public PickManager getPickManager()
    {
        return pickManager;
    }


    public void setPickManager(PickManager pickManager)
    {
        this.pickManager = pickManager;
    }


//    public SmallBodyViewConfig getSmallBodyConfig()
//    {
//        return smallBodyConfig;
//    }
//
//
//    public void setSmallBodyConfig(SmallBodyViewConfig smallBodyConfig)
//    {
//        this.smallBodyConfig = smallBodyConfig;
//    }


    public ModelManager getModelManager()
    {
        return modelManager;
    }


    public void setModelManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }


    public Renderer getRenderer()
    {
        return renderer;
    }


    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }


    public ISpectralInstrument getInstrument()
    {
        return instrument;
    }


    public void setInstrument(ISpectralInstrument instrument)
    {
        this.instrument = instrument;
    }



    public void setResultsScrollPanel(JScrollPane resultsScrollPanel)
    {
        this.resultsScrollPanel = resultsScrollPanel;
    }


    public void setNumberOfFootprintsComboBox(JComboBox numberOfFootprintsComboBox)
    {
        this.numberOfFootprintsComboBox = numberOfFootprintsComboBox;
    }


    public void setPrevButton(JButton prevButton)
    {
        this.prevButton = prevButton;
    }


    public void setNextButton(JButton nextButton)
    {
        this.nextButton = nextButton;
    }


    public void setRemoveAllFootprintsButton(JButton removeAllFootprintsButton)
    {
        this.removeAllFootprintsButton = removeAllFootprintsButton;
    }


    public void setGrayscaleCheckBox(JCheckBox grayscaleCheckBox)
    {
        this.grayscaleCheckBox = grayscaleCheckBox;
    }


    public void setCustomFunctionsButton(JButton customFunctionsButton)
    {
        this.customFunctionsButton = customFunctionsButton;
    }


    public void setRedComboBox(JComboBox redComboBox)
    {
        this.redComboBox = redComboBox;
    }


    public void setRedMinSpinner(JSpinner redMinSpinner)
    {
        this.redMinSpinner = redMinSpinner;
    }


    public void setRedMaxSpinner(JSpinner redMaxSpinner)
    {
        this.redMaxSpinner = redMaxSpinner;
    }


    public void setGreenComboBox(JComboBox greenComboBox)
    {
        this.greenComboBox = greenComboBox;
    }


    public void setGreenMinSpinner(JSpinner greenMinSpinner)
    {
        this.greenMinSpinner = greenMinSpinner;
    }


    public void setGreenMaxSpinner(JSpinner greenMaxSpinner)
    {
        this.greenMaxSpinner = greenMaxSpinner;
    }


    public void setBlueComboBox(JComboBox blueComboBox)
    {
        this.blueComboBox = blueComboBox;
    }


    public void setBlueMinSpinner(JSpinner blueMinSpinner)
    {
        this.blueMinSpinner = blueMinSpinner;
    }


    public void setBlueMaxSpinner(JSpinner blueMaxSpinner)
    {
        this.blueMaxSpinner = blueMaxSpinner;
    }


    public void setResultList(JList resultList)
    {
        this.resultList = resultList;
    }


    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }


    public void setRemoveAllBoundariesButton(JButton removeAllBoundariesButton)
    {
        this.removeAllBoundariesButton = removeAllBoundariesButton;
    }


    public void setDbSearchPanel(JPanel dbSearchPanel)
    {
        this.dbSearchPanel = dbSearchPanel;
    }


    public void setColoringDetailPanel(JPanel coloringDetailPanel)
    {
        this.coloringDetailPanel = coloringDetailPanel;
    }


    public void setColoringPanel(JPanel coloringPanel)
    {
        this.coloringPanel = coloringPanel;
    }


    public void setColoringComboBox(JComboBox coloringComboBox)
    {
        this.coloringComboBox = coloringComboBox;
    }


    public void setEmissionAngleColoringPanel(JPanel emissionAngleColoringPanel)
    {
        this.emissionAngleColoringPanel = emissionAngleColoringPanel;
    }


    public void setRgbColoringPanel(JPanel rgbColoringPanel)
    {
        this.rgbColoringPanel = rgbColoringPanel;
    }


    public void setSaveSpectraListButton(JButton saveSpectraListButton)
    {
        this.saveSpectraListButton = saveSpectraListButton;
    }


    public void setLoadSpectraListButton(JButton loadSpectraListButton)
    {
        this.loadSpectraListButton = loadSpectraListButton;
    }


    public void setPanel(JPanel panel)
    {
        this.panel = panel;
    }

    public JButton getSelectRegionButton()
    {
        return selectRegionButton;
    }


    public void setSelectRegionButton(JButton selectRegionButton)
    {
        this.selectRegionButton = selectRegionButton;
    }


    public JButton getClearRegionButton()
    {
        return clearRegionButton;
    }


    public void setClearRegionButton(JButton clearRegionButton)
    {
        this.clearRegionButton = clearRegionButton;
    }


    public JButton getSubmitButton()
    {
        return submitButton;
    }


    public void setSubmitButton(JButton submitButton)
    {
        this.submitButton = submitButton;
    }


    public void setSearchButtonPanel(JPanel searchButtonPanel)
    {
        this.searchButtonPanel = searchButtonPanel;
    }
}
