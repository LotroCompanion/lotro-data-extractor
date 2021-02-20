package delta.games.lotro.extractors.achievables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.achievables.AchievableStatus;
import delta.games.lotro.character.achievables.DeedsStatusManager;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;

/**
 * Deeds extractor.
 * @author DAM
 */
public class DeedsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(DeedsExtractor.class);

  /**
   * Extract deeds.
   * @param achievableIds Identifiers of the completed achievables.
   * @param statusManager Deeds status manager.
   */
  public void extract(List<Integer> achievableIds, DeedsStatusManager statusManager)
  {
    // Completed deeds and (some) completed quests
    List<DeedDescription> completedDeeds=getCompletedDeeds(achievableIds);
    updateDeedsStatus(completedDeeds, statusManager);
  }

  private List<DeedDescription> getCompletedDeeds(List<Integer> achievableIds)
  {
    List<DeedDescription> completedDeeds=new ArrayList<DeedDescription>();
    LOGGER.debug("Number of deeds: "+achievableIds.size());
    for(Integer achievableRef : achievableIds)
    {
      int achievableId=achievableRef.intValue();
      DeedDescription deed=DeedsManager.getInstance().getDeed(achievableId);
      if (deed!=null)
      {
        completedDeeds.add(deed);
      }
      else
      {
        QuestDescription quest=QuestsManager.getInstance().getQuest(achievableId);
        if (quest!=null)
        {
          LOGGER.debug("Found quest: "+quest.getName());
        }
        else
        {
          LOGGER.warn("Missing quest/deed ID: "+achievableId);
        }
      }
    }
    return completedDeeds;
  }

  private void updateDeedsStatus(List<DeedDescription> completedDeeds, DeedsStatusManager statusManager)
  {
    Set<Integer> completedKeys=new HashSet<Integer>();
    for(DeedDescription deed : completedDeeds)
    {
      AchievableStatus deedStatus=statusManager.get(deed,true);
      deedStatus.setCompleted(true);
      completedKeys.add(Integer.valueOf(deedStatus.getAchievableId()));
    }
    for(AchievableStatus deedStatus : statusManager.getAll())
    {
      int key=deedStatus.getAchievableId();
      if (!completedKeys.contains(Integer.valueOf(key)))
      {
        deedStatus.setCompleted(false);
      }
    }
  }
}
