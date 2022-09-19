package delta.games.lotro.extractors.hobbies;

import delta.games.lotro.character.status.hobbies.HobbiesStatusManager;
import delta.games.lotro.character.status.hobbies.HobbyStatus;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.hobbies.HobbiesManager;
import delta.games.lotro.lore.hobbies.HobbyDescription;

/**
 * Hobbies status extractor.
 * @author DAM
 */
public class HobbiesExtractor
{
  private HobbiesStatusManager _hobbiesStatusMgr;

  /**
   * Constructor.
   * @param hobbiesStatusMgr Hobbies status manager.
   */
  public HobbiesExtractor(HobbiesStatusManager hobbiesStatusMgr)
  {
    _hobbiesStatusMgr=hobbiesStatusMgr;
  }

  /**
   * Extract hobbies status.
   * @param props Character properties.
   */
  public void extract(PropertiesSet props)
  {
    _hobbiesStatusMgr.clear();
    HobbiesManager mgr=HobbiesManager.getInstance();
    for(HobbyDescription hobby : mgr.getAll())
    {
      extractHobyStatus(hobby,props);
    }
  }

  private void extractHobyStatus(HobbyDescription hobby, PropertiesSet props)
  {
    HobbyStatus status=_hobbiesStatusMgr.get(hobby,true);

    // Proficiency
    String proficiencyPropertyName=hobby.getProficiencyPropertyName();
    Integer proficiency=(Integer)props.getProperty(proficiencyPropertyName);
    status.setValue((proficiency!=null)?proficiency.intValue():0);
  }
}
