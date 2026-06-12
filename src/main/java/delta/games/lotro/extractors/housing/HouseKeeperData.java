package delta.games.lotro.extractors.housing;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.common.enums.HousingHookID;
import delta.games.lotro.common.id.InternalGameId;

/**
 * Data extracted from a house keeper.
 * @author DAM
 */
public class HouseKeeperData
{
  private Map<HousingHookID,InternalGameId> _hookInfos;

  /**
   * Constructor.
   */
  public HouseKeeperData()
  {
    _hookInfos=new HashMap<HousingHookID,InternalGameId>();
  }

  /**
   * Register a hook info.
   * @param hookID Hook identifier.
   * @param id Associated entity ID.
   */
  public void registerHookInfo(HousingHookID hookID, InternalGameId id)
  {
    _hookInfos.put(hookID,id);
  }
}
