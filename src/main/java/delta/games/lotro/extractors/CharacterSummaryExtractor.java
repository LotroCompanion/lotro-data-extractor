package delta.games.lotro.extractors;

import delta.games.lotro.character.CharacterSummary;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.NationalitiesManager;
import delta.games.lotro.character.races.NationalityDescription;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.StringUtils;

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
    ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode);
    summary.setCharacterClass(characterClass);
    // Race
    // ... not found in properties!
    // Region
    int nationalityCode=((Integer)properties.getProperty("Nationality")).intValue();
    NationalityDescription nationality=NationalitiesManager.getInstance().getNationalityDescription(nationalityCode);
    summary.setNationality(nationality);
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
    // Surname
    String surname=(String)properties.getProperty("Surname");
    surname=StringUtils.fixName(surname);
    summary.setSurname(surname);
    // Rank
    Integer rankCode=(Integer)properties.getProperty("Glory_GloryRank");
    summary.setRankCode(rankCode);
  }
}
