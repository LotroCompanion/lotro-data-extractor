package delta.games.lotro.extractors.achievables;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.achievables.AchievableStatus;
import delta.games.lotro.character.status.achievables.AchievablesStatusManager;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.quests.Achievable;

/**
 * Base class for achievables (deeds/quests) status extractors.
 * @author DAM
 */
public abstract class AchievablesStatusExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(AchievablesStatusExtractor.class);

  private static final String NB_ENTRIES="Nb entries: {}";

  private AchievableStatusBuilder _builder;
  protected AchievablesStatusManager _statusMgr;

  /**
   * Constructor.
   * @param statusMgr Status manager.
   */
  public AchievablesStatusExtractor(AchievablesStatusManager statusMgr)
  {
    _statusMgr=statusMgr;
    _builder=new AchievableStatusBuilder();
  }

  /**
   * Extract status of deeds or quests.
   * @param completedAchievables Map of the completed achievables.
   * @param activeAchievables Map of the active achievables status.
   */
  public void extract(Map<Integer,ClassInstance> completedAchievables, Map<Integer,ClassInstance> activeAchievables)
  {
    handleCompletedAchievables(completedAchievables);
    handleActiveAchievables(activeAchievables);
  }

  private void handleActiveAchievables(Map<Integer,ClassInstance> activeAchievables)
  {
    int size=activeAchievables.size();
    LOGGER.debug(NB_ENTRIES,Integer.valueOf(size));
    for(Map.Entry<Integer,ClassInstance> entry : activeAchievables.entrySet())
    {
      int achievableId=entry.getKey().intValue();
      Achievable achievable=getAchievable(achievableId);
      if (achievable!=null)
      {
        AchievableStatus status=_statusMgr.get(achievable,true);
        ClassInstance questData=entry.getValue();
        // Active quests (around 20) and deeds (around 500)
        LOGGER.debug("ID: {}",achievable);
        _builder.handleAchievable(status,questData);
      }
    }
  }

  protected abstract Achievable getAchievable(int achievableId);

  private void handleCompletedAchievables(Map<Integer,ClassInstance> completedAchievables)
  {
    int size=completedAchievables.size();
    LOGGER.debug(NB_ENTRIES,Integer.valueOf(size));
    for(Map.Entry<Integer,ClassInstance> entry : completedAchievables.entrySet())
    {
      Integer achievableId=entry.getKey();
      ClassInstance questData=entry.getValue();
      if (questData==null)
      {
        LOGGER.warn("questData is null for ID: {}",achievableId);
        continue;
      }
      LOGGER.debug("ID: {}",achievableId);
      handleCompletedAchievable(achievableId.intValue(),questData);
    }
  }

  protected abstract boolean handleCompletedAchievable(int achievableId, ClassInstance questData);
}
