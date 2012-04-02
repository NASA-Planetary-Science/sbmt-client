package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.gui.custom.CustomViewer;
import edu.jhuapl.near.gui.deimos.DeimosViewer;
import edu.jhuapl.near.gui.eros.ErosViewer;
import edu.jhuapl.near.gui.itokawa.ItokawaViewer;
import edu.jhuapl.near.gui.simple.SimpleGaskellViewer;
import edu.jhuapl.near.gui.simple.SimpleViewer;
import edu.jhuapl.near.gui.vesta.VestaViewer;
import edu.jhuapl.near.gui.vesta_old.VestaOldViewer;
import edu.jhuapl.near.util.Configuration;

public class ViewerManager extends JPanel
{
    private ArrayList<Viewer> builtInViewers = new ArrayList<Viewer>();
    private ArrayList<Viewer> customViewers = new ArrayList<Viewer>();
    private Viewer currentViewer;
    private final StatusBar statusBar;

    public ViewerManager(StatusBar statusBar)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;


        // Gaskell models
        builtInViewers.add(new ErosViewer(statusBar));
        builtInViewers.add(new ItokawaViewer(statusBar));
        builtInViewers.add(new VestaViewer(statusBar));
        builtInViewers.add(new SimpleGaskellViewer(statusBar, "Mimas", "/MIMAS"));
        builtInViewers.add(new SimpleGaskellViewer(statusBar, "Phoebe", "/PHOEBE"));
        builtInViewers.add(new SimpleGaskellViewer(statusBar, "Phobos", "/PHOBOS"));


        // Thomas models
        builtInViewers.add(new SimpleViewer(statusBar, "Ida", "/THOMAS/243ida.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Mathilde", "/THOMAS/253mathilde.llr.gz"));
        builtInViewers.add(new VestaOldViewer(statusBar));
        builtInViewers.add(new SimpleViewer(statusBar, "Gaspra", "/THOMAS/951gaspra.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Phobos", "/THOMAS/m1phobos.llr.gz"));
        builtInViewers.add(new DeimosViewer(statusBar));
        builtInViewers.add(new SimpleViewer(statusBar, "Janus", "/THOMAS/s10janus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Epimetheus", "/THOMAS/s11epimetheus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Hyperion", "/THOMAS/s7hyperion.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Tempel 1", "/THOMAS/tempel1_cart.t1.gz"));

        // Stooke models
        builtInViewers.add(new SimpleViewer(statusBar, "Halley", "/STOOKE/1682q1halley.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Ida", "/STOOKE/243ida.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Gaspra", "/STOOKE/951gaspra.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Amalthea", "/STOOKE/j5amalthea.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Arissa", "/STOOKE/n7larissa.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Proteus", "/STOOKE/n8proteus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Janus", "/STOOKE/s10janus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Epimetheus", "/STOOKE/s11epimetheus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Prometheus", "/STOOKE/s16prometheus.llr.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Pandora", "/STOOKE/s17pandora.llr.gz"));

        // Hudson models
        builtInViewers.add(new SimpleViewer(statusBar, "Geographos", "/HUDSON/1620geographos.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "KY26", "/HUDSON/1998ky26.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Bacchus", "/HUDSON/2063bacchus.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Kleopatra", "/HUDSON/216kleopatra.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Itokawa", "/HUDSON/25143itokawa.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Toutatis (Low Res)", "/HUDSON/4179toutatis.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Toutatis (High Res)", "/HUDSON/4179toutatis2.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Castalia", "/HUDSON/4769castalia.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "52760 (1998 ML14)", "/HUDSON/52760.obj.gz"));
        builtInViewers.add(new SimpleViewer(statusBar, "Golevka", "/HUDSON/6489golevka.obj.gz"));

        // Other models
        builtInViewers.add(new SimpleViewer(statusBar, "Wild 2", "/OTHER/wild2_cart_full.w2.gz"));


        currentViewer = builtInViewers.get(0);

        for (Viewer viewer : builtInViewers)
            add(viewer, viewer.getUniqueName());

        loadCustomViewers();

        for (Viewer viewer : customViewers)
            add(viewer, viewer.getUniqueName());
    }

    private void loadCustomViewers()
    {
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (dir.isDirectory())
                {
                    customViewers.add(new CustomViewer(statusBar, dir.getName()));
                }
            }
        }
    }

    public Viewer getCurrentViewer()
    {
        return currentViewer;
    }

    public void setCurrentViewer(Viewer viewer)
    {
        CardLayout cardLayout = (CardLayout)(getLayout());
        cardLayout.show(this, viewer.getUniqueName());

        // defer initialization of Viewer until we show it.
        repaint();
        validate();
        viewer.initialize();

        currentViewer = viewer;
    }

    public Viewer getBuiltInViewer(int i)
    {
        return builtInViewers.get(i);
    }

    public int getNumberOfBuiltInViewers()
    {
        return builtInViewers.size();
    }

    public Viewer getCustomViewer(int i)
    {
        return customViewers.get(i);
    }

    public int getNumberOfCustomViewers()
    {
        return customViewers.size();
    }

    public Viewer addCustomViewer(String name)
    {
        Viewer viewer = new CustomViewer(statusBar, name);
        customViewers.add(viewer);
        add(viewer, name);
        return viewer;
    }

    public Viewer removeCustomViewer(String name)
    {
        for (Viewer viewer : customViewers)
        {
            if (viewer.getUniqueName().equals(name))
            {
                customViewers.remove(viewer);
                remove(viewer);
                return viewer;
            }
        }

        return null;
    }

    public Viewer getCustomViewer(String name)
    {
        for (Viewer viewer : customViewers)
        {
            if (viewer.getUniqueName().equals(name))
            {
                return viewer;
            }
        }

        return null;
    }

    public ArrayList<Viewer> getAllViewers()
    {
        ArrayList<Viewer> allViewers = new ArrayList<Viewer>();
        allViewers.addAll(builtInViewers);
        allViewers.addAll(customViewers);
        return allViewers;
    }
}
