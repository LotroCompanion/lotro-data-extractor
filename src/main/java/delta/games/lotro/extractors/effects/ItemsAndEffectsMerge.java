package delta.games.lotro.extractors.effects;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.extractors.items.ItemsData;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;

/**
 * Merge effects into items.
 * @author DAM
 */
public class ItemsAndEffectsMerge
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ItemsAndEffectsMerge.class);

  /**
   * Merge effects data on item instances.
   * @param effects Effects.
   * @param itemsMgr Items to use.
   */
  public void mergeEffects(EffectsData effects, ItemsData itemsMgr)
  {
    List<Long> itemIids=effects.getKnownItems();
    for(Long itemIid : itemIids)
    {
      ItemInstance<? extends Item> itemInstance=itemsMgr.findItemByIid(itemIid.longValue());
      if (itemInstance!=null)
      {
        List<SingleEffectData> itemEffects=effects.getEffectsForItem(itemIid.longValue());
        mergeItemEffect(itemEffects,itemInstance);
        effects.consumeItemEffects(itemIid);
      }
    }
  }

  private void mergeItemEffect(List<SingleEffectData> effects, ItemInstance<? extends Item> itemInstance)
  {
    // Ignore items with multiple effects
    int nbEffects=effects.size();
    if (nbEffects!=1)
    {
      return;
    }
    mergeItemAndEffect(itemInstance,effects.get(0));
  }

  private void mergeItemAndEffect(ItemInstance<? extends Item> itemInstance, SingleEffectData effect)
  {
    String itemName=itemInstance.getName();
    Integer defaultItemLevel=itemInstance.getItemLevelForStats();
    if (defaultItemLevel==null)
    {
      return;
    }
    int spellCraft=(int)effect.getSpellCraft();
    if (spellCraft!=defaultItemLevel.intValue())
    {
      int itemLevel=spellCraft;
      Integer offset=itemInstance.getReference().getItemLevelOffset();
      if (offset!=null)
      {
        itemLevel-=offset.intValue();
      }
      LOGGER.info("Updating the item level for item name={}, itemLevel={}, spellcraft={}",itemName,Integer.valueOf(itemLevel),Integer.valueOf(spellCraft));
      itemInstance.setItemLevel(Integer.valueOf(itemLevel));
      itemInstance.updateAutoStats();
    }
  }
}
