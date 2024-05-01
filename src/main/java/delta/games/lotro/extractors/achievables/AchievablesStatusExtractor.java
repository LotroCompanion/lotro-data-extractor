package delta.games.lotro.extractors.achievables;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.status.achievables.AchievableStatus;
import delta.games.lotro.character.status.achievables.AchievablesStatusManager;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.extractors.TimeUtils;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;

/**
 * Achievables status extractor.
 * @author DAM
 */
public class AchievablesStatusExtractor
{
  private static final Logger LOGGER=Logger.getLogger(AchievablesStatusExtractor.class);

  private static final String NB_ENTRIES="Nb entries: ";

  private AchievableStatusBuilder _builder;
  private AchievablesStatusManager _deedsStatusMgr;
  private AchievablesStatusManager _questsStatusMgr;

  /**
   * Constructor.
   * @param deedsStatusMgr Deeds status manager.
   * @param questsStatusMgr Quests status manager.
   */
  public AchievablesStatusExtractor(AchievablesStatusManager deedsStatusMgr, AchievablesStatusManager questsStatusMgr)
  {
    _deedsStatusMgr=deedsStatusMgr;
    _questsStatusMgr=questsStatusMgr;
    _builder=new AchievableStatusBuilder();
  }

  /**
   * Extract deeds and/or quests.
   * @param completedAchievables Map of the completed achievables.
   * @param activeAchievables Map of the active achievables status.
   */
  public void extract(Map<Integer,ClassInstance> completedAchievables, Map<Integer,ClassInstance> activeAchievables)
  {
    handleCompletedAchievables(completedAchievables);
    handleActiveAchievables(activeAchievables);
  }

  @SuppressWarnings({"unchecked","unused"})
  private void handleQuestsStatus(WStateDataSet data)
  {
    List<Integer> orphanRefs=data.getOrphanReferences();
    if (orphanRefs.size()==1)
    {
      Map<Integer,ClassInstance> dataMap=(Map<Integer,ClassInstance>)data.getValueForReference(orphanRefs.get(0).intValue());
      int size=dataMap.size();
      LOGGER.debug(NB_ENTRIES+size);
      for(Map.Entry<Integer,ClassInstance> entry : dataMap.entrySet())
      {
        int questId=entry.getKey().intValue();
        ClassInstance questData=entry.getValue();
        handleQuestStatus(questId,questData);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleQuestStatus(int questId, ClassInstance questData)
  {
    // questData is a QuestRecord
    QuestsManager questMgr=QuestsManager.getInstance();
    QuestDescription quest=questMgr.getQuest(questId);
    if (quest==null)
    {
      LOGGER.warn("Quest not found: ID="+questId);
      return;
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Quest name: "+quest.getName());
      LOGGER.debug(questData);
    }
    Map<Integer,ClassInstance> objectivesData=(Map<Integer,ClassInstance>)questData.getAttributeValue("m_objectiveHash");
    for(Map.Entry<Integer,ClassInstance> entry : objectivesData.entrySet())
    {
      int objectiveIndex=entry.getKey().intValue();
      ClassInstance objectiveData=entry.getValue(); // QuestCompletionObjective
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Objective #"+objectiveIndex+": "+objectiveData);
      }
      List<ClassInstance> conditions=(List<ClassInstance>)objectiveData.getAttributeValue("m_conditionList");
      for(ClassInstance condition : conditions) // QuestCondition
      {
        LOGGER.debug(condition);
      }
    }
  }

  private void handleActiveAchievables(Map<Integer,ClassInstance> activeAchievables)
  {
    int size=activeAchievables.size();
    LOGGER.debug(NB_ENTRIES+size);
    for(Map.Entry<Integer,ClassInstance> entry : activeAchievables.entrySet())
    {
      int achievableId=entry.getKey().intValue();
      if (_deedsStatusMgr!=null)
      {
        DeedsManager deedsMgr=DeedsManager.getInstance();
        DeedDescription deed=deedsMgr.getDeed(achievableId);
        if (deed!=null)
        {
          AchievableStatus status=_deedsStatusMgr.get(deed,true);
          ClassInstance questData=entry.getValue();
          handleActiveAchievable(status,deed,questData);
          continue;
        }
      }
      if (_questsStatusMgr!=null)
      {
        QuestsManager questsMgr=QuestsManager.getInstance();
        QuestDescription quest=questsMgr.getQuest(achievableId);
        if (quest!=null)
        {
          AchievableStatus status=_questsStatusMgr.get(quest,true);
          ClassInstance questData=entry.getValue();
          handleActiveAchievable(status,quest,questData);
        }
      }
    }
  }

  private void handleActiveAchievable(AchievableStatus deedStatus, Achievable achievable, ClassInstance questData)
  {
    // Active quests (around 20) and deeds [around 500)
    LOGGER.debug("ID: "+achievable.getIdentifier());
    _builder.handleAchievable(deedStatus,questData);
   }

  private void handleCompletedAchievables(Map<Integer,ClassInstance> completedAchievables)
  {
    int size=completedAchievables.size();
    LOGGER.debug(NB_ENTRIES+size);
    for(Map.Entry<Integer,ClassInstance> entry : completedAchievables.entrySet())
    {
      int achievableId=entry.getKey().intValue();
      ClassInstance questData=entry.getValue();
      if (questData==null)
      {
        LOGGER.warn("questData is null for ID: "+achievableId);
        continue;
      }
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("ID: "+achievableId);
      }
      handleCompletedAchievable(questData,achievableId);
    }
  }

  private void handleCompletedAchievable(ClassInstance questData, int achievableId)
  {
    boolean done=false;
    if (_questsStatusMgr!=null)
    {
      QuestsManager questsMgr=QuestsManager.getInstance();
      QuestDescription quest=questsMgr.getQuest(achievableId);
      if (quest!=null)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Quest name: "+quest.getName());
        }
        AchievableStatus status=_questsStatusMgr.get(quest,true);
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
            LOGGER.debug("Quest: "+quest+" => x"+count+", date="+date);
          }
        }
        done=true;
      }
    }
    if ((!done) && (_deedsStatusMgr!=null))
    {
      DeedsManager deedsMgr=DeedsManager.getInstance();
      DeedDescription deed=deedsMgr.getDeed(achievableId);
      if (deed!=null)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Deed name: "+deed.getName());
        }
        AchievableStatus status=_deedsStatusMgr.get(deed,true);
        status.setCompleted(true);
        status.updateInternalState();
        done=true;
      }
    }
    if ((_questsStatusMgr!=null) && (_deedsStatusMgr!=null) && (!done))
    {
      LOGGER.warn("Deed/quest not found: "+achievableId);
    }
  }
}
