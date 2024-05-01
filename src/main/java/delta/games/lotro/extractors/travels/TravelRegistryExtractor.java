package delta.games.lotro.extractors.travels;

import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.status.travels.AnchorStatus;
import delta.games.lotro.character.status.travels.AnchorsStatusManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.maps.Zone;
import delta.games.lotro.lore.maps.ZoneUtils;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;

/**
 * Travels status extractor.
 * @author DAM
 */
public class TravelRegistryExtractor
{
  private static final Logger LOGGER=Logger.getLogger(TravelRegistryExtractor.class);

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
      LOGGER.warn("Travel type not known: "+type);
      return;
    }
    AnchorStatus status=_statusMgr.get(type,true);
    status.setName(name);
    ExtendedPosition extendedPosition=buildPosition(position);
    status.setPosition(extendedPosition);
    LOGGER.debug("Got anchor: "+status);
  }

  private ExtendedPosition buildPosition(DatPosition position)
  {
    if (position==null)
    {
      return null;
    }
    ExtendedPosition ret=new ExtendedPosition();
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    Position pos=new Position(position.getRegion(),lonLat[0],lonLat[1]);
    ret.setPosition(pos);
    Zone zone=null;
    Integer zoneID=getZoneID(position);
    if (zoneID!=null)
    {
      zone=ZoneUtils.getZone(zoneID.intValue());
      ret.setZone(zone);
    }
    return ret;
  }

  private Integer getZoneID(DatPosition position)
  {
    Integer zoneID=null;
    int region=position.getRegion();
    int blockX=position.getBlockX();
    int blockY=position.getBlockY();
    LandblocksManager mgr=LandblocksManager.getInstance();
    Landblock landblock=mgr.getLandblock(region,blockX,blockY);
    if (landblock!=null)
    {
      int cell=position.getCell();
      zoneID=landblock.getParentData(cell,position.getPosition());
    }
    return zoneID;
  }
}
