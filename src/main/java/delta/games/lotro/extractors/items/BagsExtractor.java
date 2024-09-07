package delta.games.lotro.extractors.items;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.storage.bags.BagsManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.CountedItem;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;

/**
 * Extractor for bags data using items loaded from memory.
 * @author DAM
 */
public class BagsExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(BagsExtractor.class);

  private MemoryItemsManager _itemsMgr;

  /**
   * Constructor.
   * @param itemsMgr Loaded items manager.
   */
  public BagsExtractor(MemoryItemsManager itemsMgr)
  {
    _itemsMgr=itemsMgr;
  }

  /**
   * Extract bags definition from the loaded items.
   * @return a bags manager.
   */
  public BagsManager extract()
  {
    BagsManager ret=new BagsManager();
    List<MemoryItem> equippedItems=_itemsMgr.getBagItems();
    for(MemoryItem equippedItem : equippedItems)
    {
      PropertiesSet props=equippedItem.getProperties();
      // Quantity
      Integer quantityValue=(Integer)props.getProperty("Inventory_Quantity");
      if (quantityValue!=null)
      {
        LOGGER.debug("Quantity: "+quantityValue);
      }
      ItemInstance<? extends Item> itemInstance=equippedItem.getItem();
      int quantity=(quantityValue!=null)?quantityValue.intValue():1;
      CountedItem<ItemInstance<? extends Item>> countedItemInstance=new CountedItem<ItemInstance<? extends Item>>(itemInstance,quantity);
      Integer slotCode=equippedItem.getSlotCode();
      int index=slotCode.intValue()&0xFFFF;
      LOGGER.debug("Index: "+index+" => "+countedItemInstance);
      ret.addBagItem(countedItemInstance,index);
    }
    return ret;
  }
}
