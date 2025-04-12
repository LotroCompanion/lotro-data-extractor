package delta.games.lotro.extractors.items;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.gear.CharacterGear;
import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.character.gear.GearSlotContents;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Extractor for equipment data using items loaded from memory.
 * @author DAM
 */
public class EquipmentExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EquipmentExtractor.class);

  private ItemsData _itemsData;

  /**
   * Constructor.
   * @param itemsData Loaded items manager.
   */
  public EquipmentExtractor(ItemsData itemsData)
  {
    _itemsData=itemsData;
  }

  /**
   * Extract gear from the loaded items.
   * @param gear Storage for gear.
   * @param playerProps Player properties.
   */
  public void extract(CharacterGear gear, PropertiesSet playerProps)
  {
    CharacterGearRegistry gearRegistry=new CharacterGearRegistry();
    gearRegistry.useProperties(playerProps);
    List<ItemData> equippedItems=_itemsData.getEquippedItems();
    for(ItemData equippedItem : equippedItems)
    {
      Integer slotCode=equippedItem.getSlotCode();
      GearSlot slot=DatEnumsUtils.getEquipmentSlot(slotCode.intValue());
      if (slot!=null)
      {
        ItemInstance<? extends Item> itemInstance=equippedItem.getItem();
        InternalGameId iid=itemInstance.getInstanceId();
        LOGGER.debug("\t{} => {}",slot,iid);
        boolean ok=gearRegistry.hasIID(iid.asLong());
        if (ok)
        {
          GearSlotContents contents=gear.getSlotContents(slot,true);
          contents.setItem(itemInstance);
        }
      }
    }
  }
}
