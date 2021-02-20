package delta.games.lotro.extractors.legendary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.legendary.LegendaryInstanceAttrs;

/**
 * Legendary data description.
 * @author DAM
 */
public class LegendaryDataManager
{
  private Map<Long,LegendaryInstanceAttrs> _mapItemIIDToLegendaryData;

  /**
   * Constructor.
   */
  public LegendaryDataManager()
  {
    _mapItemIIDToLegendaryData=new HashMap<Long,LegendaryInstanceAttrs>();
  }

  /**
   * Register a legendary data.
   * @param itemIID Item instance identifier.
   * @param legendaryAttrs legendary attributes.
   */
  public void addLegendaryData(long itemIID, LegendaryInstanceAttrs legendaryAttrs)
  {
    _mapItemIIDToLegendaryData.put(Long.valueOf(itemIID),legendaryAttrs);
  }

  /**
   * Get legendary data for an item.
   * @param itemIID Item instance identifier.
   * @return legendary data or <code>null</code> if not found.
   */
  public LegendaryInstanceAttrs getLegendaryData(long itemIID)
  {
    return _mapItemIIDToLegendaryData.get(Long.valueOf(itemIID));
  }

  /**
   * Get the managed item instance identifiers.
   * @return A list of item instance identifiers.
   */
  public List<Long> getItemIIDs()
  {
    List<Long> ret=new ArrayList<Long>(_mapItemIIDToLegendaryData.keySet());
    Collections.sort(ret);
    return ret;
  }
}
