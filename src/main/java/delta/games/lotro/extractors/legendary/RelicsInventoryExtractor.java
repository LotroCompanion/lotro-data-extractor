package delta.games.lotro.extractors.legendary;

import java.util.Map;

import delta.games.lotro.character.status.relics.RelicsInventory;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Relics inventory extractor.
 * @author DAM
 */
public class RelicsInventoryExtractor
{
  /**
   * Extract relics inventory.
   * @param iaRegistry Item advancement registry.
   * @return the loaded relics inventory.
   */
  @SuppressWarnings("unchecked")
  public RelicsInventory extract(ClassInstance iaRegistry)
  {
    RelicsInventory ret=new RelicsInventory();
    Map<Integer,Integer> counts=(Map<Integer,Integer>)iaRegistry.getAttributeValue("135264115");
    if (counts!=null)
    {
      for(Map.Entry<Integer,Integer> entry : counts.entrySet())
      {
        int relicId=entry.getKey().intValue();
        int count=entry.getValue().intValue();
        ret.setRelicCount(relicId,count);
      }
    }
    return ret;
  }
}
