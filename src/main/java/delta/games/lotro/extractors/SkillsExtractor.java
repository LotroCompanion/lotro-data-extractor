package delta.games.lotro.extractors;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.status.skills.SkillStatus;
import delta.games.lotro.character.status.skills.SkillsStatusManager;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Skills extractor.
 * @author DAM
 */
public class SkillsExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SkillsExtractor.class);

  private SkillsStatusManager _statusMgr;

  /**
   * Set the status manager for storage.
   * @param statusMgr Status manager.
   */
  public void setStatusManager(SkillsStatusManager statusMgr)
  {
    _statusMgr=statusMgr;
  }

  /**
   * Extract skills.
   * @param skillPool Skills pool.
   */
  public void extract(ClassInstance skillPool)
  {
    @SuppressWarnings("unchecked")
    List<ClassInstance> skillInfos=(List<ClassInstance>)skillPool.getAttributeValue("m_listSkillInfo");
    SkillsManager skillsMgr=SkillsManager.getInstance();
    for(ClassInstance skillInfo : skillInfos)
    {
      if (skillInfo==null)
      {
        continue;
      }
      Integer acquiredBy=(Integer)skillInfo.getAttributeValue("m_acquiredBy");
      Integer skillId=(Integer)skillInfo.getAttributeValue("m_didSkill");
      SkillDescription skill=skillsMgr.getSkill(skillId.intValue());
      if (skill!=null)
      {
        SkillStatus status=_statusMgr.get(skill,true);
        status.setAvailable(true);
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Skill: {} acquired by: {}, category={}",skill.getName(),acquiredBy,skill.getCategory());
        }
      }
      else
      {
        LOGGER.warn("Skill not found: ID={}",skillId);
      }
    }
  }
}
