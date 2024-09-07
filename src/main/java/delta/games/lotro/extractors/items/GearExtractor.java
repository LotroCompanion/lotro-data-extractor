package delta.games.lotro.extractors.items;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.gear.CharacterGear;
import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.character.gear.GearSlotContents;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Extractor for gear data using items loaded from memory.
 * @author DAM
 */
public class GearExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(GearExtractor.class);

  private MemoryItemsManager _itemsMgr;

  /**
   * Constructor.
   * @param itemsMgr Loaded items manager.
   */
  public GearExtractor(MemoryItemsManager itemsMgr)
  {
    _itemsMgr=itemsMgr;
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
    List<MemoryItem> equippedItems=_itemsMgr.getEquippedItems();
    for(MemoryItem equippedItem : equippedItems)
    {
      Integer slotCode=equippedItem.getSlotCode();
      GearSlot slot=DatEnumsUtils.getEquipmentSlot(slotCode.intValue());
      if (slot!=null)
      {
        ItemInstance<? extends Item> itemInstance=equippedItem.getItem();
        long iid=itemInstance.getInstanceId().asLong();
        LOGGER.debug("\t"+slot+" => "+iid);
        boolean ok=gearRegistry.hasIID(iid);
        if (ok)
        {
          GearSlotContents contents=gear.getSlotContents(slot,true);
          contents.setItem(itemInstance);
        }
      }
    }
  }
}
