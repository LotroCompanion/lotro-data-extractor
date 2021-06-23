package delta.games.lotro.extractors.achievables;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.achievables.AchievableStatus;
import delta.games.lotro.character.achievables.AchievablesStatusManager;
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

  private AchievableStatusBuilder _builder;
  private AchievablesStatusManager _deedsMgr;
  private AchievablesStatusManager _questsMgr;

  /**
   * Constructor.
   * @param deedsMgr Deeds status manager.
   * @param questsMgr Quests status manager.
   */
  public AchievablesStatusExtractor(AchievablesStatusManager deedsMgr, AchievablesStatusManager questsMgr)
  {
    _deedsMgr=deedsMgr;
    _questsMgr=questsMgr;
    _builder=new AchievableStatusBuilder();
  }

  /**
   * Extract deeds.
   * @param completedAchievableIds Identifiers of the completed achievables.
   * @param activeAchievables Map of active achievables status.
   */
  public void extract(List<Integer> completedAchievableIds, Map<Integer,ClassInstance> activeAchievables)
  {
    handleCompletedAchievables(completedAchievableIds);
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
      LOGGER.debug("Nb entries: "+size);
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
    LOGGER.debug("Quest name: "+quest.getName());
    //System.out.println("\tQuest name: "+quest.getName());
    LOGGER.debug(questData);
    Map<Integer,ClassInstance> objectivesData=(Map<Integer,ClassInstance>)questData.getAttributeValue("m_objectiveHash");
    for(Map.Entry<Integer,ClassInstance> entry : objectivesData.entrySet())
    {
      int objectiveIndex=entry.getKey().intValue();
      ClassInstance objectiveData=entry.getValue(); // QuestCompletionObjective
      LOGGER.debug("Objective #"+objectiveIndex+": "+objectiveData);
      //System.out.println("\t\tObjective #"+objectiveIndex+": "+objectiveData);
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
    LOGGER.debug("Nb entries: "+size);
    for(Map.Entry<Integer,ClassInstance> entry : activeAchievables.entrySet())
    {
      int achievableId=entry.getKey().intValue();
      if (_deedsMgr!=null)
      {
        DeedsManager deedsMgr=DeedsManager.getInstance();
        DeedDescription deed=deedsMgr.getDeed(achievableId);
        if (deed!=null)
        {
          AchievableStatus status=_deedsMgr.get(deed,true);
          ClassInstance questData=entry.getValue();
          handleActiveAchievable(status,deed,questData);
          continue;
        }
      }
      if (_questsMgr!=null)
      {
        QuestsManager questsMgr=QuestsManager.getInstance();
        QuestDescription quest=questsMgr.getQuest(achievableId);
        if (quest!=null)
        {
          AchievableStatus status=_questsMgr.get(quest,true);
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

  private void handleCompletedAchievables(List<Integer> completedAchievableIds)
  {
    int size=completedAchievableIds.size();
    LOGGER.debug("Nb entries: "+size);
    for(Integer achievableId : completedAchievableIds)
    {
      LOGGER.debug("ID: "+achievableId);
      boolean done=false;
      if (_questsMgr!=null)
      {
        QuestsManager questsMgr=QuestsManager.getInstance();
        QuestDescription quest=questsMgr.getQuest(achievableId.intValue());
        if (quest!=null)
        {
          LOGGER.debug("Quest name: "+quest.getName());
          //System.out.println("\tQuest name: "+quest.getName());
          AchievableStatus status=_questsMgr.get(quest,true);
          status.setCompleted(true);
          status.updateInternalState();
          done=true;
        }
      }
      if ((!done) && (_deedsMgr!=null))
      {
        DeedsManager deedsMgr=DeedsManager.getInstance();
        DeedDescription deed=deedsMgr.getDeed(achievableId.intValue());
        if (deed!=null)
        {
          LOGGER.debug("Deed name: "+deed.getName());
          //System.out.println("\tDeed name: "+deed.getName());
          AchievableStatus status=_deedsMgr.get(deed,true);
          status.setCompleted(true);
          status.updateInternalState();
          done=true;
        }
      }
      if (!done)
      {
        LOGGER.warn("Deed/quest not found: "+achievableId);
      }
    }
  }

  @SuppressWarnings("unused")
  private void handleQuestsCount(WStateDataSet data)
  {
    List<Integer> orphanRefs=data.getOrphanReferences();
    if (orphanRefs.size()!=1)
    {
      LOGGER.warn("Bad number of orphan references: "+orphanRefs.size());
      return;
    }
    QuestsManager questsMgr=QuestsManager.getInstance();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> dataMap=(Map<Integer,ClassInstance>)data.getValueForReference(orphanRefs.get(0).intValue());
    int size=dataMap.size();
    LOGGER.debug("Nb entries: "+size);
    //System.out.println("Nb entries: "+size);
    for(Map.Entry<Integer,ClassInstance> entry : dataMap.entrySet())
    {
      int questId=entry.getKey().intValue();
      ClassInstance questData=entry.getValue();
      //int questId2=((Integer)questData.getAttributeValue("262113540")).intValue();
      // Completion counts
      int count=((Integer)questData.getAttributeValue("240034292")).intValue();
      // Date: of first or last? completion
      Integer timestamp=(Integer)questData.getAttributeValue("212351221");
      Date date=TimeUtils.getDate(timestamp);
      QuestDescription quest=questsMgr.getQuest(questId);
      if (quest!=null)
      {
        LOGGER.debug("Quest name: "+quest.getName()+" ID="+questId+", count="+count+", date="+date);
        //System.out.println("\tQuest name: "+quest.getName()+" ID="+questId+", count="+count+", date="+date);
      }
      else
      {
        LOGGER.warn("Quest not found: "+questId);
      }
      /*
      if (questId!=questId2)
      {
        LOGGER.warn("ID mismatch");
      }
      */
    }
  }
}
