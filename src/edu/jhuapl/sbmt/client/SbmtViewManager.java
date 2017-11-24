package edu.jhuapl.sbmt.client;

import java.awt.Frame;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;

public class SbmtViewManager extends ViewManager
{
    // These two collections are used to maintain a sorted hierarchical order for
    // small bodies.
    // A comprehensive list of all possible small bodies in canonical order. Note that this
    // list can also include strings that do *not* refer to bodies, but rather
    // start with LABEL_PREFIX. These are used to add labels and separators in menus
    // generated from view managers.
    private static final ImmutableList<String> SMALL_BODY_LIST = listModels();

    // A comprehensive map of small bodies to the index of their positions in SMALL_BODY_LIST.
    // Keys of this map are a subset of SMALL_BODY_LIST -- strings that begin with LABEL_PREFIX
    // are skipped.
    private static final ImmutableMap<String, Integer> SMALL_BODY_LOOKUP = mapModels(SMALL_BODY_LIST);

    // Prefix used to flag elements of SMALL_BODY_LIST that are not actually names of bodies.
    private static final String LABEL_PREFIX = "SBMT ";

    // Flag indicating preference to add a separator in a menu.
    private static final String SEPARATOR = LABEL_PREFIX + "---";

    // These are similar but distinct from SMALL_BODY_LIST/SMALL_BODY_LOOKUP.
    // This list contains all menu entries, each of which may refer to a View/ViewConfig,
    // or a simple text string/label, or a marker for a separator. Only ViewConfig objects
    // that are enabled are added to this list. Note that instead of strings, this uses
    // the internal marker interface MenuEntry, which is used to wrap objects of different
    // types.
    private final List<MenuEntry> menuEntries;

