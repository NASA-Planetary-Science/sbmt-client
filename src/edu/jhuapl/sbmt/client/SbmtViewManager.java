package edu.jhuapl.sbmt.client;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.camera.gui.CameraQuaternionAction;
import edu.jhuapl.saavtk.camera.gui.CameraRegularAction;
import edu.jhuapl.saavtk.gui.Console;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.ShapeModelImporter;
import edu.jhuapl.saavtk.gui.ShapeModelImporter.FormatType;
import edu.jhuapl.saavtk.gui.ShapeModelImporter.ShapeModelType;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.menu.FavoritesMenu;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.gui.menu.PickToleranceAction;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.scalebar.gui.ScaleBarAction;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.view.lod.gui.LodAction;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.TrackedMetadataManager;

public class SbmtViewManager extends ViewManager
{
    private static final long serialVersionUID = 1L;

    // Prefix used to flag elements of SMALL_BODY_LIST that are not actually names of bodies.
    private static final String LABEL_PREFIX = "SBMT ";

    // Flag indicating preference to add a separator in a menu.
    private static final String SEPARATOR = LABEL_PREFIX + "---";

    private static boolean failsafeModelInitialized = false;

    private static String failsafeModelName = null;

    // These are similar but distinct from SMALL_BODY_LIST/SMALL_BODY_LOOKUP.
    // This list contains all menu entries, each of which may refer to a View/ViewConfig,
    // or a simple text string/label, or a marker for a separator. Only ViewConfig objects
    // that are enabled are added to this list. Note that instead of strings, this uses
    // the internal marker interface MenuEntry, which is used to wrap objects of different
    // types.
    private final List<MenuEntry> menuEntries;

    // A map of config objects to their indices in the menuEntries list.
//    private final Map<ViewConfig, Integer> configMap;
    private final Map<BasicConfigInfo, Integer> configMap;

    private final TrackedMetadataManager stateManager;

    private List<String> registeredConfigURLs = new ArrayList<String>();

