package delta.games.lotro.extractors.achievables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.achievables.AchievableStatus;
import delta.games.lotro.character.status.achievables.AchievablesStatusManager;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.Achievable;

/**
 * @author dm
 */
public class DeedsStatusExtractor extends AchievablesStatusExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DeedsStatusExtractor.class);

  /**
   * Constructor.
   * @param statusMgr Status manager.
   */
  public DeedsStatusExtractor(AchievablesStatusManager statusMgr)
  {
    super(statusMgr);
  }

  @Override
  protected Achievable getAchievable(int achievableId)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    DeedDescription deed=deedsMgr.getDeed(achievableId);
    return deed;
  }

  @Override
  public boolean handleCompletedAchievable(int achievableId, ClassInstance questData)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    DeedDescription deed=deedsMgr.getDeed(achievableId);
    if (deed==null)
    {
      return false;
    }
    LOGGER.debug("Deed: {}",deed);
    AchievableStatus status=_statusMgr.get(deed,true);
    status.setCompleted(true);
    status.updateInternalState();
    return true;
  }
}
