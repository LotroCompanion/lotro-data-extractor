package delta.games.lotro.extractors.items;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;

/**
 * Storage for item data as loaded from memory.
 * @author DAM
 */
public class MemoryItem
{
  private PropertiesSet _properties;
  private ItemInstance<? extends Item> _item;

  /**
   * Constructor.
   * @param item Decoded item.
   * @param properties Source properties.
   */
  public MemoryItem(ItemInstance<? extends Item> item, PropertiesSet properties)
  {
    _properties=properties;
    _item=item;
  }

  /**
   * Get the item properties.
   * @return the item properties.
   */
  public PropertiesSet getProperties()
  {
    return _properties;
  }

  /**
   * Get the slot code.
   * @return A slot code or <code>null</code>.
   */
  public Integer getSlotCode()
  {
    Integer slotCode=(Integer)_properties.getProperty("Container_Slot");
    if ((slotCode==null) || (slotCode.intValue()==0))
    {
      return null;
    }
    return slotCode;
  }

  /**
   * Get the decoded item.
   * @return the decoded item.
   */
  public ItemInstance<? extends Item> getItem()
  {
    return _item;
  }
}
