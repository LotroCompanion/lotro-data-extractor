package delta.games.lotro.extractors;

import delta.games.lotro.character.CharacterSummary;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.StringUtils;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Extracts character summary.
 * @author DAM
 */
public class CharacterSummaryExtractor
{
  /**
   * Use the given properties to get character summary data.
   * @param summary Storage to use.
   * @param properties Properties to use.
   */
  public void useProperties(CharacterSummary summary, PropertiesSet properties)
  {
    // Name
    String name=(String)properties.getProperty("Name");
    name=StringUtils.fixName(name).trim();
    summary.setName(name);
    // Class
    int classCode=((Integer)properties.getProperty("Agent_Class")).intValue();
    CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classCode);
    summary.setCharacterClass(characterClass);
    // Race
    // ... not found in properties!
    // Region
    int nationalityCode=((Integer)properties.getProperty("Nationality")).intValue();
    String nationality=DatEnumsUtils.getNationalityFromNationalityId(nationalityCode);
    summary.setRegion(nationality);
    // Kinship
    Long kinshipID=(Long)properties.getProperty("Guild_ID");
    if (kinshipID!=null)
    {
      summary.setKinshipID(new InternalGameId(kinshipID.longValue()));
    }
    // Import date
    summary.setImportDate(Long.valueOf(System.currentTimeMillis()));
    // Level
    Integer level=(Integer)properties.getProperty("Advancement_Level");
    summary.setLevel(level!=null?level.intValue():1);
  }
}
