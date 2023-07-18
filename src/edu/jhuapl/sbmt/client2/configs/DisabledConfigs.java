package edu.jhuapl.sbmt.client2.configs;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;

public class DisabledConfigs extends SmallBodyViewConfig
{

	public DisabledConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(ConfigArrayList configArray)
    {
        DisabledConfigs c = new DisabledConfigs();

        c = new DisabledConfigs();
        c.body = ShapeModelBody.BETULIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/BETULIA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "betulia.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/GEOGRAPHOS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1620geographos.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/BACCHUS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "2063bacchus.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/RASHALOM";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "rashalom.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/MITHRA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "Mithra.v1.PA.prograde.mod.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/NEREUS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "Nereus_alt1.mod.wf.gz");
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
        c.rootDirOnServer = "/HUDSON/CASTALIA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "4769castalia.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/GOLEVKA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "6489golevka.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/HW1";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1996hw1.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/SK";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "sk.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/1950DAPROGRADE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1950DA_ProgradeModel.wf.gz");
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
        c.rootDirOnServer = "/HUDSON/1950DARETROGRADE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1950DA_RetrogradeModel.wf.gz");
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
        c.rootDirOnServer = "/HUDSON/WT24";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "wt24.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/52760";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "52760.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/YORP";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "yorp.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/KW4A";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "kw4a.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/KW4B";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "kw4b.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/CCALPHA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1994CC_nominal.mod.wf.gz");
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
        c.rootDirOnServer = "/HUDSON/CE26";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "ce26.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/EV5";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "2008ev5.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/KY26";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "1998ky26.obj.gz");
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
        c.rootDirOnServer = "/CARRY/PALLAS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "pallas.obj.gz");
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
        c.rootDirOnServer = "/CARRY/DAPHNE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "daphne.obj.gz");
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
        c.rootDirOnServer = "/CARRY/HERMIONE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "hermione.obj.gz");
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
        c.rootDirOnServer = "/HUDSON/KLEOPATRA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "216kleopatra.obj.gz");
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
        c.rootDirOnServer = "/STOOKE/LARISSA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "n7larissa.llr.gz");
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
        c.rootDirOnServer = "/STOOKE/PROTEUS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "n8proteus.llr.gz");
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
}