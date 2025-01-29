package delta.games.lotro.extractors;

import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.lore.maps.Zone;
import delta.games.lotro.lore.maps.ZoneUtils;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;

/**
 * Utility methods related to positions.
 * @author DAM
 */
public class PositionUtils
{
  /**
   * Build an extended position from a DAT position.
   * @param position Input position.
   * @return the new position.
   */
  public static ExtendedPosition buildExtendedPosition(DatPosition position)
  {
    if (position==null)
    {
      return null;
    }
    ExtendedPosition ret=new ExtendedPosition();
    Position pos=buildPosition(position);
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

  /**
   * Build a position from a DAT position.
   * @param position Input position.
   * @return the new position.
   */
  public static Position buildPosition(DatPosition position)
  {
    if (position==null)
    {
      return null;
    }
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    Position pos=new Position(position.getRegion(),lonLat[0],lonLat[1]);
    return pos;
  }

  private static Integer getZoneID(DatPosition position)
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
