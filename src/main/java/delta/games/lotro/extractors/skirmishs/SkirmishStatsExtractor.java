package delta.games.lotro.extractors.skirmishs;

import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.status.skirmishes.SingleSkirmishStats;
import delta.games.lotro.character.status.skirmishes.SkirmishLevel;
import delta.games.lotro.character.status.skirmishes.SkirmishStats;
import delta.games.lotro.character.status.skirmishes.SkirmishStatsManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishGroupSize;
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
    for(SkirmishGroupSize size : SkirmishGroupSize.values())
    {
      for(SkirmishLevel level : SkirmishLevel.values())
      {
        SkirmishStats ss=handleSkirmishProps(size,level,props);
        stats.setStats(size,level,ss);
      }
    }
  }

  private SkirmishStats handleSkirmishProps(SkirmishGroupSize size, SkirmishLevel level, PropertiesSet props)
  {
    SkirmishStats ret=new SkirmishStats();
    // Skirmishes attempted
    Integer attempts=(Integer)props.getProperty(getPropertyName("Attempts",size,level));
    ret.setSkirmishesAttempted((attempts!=null)?attempts.intValue():0);
    // Best time
    Float bestTime=(Float)props.getProperty(getPropertyName("Best_Time",size,level));
    ret.setBestTime((bestTime!=null)?bestTime.floatValue():0.0f);
    // Unused: Boss_Kills, Boss_Resets
    // Control Point Taken
    Integer cpTaken=(Integer)props.getProperty(getPropertyName("CP_Flips",size,level));
    ret.setControlPointsTaken((cpTaken!=null)?cpTaken.intValue():0);
    // Skirmishes completed
    Integer completed=(Integer)props.getProperty(getPropertyName("Completions",size,level));
    ret.setSkirmishesCompleted((completed!=null)?completed.intValue():0);
    // Unused: Defenders_Lost, Defenders_Saved
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
    // Marks earned
    Integer marksEarned=(Integer)props.getProperty(getPropertyName("SP_Gained_Other",size,level));
    ret.setTotalMarksEarned((marksEarned!=null)?marksEarned.intValue():0);
    // Played time
    Float playedTime=(Float)props.getProperty(getPropertyName("Skirmish_Played_Time",size,level));
    ret.setPlayTime((playedTime!=null)?playedTime.floatValue():0.0f);
    // Unused: Soldier_Deaths
    return ret;
  }

  private String getPropertyName(String seed, SkirmishGroupSize size, SkirmishLevel level)
  {
    String sizeKey=getSizeKey(size);
    String levelKey=getLevelKey(level);
    return "SkirmishStats_"+seed+"_"+sizeKey+"_"+levelKey;
  }

  private String getSizeKey(SkirmishGroupSize size)
  {
    if (size==SkirmishGroupSize.SOLO) return "Solo";
    if (size==SkirmishGroupSize.DUO) return "Duo";
    if (size==SkirmishGroupSize.SMALL_FELLOWSHIP) return "3man";
    if (size==SkirmishGroupSize.FELLOWSHIP) return "6man";
    if (size==SkirmishGroupSize.RAID12) return "12man";
    if (size==SkirmishGroupSize.RAID24) return "24man";
    return null;
  }

  private String getLevelKey(SkirmishLevel level)
  {
    if (level==SkirmishLevel.ON_LEVEL) return "OnLevel";
    if (level==SkirmishLevel.OFF_LEVEL) return "OffLevel";
    return null;
  }
}
