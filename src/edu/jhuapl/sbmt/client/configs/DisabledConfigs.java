package edu.jhuapl.sbmt.client.configs;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.Instrument;

public class DisabledConfigs extends SmallBodyViewConfig
{

	public DisabledConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(List<ViewConfig> configArray)
    {
        DisabledConfigs c = new DisabledConfigs();

        c = new DisabledConfigs();
        c.body = ShapeModelBody.BETULIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/BETULIA/betulia.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.GEOGRAPHOS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.BACCHUS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.RASHALOM;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/RASHALOM/rashalom.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);



//       c = new DisabledConfigs();
//       c.body = ShapeModelBody.TOUTATIS;
//       c.type = BodyType.ASTEROID;
//       c.population = ShapeModelPopulation.NEO;
//       c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
//       c.author = ShapeModelType.HUDSON;
//       c.modelLabel = "Hudson et al. (2004)";
//       c.rootDirOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
//       c.version = "High resolution";
//       c.setResolution(ImmutableList.of(39996));
//       configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.MITHRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/MITHRA/Mithra.v1.PA.prograde.mod.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.NEREUS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/NEREUS/Nereus_alt1.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.CASTALIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.GOLEVKA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.HW1;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/HW1/1996hw1.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.SK;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/SK/sk.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody._1950DAPROGRADE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DAPROGRADE/1950DA_ProgradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody._1950DARETROGRADE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DARETROGRADE/1950DA_RetrogradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.WT24;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/WT24/wt24.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody._52760_1998_ML14;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/52760/52760.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.YORP;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/YORP/yorp.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.KW4A;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4A/kw4a.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.KW4B;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4B/kw4b.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.CCALPHA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/CCALPHA/1994CC_nominal.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.CE26;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/CE26/ce26.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.EV5;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/EV5/2008ev5.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.KY26;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in this block so that Eclipse updates will continue to keep this code
        // intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.PALLAS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/PALLAS/pallas.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out
        // anything else in this block so that Eclipse updates will continue
        // to keep this code intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.DAPHNE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/DAPHNE/daphne.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in
        // this block so that Eclipse updates will continue to keep this code intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.HERMIONE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/HERMIONE/hermione.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in
        // this block so that Eclipse updates will continue to keep this code intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.KLEOPATRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in
        // this block so that Eclipse updates will continue to keep this code intact.
        configArray.add(c);


        c = new DisabledConfigs();
        c.body = ShapeModelBody.LARISSA;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        // 2017-12-20: this name will be correct when "the new model" has been brought
        // in.
        // c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in
        // this block so that Eclipse updates will continue to keep this code intact.
        configArray.add(c);

        c = new DisabledConfigs();
        c.body = ShapeModelBody.PROTEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        // 2017-12-20: this name will be correct when "the new model" has been brought
        // in.
        // c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything
        // else in
        // this block so that Eclipse updates will continue to keep this code intact.
        configArray.add(c);
    }

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }

    @Override
    public Instrument getLidarInstrument()
    {
        // TODO Auto-generated method stub
        return lidarInstrumentName;
    }

    public boolean hasHypertreeLidarSearch()
    {
        return hasHypertreeBasedLidarSearch;
    }

    public SpectraHierarchicalSearchSpecification<?> getHierarchicalSpectraSearchSpecification()
    {
        return hierarchicalSpectraSearchSpecification;
    }
}