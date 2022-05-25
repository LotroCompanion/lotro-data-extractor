package delta.games.lotro.extractors.items;

import org.apache.log4j.Logger;

import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Extracts character items from entity data.
 * @author DAM
 */
public class CharacterItemsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(CharacterItemsExtractor.class);

  private ItemInstancesExtractor _itemExtractor;

  /**
   * Constructor.
   */
  public CharacterItemsExtractor()
  {
    _itemExtractor=new ItemInstancesExtractor();
  }

  /**
   * Extract an item instance from an entity.
   * @param did Item DID.
   * @param iid Instance identifier.
   * @param props Entity properties.
   * @return the new instance or <code>null</code> if none.
   */
  public ItemInstance<? extends Item> extractItemInstance(int did, long iid, PropertiesSet props)
  {
    Item item=ItemsManager.getInstance().getItem(did);
    if (item==null)
    {
      // Sometimes we find here a data ID for NPCs, EntityDesc
      LOGGER.debug("No match: "+did);
      return null;
    }
    if (props==null)
    {
      return null;
    }
    ItemInstance<? extends Item> itemInstance=_itemExtractor.buildItemInstanceFromProps(props,item);
    if (itemInstance!=null)
    {
      itemInstance.setInstanceId(new InternalGameId(iid));
    }
    return itemInstance;
  }
}
