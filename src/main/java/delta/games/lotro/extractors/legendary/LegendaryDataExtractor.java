package delta.games.lotro.extractors.legendary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.LegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.PassivesManager;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacy;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegaciesManager;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegacyTier;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.TieredNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.passives.Passive;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.lore.items.legendary.relics.RelicTypes;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.lore.items.legendary.titles.LegendaryTitle;
import delta.games.lotro.lore.items.legendary.titles.LegendaryTitlesManager;

/**
 * Legendary data extractor.
 * @author DAM
 */
public class LegendaryDataExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LegendaryDataExtractor.class);

  /**
   * Constructor.
   */
  public LegendaryDataExtractor()
  {
    // Nothing!
  }

  /**
   * Extract legendary data.
   * @param legendaryData WSL legendary data (ItemAdvancementRegistry).
   * @return A legendary data manager.
   */
  @SuppressWarnings("unchecked")
  public LegendaryDataManager extract(ClassInstance legendaryData)
  {
    LegendaryDataManager ret=new LegendaryDataManager();
    // Relics registry: see RelicsInventoryExtractor
    // Legendary items
    HashMap<Long,ClassInstance> map=(HashMap<Long,ClassInstance>)legendaryData.getAttributeValue("262884911");
    if (map!=null)
    {
      for(Map.Entry<Long,ClassInstance> entry : map.entrySet())
      {
        Long iid=entry.getKey();
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Found legendary data for IID: {}",iid);
        }
        ClassInstance class2337=entry.getValue();
        LegendaryInstanceAttrs attrs=loadLegendaryAttrs(class2337);
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Legendary data: {}",attrs.dump());
        }
        ret.addLegendaryData(iid.longValue(),attrs);
      }
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  private LegendaryInstanceAttrs loadLegendaryAttrs(ClassInstance class2337)
  {
    LegendaryInstanceAttrs attrs=new LegendaryInstanceAttrs();
    // Non imbued legacies
    handleNonImbuedLegacies(class2337,attrs);
    // Slotted relics
    handleRelics(class2337,attrs);
    // Imbued legacies
    handleImbuedLegacies(class2337,attrs);
    // Title
    Integer titleId=(Integer)class2337.getAttributeValue("m_didTitle");
    if ((titleId!=null) && (titleId.intValue()!=0))
    {
      LegendaryTitlesManager titlesMgr=LegendaryTitlesManager.getInstance();
      LegendaryTitle title=titlesMgr.getLegendaryTitle(titleId.intValue());
      if (title!=null)
      {
        attrs.setTitle(title);
      }
    }
    // Passives
    List<Integer> passives=(List<Integer>)class2337.getAttributeValue("18304467");
    if (passives!=null)
    {
      PassivesManager passivesMgr=PassivesManager.getInstance();
      for(Integer passiveId : passives)
      {
        Passive passive=passivesMgr.getPassive(passiveId.intValue());
        if (passive!=null)
        {
          attrs.addPassive(passive);
        }
      }
    }
    // XP before imbuement?
    // LONG 121029072 = 1980000
    // Index of slot in the 'Legendary Items' panel (starting at 0 for item #1)
    Integer slotIndex=(Integer)class2337.getAttributeValue("57320212");
    LOGGER.debug("Slot: {}",slotIndex);
    return attrs;
  }

  private void handleNonImbuedLegacies(ClassInstance class2337, LegendaryInstanceAttrs attrs)
  {
    @SuppressWarnings("unchecked")
    Map<Integer,Integer> subMap=(Map<Integer,Integer>)class2337.getAttributeValue("224277884");
    NonImbuedLegendaryInstanceAttrs nonImbuedAttrs=attrs.getNonImbuedAttrs();
    int index=0;
    for(Map.Entry<Integer,Integer> subEntry : subMap.entrySet())
    {
      int legacyId=subEntry.getKey().intValue();
      int rank=subEntry.getValue().intValue();
      Object legacy=getNonImbuedLegacy(legacyId);
      if (legacy instanceof DefaultNonImbuedLegacy)
      {
        DefaultNonImbuedLegacyInstance defaultLegacyInstance=nonImbuedAttrs.getDefaultLegacy();
        defaultLegacyInstance.setLegacy((DefaultNonImbuedLegacy)legacy);
        defaultLegacyInstance.setRank(rank);
      }
      else if (legacy instanceof NonImbuedLegacyTier)
      {
        TieredNonImbuedLegacyInstance legacyInstance=nonImbuedAttrs.getLegacy(index);
        legacyInstance.setLegacyTier((NonImbuedLegacyTier)legacy);
        legacyInstance.setRank(rank);
        index++;
      }
    }
  }

  private void handleRelics(ClassInstance class2337, LegendaryInstanceAttrs attrs)
  {
    @SuppressWarnings("unchecked")
    Map<Integer,Integer> slottedRelics=(Map<Integer,Integer>)class2337.getAttributeValue("40715683");
    if (slottedRelics!=null)
    {
      RelicsManager relicsMgr=RelicsManager.getInstance();
      for(Map.Entry<Integer,Integer> slottedRelic : slottedRelics.entrySet())
      {
        int relicId=slottedRelic.getKey().intValue();
        Integer slotId=slottedRelic.getValue();
        Relic relic=relicsMgr.getById(relicId);
        if (relic!=null)
        {
          RelicType type=getRelicTypeFromSlotId(slotId.intValue());
          if (type!=null)
          {
            attrs.getRelicsSet().slotRelic(relic,type);
          }
          if (LOGGER.isDebugEnabled())
          {
            LOGGER.debug("Found relic: {} on slot {}",relic.getName(),slotId);
          }
        }
      }
    }
  }

  private RelicType getRelicTypeFromSlotId(int slotId)
  {
    if (slotId==1) return RelicTypes.SETTING;
    if (slotId==2) return RelicTypes.GEM;
    if (slotId==3) return RelicTypes.RUNE;
    if (slotId==4) return RelicTypes.CRAFTED_RELIC;
    return null;
  }

  private void handleImbuedLegacies(ClassInstance class2337, LegendaryInstanceAttrs attrs)
  {
    @SuppressWarnings("unchecked")
    List<ClassInstance> imbuedLegacies=(List<ClassInstance>)class2337.getAttributeValue("20012243");
    if (imbuedLegacies!=null)
    {
      ImbuedLegendaryInstanceAttrs imbuedAttrs=attrs.getImbuedAttrs();
      LegaciesManager legaciesMgr=LegaciesManager.getInstance();
      int index=0;
      for(ClassInstance imbuedLegacy : imbuedLegacies)
      {
        Integer legacyId=(Integer)imbuedLegacy.getAttributeValue("162330420");
        if (legacyId!=null)
        {
          ImbuedLegacyInstance imbuedLegacyInstance=imbuedAttrs.getLegacy(index);
          ImbuedLegacy legacy=legaciesMgr.getLegacy(legacyId.intValue());
          imbuedLegacyInstance.setLegacy(legacy);
          Long xp=(Long)imbuedLegacy.getAttributeValue("121029072");
          if (xp!=null)
          {
            imbuedLegacyInstance.setXp(xp.intValue());
          }
          Integer unlockedLevels=(Integer)imbuedLegacy.getAttributeValue("219149635");
          if (unlockedLevels!=null)
          {
            imbuedLegacyInstance.setUnlockedLevels(unlockedLevels.intValue());
          }
        }
        index++;
      }
    }
  }

  private Object getNonImbuedLegacy(int legacyId)
  {
    NonImbuedLegaciesManager nonImbuedLegaciesMgr=NonImbuedLegaciesManager.getInstance();
    Object ret=nonImbuedLegaciesMgr.getDefaultLegacy(legacyId);
    if (ret==null)
    {
      ret=nonImbuedLegaciesMgr.getLegacyTier(legacyId);
    }
    if (ret==null)
    {
      LOGGER.warn("Non imbued legacy non found: {}",Integer.valueOf(legacyId));
    }
    return ret;
  }
}
