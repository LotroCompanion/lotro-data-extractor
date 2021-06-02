package delta.games.lotro.extractors.items;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterEquipment.EQUIMENT_SLOT;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.CountedItem;
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
  private CharacterGearRegistry _gearRegistry;

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
   * Set the associated gear registry, if any.
   * @param gearRegistry Registry to set.
   */
  public void setGearRegistry(CharacterGearRegistry gearRegistry)
  {
    _gearRegistry=gearRegistry;
  }

  /**
   * Use an item entity.
   * @param did Item DID.
   * @param iid Instance identifier.
   * @param props Entity properties.
   * @return the new instance or <code>null</code> if not used.
   */
  public ItemInstance<? extends Item> useItemEntity(int did, long iid, PropertiesSet props)
  {
    ItemInstance<? extends Item> itemInstance=extractItemInstance(did,iid,props);
    if (itemInstance!=null)
    {
      return useItem(itemInstance,props);
    }
    return null;
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

  /**
   * Use an item.
   * @param itemInstance Item instance.
   * @param props Entity properties.
   * @return the used item or <code>null</code> if not used.
   */
  public ItemInstance<? extends Item> useItem(ItemInstance<? extends Item> itemInstance, PropertiesSet props)
  {
    Integer slotCode=(Integer)props.getProperty("Container_Slot");
    if ((slotCode==null) || (slotCode.intValue()==0))
    {
      return null;
    }
    boolean isEquipped=DatEnumsUtils.isEquipped(slotCode.intValue());
    if (isEquipped)
    {
      EQUIMENT_SLOT slot=DatEnumsUtils.getEquipmentSlot(slotCode.intValue());
      if (slot!=null)
      {
        LOGGER.debug("\t"+slot);
        long iid=itemInstance.getInstanceId().asLong();
        System.out.println("Testing IID "+iid+" for slot "+slot);
        if (shallUseItem(iid))
        {
          _itemsMgr.setGearSlot(slot,itemInstance);
        }
      }
      return itemInstance;
    }
    boolean isInBags=DatEnumsUtils.isInBags(slotCode.intValue());
    if (isInBags)
    {
      LOGGER.debug("\tIn Bags");
      // Quantity
      Integer quantityValue=(Integer)props.getProperty("Inventory_Quantity");
      if (quantityValue!=null)
      {
        LOGGER.debug("Quantity: "+quantityValue);
      }
      int quantity=(quantityValue!=null)?quantityValue.intValue():1;
      CountedItem<ItemInstance<? extends Item>> countedItemInstance=new CountedItem<ItemInstance<? extends Item>>(itemInstance,quantity);
      int index=slotCode.intValue()&0xFFFF;
      LOGGER.debug("Index: "+index+" => "+countedItemInstance);
      _itemsMgr.getBagsManager().addBagItem(countedItemInstance,index);
      return itemInstance;
    }
    LOGGER.warn("Unmanaged item location! "+slotCode+" => "+props.dump());
    return null;
  }

  private boolean shallUseItem(long iid)
  {
    if (_gearRegistry==null)
    {
      return true;
    }
    return _gearRegistry.hasIID(iid);
  }
}
