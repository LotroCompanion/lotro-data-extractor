package delta.games.lotro.extractors.legendary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.lore.items.legendary.titles.LegendaryTitle;
import delta.games.lotro.lore.items.legendary.titles.LegendaryTitlesManager;

/**
 * Legendary data extractor.
 * @author DAM
 */
public class LegendaryDataExtractor
{
  private static final Logger LOGGER=Logger.getLogger(LegendaryDataExtractor.class);

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
    // Relics registry
    HashMap<Integer,Integer> relicsCount=(HashMap<Integer,Integer>)legendaryData.getAttributeValue("135264115");
    if (relicsCount!=null)
    {
      for(Map.Entry<Integer,Integer> relicEntry : relicsCount.entrySet())
      {
        int relicId=relicEntry.getKey().intValue();
        int count=relicEntry.getValue().intValue();
        Relic relic=RelicsManager.getInstance().getById(relicId);
        if (relic!=null)
        {
          LOGGER.debug("Relic: "+relic.getName()+" => "+count);
        }
      }
    }
    // Legendary items
    HashMap<Long,ClassInstance> map=(HashMap<Long,ClassInstance>)legendaryData.getAttributeValue("262884911");
    if (map!=null)
    {
      for(Map.Entry<Long,ClassInstance> entry : map.entrySet())
      {
        long iid=entry.getKey().longValue();
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Found legendary data for IID: "+iid);
        }
        ClassInstance class2337=entry.getValue();
        LegendaryInstanceAttrs attrs=loadLegendaryAttrs(class2337);
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Legendary data: "+attrs.dump());
        }
        ret.addLegendaryData(iid,attrs);
      }
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  private LegendaryInstanceAttrs loadLegendaryAttrs(ClassInstance class2337)
  {
    LegendaryInstanceAttrs attrs=new LegendaryInstanceAttrs();
    // Non imbued legacies
    {
      NonImbuedLegendaryInstanceAttrs nonImbuedAttrs=attrs.getNonImbuedAttrs();
      Map<Integer,Integer> subMap=(Map<Integer,Integer>)class2337.getAttributeValue("224277884");
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
    // Slotted relics
    Map<Integer,Integer> slottedRelics=(Map<Integer,Integer>)class2337.getAttributeValue("40715683");
    if (slottedRelics!=null)
    {
      RelicsManager relicsMgr=RelicsManager.getInstance();
      for(Map.Entry<Integer,Integer> slottedRelic : slottedRelics.entrySet())
      {
        int relicId=slottedRelic.getKey().intValue();
        int slotId=slottedRelic.getValue().intValue();
        Relic relic=relicsMgr.getById(relicId);
        if (relic!=null)
        {
          if (slotId==1) attrs.getRelicsSet().setSetting(relic);
          if (slotId==2) attrs.getRelicsSet().setGem(relic);
          if (slotId==3) attrs.getRelicsSet().setRune(relic);
          if (slotId==4) attrs.getRelicsSet().setCraftedRelic(relic);
          if (LOGGER.isDebugEnabled())
          {
            LOGGER.debug("Found relic: "+relic.getName()+" on slot "+slotId);
          }
        }
      }
    }
    // Imbued legacies
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
    LOGGER.debug("Slot: "+slotIndex);
    return attrs;
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
      LOGGER.warn("Non imbued legacy non found: "+legacyId);
    }
    return ret;
  }
}
