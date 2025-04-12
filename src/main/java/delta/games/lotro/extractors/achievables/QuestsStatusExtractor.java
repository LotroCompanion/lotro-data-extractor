package delta.games.lotro.extractors.achievables;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.achievables.AchievableStatus;
import delta.games.lotro.character.status.achievables.AchievablesStatusManager;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.extractors.TimeUtils;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;

/**
 * @author dm
 */
public class QuestsStatusExtractor extends AchievablesStatusExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(QuestsStatusExtractor.class);

  /**
   * Constructor.
   * @param statusMgr Status manager.
   */
  public QuestsStatusExtractor(AchievablesStatusManager statusMgr)
  {
    super(statusMgr);
  }

  @Override
  protected Achievable getAchievable(int achievableId)
  {
    QuestsManager questsMgr=QuestsManager.getInstance();
    QuestDescription quest=questsMgr.getQuest(achievableId);
    return quest;
  }

  @Override
  public boolean handleCompletedAchievable(int achievableId, ClassInstance questData)
  {
    QuestsManager questsMgr=QuestsManager.getInstance();
    QuestDescription quest=questsMgr.getQuest(achievableId);
    if (quest==null)
    {
      return false;
    }
    LOGGER.debug("Quest: {}",quest);
    AchievableStatus status=_statusMgr.get(quest,true);
    status.setCompleted(true);
    status.updateInternalState();
    // Completion count
    Integer count=(Integer)questData.getAttributeValue("240034292");
    if (count!=null)
    {
      if (count.intValue()>1)
      {
        status.setCompletionCount(count);
      }
      // Date: of first or last? completion
      Integer timestamp=(Integer)questData.getAttributeValue("212351221");
      Date date=TimeUtils.getDate(timestamp);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Quest: {} => x{}, date={}",quest,count,date);
      }
    }
    return true;
  }
}
