package delta.games.lotro.extractors;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Skills extractor.
 * @author DAM
 */
public class SkillsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(SkillsExtractor.class);

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
      Integer acquiredBy=(Integer)skillInfo.getAttributeValue("m_acquiredBy");
      Integer skillId=(Integer)skillInfo.getAttributeValue("m_didSkill");
      SkillDescription skill=skillsMgr.getSkill(skillId.intValue());
      if (skill!=null)
      {
        LOGGER.debug("Skill: "+skill.getName()+" acquired by: "+acquiredBy);
      }
      else
      {
        LOGGER.warn("Skill not found: ID="+skillId);
      }
    }
  }
}
