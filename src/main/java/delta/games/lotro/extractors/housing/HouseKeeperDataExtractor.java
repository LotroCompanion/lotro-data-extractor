package delta.games.lotro.extractors.housing;

import java.util.Map;

import delta.games.lotro.common.enums.HousingHookID;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Extractor for house-keeper data.
 * @author DAM
 */
public class HouseKeeperDataExtractor
{
  private LotroEnum<HousingHookID> _hookIDEnum;

  /**
   * Constructor.
   */
  public HouseKeeperDataExtractor()
  {
    _hookIDEnum=LotroEnumsRegistry.getInstance().get(HousingHookID.class);
  }

  /**
   * Load data.
   * @param houseKeeper Raw WSL data.
   * @return the loaded data.
   */
  public HouseKeeperData load(ClassInstance houseKeeper)
  {
    HouseKeeperData ret=new HouseKeeperData();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> hookInfos=(Map<Integer,ClassInstance>)houseKeeper.getAttributeValue("51048511");
    handleHookInfos(ret,hookInfos);
    return ret;
  }

  private void handleHookInfos(HouseKeeperData data, Map<Integer,ClassInstance> hookInfos)
  {
    for(Map.Entry<Integer,ClassInstance> entry : hookInfos.entrySet())
    {
      Integer hookCode=entry.getKey();
      ClassInstance hookInfo=entry.getValue();
      handleHookInfo(data,hookCode,hookInfo);
    }
  }

  private void handleHookInfo(HouseKeeperData data, Integer hookCode,ClassInstance hookInfo)
  {
    HousingHookID hookID=_hookIDEnum.getEntry(hookCode.intValue());
    Long entityID=(Long)hookInfo.getAttributeValue("267953931");
    InternalGameId id=new InternalGameId(entityID.longValue());
    data.registerHookInfo(hookID,id);
  }
}
