package delta.games.lotro.extractors.housing;

import delta.common.utils.math.geometry.Vector3D;
import delta.games.lotro.character.status.housing.HouseAddress;
import delta.games.lotro.character.status.housing.HousingItem;
import delta.games.lotro.common.enums.HousingHookID;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extractor for house contents.
 * @author DAM
 */
public class HouseContentsExtractor
{
  private LotroEnum<HousingHookID> _hookIDEnum;

  /**
   * Constructor.
   */
  public HouseContentsExtractor()
  {
    _hookIDEnum=LotroEnumsRegistry.getInstance().get(HousingHookID.class);
  }

  /**
   * Find if we're in a house, and get its address.
   * @param props Character properties.
   * @return An address or <code>null</code>.
   */
  public HouseAddress findHouse(PropertiesSet props)
  {
    Integer neighborhoodID=(Integer)props.getProperty("HousingSystem_NeighborhoodPlayerIsIn");
    if ((neighborhoodID==null) || (neighborhoodID.intValue()==0))
    {
      return null;
    }
    Integer houseID=(Integer)props.getProperty("HousingSystem_HousePlayerIsInside");
    if (houseID==null)
    {
      return null;
    }
    HouseAddress address=new HouseAddress(neighborhoodID.intValue(),houseID.intValue());
    return address;
  }

  /**
   * Handle a housing entity.
   * @param position Entity position.
   * @param did DID.
   * @param props Properties.
   * @return the loaded item, or <code>null</code>.
   */
  public HousingItem handleEntity(int did, Position position, PropertiesSet props)
  {
    // Hook ID
    Integer hookIDCode=(Integer)props.getProperty("HousingSystem_DecorationItem_HookID");
    HousingHookID hookID=(hookIDCode!=null)?_hookIDEnum.getEntry(hookIDCode.intValue()):null;
    if(hookID==null)
    {
      return null;
    }
    HousingItem ret=new HousingItem(did,position,hookID);
    Float hookRotation=(Float)props.getProperty("HousingDecoration_HookRotation");
    if (hookRotation!=null)
    {
      ret.setHookRotation(hookRotation.floatValue());
    }
    Vector3D positionOffset=(Vector3D)props.getProperty("HousingDecoration_PositionOffset");
    ret.setPositionOffset(positionOffset);
    Float rotationOffset=(Float)props.getProperty("HousingDecoration_RotationOffset");
    if (rotationOffset!=null)
    {
      ret.setRotationOffset(rotationOffset.floatValue());
    }
    Long boundTo=(Long)props.getProperty("Inventory_BoundToID");
    if (boundTo!=null)
    {
      InternalGameId boundToID=new InternalGameId(boundTo.longValue());
      ret.setBoundTo(boundToID);
    }
    return ret;
  }
}
