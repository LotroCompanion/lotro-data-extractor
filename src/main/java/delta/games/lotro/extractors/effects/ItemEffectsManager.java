package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.extractors.items.MemoryItemsManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;

/**
 * Items effects manager for a character.
 * @author DAM
 */
public class ItemEffectsManager
{
  private static final Logger LOGGER=Logger.getLogger(ItemEffectsManager.class);

  private Map<Long,List<ItemEffectRecord>> _mapItemIIDToEffects;

  /**
   * Constructor.
   */
  public ItemEffectsManager()
  {
    _mapItemIIDToEffects=new HashMap<Long,List<ItemEffectRecord>>();
  }

  /**
   * Merge effects data on item instances.
   * @param itemsMgr Items to use.
   */
  public void mergeEffects(MemoryItemsManager itemsMgr)
  {
    for(Long itemIid : _mapItemIIDToEffects.keySet())
    {
      ItemInstance<? extends Item> itemInstance=itemsMgr.findItemByIid(itemIid.longValue());
      if (itemInstance!=null)
      {
        mergeItemEffect(itemIid,itemInstance);
      }
    }
  }

  private void mergeItemEffect(Long itemIid, ItemInstance<? extends Item> itemInstance)
  {
    List<ItemEffectRecord> effects=_mapItemIIDToEffects.get(itemIid);
    if (effects==null)
    {
      return;
    }
    // Ignore items with multiple effects
    int nbEffects=effects.size();
    if (nbEffects!=1)
    {
      return;
    }
    mergeItemAndEffect(itemInstance,effects.get(0));
  }

  private void mergeItemAndEffect(ItemInstance<? extends Item> itemInstance, ItemEffectRecord effect)
  {
    String itemName=itemInstance.getName();
    Integer itemLevel=itemInstance.getEffectiveItemLevel();
    if (itemLevel==null)
    {
      return;
    }
    int spellCraft=(int)effect.getSpellCraft();
    if (spellCraft!=itemLevel.intValue())
    {
      LOGGER.info("Updating the item level for item name="+itemName+", itemLevel="+itemLevel+", spellcraft="+spellCraft);
      itemInstance.setItemLevel(Integer.valueOf(spellCraft));
      itemInstance.updateAutoStats();
    }
  }

  /**
   * Add an item effect.
   * @param effect Effect to add.
   */
  public void addEffect(ItemEffectRecord effect)
  {
    Long itemIid=Long.valueOf(effect.getItemIid());
    List<ItemEffectRecord> effects=_mapItemIIDToEffects.get(itemIid);
    if (effects==null)
    {
      effects=new ArrayList<ItemEffectRecord>();
      _mapItemIIDToEffects.put(itemIid,effects);
    }
    effects.add(effect);
  }
}
