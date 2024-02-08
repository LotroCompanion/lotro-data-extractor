package delta.games.lotro.extractors.items;

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
import delta.games.lotro.lore.items.legendary.passives.Passive;
import delta.games.lotro.lore.items.legendary.relics.RelicsSet;

/**
 * Utility methods related to legendary data import.
 * @author DAM
 */
public class LegendaryDataUtils
{
  /**
   * Merge legendary data.
   * @param itemInstance Targeted item instance.
   * @param from Legendary data to use.
   */
  public static void mergeLegendaryData(ItemInstance<? extends Item> itemInstance, LegendaryInstanceAttrs from)
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
    for(Passive passive : from.getPassives())
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
}