    private String defaultModelName;

    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
        this.menuEntries = Lists.newArrayList();
        this.configMap = Maps.newHashMap();
        this.stateManager = TrackedMetadataManager.of("ViewManager");
        setupViews(); // Must be called before this view manager is used.
    }

    /**
     * This implementation uses the default model name provided by
     * {@link SmallBodyViewConfig#getDefaultModelName()} to set the initial default
     * model, but this may be changed by calling
     * {@link #setDefaultModelName(String)}.
     */
    @Override
    public String getDefaultModelName()
    {
        if (defaultModelName == null)
        {
            defaultModelName = SmallBodyViewConfig.getDefaultModelName();
        }

        return defaultModelName;
    }

    /**
     * Set the current default model name. Note this affects only the running tool,
     * not the persistent model name saved on disk.
     */
    @Override
    public void setDefaultModelName(String modelName)
    {
        this.defaultModelName = modelName;
    }

    /**
     * Make the current default model persistent so that it will be used for the
     * default model next time the tool is run. This implementation uses using
     * {@link SmallBodyViewConfig#setDefaultModelName(String).
     */
    @Override
    public void saveDefaultModelName()
    {
        SmallBodyViewConfig.setDefaultModelName(defaultModelName);
    }

    /**
     * Revert the default model name which will be used the next time the tool
     * starts. This implementation uses
     * {@link SmallBodyViewConfig#resetDefaultModelName()}.
     */
    @Override
    public void resetDefaultModelName()
    {
        SmallBodyViewConfig.resetDefaultModelName();
        this.defaultModelName = SmallBodyViewConfig.getDefaultModelName();
    }

    /**
     * Returns the name of a failsafe built-in model of Eros that is equivalent to
     * the Eros/Gaskell 2008 low resolution body model, but without any plate
     * colorings, images, spectra etc. This model will be used only in the case
     * where a first-time user starts without internet access.
     * <p>
     * The first time this is called, it will attempt to create the model using
     * resources found by the class loader. If the resources are not found, or if
     * model creation fails for any other reason, it will not be attempted again.
     * Instead, the basic model name returned will be null whenever this method is
     * called.
     * <p>
     * The model created will be treated as if it were created by the user as a
     * custom model.
     */
    @Override
    protected String provideBasicModel()
    {
        if (!failsafeModelInitialized)
        {
            failsafeModelInitialized = true;

            String failsafeParent = SafeURLPaths.instance().getString(Configuration.getApplicationDataDir(), "failsafeErosModel");
            File failsafeModel = ConvertResourceToFile.convertResourceToRealFile(this, "/edu/jhuapl/sbmt/data/Eros_ver64q.vtk", failsafeParent);

            if (failsafeModel != null && failsafeModel.exists())
            {
                String modelName = "Eros-Gaskell-2008";

                ShapeModelImporter importer = new ShapeModelImporter();

                importer.setShapeModelType(ShapeModelType.FILE);
                importer.setName(modelName);
                importer.setFormat(FormatType.VTK);
                importer.setModelPath(failsafeModel.getPath());

                String[] errorMessage = new String[1];

                boolean result = importer.importShapeModel(errorMessage, false);

                if (result)
                {
                    failsafeModelName = modelName;
                }
                else
                {
                    System.err.println(errorMessage[0]);
                }
            }
        }

        return failsafeModelName;
    }

    @Override
    protected void createMenus(JMenuBar menuBar) {
        fileMenu = new FileMenu(this, ImmutableList.of("sbmt"));
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        // Body menu
        recentsMenu = new RecentlyViewed(this);

        bodyMenu = new SbmtViewMenu(this, recentsMenu);
        bodyMenu.setMnemonic('B');
        bodyMenu.add(new JSeparator());
        bodyMenu.add(new FavoritesMenu(this));
        bodyMenu.add(createPasswordMenu());
        bodyMenu.add(new JSeparator());
        bodyMenu.add(recentsMenu);
        menuBar.add(bodyMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        viewMenu.add(new JMenuItem(new CameraRegularAction(this)));
        viewMenu.add(new JMenuItem(new CameraQuaternionAction(this)));
        viewMenu.add(new JMenuItem(new ScaleBarAction(this)));

        viewMenu.addSeparator();
        viewMenu.add(new JMenuItem(new LodAction(this)));
        viewMenu.add(new PickToleranceAction(this));

        menuBar.add(viewMenu);

        // Console menu
        Console.addConsoleMenu(menuBar);

        // Help menu
        helpMenu = new SbmtHelpMenu(this);
        helpMenu.setMnemonic('H');
        menuBar.add(helpMenu);
    }

    @Override
    protected void addBuiltInView(View view)
    {
        List<View> builtInViews = getBuiltInViews();
        if (builtInViews.contains(view)) return; // View was already added.

        // At this point, should be OK to add the view/model.
        // Create a set in the correct order from the flat list of views
        // the base class uses. The order is established by ViewComparator.
        SortedSet<View> viewSet = Sets.newTreeSet(new ViewComparator2());
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

        // TODO: make the code below handle putting the spacecraft labels in front of groups of bodies
        // listed in MARK_VISITED_BY_SPACECRAFT (see below).
        // Use an iterator to traverse all the Views in viewSet.
        Iterator<View> viewItor = viewSet.iterator();

        // Loop simultaneously over the list of all labels and the set of all Views.
        while (viewItor.hasNext())
        {
            SbmtView nextView = (SbmtView)viewItor.next();

//            ViewConfig nextConfig = nextView.getConfig();
            configMap.put(nextView.getConfigInfo(), menuEntries.size());
            menuEntries.add(makeEntry(nextView));
        }
    }

    @Override
    protected void addBuiltInViews(StatusBar statusBar)
    {
//        for (ViewConfig config: SmallBodyViewConfig.getBuiltInConfigs())
//        {
////            System.out.println(config.getUniqueName());
//            //if (config.getUniqueName().equals("Gaskell/25143 Itokawa"))
//                addBuiltInView(new SbmtView(statusBar, (SmallBodyViewConfig)config));
//        }

        for (BasicConfigInfo configInfo: SmallBodyViewConfig.getConfigIdentifiers())
        {
        	if (configInfo.isEnabled())
        		addBuiltInView(new SbmtView(statusBar, configInfo));
        }
//    	for (String configKey: SmallBodyViewConfig.getConfigIdentifiers().keySet())
//    	{
//    		System.out.println("SbmtViewManager: addBuiltInViews: config key " + configKey);
//    		addBuiltInView(configKey);
//    		MenuEntry entry = makeEntryFromConfigKey(configKey);
//    		menuEntries.add(entry);
//    	}
    }

    protected void addBuiltInView(String configURL)
    {
    	registeredConfigURLs.add(configURL);
    }


    @Override
    protected View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        SmallBodyViewConfig customConfig = SmallBodyViewConfig.ofCustom(name, temporary);

        return new SbmtView(statusBar, customConfig);
    }

    @Override
    public View createCustomView(String name, boolean temporary, File metadata)
    {
        SmallBodyViewConfig customConfig = SmallBodyViewConfig.ofCustom(name, temporary);
        SmallBodyViewConfigMetadataIO customConfigImporter = new SmallBodyViewConfigMetadataIO();
        try
        {
            customConfigImporter.read(metadata, name, customConfig);
        }
        catch (NullPointerException | IllegalArgumentException iae)
        {
//            System.err.println("Custom Model Import Error: Unable to read custom model metadata for " + name + " attempting older style");
           	return new SbmtView(statusBar, customConfig);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //write this back out with the new metadata data changes to denote the customization
        try
        {
            customConfigImporter.write(metadata, name);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new SbmtView(statusBar, customConfig);
    }

    @Override
    protected View getBuiltInView(String uniqueName)
    {
        for (View view : getBuiltInViews())
        {
            if (view.getUniqueName().equals(uniqueName) && view.isAccessible())
            {
                return view;
            }
        }

        return null;
    }

//    @Override
//    public String getDefaultBodyToLoad()
//    {
//    	defaultModelName = SmallBodyViewConfig.getConfigIdentifiers().keySet().
//        if (defaultModelFile.toFile().exists())
//        {
//            try (Scanner scanner = new Scanner(ViewManager.defaultModelFile.toFile()))
//            {
//                if (scanner.hasNextLine())
//                    defaultModelName = scanner.nextLine();
//            }
//            catch (IOException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        return defaultModelName;
//    }

    @Override
    public void initializeStateManager() {
        if (!stateManager.isRegistered())
        {
            stateManager.register(new MetadataManager() {
                final Key<String> currentViewKey = Key.of("currentView");

                @Override
                public Metadata store()
                {
                    SettableMetadata state = SettableMetadata.of(Version.of(1, 0));
                    View currentView = getCurrentView();
                    state.put(currentViewKey, currentView != null ? currentView.getUniqueName() : null);
                    return state;
                }

                @Override
                public void retrieve(Metadata source)
                {
                    final View retrievedView = getView(source.get(currentViewKey));
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run()
                        {
                            setCurrentView(retrievedView);
                        }

                    });
                }
            });
        }
    }

    /**
     * Return whether this body/model/view should be preceded by a separator in the menu.
     * The menu item must match the name of the body associated with the supplied config (body/model/view).
     * @param config the body/model/view
     * @param menuItem the menu item
     * @return true if the body/model/view should be preceded by a separator.
     */
    public boolean isAddSeparator(BasicConfigInfo config, String menuItem)
    {
        boolean result = false;
        if (config.shapeModelName.equals(menuItem) && configMap.containsKey(config))
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
    private static class ViewComparator2 implements Comparator<View>
    {
        @Override
        public int compare(View view1, View view2) {
            int result = 0;
            if (view1 == view2) return result;

            BasicConfigInfo config1 = ((SbmtView)view1).getConfigInfo();
            BasicConfigInfo config2 = ((SbmtView)view2).getConfigInfo();
            if (config1 == config2) return result;

            // If we get to here, equality is not an option -- two ViewConfigs must differ
            // in one of their significant fields. From here on down is a series of tie-breakers.

            result = TYPE_COMPARATOR.compare(config1.type, config2.type);

            if (result == 0)
            {
                result = POPULATION_COMPARATOR.compare(config1.population, config2.population);
            }

            if (result == 0)
            {
                result = MARK_VISITED_BY_SPACECRAFT_COMPARATOR.compare(config1.body, config2.body);
            }

            if (result == 0)
            {
                result = BODY_COMPARATOR.compare(config1.body, config2.body);
            }

            if (result == 0) {
                result = DATA_USED_COMPARATOR.compare(config1.dataUsed, config2.dataUsed);
            }

            if (result == 0)
            {
                if (ORDER_ADDED_COMPARATOR == null)
                {
                    ORDER_ADDED_COMPARATOR = OrderedComparator.of(SmallBodyViewConfig.getConfigIdentifiers());
                }
                result = ORDER_ADDED_COMPARATOR.compare(config1, config2);
            }

            if (result == 0)
            {
                throw new AssertionError("Two models have the same designation: " + config1.toString());
            }

            return result;
        }
    }

    private static final OrderedComparator<BodyType> TYPE_COMPARATOR = OrderedComparator.of(Lists.newArrayList(
            BodyType.ASTEROID,
            BodyType.COMETS,
            BodyType.KBO,
            BodyType.PLANETS_AND_SATELLITES,
            null
            ));

    private static final OrderedComparator<ShapeModelPopulation> POPULATION_COMPARATOR = OrderedComparator.of(Lists.newArrayList(
            ShapeModelPopulation.NEO,
            ShapeModelPopulation.MAIN_BELT,
            ShapeModelPopulation.PLUTO,
            ShapeModelPopulation.MARS,
            ShapeModelPopulation.JUPITER,
            ShapeModelPopulation.SATURN,
            ShapeModelPopulation.NEPTUNE,
            ShapeModelPopulation.EARTH,
            ShapeModelPopulation.NA,
            null
            ));

    private static final ImmutableSet<ShapeModelBody> MARK_VISITED_BY_SPACECRAFT = ImmutableSet.of(
            ShapeModelBody.EROS,
            ShapeModelBody.ITOKAWA,
            ShapeModelBody.RQ36,
            ShapeModelBody.RYUGU,
            ShapeModelBody.CERES,
            ShapeModelBody.VESTA,
            ShapeModelBody.PSYCHE,
            ShapeModelBody.LUTETIA,
            ShapeModelBody.IDA,
            ShapeModelBody.MATHILDE,
            ShapeModelBody.GASPRA,
            ShapeModelBody.STEINS
            );

    private static final Comparator<ShapeModelBody> MARK_VISITED_BY_SPACECRAFT_COMPARATOR = new Comparator<ShapeModelBody>() {
        @Override
        public int compare(ShapeModelBody o1, ShapeModelBody o2)
        {
            int result = 0;
            if (o1 != null && o2 != null)
            {
                if (MARK_VISITED_BY_SPACECRAFT.contains(o1))
                {
                    result = MARK_VISITED_BY_SPACECRAFT.contains(o2) ? 0 : -1;
                }
                else if (MARK_VISITED_BY_SPACECRAFT.contains(o2))
                {
                    result = 1;
                }
            }
            return result;
        }

    };

    private static final OrderedComparator<ShapeModelBody> BODY_COMPARATOR = OrderedComparator.of(Lists.newArrayList(
            // Asteroids -> NEO (visited)
            ShapeModelBody.EROS,
            ShapeModelBody.ITOKAWA,
            ShapeModelBody.RQ36,
            ShapeModelBody.RYUGU,
            // Asteroids -> NEO (not visited)
            ShapeModelBody.BETULIA,
            ShapeModelBody.GEOGRAPHOS,
            ShapeModelBody.KY26,
            ShapeModelBody.BACCHUS,
            ShapeModelBody.RASHALOM,
            ShapeModelBody.TOUTATIS,
            ShapeModelBody.NEREUS,
            ShapeModelBody.CASTALIA,
            ShapeModelBody.MITHRA,
            ShapeModelBody.GOLEVKA,
            ShapeModelBody.YORP,
            ShapeModelBody.HW1,
            ShapeModelBody.SK,
            ShapeModelBody._1950DAPROGRADE,
            ShapeModelBody._1950DARETROGRADE,
            ShapeModelBody.WT24,
            ShapeModelBody._52760_1998_ML14,
            ShapeModelBody.KW4A,
            ShapeModelBody.KW4B,
            ShapeModelBody.CCALPHA,
            ShapeModelBody.CE26,
            ShapeModelBody.EV5,
            // Asteroids -> Main Belt (visited)
            ShapeModelBody.CERES,
            ShapeModelBody.VESTA,
            ShapeModelBody.PSYCHE,
            ShapeModelBody.LUTETIA,
            ShapeModelBody.IDA,
            ShapeModelBody.MATHILDE,
            ShapeModelBody.GASPRA,
            ShapeModelBody.STEINS,
            // Asteroids -> Main Belt (not visited)
            ShapeModelBody.PALLAS,
            ShapeModelBody.DAPHNE,
            ShapeModelBody.HERMIONE,
            ShapeModelBody.KLEOPATRA,
            // Comets
            ShapeModelBody.HALLEY,
            ShapeModelBody.TEMPEL_1,
            ShapeModelBody.WILD_2,
            ShapeModelBody._67P,
            ShapeModelBody.HARTLEY,
            // KBO
            ShapeModelBody.PLUTO,
            ShapeModelBody.CHARON,
            ShapeModelBody.HYDRA,
            ShapeModelBody.KERBEROS,
            ShapeModelBody.NIX,
            ShapeModelBody.STYX,
            // Planets -> Mars
            ShapeModelBody.DEIMOS,
            ShapeModelBody.PHOBOS,
            // Planets -> Jupiter
            ShapeModelBody.AMALTHEA,
            ShapeModelBody.CALLISTO,
            ShapeModelBody.EUROPA,
            ShapeModelBody.GANYMEDE,
            ShapeModelBody.IO,
            // Planets -> Saturn
            ShapeModelBody.ATLAS,
            ShapeModelBody.CALYPSO,
            ShapeModelBody.DIONE,
            ShapeModelBody.ENCELADUS,
            ShapeModelBody.EPIMETHEUS,
            ShapeModelBody.HELENE,
            ShapeModelBody.HYPERION,
            ShapeModelBody.IAPETUS,
            ShapeModelBody.JANUS,
            ShapeModelBody.MIMAS,
            ShapeModelBody.PAN,
            ShapeModelBody.PANDORA,
            ShapeModelBody.PHOEBE,
            ShapeModelBody.PROMETHEUS,
            ShapeModelBody.RHEA,
            ShapeModelBody.TELESTO,
            ShapeModelBody.TETHYS,
            // Planets -> Neptune
            ShapeModelBody.LARISSA,
            ShapeModelBody.PROTEUS,
            // Planets -> Earth
            ShapeModelBody.EARTH,
            ShapeModelBody.MU69,
            null
            ));

    private static final Comparator<ShapeModelDataUsed> DATA_USED_COMPARATOR = new Comparator<ShapeModelDataUsed>() {

        @Override
        public int compare(ShapeModelDataUsed o1, ShapeModelDataUsed o2)
        {
            return o1.compareTo(o2);
        }

    };

    private static Comparator<BasicConfigInfo> ORDER_ADDED_COMPARATOR = null;

    private static final class OrderedComparator<T> implements Comparator<T>
    {
        public static <T> OrderedComparator<T> of(List<T> list)
        {
            Map<T, Integer> map = Maps.newHashMap();
            for (int index = 0; index != list.size(); ++index)
            {
                T item = list.get(index);
                if (map.containsKey(item)) throw new IllegalArgumentException("List cannot contain duplicates");
                map.put(list.get(index), index);
            }
            return new OrderedComparator<>(map);
        }
        private final Map<T, Integer> map;

        private OrderedComparator(Map<T, Integer> map)
        {
            this.map = map;
        }

        @Override
        public final int compare(T object1, T object2)
        {
            int result = 0;
            // If either object is not in the map -- return 0, i.e., the determination must be made by other means.
            if (map.containsKey(object1) && map.containsKey(object2))
            {
                return Integer.compare(map.get(object1), map.get(object2));
            }
            return result;
        }
    }

}
