package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jcodec.common.Preconditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;

/**
 * General builder for {@link SmallBodyViewConfig} instances, with methods to
 * help organize and enforce self-consistency between the many fields. This
 * builder is meant to be reusable; immediately after calling the
 * {@link #build()} method, the builder is reset to its initial state, so that
 * it may be used to tailor/build another independent config.
 * <p>
 * This class may be used as-is, or it may be extended. If extending, it may be
 * useful to override the methods {@link #init()} and/or {@link #createConfig()}
 * in order to establish a new baseline state for the builder and in turn for
 * the configs it builds.
 * <p>
 * This class is designed to use lazy initialization; the constructor
 * instantiates fields but the {@link #init()} method does the rest of the
 * initialization process. It is required that every public method call
 * {@link #init()} before executing further; this behavior must be preserved if
 * extending the class.
 * <p>
 * This class "cheats" the builder pattern in the sense that it instantiates the
 * config right when the builder is initialized and then changes its state,
 * rather than instantiating the config just before returning it.
 * <p>
 * Not thread-safe.
 *
 * @author James Peachey
 *
 */
public class SmallBodyViewConfigBuilder
{
    public static final ImmutableList<String> SpcResolutionLabels5Levels = ImmutableList.of( //
            "Very Low (12288 plates)", //
            "Low (49152 plates)", //
            "Medium (196608 plates)", //
            "High (786432 plates)", //
            "Very High (3145728 plates)" //
    );

    public static final ImmutableList<Integer> SpcResolutionFacets5Levels = ImmutableList.of( //
            12288, //
            49152, //
            196608, //
            786432, //
            3145728 //
    );

    /**
     * (Empty) array containing no missions (clients).
     */
    protected static final Mission[] NoMissions = {};

    /**
     * Array containing only proprietary (APL) missions (clients).
     */
    protected static final Mission[] ProprietaryMissions = { //
            Mission.APL_INTERNAL, //
            Mission.APL_INTERNAL_NIGHTLY, //
            Mission.STAGE_APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
    };

    /**
     * Array containing public as well as proprietary (APL) missions (clients).
     */
    protected static final Mission[] PublicMissions = { //
            Mission.PUBLIC_RELEASE, //
            Mission.STAGE_PUBLIC_RELEASE, //
            Mission.TEST_PUBLIC_RELEASE, //
            Mission.APL_INTERNAL, //
            Mission.APL_INTERNAL_NIGHTLY, //
            Mission.STAGE_APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
    };

    private final AtomicReference<SmallBodyViewConfig> c;
    protected final List<ImagingInstrument> imagingInstruments;
    protected final List<DBRunInfo> dbRunInfos;
    private final AtomicBoolean midInit;
    private final AtomicBoolean initDone;
    private final AtomicBoolean dtmCatalogs;

    /**
     * Constructs a builder, but does not completely initialize it. See
     * {@link #init()} for a description of how the builder is (lazily)
     * initialized.
     * <p>
     * If extending this class, override the {@link #reset()} method as needed
     * to ensure the state of the builder at the end of that method is the same
     * as the newly-constructed state.
     */
    public SmallBodyViewConfigBuilder()
    {
        super();

        this.c = new AtomicReference<>();
        this.imagingInstruments = new ArrayList<>();
        this.dbRunInfos = new ArrayList<>();
        this.midInit = new AtomicBoolean(false);
        this.initDone = new AtomicBoolean(false);
        this.dtmCatalogs = new AtomicBoolean(false);
    }

    /**
	 * Set the required body-related fields in the config being built. Use this
	 * variant for single-body systems. Use
	 * {@link #body(ShapeModelBody, BodyType, ShapeModelPopulation, ShapeModelBody)}
	 * for bodies that are part of a multi-body system, whether or not the body
	 * is the main body of the system.
	 * <p>
	 * This method (or the other variant) must be called prior to calling
	 * {@link #build()}. The caller must ensure the parameters are
	 * self-consistent.
	 *
	 * @param body the body, e.g., Eros
	 * @param bodyType the body type, i.e., asteroid or comet
	 * @param population the nature of the body's location, e.g. NEO
	 * @return this builder
	 */
    public SmallBodyViewConfigBuilder body(ShapeModelBody body, BodyType bodyType, ShapeModelPopulation population)
    {
        return body(body, bodyType, population, null);
    }

