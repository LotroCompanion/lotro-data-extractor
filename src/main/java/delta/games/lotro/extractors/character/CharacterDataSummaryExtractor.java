package delta.games.lotro.extractors.character;

import delta.games.lotro.character.CharacterDataSummary;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.StringUtils;

/**
 * Extracts character data summary.
 * @author DAM
 */
public class CharacterDataSummaryExtractor
{
  /**
   * Use the given properties to get character summary data.
   * @param summary Storage to use.
   * @param properties Properties to use.
   */
  public void useProperties(CharacterDataSummary summary, PropertiesSet properties)
  {
    // Name
    String name=(String)properties.getProperty("Name");
    name=StringUtils.fixName(name).trim();
    summary.setName(name);
    // Level
    Integer level=(Integer)properties.getProperty("Advancement_Level");
    summary.setLevel(level!=null?level.intValue():1);
  }
}
