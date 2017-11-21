package edu.jhuapl.sbmt.client;

import java.awt.Frame;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;

public class SbmtViewManager extends ViewManager
{

    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    @Override
    protected void addBuiltInView(View view)
    {
        boolean superVersion = false;
        if (superVersion)
        {
            super.addBuiltInView(view);
            return;
        }

        List<View> builtInViews = getBuiltInViews();

        ViewConfig config = view.getConfig();
        if (!config.isEnabled()) return;
        ViewComparator.isInOrder(config);

        SortedSet<View> viewSet = Sets.newTreeSet(new ViewComparator());
        viewSet.addAll(builtInViews);
        viewSet.add(view);
        builtInViews.clear();
        for (View each: viewSet)
        {
            builtInViews.add(each);
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

    private static class ViewComparator implements Comparator<View> {

        private static final ImmutableMap<String, Integer> MODEL_LOOKUP = mapModels();

        @Override
        public int compare(View view1, View view2) {
            int result = 0;
            ViewConfig config1 = view1.getConfig();
            ViewConfig config2 = view2.getConfig();
            String name1 = config1.getShapeModelName();
            String name2 = config2.getShapeModelName();

            if (name1.equals(name2) && config1.author.equals(config2.author) && config1 != config2)
                throw new AssertionError("Configuration duplicated: " + name1 + "/" + config1.author);

            if (result == 0 && MODEL_LOOKUP.containsKey(name1) && MODEL_LOOKUP.containsKey(name2))
                result = Integer.compare(MODEL_LOOKUP.get(name1), MODEL_LOOKUP.get(name2));
            if (result == 0)
                result = name1.compareTo(name2);

            if (result == 0 && config1 instanceof BodyViewConfig && config2 instanceof BodyViewConfig)
            {
                BodyViewConfig body1 = (BodyViewConfig) config1;
                BodyViewConfig body2 = (BodyViewConfig) config2;
                result = body1.dataUsed.compareTo(body2.dataUsed);
            }

            if (result == 0 && name1.equals(name2))
                result = config1.author.compareTo(config2.author);
            return result;
        }

        private static void isInOrder(ViewConfig config) {
            if (!MODEL_LOOKUP.containsKey(config.getShapeModelName())) {
                System.err.println("Don't know how to order model " + config.getShapeModelName());
            }
        }

        private static ImmutableMap<String, Integer> mapModels()
        {
            ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
            // This order was based on an email from Terik Daly to James Peachey on 2017-11-14,
            // which included this information in a word document titled Order-of-Objects.docx,
            // as described in Redmine issue #1009. One difference is that Carolyn Ernst asked
            // that Near Earth come before Main Belt asteroids.
            String[] modelOrder = new String[] {
                    ////////////////////////////////////////
                    // Asteroids
                    ////////////////////////////////////////
                    // Near Earth
                    "433 Eros",
                    "25143 Itokawa",
                    "101955 Bennu",
                    "101955 Bennu (V3 Image)",
                    "101955 Bennu (V4 Image)",
                    "162173 Ryugu",
                    "1580 Betulia",
                    "1620 Geographos",
                    "1998 KY26",
                    "2063 Bacchus",
                    "2100 Ra-Shalom",
                    "4179 Toutatis (High Res)",
                    "4179 Toutatis (Low Res)",
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
                    "1 Ceres",
                    "4 Vesta",
                    "21 Lutetia",
                    "243 Ida",
                    "253 Mathilde",
                    "951 Gaspra",
                    "2867 Steins",
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
            for (int ii = 0; ii < modelOrder.length; ++ii)
            {
                builder.put(modelOrder[ii], ii);
            }
            return builder.build();
        }
    }
}
