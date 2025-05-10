package delta.games.lotro.extractors.achievables;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.achievables.AchievableElementState;
import delta.games.lotro.character.status.achievables.AchievableObjectiveStatus;
import delta.games.lotro.character.status.achievables.AchievableStatus;
import delta.games.lotro.character.status.achievables.ObjectiveConditionStatus;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;

/**
 * Builds the status of achievables.
 * @author DAM
 */
public class AchievableStatusBuilder
{
  private static final Logger LOGGER=LoggerFactory.getLogger(AchievableStatusBuilder.class);

  /**
   * Status code attribute name.
   */
  private static final String STATUS_CODE_ATTR="180500243";

  /**
   * Handle a single achievable.
   * @param achievableStatus Storage for loaded data.
   * @param questData Input status data.
   */
  @SuppressWarnings("unchecked")
  public void handleAchievable(AchievableStatus achievableStatus, ClassInstance questData)
  {
    Achievable achievable=achievableStatus.getAchievable();
    // questData is a QuestRecord
    if (questData==null)
    {
      LOGGER.warn("Could not use status for achievable: {}",achievable);
      return;
    }
    // State
    Integer statusCode=(Integer)questData.getAttributeValue(STATUS_CODE_ATTR);
    AchievableElementState state=getStateFromCode(statusCode);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Achievable state: {} => {}",achievable,state);
    }
    achievableStatus.setState(state);
    // Objectives
    Map<Integer,ClassInstance> objectivesData=(Map<Integer,ClassInstance>)questData.getAttributeValue("m_objectiveHash");
    if (objectivesData==null)
    {
      return;
    }
    for(Map.Entry<Integer,ClassInstance> entry : objectivesData.entrySet())
    {
      int objectiveIndex=entry.getKey().intValue();
      AchievableObjectiveStatus objectiveStatus=achievableStatus.getObjectiveStatus(objectiveIndex);
      if (objectiveStatus==null)
      {
        continue;
      }
      ClassInstance objectiveData=entry.getValue();
      // objectiveData is a QuestCompletionObjective
      handleAchievableObjective(objectiveStatus,objectiveData);
    }
  }

  private void handleAchievableObjective(AchievableObjectiveStatus objectiveStatus, ClassInstance objectiveData)
  {
    if (objectiveData==null)
    {
      return;
    }
    Objective objective=objectiveStatus.getObjective();
    @SuppressWarnings("unchecked")
    List<ClassInstance> questConditions=(List<ClassInstance>)objectiveData.getAttributeValue("m_conditionList");
    if (questConditions==null)
    {
      return;
    }
    // Checks
    Integer index=(Integer)objectiveData.getAttributeValue("125031076");
    if ((index==null) || (index.intValue()!=objective.getIndex()))
    {
      LOGGER.warn("Objective index mismatch: expected={}, got={}",Integer.valueOf(objective.getIndex()),index);
      return;
    }
    // Status
    Integer statusCode=(Integer)objectiveData.getAttributeValue(STATUS_CODE_ATTR);
    AchievableElementState state=getStateFromCode(statusCode);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("\t\tObjective #{}: ",index);
      LOGGER.debug("\t\t\tState: {}",state);
    }
    objectiveStatus.setState(state);

    // Loop on conditions
    int nbQuestConditions=questConditions.size();
    for(int i=0;i<nbQuestConditions;i++)
    {
      LOGGER.debug("\t\t\t- questCondition #{}",Integer.valueOf(i));
      ClassInstance questCondition=questConditions.get(i);
      handleQuestCondition(questCondition,objectiveStatus);
    }
  }

  private void handleQuestCondition(ClassInstance questCondition, AchievableObjectiveStatus objectiveStatus)
  {
    if (LOGGER.isDebugEnabled())
    {
      Integer condIndex=(Integer)questCondition.getAttributeValue("225496484");
      LOGGER.debug("\t\t\t  questCondition index #{}",condIndex);
    }

    if (questCondition==null)
    {
      LOGGER.warn("questCondition is null!");
      return;
    }
    @SuppressWarnings("unchecked")
    List<ClassInstance> dynamicQuestEvents=(List<ClassInstance>)questCondition.getAttributeValue("m_eventList");
    if (dynamicQuestEvents==null)
    {
      LOGGER.warn("dynamicQuestEvents is null!");
      return;
    }
    Objective objective=objectiveStatus.getObjective();
    for(ClassInstance dynamicQuestEvent : dynamicQuestEvents)
    {
      if (dynamicQuestEvent==null)
      {
        LOGGER.warn("dynamicQuestEvent is null!");
        continue;
      }
      Integer questEventId=(Integer)dynamicQuestEvent.getAttributeValue("m_questEventID");
      if ((questEventId==null) || (questEventId.intValue()<1))
      {
        LOGGER.warn("Bad event ID: got={}",questEventId);
        continue;
      }
      ObjectiveCondition condition=objective.getConditionByEventID(questEventId.intValue());
      if (condition==null)
      {
        LOGGER.warn("Condition not found. Event ID={}",questEventId);
        continue;
      }
      int indexToUse=condition.getIndex();
      ObjectiveConditionStatus conditionStatus=objectiveStatus.getConditionStatus(indexToUse);
      handleObjectiveCondition(conditionStatus,dynamicQuestEvent);
    }
  }

  private void handleObjectiveCondition(ObjectiveConditionStatus conditionStatus, ClassInstance dynamicQuestEvent)
  {
    // State
    Integer stateCode=(Integer)dynamicQuestEvent.getAttributeValue(STATUS_CODE_ATTR);
    AchievableElementState state=getStateFromCode(stateCode);
    conditionStatus.setState(state);
    LOGGER.debug("\t\t\t\tState: {}",state);
    Integer count=(Integer)dynamicQuestEvent.getAttributeValue("m_uCount");
    if ((count!=null) && (count.intValue()>0))
    {
      LOGGER.debug("\t\t\t\tCount: {}",count);
      conditionStatus.setCount(count);
    }
    conditionStatus.clearKeys();
    @SuppressWarnings("unchecked")
    List<String> strings=(List<String>)dynamicQuestEvent.getAttributeValue("m_rRuntimeStringList");
    if (strings!=null)
    {
      LOGGER.debug("\t\t\t\tKeys: {}",strings);
      for(String key : strings)
      {
        conditionStatus.addKey(key);
      }
    }
    /*
Instance of class DynamicQuestEvent:
  INTEGER 251897092 = 0
  REFERENCE m_rRuntimeStringList = null
  INTEGER 55837717 = 0
  REFERENCE m_rRuntimeDataIDHash = null
  INTEGER m_uCount = 1
  LONG 61093828 = 0
  REFERENCE 81851368 = null
  REFERENCE m_collection = null
  INTEGER 186091059 = 0
  INTEGER m_questEventID = 1
  INTEGER m_questDID = 1879260393
  LONG 143181940 = 300665784428920902
  INTEGER 122406325 = 0
  INTEGER 180500243 = 805306368
     */
  }

  private static AchievableElementState getStateFromCode(Integer code)
  {
    if (code==null) return null;
    if (code.intValue()==0) return AchievableElementState.UNDEFINED;
    if (code.intValue()==268435456) return AchievableElementState.UNDERWAY;
    if (code.intValue()==805306368) return AchievableElementState.COMPLETED;
    LOGGER.warn("Unmanaged status code: {}",code);
    return null;
  }
}
