package delta.games.lotro.extractors.housing;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.status.housing.AccountHousingData;
import delta.games.lotro.character.status.housing.HouseAddress;
import delta.games.lotro.character.status.housing.HouseReference;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extractor for account/server housing data.
 * @author DAM
 */
public class AccountHousingDataExtractor
{
  private PropertiesSet _accountProps;
  private PropertiesSet _characterProps;

  /**
   * Constructor.
   * @param accountProps Account properties.
   * @param characterProps Character properties.
   */
  public AccountHousingDataExtractor(PropertiesSet accountProps, PropertiesSet characterProps)
  {
    _accountProps=accountProps;
    _characterProps=characterProps;
  }

  /**
   * Build account housing data.
   * @return the loaded data.
   */
  public AccountHousingData build()
  {
    AccountHousingData ret=new AccountHousingData();
    HouseReference classicHouse=loadClassicHouse();
    ret.setClassicHouse(classicHouse);
    List<HouseReference> premiumHouses=loadPremiumHouses();
    for(HouseReference premiumHouse : premiumHouses)
    {
      ret.addPremiumHouse(premiumHouse);
    }
    return ret;
  }

  private HouseReference loadClassicHouse()
  {
    Long houseOwner=(Long)_accountProps.getProperty("HousingSystem_AccountHouseOwner");
    Long personalAddress=(Long)_characterProps.getProperty("HousingSystem_PersonalAddress");
    return buildHouseReference(personalAddress,houseOwner);
  }

  private List<HouseReference> loadPremiumHouses()
  {
    List<HouseReference> ret=new  ArrayList<HouseReference>();
    Object[] premiumHouses=(Object[])_accountProps.getProperty("HousingSystem_AccountPremiumHouseOwners_Array");
    if (premiumHouses!=null)
    {
      for(Object premiumHouseObj : premiumHouses)
      {
        PropertiesSet premiumHouseProps=(PropertiesSet)premiumHouseObj;
        HouseReference houseReference=buildPremiumHouse(premiumHouseProps);
        if (houseReference!=null)
        {
          ret.add(houseReference);
        }
      }
    }
    return ret;
  }

  private HouseReference buildPremiumHouse(PropertiesSet props)
  {
    Long houseAddress=(Long)props.getProperty("HousingSystem_AccountPremiumHouseOwner_HouseAddress");
    Long houseOwner=(Long)props.getProperty("HousingSystem_AccountPremiumHouseOwner_IID");
    return buildHouseReference(houseAddress,houseOwner);
  }

  private HouseReference buildHouseReference(Long addressLong, Long houseOwner)
  {
    if (houseOwner==null)
    {
      return null;
    }
    if (addressLong==null)
    {
      return null;
    }
    InternalGameId owner=new InternalGameId(houseOwner.longValue());
    HouseAddress address=buildAddress(addressLong);
    return new HouseReference(address,owner);
  }

  private HouseAddress buildAddress(Long addressLong)
  {
    InternalGameId addressID=new InternalGameId(addressLong.longValue());
    int neighborhoodID=addressID.getId2();
    int houseID=addressID.getId1();
    HouseAddress address=new HouseAddress(neighborhoodID,houseID);
    return address;
  }
}