    // A map of config objects to their indices in the menuEntries list.
    private final Map<ViewConfig, Integer> configMap;

    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
        this.menuEntries = Lists.newArrayList();
        this.configMap = Maps.newHashMap();
        setupViews(); // Must be called before this view manager is used.
    }

    @Override
    protected void addBuiltInView(View view)
    {
        // Make sure this view/body/model can and should be added. To be added, it
        // must be enabled, and must not be added more than once.
        ViewConfig config = view.getConfig();
        if (!config.isEnabled()) return;
        List<View> builtInViews = getBuiltInViews();
        if (builtInViews.contains(config)) return; // View was already added.

        // Ensure that this view's body has a canonical position in the master list of bodies.
        // This is important for the algorithm below, which requires all bodies to be named in SMALL_BODY_LIST.
        String name = config.getShapeModelName();
        if (!SMALL_BODY_LIST.contains(name))
        {
            // Need to add the body in order to the content of SMALL_BODY_LIST below.
            throw new IllegalArgumentException("Cannot determine where to add body " + name + " in ordered list");
        }

        // At this point, should be OK to add the view/model.

        // Create a set in the correct order from the flat list of views
        // the base class uses. The order is established by ViewComparator.
        SortedSet<View> viewSet = Sets.newTreeSet(new ViewComparator());
        viewSet.addAll(builtInViews);
        viewSet.add(view);

        // Next replace the base class's list with the sorted set's contents in its preferred order.
        builtInViews.clear();
        // NOTE: this is very important: all the collections basically have the same order for
        // their content. This is critical because it is assumed true in the loop below.
        builtInViews.addAll(viewSet);

        // Now populate menuEntries and configMap from the views.
        menuEntries.clear();
        configMap.clear();

        // Use an iterator to traverse all the Views in viewSet.
        Iterator<View> viewItor = viewSet.iterator();

        // Use an index for the labels, rather than an iterator, so that we can easily access
        // preceding entries in SMALL_BODY_LIST.
        int labelIndex = 0;

        // Loop simultaneously over the list of all labels and the set of all Views.
        while (viewItor.hasNext() && labelIndex < SMALL_BODY_LIST.size())
        {
            // Each time through this "while", process the next view from viewItor,
            // possibly also going through 1 or more labels (using labelIndex).
            View nextView = viewItor.next();
            ViewConfig nextConfig = nextView.getConfig();
            String modelName = nextConfig.getShapeModelName();

            String label = SMALL_BODY_LIST.get(labelIndex);

            // Process cosmetic labels and views that are not part of the new built-in view set.
            while (!label.equals(modelName))
            {
                // If a label starts with LABEL_PREFIX, it must be included in menuEntries so that
                // the latter includes all labels and separator positions.
                if (label.startsWith(LABEL_PREFIX)) menuEntries.add(makeEntry(SMALL_BODY_LIST.get(labelIndex)));

                // Done with this label -- it doesn't match this model, and because the collections
                // go in a parallel order, it won't match any in later loop iterations. So go on
                // to the next label, but be prepared to escape from the loop if needed.
                ++labelIndex;
                if (labelIndex < SMALL_BODY_LIST.size()) label = SMALL_BODY_LIST.get(labelIndex);
                else break;
            }
            if (labelIndex < SMALL_BODY_LIST.size())
            {
                // The loop above exits either a) if we ran out of labels or b) if the label equals modelName.
                // The predicate eliminates running out of labels, so at this point the label must
                // equal the modelName. This is a match, so put the index in the configMap and add
                // the wrapped view to the menuEntries list.
                if (!label.equals(modelName)) throw new AssertionError(); // This check could be commented out at some point.
                configMap.put(nextConfig, menuEntries.size());
                menuEntries.add(makeEntry(nextView));
            }
        }
    }

    @Override
    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (ViewConfig config: SmallBodyViewConfig.getBuiltInConfigs())
        {
            addBuiltInView(new SbmtView(statusBar, (SmallBodyViewConfig)config));
        }
    }

    @Override
    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        SmallBodyViewConfig config = new SmallBodyViewConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new SbmtView(statusBar, config);
    }

    /**
     * Return whether this body/model/view should be preceded by an informational label.
     * @param config the body/model/view
     * @param menuItem the current menu item at the level where the configuration is being added.
     * @return true if the body/model should be preceded by a label.
     */
    public boolean isAddLabel(ViewConfig config, String menuItem)
    {
        boolean result = false;
        if (config.getShapeModelName().equals(menuItem) && configMap.containsKey(config))
        {
            int index = configMap.get(config);
            result = index > 0 && menuEntries.get(index - 1) instanceof LabelEntry;
        }
        return result;
    }

    /**
     * Get the label that should precede this body/model/view.
     * @param config the body/model/view
     * @return the label
     * @throws IllegalArgumentException if the supplied body/model/view is not one of the
     * body/models this manager manages.
     */
    public String getLabel(ViewConfig config)
    {
        String result = null;
        if (configMap.containsKey(config))
        {
            int index = configMap.get(config);
            if (index > 0 && menuEntries.get(index - 1) instanceof LabelEntry)
            {
                result = menuEntries.get(index - 1).toString();
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Return whether this body/model/view should be preceded by a separator in the menu.
     * The menu item must match the name of the body associated with the supplied config (body/model/view).
     * @param config the body/model/view
     * @param menuItem the menu item
     * @return true if the body/model/view should be preceded by a separator.
     */
    public boolean isAddSeparator(ViewConfig config, String menuItem)
    {
        boolean result = false;
        if (config.getShapeModelName().equals(menuItem) && configMap.containsKey(config))
        {
            int index = configMap.get(config);
            result = index > 0 && menuEntries.get(index - 1) instanceof SeparatorEntry;
        }
        return result;
    }

    /**
     * Marker interface for generic menu entries.
     *
     */
    private interface MenuEntry
    {
    }

    /**
     * Menu entry that wraps a view.
     *
     */
    private static class ViewEntry implements MenuEntry
    {
        private final View view;
        ViewEntry(View view)
        {
            this.view = view;
        }

        @Override
        public String toString()
        {
            return view.getConfig().getShapeModelName();
        }
    }

    /**
     * Menu entry that denotes a separator.
     *
     */
    private static class SeparatorEntry implements MenuEntry
    {
        @Override
        public String toString()
        {
            return SEPARATOR.replaceFirst(LABEL_PREFIX, "");
        }
    }

    /**
     * Menu entry that denotes a label.
     *
     */
    private static class LabelEntry implements MenuEntry
    {
        private final String label;

        LabelEntry(String label)
        {
            this.label = label.replaceFirst(LABEL_PREFIX, "");
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    /**
     * Factory method to create a menu entry of the appropriate type.
     * @param label the label, which must begin with LABEL_PREFIX
     * @return the entry
     */
    private static MenuEntry makeEntry(String label)
    {
        if (label.equals(SEPARATOR)) return new SeparatorEntry();
        if (label.startsWith(LABEL_PREFIX)) return new LabelEntry(label);
        throw new IllegalArgumentException();
    }

    /**
     * Factory method that creates a menu entry from a supplied view/body/model
     * @param view the view/body/model
     * @return the entry
     */
    private static MenuEntry makeEntry(View view)
    {
        return new ViewEntry(view);
    }

    /**
     * Comparator used to order Views
     */
    private static class ViewComparator implements Comparator<View> {

        @Override
        public int compare(View view1, View view2) {
            int result = 0;
            ViewConfig config1 = view1.getConfig();
            ViewConfig config2 = view2.getConfig();
            String name1 = config1.getShapeModelName();
            String name2 = config2.getShapeModelName();

            // Two views may refer to the same author and shape model only if they are the same
            // object.
            if (name1.equals(name2) && config1.author.equals(config2.author) && config1 != config2)
                throw new AssertionError("Configuration duplicated: " + name1 + "/" + config1.author);

            // First look up the body names in the canonical order.
            if (result == 0 && SMALL_BODY_LOOKUP.containsKey(name1) && SMALL_BODY_LOOKUP.containsKey(name2))
                result = Integer.compare(SMALL_BODY_LOOKUP.get(name1), SMALL_BODY_LOOKUP.get(name2));

            if (result == 0 && config1 instanceof BodyViewConfig && config2 instanceof BodyViewConfig)
            {
                // Break ties where both views refer to the same body. Compare how models were generated (image-based/radar-based, etc.) and sort them alphabetically
                // by the method.
                BodyViewConfig body1 = (BodyViewConfig) config1;
                BodyViewConfig body2 = (BodyViewConfig) config2;
                result = body1.dataUsed.compareTo(body2.dataUsed);
            }

            // Final tie-breaker: all else is equal; require distinct authors.
            if (result == 0 && name1.equals(name2))
                result = config1.author.compareTo(config2.author);
            return result;
        }

    }

    private static ImmutableList<String> listModels()
    {
        // This order was based on an email from Terik Daly to James Peachey on 2017-11-14,
        // which included this information in a Word document titled Order-of-Objects.docx,
        // as described in Redmine issue #1009. One difference is that Carolyn Ernst asked
        // that Near Earth come before Main Belt asteroids.
        // Using redundant strings here that for now must be kept in sync manually with names of objects
        // in enum ShapeModelBody. This is deliberate so that the latter enumeration may be
        // phased out.
        String[] modelOrder = new String[] {
                ////////////////////////////////////////
                // Asteroids
                ////////////////////////////////////////
                // Near Earth
                LABEL_PREFIX + "Spacecraft Data",
                "433 Eros",
                "25143 Itokawa",
                "101955 Bennu",
                "101955 Bennu (V3 Image)",
                "101955 Bennu (V4 Image)",
                "162173 Ryugu",
                SEPARATOR,
                "1580 Betulia",
                "1620 Geographos",
                "1998 KY26",
                "2063 Bacchus",
                "2100 Ra-Shalom",
                "4179 Toutatis (High resolution)",
                "4179 Toutatis (Low resolution)",
                "4660 Nereus",
                "4769 Castalia",
                "4486 Mithra",
                "6489 Golevka",
                "54509 YORP",
                "(8567) 1996 HW1",
                "(10115) 1992 SK",
                "(29075) 1950 DA Prograde",
                "(29075) 1950 DA Retrograde",
                "(33342) 1998 WT24",
                "(52760) 1998 ML14",
                "(66391) 1999 KW4 A",
                "(66391) 1999 KW4 B",
                "(136617) 1994 CC",
                "(276049) 2002 CE26",
                "(341843) 2008 EV5",
                // Main Belt
                LABEL_PREFIX + "Spacecraft Data",
                "1 Ceres",
                "4 Vesta",
                "21 Lutetia",
                "243 Ida",
                "253 Mathilde",
                "951 Gaspra",
                "2867 Steins",
                SEPARATOR,
                "2 Pallas",
                "41 Daphne",
                "121 Hermione",
                "216 Kleopatra",

                ////////////////////////////////////////
                // Comets
                ////////////////////////////////////////
                "9P/Tempel 1",
                "67P/Churyumov-Gerasimenko (SHAP4S)",
                "67P/Churyumov-Gerasimenko (SHAP5 V0.3)",
                "67P/Churyumov-Gerasimenko (V2)",
                "67P/Churyumov-Gerasimenko (V3)",
                "81P/Wild 2",
                "103P/Hartley 2",
                "1P/Halley",

                ////////////////////////////////////////
                // Kuiper Belt Objects
                ////////////////////////////////////////
                // Pluto
                "Charon",
                "Hydra",
                "Kerberos",
                "Nix",
                "Pluto",
                "Styx",

                ////////////////////////////////////////
                // Planets and Satellites
                ////////////////////////////////////////
                "Earth",
                // Mars
                "Mars",
                "Deimos",
                "Phobos",
                // Jupiter
                "Jupiter",
                "Amalthea",
                "Callisto",
                "Europa",
                "Ganymede",
                "Io",
                // Saturn
                "Saturn",
                "Atlas",
                "Calypso",
                "Dione",
                "Enceladus",
                "Epimetheus",
                "Helene",
                "Hyperion",
                "Iapetus",
                "Janus",
                "Mimas",
                "Pan",
                "Pandora",
                "Phoebe",
                "Prometheus",
                "Rhea",
                "Telesto",
                "Tethys",
                // Neptune
                "Neptune",
                "Larissa",
                "Proteus",
        };
        return ImmutableList.copyOf(modelOrder);
    }

    private static ImmutableMap<String, Integer> mapModels(ImmutableList<String> modelOrder)
    {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        for (int ii = 0; ii < modelOrder.size(); ++ii)
        {
            String model = modelOrder.get(ii);
            if (!model.startsWith(LABEL_PREFIX))
            {
                builder.put(model, ii);
            }
        }
        return builder.build();
    }
}
