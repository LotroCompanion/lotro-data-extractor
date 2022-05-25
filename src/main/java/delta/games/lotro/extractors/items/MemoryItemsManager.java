package delta.games.lotro.extractors.items;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Storage for items loaded from memory.
 * @author DAM
 */
public class MemoryItemsManager
{
  private static final Logger LOGGER=Logger.getLogger(MemoryItemsManager.class);

  private List<MemoryItem> _allItems;
  private List<MemoryItem> _equippedItems;
  private List<MemoryItem> _bagItems;
  private List<MemoryItem> _overflowItems;
  private List<MemoryItem> _otherItems;

  /**
   * Constructor.
   */
  public MemoryItemsManager()
  {
    _allItems=new ArrayList<MemoryItem>();
    _equippedItems=new ArrayList<MemoryItem>();
    _bagItems=new ArrayList<MemoryItem>();
    _overflowItems=new ArrayList<MemoryItem>();
    _otherItems=new ArrayList<MemoryItem>();
  }

  /**
   * Add an item.
   * @param item Item to add.
   */
  public void addItem(MemoryItem item)
  {
    PropertiesSet props=item.getProperties();
    Integer slotCode=(Integer)props.getProperty("Container_Slot");
    if ((slotCode==null) || (slotCode.intValue()==0))
    {
      // Ignore
      return;
    }
    _allItems.add(item);
    // Overflow?
    boolean isInOverflow=DatEnumsUtils.isInOverflow(slotCode.intValue());
    if (isInOverflow)
    {
      _overflowItems.add(item);
      return;
    }
    // Equipped?
    boolean isEquipped=DatEnumsUtils.isEquipped(slotCode.intValue());
    if (isEquipped)
    {
      _equippedItems.add(item);
      return;
    }
    // Bags?
    boolean isInBags=DatEnumsUtils.isInBags(slotCode.intValue());
    if (isInBags)
    {
      _bagItems.add(item);
      return;
    }
    // Other
    ItemInstance<? extends Item> itemInstance=item.getItem();
    LOGGER.warn("Unmanaged item location! "+slotCode+" => "+props.dump()+", item="+itemInstance.dump());
    _otherItems.add(item);
  }

  /**
   * Find an item instance using its identifier.
   * @param itemIid Item instance identifier to search.
   * @return an item instance or <code>null</code> if not found.
   */
  public ItemInstance<? extends Item> findItemByIid(long itemIid)
  {
    for(MemoryItem item : _allItems)
    {
      ItemInstance<? extends Item> itemInstance=item.getItem();
      if (itemInstance!=null)
      {
        InternalGameId instanceId=itemInstance.getInstanceId();
        if (instanceId!=null)
        {
          if (InternalGameId.lightMatch(instanceId.asLong(),itemIid))
          {
            return itemInstance;
          }
        }
      }
    }
    return null;
  }

  /**
   * Get the equipped items.
   * @return the equipped items.
   */
  public List<MemoryItem> getEquippedItems()
  {
    return new ArrayList<MemoryItem>(_equippedItems);
  }

  /**
   * Get the bag items.
   * @return the bag items.
   */
  public List<MemoryItem> getBagItems()
  {
    return new ArrayList<MemoryItem>(_bagItems);
  }
}
