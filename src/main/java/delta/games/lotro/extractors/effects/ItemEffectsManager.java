package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.extractors.items.ItemsData;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;

/**
 * Items effects manager for a character.
 * @author DAM
 */
public class ItemEffectsManager
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ItemEffectsManager.class);

  private Map<Long,List<ItemEffectData>> _mapItemIIDToEffects;

  /**
   * Constructor.
   */
  public ItemEffectsManager()
  {
    _mapItemIIDToEffects=new HashMap<Long,List<ItemEffectData>>();
  }

  /**
   * Merge effects data on item instances.
   * @param itemsMgr Items to use.
   */
  public void mergeEffects(ItemsData itemsMgr)
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
    List<ItemEffectData> effects=_mapItemIIDToEffects.get(itemIid);
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

  private void mergeItemAndEffect(ItemInstance<? extends Item> itemInstance, ItemEffectData effect)
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
      LOGGER.info("Updating the item level for item name="+itemName+", itemLevel="+itemLevel+", spellcraft="+spellCraft);
      itemInstance.setItemLevel(Integer.valueOf(itemLevel));
      itemInstance.updateAutoStats();
    }
  }

  /**
   * Add an item effect.
   * @param effect Effect to add.
   */
  public void addEffect(ItemEffectData effect)
  {
    Long itemIid=effect.getItemIid();
    List<ItemEffectData> effects=_mapItemIIDToEffects.get(itemIid);
    if (effects==null)
    {
      effects=new ArrayList<ItemEffectData>();
      _mapItemIIDToEffects.put(itemIid,effects);
    }
    effects.add(effect);
  }
}
