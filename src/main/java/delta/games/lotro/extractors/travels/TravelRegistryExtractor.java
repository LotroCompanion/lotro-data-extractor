package delta.games.lotro.extractors.travels;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.travels.AnchorStatus;
import delta.games.lotro.character.status.travels.AnchorsStatusManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.extractors.PositionUtils;

/**
 * Travels status extractor.
 * @author DAM
 */
public class TravelRegistryExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(TravelRegistryExtractor.class);

  private AnchorsStatusManager _statusMgr;

  /**
   * Constructor.
   * @param statusMgr Status manager.
   */
  public TravelRegistryExtractor(AnchorsStatusManager statusMgr)
  {
    _statusMgr=statusMgr;
  }

  /**
   * Extract travels.
   * @param travelRegistry Skills pool.
   */
  public void extract(ClassInstance travelRegistry)
  {
    _statusMgr.clear();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> travelsMap=(Map<Integer,ClassInstance>)travelRegistry.getAttributeValue("194458403");
    if (travelsMap==null)
    {
      return;
    }
    for(Map.Entry<Integer,ClassInstance> entry : travelsMap.entrySet())
    {
      int travelType=entry.getKey().intValue();
      ClassInstance travelRecord=entry.getValue(); // TravelRecord
      handleTravelRecord(travelType,travelRecord);
    }
  }

  private void handleTravelRecord(int typeCode, ClassInstance travelRecord)
  {
    DatPosition position=(DatPosition)travelRecord.getAttributeValue("111647278");
    String name=(String)travelRecord.getAttributeValue("47172757");
    LotroEnum<TravelLink> typeEnum=LotroEnumsRegistry.getInstance().get(TravelLink.class);
    TravelLink type=typeEnum.getEntry(typeCode);
    if (type==null)
    {
      LOGGER.warn("Travel type not known: {}",type);
      return;
    }
    AnchorStatus status=_statusMgr.get(type,true);
    status.setName(name);
    ExtendedPosition extendedPosition=PositionUtils.buildExtendedPosition(position);
    status.setPosition(extendedPosition);
    LOGGER.debug("Got anchor: {}",status);
  }
}
