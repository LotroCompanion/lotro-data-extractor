package delta.games.lotro.extractors.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.CharacterEquipment;
import delta.games.lotro.character.CharacterEquipment.EQUIMENT_SLOT;
import delta.games.lotro.character.CharacterEquipment.SlotContents;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.lore.items.CountedItemInstance;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.legendary.LegendaryInstance;
import delta.games.lotro.lore.items.legendary.LegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.TieredNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.relics.RelicsSet;

/**
 * Items manager for a character.
 * @author DAM
 */
public class CharacterItemsManager
{
  private Map<Integer,CountedItemInstance> _bag;
  private CharacterEquipment _gear;

  /**
   * Constructor.
   * @param gear Character gear.
   */
  public CharacterItemsManager(CharacterEquipment gear)
  {
    _bag=new HashMap<Integer,CountedItemInstance>();
    _gear=gear;
  }

  /**
   * Set the item instance for a gear slot.
   * @param slot Targeted slot.
   * @param itemInstance Item instance to set.
   */
  public void setGearSlot(EQUIMENT_SLOT slot, ItemInstance<? extends Item> itemInstance)
  {
    SlotContents contents=_gear.getSlotContents(slot,true);
    contents.setItem(itemInstance);
  }

  /**
   * Get the slot for the given item instance.
   * @param itemInstance Item instance to use.
   * @return A slot or <code>null</code> if not found.
   */
  public EQUIMENT_SLOT getSlotForItem(ItemInstance<? extends Item> itemInstance)
  {
    for(EQUIMENT_SLOT slot : EQUIMENT_SLOT.values())
    {
      SlotContents contents=_gear.getSlotContents(slot,false);
      if (contents!=null)
      {
        ItemInstance<? extends Item> currentInstance=contents.getItem();
        if (itemInstance==currentInstance)
        {
          return slot;
        }
      }
    }
    return null;
  }

  /**
   * Merge legenday data.
   * @param itemInstance Targeted item instance.
   * @param from Legenday data to use.
   */
  public void mergeLegendaryData(ItemInstance<? extends Item> itemInstance, LegendaryInstanceAttrs from)
  {
    if (!(itemInstance instanceof LegendaryInstance))
    {
      return;
    }
    LegendaryInstance legendaryInstance=(LegendaryInstance)itemInstance;
    LegendaryInstanceAttrs to=legendaryInstance.getLegendaryAttributes();
    // Relics
    RelicsSet toRelics=to.getRelicsSet();
    RelicsSet fromRelics=from.getRelicsSet();
    toRelics.setSetting(fromRelics.getSetting());
    toRelics.setGem(fromRelics.getGem());
    toRelics.setRune(fromRelics.getRune());
    toRelics.setCraftedRelic(fromRelics.getCraftedRelic());
    // Title
    to.setTitle(from.getTitle());
    // Passives
    to.removeAllPassvies();
    for(Effect passive : from.getPassives())
    {
      to.addPassive(passive);
    }
    // Legacies
    // - imbued
    {
      ImbuedLegendaryInstanceAttrs fromImbuedAttrs=from.getImbuedAttrs();
      ImbuedLegendaryInstanceAttrs toImbuedAttrs=to.getImbuedAttrs();
      int nbLegacies=fromImbuedAttrs.getNumberOfLegacies();
      for(int i=0;i<nbLegacies;i++)
      {
        ImbuedLegacyInstance fromLegacy=fromImbuedAttrs.getLegacy(i);
        ImbuedLegacyInstance toLegacy=toImbuedAttrs.getLegacy(i);
        toLegacy.setLegacy(fromLegacy.getLegacy());
        toLegacy.setUnlockedLevels(fromLegacy.getUnlockedLevels());
        toLegacy.setXp(fromLegacy.getXp());
      }
    }
    // - non-imbued
    NonImbuedLegendaryInstanceAttrs fromNonImbuedAttrs=from.getNonImbuedAttrs();
    NonImbuedLegendaryInstanceAttrs toNonImbuedAttrs=to.getNonImbuedAttrs();
    // Default legacy
    DefaultNonImbuedLegacyInstance defaultLegacyInstance=fromNonImbuedAttrs.getDefaultLegacy();
    DefaultNonImbuedLegacy defaultLegacy=defaultLegacyInstance.getLegacy();
    if (defaultLegacy!=null)
    {
      DefaultNonImbuedLegacyInstance toDefaultLegacyInstance=toNonImbuedAttrs.getDefaultLegacy();
      toDefaultLegacyInstance.setLegacy(defaultLegacy);
      toDefaultLegacyInstance.setRank(defaultLegacyInstance.getRank());
    }
    // Tiered legacies
    int nbLegacies=fromNonImbuedAttrs.getLegacies().size();
    for(int i=0;i<nbLegacies;i++)
    {
      TieredNonImbuedLegacyInstance toLegacy=toNonImbuedAttrs.getLegacy(i);
      TieredNonImbuedLegacyInstance fromLegacy=fromNonImbuedAttrs.getLegacy(i);
      toLegacy.setLegacyTier(fromLegacy.getLegacyTier());
      toLegacy.setRank(fromLegacy.getRank());
    }
  }

  /**
   * Add a bag item.
   * @param itemInstance Item to add in bag.
   * @param index Position in bag.
   */
  public void addBagItem(CountedItemInstance itemInstance, int index)
  {
    _bag.put(Integer.valueOf(index),itemInstance);
  }

  /**
   * Dump the contents of this aggregator.
   */
  public void dumpContents()
  {
    System.out.println("Gear:");
    for(EQUIMENT_SLOT slot : EQUIMENT_SLOT.values())
    {
      SlotContents contents=_gear.getSlotContents(slot,false);
      if (contents!=null)
      {
        ItemInstance<? extends Item> itemInstance=contents.getItem();
        if (itemInstance!=null)
        {
          System.out.println("Slot "+slot+": "+itemInstance.dump());
        }
      }
    }
    //System.out.println("Legendary data: "+_mapItemIIDToLegendaryData);
    //System.out.println("Slot data: "+_mapSlotToItemIID);
    System.out.println("Bags:");
    List<Integer> positions=new ArrayList<Integer>(_bag.keySet());
    Collections.sort(positions);
    for(Integer position : positions)
    {
      CountedItemInstance itemInstance=_bag.get(position);
      System.out.println("\t"+position+" => "+itemInstance);
    }
  }
}
