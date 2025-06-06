package delta.games.lotro.extractors.allegiances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.allegiances.AllegianceStatus;
import delta.games.lotro.character.status.allegiances.AllegiancesStatusManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.allegiances.AllegianceDescription;
import delta.games.lotro.lore.allegiances.AllegiancesManager;
import delta.games.lotro.lore.allegiances.Points2LevelCurve;

/**
 * Allegiances status extractor.
 * @author DAM
 */
public class AllegiancesExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(AllegiancesExtractor.class);

  private AllegiancesStatusManager _allegiancesStatusMgr;

  /**
   * Constructor.
   * @param allegiancesStatusMgr Allegiances status manager.
   */
  public AllegiancesExtractor(AllegiancesStatusManager allegiancesStatusMgr)
  {
    _allegiancesStatusMgr=allegiancesStatusMgr;
  }

  /**
   * Extract allegiances status.
   * @param props Character properties.
   */
  public void extract(PropertiesSet props)
  {
    _allegiancesStatusMgr.clear();
    AllegiancesManager mgr=AllegiancesManager.getInstance();
    // Current allegiance
    Integer currentAllegianceID=(Integer)props.getProperty("Allegiance_Active");
    if (currentAllegianceID!=null)
    {
      AllegianceDescription currentAllegiance=mgr.getAllegiance(currentAllegianceID.intValue());
      _allegiancesStatusMgr.setCurrentAllegiance(currentAllegiance);
    }
    Object[] memberArray=(Object[])props.getProperty("Allegiance_Member_Array");
    if (memberArray!=null)
    {
      for(Object memberObj : memberArray)
      {
        PropertiesSet memberProps=(PropertiesSet)memberObj;
        Integer allegianceID=(Integer)memberProps.getProperty("Allegiance_Type_Entry");
        if (allegianceID==null)
        {
          LOGGER.warn("Unknown allegiance: {}",allegianceID);
          continue;
        }
        AllegianceDescription allegiance=mgr.getAllegiance(allegianceID.intValue());
        AllegianceStatus status=_allegiancesStatusMgr.get(allegiance,true);
        status.setStarted(true);
        Integer curveID=(Integer)memberProps.getProperty("Allegiance_Advancement_Progression_Entry");
        if (curveID!=null)
        {
          Points2LevelCurve curve=mgr.getCurvesManager().getCurve(curveID.intValue());
          status.setPoints2LevelCurve(curve);
        }
        Long points=(Long)memberProps.getProperty("Allegiance_Points_Earned");
        status.setPointsEarned((points!=null)?points.intValue():0);
        Long flags=(Long)memberProps.getProperty("Allegiance_Reward_Flags");
        status.setClaimedRewardsFlags((flags!=null)?flags.intValue():0);
      }
    }
  }
}
