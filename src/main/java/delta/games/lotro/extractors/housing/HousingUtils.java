package delta.games.lotro.extractors.housing;

import delta.games.lotro.character.status.housing.HouseAddress;
import delta.games.lotro.common.id.InternalGameId;

/**
 * Utility methods related to housing.
 * @author DAM
 */
public class HousingUtils
{
  /**
   * Build a house address from a Long address.
   * @param addressLong Input
   * @return the loaded address.
   */
  public static HouseAddress buildAddress(Long addressLong)
  {
    InternalGameId addressID=new InternalGameId(addressLong.longValue());
    int neighborhoodID=addressID.getId2();
    int houseID=addressID.getId1();
    HouseAddress address=new HouseAddress(neighborhoodID,houseID);
    return address;
  }
}
