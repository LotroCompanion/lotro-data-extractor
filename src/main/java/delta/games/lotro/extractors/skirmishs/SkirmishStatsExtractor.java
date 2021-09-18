package delta.games.lotro.extractors.skirmishs;

import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.status.skirmishes.SingleSkirmishStats;
import delta.games.lotro.character.status.skirmishes.SkirmishLevel;
import delta.games.lotro.character.status.skirmishes.SkirmishStats;
import delta.games.lotro.character.status.skirmishes.SkirmishStatsManager;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;

/**
 * Skirmish statistics extractor.
 * @author DAM
 */
public class SkirmishStatsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(SkirmishStatsExtractor.class);

  private SkirmishStatsManager _statsManager;

  /**
   * Constructor.
   * @param statsManager Stats manager.
   */
  public SkirmishStatsExtractor(SkirmishStatsManager statsManager)
  {
    _statsManager=statsManager;
  }

  /**
   * Extract deeds.
   * @param skirmishStatsMap Map of the skirmish stats.
   */
  public void extract(Map<Integer,PropertiesSet> skirmishStatsMap)
  {
    int size=skirmishStatsMap.size();
    LOGGER.debug("Nb entries: "+size);
    for(Map.Entry<Integer,PropertiesSet> entry : skirmishStatsMap.entrySet())
    {
      int skirmishID=entry.getKey().intValue();
      PropertiesSet skirmishProps=entry.getValue();
      handleSkirmishProps(skirmishID,skirmishProps);
    }
  }

  private void handleSkirmishProps(int skirmishID, PropertiesSet props)
  {
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    PrivateEncounter pe=peMgr.getPrivateEncounterById(skirmishID);
    if (pe instanceof SkirmishPrivateEncounter)
    {
      SkirmishPrivateEncounter skirmish=(SkirmishPrivateEncounter)pe;
      SingleSkirmishStats stats=new SingleSkirmishStats(skirmish);
      handleSkirmishProps(stats,props);
      _statsManager.add(stats);
    }
  }

  private void handleSkirmishProps(SingleSkirmishStats stats, PropertiesSet props)
  {
    LotroEnum<GroupSize> groupSizesMgr=LotroEnumsRegistry.getInstance().get(GroupSize.class);
    for(GroupSize size : groupSizesMgr.getAll())
    {
      for(SkirmishLevel level : SkirmishLevel.values())
      {
        SkirmishStats ss=handleSkirmishProps(size,level,props);
        stats.setStats(size,level,ss);
      }
    }
  }

  private SkirmishStats handleSkirmishProps(GroupSize size, SkirmishLevel level, PropertiesSet props)
  {
    SkirmishStats ret=new SkirmishStats();
    // Skirmishes attempted
    Integer attempts=(Integer)props.getProperty(getPropertyName("Attempts",size,level));
    ret.setSkirmishesAttempted((attempts!=null)?attempts.intValue():0);
    // Best time
    Float bestTime=(Float)props.getProperty(getPropertyName("Best_Time",size,level));
    ret.setBestTime((bestTime!=null)?bestTime.floatValue():0.0f);
    // Boss kills
    Integer bossKills=(Integer)props.getProperty(getPropertyName("Boss_Kills",size,level));
    ret.setBossKills((bossKills!=null)?bossKills.intValue():0);
    // Boss resets
    Integer bossResets=(Integer)props.getProperty(getPropertyName("Boss_Resets",size,level));
    ret.setBossResets((bossResets!=null)?bossResets.intValue():0);
    // Control Point Taken
    Integer cpTaken=(Integer)props.getProperty(getPropertyName("CP_Flips",size,level));
    ret.setControlPointsTaken((cpTaken!=null)?cpTaken.intValue():0);
    // Skirmishes completed
    Integer completed=(Integer)props.getProperty(getPropertyName("Completions",size,level));
    ret.setSkirmishesCompleted((completed!=null)?completed.intValue():0);
    // Defenders lost
    Integer defendersLost=(Integer)props.getProperty(getPropertyName("Defenders_Lost",size,level));
    ret.setDefendersLost((defendersLost!=null)?defendersLost.intValue():0);
    // Defenders saved
    Integer defendersSaved=(Integer)props.getProperty(getPropertyName("Defenders_Saved",size,level));
    ret.setDefendersSaved((defendersSaved!=null)?defendersSaved.intValue():0);
    // Lieutenant kills
    Integer lieutenantKills=(Integer)props.getProperty(getPropertyName("Lieutenant_Kills",size,level));
    ret.setLieutenantKills((lieutenantKills!=null)?lieutenantKills.intValue():0);
    // Monster kills
    Integer monsterKills=(Integer)props.getProperty(getPropertyName("Monster_Kills",size,level));
    ret.setMonsterKills((monsterKills!=null)?monsterKills.intValue():0);
    // Encounters completed
    Integer encountersCompleted=(Integer)props.getProperty(getPropertyName("Optionals_Completed",size,level));
    ret.setEncountersCompleted((encountersCompleted!=null)?encountersCompleted.intValue():0);
    // Unused: SM_Gained_BossKills, SP_Gained_CP, SP_Gained_Defenders, SP_Gained_Optionals, SP_Gained_Tokens
    Integer smGainedBossKills=(Integer)props.getProperty(getPropertyName("SM_Gained_BossKills",size,level));
    if ((smGainedBossKills!=null) && (smGainedBossKills.intValue()!=0))
    {
      LOGGER.warn("Unused SM_Gained_BossKills: "+smGainedBossKills);
    }
    Integer SP_Gained_CP=(Integer)props.getProperty(getPropertyName("SP_Gained_CP",size,level));
    if ((SP_Gained_CP!=null) && (SP_Gained_CP.intValue()!=0))
    {
      LOGGER.warn("Unused SP_Gained_CP: "+SP_Gained_CP);
    }
    Integer SP_Gained_Defenders=(Integer)props.getProperty(getPropertyName("SP_Gained_Defenders",size,level));
    if ((SP_Gained_Defenders!=null) && (SP_Gained_Defenders.intValue()!=0))
    {
      LOGGER.warn("Unused SP_Gained_Defenders: "+SP_Gained_Defenders);
    }
    Integer SP_Gained_Optionals=(Integer)props.getProperty(getPropertyName("SP_Gained_Optionals",size,level));
    if ((SP_Gained_Optionals!=null) && (SP_Gained_Optionals.intValue()!=0))
    {
      LOGGER.warn("Unused SP_Gained_Optionals: "+SP_Gained_Optionals);
    }
    Integer SP_Gained_Tokens=(Integer)props.getProperty(getPropertyName("SP_Gained_Tokens",size,level));
    if ((SP_Gained_Tokens!=null) && (SP_Gained_Tokens.intValue()!=0))
    {
      LOGGER.warn("Unused SP_Gained_Tokens: "+SP_Gained_Tokens);
    }
    // Marks earned
    Integer marksEarned=(Integer)props.getProperty(getPropertyName("SP_Gained_Other",size,level));
    ret.setTotalMarksEarned((marksEarned!=null)?marksEarned.intValue():0);
    // Played time
    Float playedTime=(Float)props.getProperty(getPropertyName("Skirmish_Played_Time",size,level));
    ret.setPlayTime((playedTime!=null)?playedTime.floatValue():0.0f);
    // Soldier deaths
    Integer soldierDeaths=(Integer)props.getProperty(getPropertyName("Soldier_Deaths",size,level));
    ret.setSoldiersDeaths((soldierDeaths!=null)?soldierDeaths.intValue():0);
    return ret;
  }

  private String getPropertyName(String seed, GroupSize size, SkirmishLevel level)
  {
    String sizeKey=getSizeKey(size);
    String levelKey=getLevelKey(level);
    return "SkirmishStats_"+seed+"_"+sizeKey+"_"+levelKey;
  }

  private String getSizeKey(GroupSize size)
  {
    String key=size.getKey();
    if ("SOLO".equals(key)) return "Solo";
    if ("DUO".equals(key)) return "Duo";
    if ("SMALL_FELLOWSHIP".equals(key)) return "3man";
    if ("FELLOWSHIP".equals(key)) return "6man";
    if ("RAID12".equals(key)) return "12man";
    if ("RAID24".equals(key)) return "24man";
    return null;
  }

  private String getLevelKey(SkirmishLevel level)
  {
    if (level==SkirmishLevel.ON_LEVEL) return "OnLevel";
    if (level==SkirmishLevel.OFF_LEVEL) return "OffLevel";
    return null;
  }
}