    /**
     * Set the required body-related fields in the config being built. Use this
     * variant for bodies that are part of a multi-body system. Use
     * {@link #body(ShapeModelBody, BodyType, ShapeModelPopulation)} for
     * single-body systems, (or for the main body of a multi-body system).
     * <p>
     * This method (or the other variant) must be called prior to calling
     * {@link #build()}
     *
     * @param body the body, e.g., Eros
     * @param bodyType the body type, i.e., asteroid or comet
     * @param population the nature of the body's location, e.g. NEO
     * @param system main body in a multi-body system
     * @return this builder
     */
    public SmallBodyViewConfigBuilder body(ShapeModelBody body, BodyType bodyType, ShapeModelPopulation population, ShapeModelBody system)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.body = body;
        c.type = bodyType;
        c.population = population;
        c.system = system;

        return this;
    }

    /**
     * Set the required model-related fields in the config being built. This
     * method must be called prior to calling {@link #build()}.
     *
     * @param author the shape model type (author) of the model, used to
     *            distinguish models of the same body
     * @param dataUsed the basis for the model, used mainly to organize the view
     *            menu
     * @param label the label for the model used in the view menu
     * @return this builder
     */
    public SmallBodyViewConfigBuilder model(ShapeModelType author, ShapeModelDataUsed dataUsed, String label)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.author = author;
        c.dataUsed = dataUsed;
        c.modelLabel = label;

        return this;
    }

    /**
     * Set the resolution levels for the config being built. This method is
     * optional; see the {@link #init()} for more information. This variant
     * derives the labels of the resolution levels from the number of facets.
     * For example, the label corresponding to a level of 12345 would be "12345
     * plates". Use {@link #modelRes(Iterable, Iterable)} to specify custom
     * labels for each level.
     *
     * @param resolutionNumberFacets list of the number of facets in each
     *            resolution
     * @return this builder
     */
    public SmallBodyViewConfigBuilder modelRes(Iterable<Integer> resolutionNumberFacets)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.setResolution(resolutionNumberFacets);

        return this;
    }

    /**
     * Set the resolution levels expected for the config being built. This
     * method is optional; see {@link #init()} for more information. This
     * variant requires the caller to specify custom labels for each resolution
     * level. Use {@link #modelRes(Iterable)} instead to derive the labels from
     * the resolution levels.
     *
     * @param resolutionLabels list of the labels for each resolution
     * @param resolutionNumberFacets list of the number of facets in each
     *            resolution
     * @return this builder
     */
    public SmallBodyViewConfigBuilder modelRes(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberFacets)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.setResolution(resolutionLabels, resolutionNumberFacets);

        return this;
    }

    public SmallBodyViewConfigBuilder modelRes(String[] resolutionLabels, Integer[] resolutionNumberFacets)
    {
        return modelRes(ImmutableList.copyOf(resolutionLabels), ImmutableList.copyOf(resolutionNumberFacets));
    }

    /**
     * Set the extension used by all shape model files associated with the
     * config being built. This method is optional; see {@link #init()} method
     * for more information.
     *
     * @param ext the shape model file extension
     * @return this builder
     */
    public SmallBodyViewConfigBuilder shapeFileExt(String ext)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.setShapeModelFileExtension(ext);

        return this;
    }

    /**
     * Set the missions/clients in which the model associated with the config
     * being built should appear. These are the clients in which the model
     * should appear. The model will not be the default model for any clients.
     * To make a model the default model for one or more clients, call the other
     * variant {@link #clients(Mission[], Mission[])}. This method is optional;
     * see {@link #init()} for more information.
     *
     * @param clients the clients in which this model should appear
     * @return this builder
     */
    public SmallBodyViewConfigBuilder clients(Mission[] clients)
    {
        return clients(clients, NoMissions);
    }

    /**
     * Set the missions/clients in which the model associated with the config
     * being built should appear. The model will also be set as the default
     * model for the clients specified by the second parameter. It is the
     * caller's responsibility to ensure that every client specified in the
     * second array is also present in the first array. This method is optional;
     * see {@link #init()} for more information.
     *
     * @param clients the clients in which this model should appear
     * @param defaultForClients the clients for which this model should be the
     *            default model
     * @return this builder
     */
    public SmallBodyViewConfigBuilder clients(Mission[] clients, Mission[] defaultForClients)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.presentInMissions = clients;
        c.defaultForMissions = defaultForClients;

        return this;
    }

    /**
     * Set the top directory of the model associated with the config being built
     * relative to the top of the server. All files specific to the model are
     * organized under this top-level directory. This method is optional; see
     * the {@link #build()} method for more information.
     * <p>
     * This method ensures the model top directory set in the config begins with
     * a single slash, as required in a config.
     *
     * @param modelTopDir
     * @return this builder
     */
    public SmallBodyViewConfigBuilder modelTopDir(String modelTopDir)
    {
        init();
        SmallBodyViewConfig c = getConfig();

        c.rootDirOnServer = modelTopDir.replaceFirst("^/*", "/");

        return this;
    }

    /**
     * Add an imaging instrument with associated database generation information
     * to the config being built. This method is optional. If it is not called,
     * the model associated with the built config will not have any images. This
     * method may be called multiple times to add multiple imaging instruments.
     * <p>
     * If calling this method one or more times, also call
     * {@link #imageSearchRanges(Date, Date, double, double)} one time. The same
     * ranges will be used for all instruments.
     *
     * @param instrument the instrument
     * @param dbRunInfos the database generation info
     * @return this builder
     */
    public SmallBodyViewConfigBuilder add(ImagingInstrument instrument, DBRunInfo... dbRunInfos)
    {
        init();
        imagingInstruments.add(instrument);

        for (DBRunInfo info : dbRunInfos)
        {
            this.dbRunInfos.add(info);
        }
        return this;
    }

    /**
     * Set range information used for filtering images to the config being
     * built. This method is optional, but should be called once if
     * {@link #add(ImagingInstrument, DBRunInfo...)} is called.
     *
     * @param startDate a date/time before the first expected image time stamp
     * @param endDate a date/time after the last expected image time stamp
     * @param maxScDistance the maximum distance from the spacecraft to the body
     *            in km
     * @param maxResolution the maximum pixel resolution in km
     * @return the builder
     */
    public SmallBodyViewConfigBuilder imageSearchRanges(Date startDate, Date endDate, double maxScDistance, double maxResolution)
    {
    	init();
        SmallBodyViewConfig c = getConfig();
        c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imageSearchDefaultStartDate = startDate;
        imagingConfig.imageSearchDefaultEndDate = endDate;
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = maxScDistance;
        imagingConfig.imageSearchDefaultMaxResolution = maxResolution;
        return this;
    }

    /**
     * Enable/disable DTM catalogs for the model. Disabled by default.
     *
     * @param enable if true, enable DTM catalogs, otherwise don't.
     * @return the builder
     */
    public SmallBodyViewConfigBuilder dtmCatalogs(boolean enable)
    {
        dtmCatalogs.set(enable);
        return this;
    }

    /**
     * Set the parameters used to compute gravitation-related plate colorings.
     *
     * @param density the body's density
     * @param rotationRate the body's rotation rate
     * @return the builder
     */
    public SmallBodyViewConfigBuilder gravityInputs(double density, double rotationRate)
    {
        init();
        SmallBodyViewConfig c = getConfig();
        c.density = density;
        c.rotationRate = rotationRate;
        return this;
    }

    /**
     * Build and return the {@link SmallBodyViewConfig}. The base implementation
     * retrieves the partially-constructed config in whatever state has resulted
     * from calling other builder methods, confirms that required methods have
     * been called, tweaks any fields that have not been completely initialized
     * and returns the config. Prior to return, this method calls the
     * {@link #reset()} method so that the builder is ready to be used again to
     * build another independent config.
     * <p>
     * The base implementation {@link SmallBodyViewConfigBuilder} makes the
     * following changes to the config before returning it:
     * <ol>
     * <li>If the config's rootDirOnServer field is not yet set, it will be set
     * to "/&lt;body-name&gt;/&lt;model-name&gt;" where &lt;body-name&gt; is
     * derived from the config's shape model body field and &lt;model-name&gt;
     * is derived from the config's shape model type (author) field. The
     * directory is all lowercase, and all special characters are replaced with
     * dashes in order to comply with server-side requirements.
     * <li>The imaging instruments added by the
     * {@link #add(ImagingInstrument, DBRunInfo...)} method are used to populate
     * the config's imaging instruments and database generation info objects.
     * </ol>
     * The methods {@link #build()}, {@link #init()}, {@link #reset()}, and
     * {@link #createConfig()} work together a specific way. If overriding any
     * of them, review all of them and override others as needed.
     *
     * @return the fully-built config
     */
    public SmallBodyViewConfig build()
    {
        init();
        SmallBodyViewConfig c = getConfig();
        Preconditions.checkState(c.body != null, "Must call one of the body(...) methods before calling build");
        Preconditions.checkState(c.author != null, "Must call the model(...) method before calling build");

        ImagingInstrumentConfig imagingConfig = null;
        if (!imagingInstruments.isEmpty())
        {
            imagingConfig = (ImagingInstrumentConfig) c.getConfigForClass(ImagingInstrumentConfig.class);
            if (imagingConfig == null)
            {
                imagingConfig = new ImagingInstrumentConfig(c);
                c.addFeatureConfig(ImagingInstrumentConfig.class, imagingConfig);
                FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig(c);
            }

            Preconditions.checkState(imagingConfig.imageSearchDefaultStartDate != null, //
                    "Must set ALL imaging parameters (start date etc.) befre calling build");
        }

        if (c.rootDirOnServer == null)
        {
            modelTopDir(bodyId(c.body) + "/" + modelId(c.author));
        }

        if (c.presentInMissions == null)
        {
            // c.presentInMissions =
        }
        if (!imagingInstruments.isEmpty())
        {
        	imagingConfig.imagingInstruments = Lists.newArrayList(imagingInstruments);
        }

        if (!dbRunInfos.isEmpty())
        {
            c.databaseRunInfos = dbRunInfos.toArray(new DBRunInfo[dbRunInfos.size()]);
        }

        reset();

        return c;
    }

    /**
     * This method ensures that the builder has been initialized and is
     * ready-to-use. Each public method *must* call this method before
     * proceding.
     * <p>
     * This method supports lazy one-time initialization, or reinitialization if
     * called after {@link #reset()}. Preserve this behavior whenever
     * overriding.
     * <p>
     * This method is final because it is designed to guard against infinite
     * recursion. The actual initialization is delegated to the
     * {@link #doInit()} method, which may be overridden. However, also consider
     * overriding {@link #createConfig()} instead if changing some initial
     * default fields of the config is all that is necessary.
     * <p>
     * The methods {@link #build()}, {@link #init()}, {@link #reset()}, and
     * {@link #createConfig()} work together a specific way. If overriding any
     * of them, review all of them and override others as needed.
     */
    protected final void init()
    {
        if (midInit.compareAndSet(false, true))
        {
            try
            {
                if (initDone.compareAndSet(false, true))
                {
                    doInit();
                }
            }
            finally
            {
                midInit.compareAndSet(true, false);
            }
        }
    }

    /**
     * Complete initializations that must be done prior to using the builder.
     * The base implementation only creates and caches a config. Override this
     * as needed to perform additional steps after the config is created.
     * <p>
     * Note that, when overriding this method, it is safe to call public methods
     * of this class because {@link #init()} will not recursively call
     * {@link #doInit()}.
     */
    protected void doInit()
    {
        SmallBodyViewConfig c = createConfig();
        setConfig(c);
    }

    /**
     * Create a {@link SmallBodyViewConfig} instance and place it in an initial
     * state suitable for modification by other builder methods.
     * <p>
     * The config created by the base implementation
     * {@link SmallBodyViewConfigBuilder} has the following characteristics:
     * <ol>
     * <li>It assumes the model has 5 standard SPC resolution levels ranging
     * from Very Low to Very High.
     * <li>It assumes shape model files are provided as OBJ files (with file
     * extension ".obj").
     * <li>It is present in all the {@link #PublicMissions}. It is the default
     * for {@link #NoMissions}.
     * <li>It does not support cloning, i.e., its clone() method throws
     * UnsupportedOperationException.
     * </ol>
     * Note that the {@link #build()} method makes other changes that can only
     * be made after other required builder methods have been called.
     * <p>
     * The methods {@link #build()}, {@link #init()}, {@link #reset()}, and
     * {@link #createConfig()} work together a specific way. If overriding any
     * of them, review all of them and override others as needed.
     *
     * @return the config
     */
    protected SmallBodyViewConfig createConfig()
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(SpcResolutionLabels5Levels, SpcResolutionFacets5Levels) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };
        c.setShapeModelFileExtension(".obj");
        c.presentInMissions = PublicMissions;
        c.defaultForMissions = NoMissions;

        return c;
    }

    /**
     * Place the builder in the same state it was in when it was first
     * constructed. This "pre-init" half-constructed state is nonetheless ready
     * to be reused, because the {@link #init()} method will be called at the
     * beginning of every public method.
     * <p>
     * The methods {@link #build()}, {@link #init()}, {@link #reset()}, and
     * {@link #createConfig()} work together a specific way. If overriding any
     * of them, review all of them and override others as needed.
     */
    protected void reset()
    {
        setConfig(null);
        imagingInstruments.clear();
        dbRunInfos.clear();
        initDone.set(false);
    }

    /**
     * This low-level method returns the currently-cached config instance as-is.
     * It does not create a new config.
     *
     * @return the config
     */
    protected SmallBodyViewConfig getConfig()
    {
        return c.get();
    }

    /**
     * This low-level method sets the currently-cached config instance.
     *
     * @param c the config
     */
    protected void setConfig(SmallBodyViewConfig c)
    {
        this.c.set(c);
    }

    /**
     * Helper method for creating {@link DBRunInfo} instances in a standard way
     * based on the specified parameters.
     *
     * @param body the body
     * @param author the model type (author) of the model
     * @param instrument instrument identifier
     * @param sources (pointing types)
     * @return array of run-info objects
     */
    protected DBRunInfo[] createDbInfos(ShapeModelBody body, ShapeModelType author, Instrument instrument, PointingSource... sources)
    {
        String tablePrefix = dbTablePrefix(body, author, instrument);

        String lcInstrumentName = instrument.name().toLowerCase();

        String modelImageDir = modelTopDir(body, author) + "/" + lcInstrumentName;

        return createDbInfos(instrument, body, tablePrefix, modelImageDir, sources);
    }

    protected DBRunInfo[] createDbInfos(Instrument instrument, ShapeModelBody body, String tablePrefix, String modelImageDir, PointingSource... sources)
    {
        List<DBRunInfo> runInfos = new ArrayList<>();

        for (PointingSource source : sources)
        {
            String imageListFile = modelImageDir + "/imagelist-fullpath-" + getPointingFileSuffix(source) + ".txt";
            runInfos.add(new DBRunInfo(source, instrument, body.toString(), imageListFile, tablePrefix));
        }

        return runInfos.toArray(new DBRunInfo[runInfos.size()]);
    }

    protected ShapeModelType author(String label)
    {
        // ShapeModelType rules: no spaces (replace with underscores). Mixed
        // case, underscores and dashes are all OK. Includes a DART-specific
        // hack to remove one dash that was not present in the early models.
        return ShapeModelType.provide(label.replaceAll("\\s+", "-").toLowerCase().replace("impact-", "impact"));
    }

    protected String bodyId(ShapeModelBody body)
    {
        // Body identifier string rules: lowercase, no spaces nor underscores.
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        return body.name().replaceAll("[\\s-_]+", "-").toLowerCase();
    }

    protected String modelId(ShapeModelType author)
    {
        // Model identifier string rules: lowercase, no spaces nor underscores
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        return author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
    }

    protected String dbTablePrefix(ShapeModelBody body, ShapeModelType author, Instrument instrument)
    {
        String modelId = modelId(author);
        String bodyId = bodyId(body);
        String lcInstrumentName = instrument.name().toLowerCase();

        // Database table rules: lowercase, no dashes (replace with
        // underscores). Underscores are OK.
        String tableBaseName = (bodyId + "_" + modelId + "_").replaceAll("-", "_").toLowerCase();

        return tableBaseName + lcInstrumentName;
    }

    protected String modelTopDir(ShapeModelBody body, ShapeModelType author)
    {
        return "/" + bodyId(body) + "/" + modelId(author);
    }

    protected String getPointingFileSuffix(PointingSource source)
    {
        String suffix;
        switch (source)
        {
        case GASKELL:
        case CORRECTED:
        case GASKELL_UPDATED:
            suffix = "sum";
            break;
        case SPICE:
        case CORRECTED_SPICE:
            suffix = "info";
            break;
        default:
            // Won't happen.
            throw new IllegalArgumentException("No pointing file suffix for source " + source);
        }

        return suffix;
    }

}
