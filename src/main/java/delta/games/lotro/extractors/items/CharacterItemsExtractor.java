package delta.games.lotro.extractors.items;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterEquipment.EQUIMENT_SLOT;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.CountedItemInstance;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Extracts character items from entity data.
 * @author DAM
 */
public class CharacterItemsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(CharacterItemsExtractor.class);

  private ItemInstancesExtractor _itemExtractor;
  private CharacterItemsManager _itemsMgr;

  /**
   * Constructor.
   * @param itemsMgr Storage for items.
   */
  public CharacterItemsExtractor(CharacterItemsManager itemsMgr)
  {
    _itemExtractor=new ItemInstancesExtractor();
    _itemsMgr=itemsMgr;
  }

  /**
   * Use an item entity.
   * @param did Item DID.
   * @param iid Instance identifier.
   * @param props Entity properties.
   * @return the new instance or <code>null</code> if none.
   */
  public ItemInstance<? extends Item> useItemEntity(int did, long iid, PropertiesSet props)
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
    Integer slotCode=(Integer)props.getProperty("Container_Slot");
    if ((slotCode==null) || (slotCode.intValue()==0))
    {
      return null;
    }
    boolean isEquipped=DatEnumsUtils.isEquipped(slotCode.intValue());
    if (isEquipped)
    {
      ItemInstance<? extends Item> itemInstance=null;
      EQUIMENT_SLOT slot=DatEnumsUtils.getEquipmentSlot(slotCode.intValue());
      if (slot!=null)
      {
        LOGGER.debug("\t"+slot);
        itemInstance=_itemExtractor.buildItemInstanceFromProps(props,item);
        if (itemInstance!=null)
        {
          itemInstance.setInstanceId(new InternalGameId(iid));
          _itemsMgr.setGearSlot(slot,itemInstance);
        }
      }
      return itemInstance;
    }
    boolean isInBags=DatEnumsUtils.isInBags(slotCode.intValue());
    if (isInBags)
    {
      LOGGER.debug("\tIn Bags");
      ItemInstance<? extends Item> itemInstance=_itemExtractor.buildItemInstanceFromProps(props,item);
      if (itemInstance!=null)
      {
        itemInstance.setInstanceId(new InternalGameId(iid));
        // Quantity
        Integer quantityValue=(Integer)props.getProperty("Inventory_Quantity");
        if (quantityValue!=null)
        {
          LOGGER.debug("Quantity: "+quantityValue);
        }
        int quantity=(quantityValue!=null)?quantityValue.intValue():1;
        CountedItemInstance countedItemInstance=new CountedItemInstance(itemInstance,quantity);
        int index=slotCode.intValue()&0xFFFF;
        LOGGER.debug("Index: "+index+" => "+countedItemInstance);
        _itemsMgr.getBagsManager().addBagItem(countedItemInstance,index);
      }
      return itemInstance;
    }
    LOGGER.warn("Unmanaged item location! "+slotCode+" => "+props.dump());
    return null;
  }
}
