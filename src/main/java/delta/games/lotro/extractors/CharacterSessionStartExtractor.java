package delta.games.lotro.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.character.stats.buffs.BuffsManager;
import delta.games.lotro.character.stats.tomes.StatTome;
import delta.games.lotro.character.stats.tomes.StatTomesManager;
import delta.games.lotro.character.stats.tomes.TomesSet;
import delta.games.lotro.character.stats.virtues.VirtuesSet;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.VirtuesManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;

/**
 * Character session start extractor.
 * @author DAM
 */
public class CharacterSessionStartExtractor
{
  private static final Logger LOGGER=Logger.getLogger(CharacterSessionStartExtractor.class);

  private DataFacade _facade;
  private EnumMapper _traitAcquisitionType;
  private EnumMapper _traitNature;
  private EnumMapper _titleAcquisitionType;
  private CharacterData _storage;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param storage for loaded data. 
   */
  public CharacterSessionStartExtractor(DataFacade facade, CharacterData storage)
  {
    _facade=facade;
    _traitAcquisitionType=_facade.getEnumsManager().getEnumMapper(587202676);
    _traitNature=_facade.getEnumsManager().getEnumMapper(587202647);
    _titleAcquisitionType=_facade.getEnumsManager().getEnumMapper(587202681);
    _storage=storage;
  }

  /**
   * Extract titles.
   * @param titlesRegistry WSL titles registry.
   */
  public void handleTitles(ClassInstance titlesRegistry)
  {
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> titlesStatus=(Map<Integer,ClassInstance>)titlesRegistry.getAttributeValue("m_rhTitles");
    int size=titlesStatus.size();
    LOGGER.debug("Nb entries: "+size);
    for(Map.Entry<Integer,ClassInstance> entry : titlesStatus.entrySet())
    {
      int titleId=entry.getKey().intValue();
      ClassInstance titleAcquisitionData=entry.getValue();
      handleTitleStatus(titleId,titleAcquisitionData);
    }
  }

  private void handleTitleStatus(int titleId, ClassInstance titleAcquisitionData)
  {
    TitlesManager titles=TitlesManager.getInstance();
    TitleDescription title=titles.getTitle(titleId);
    LOGGER.debug("Title name: "+title.getName());
    int acquisitionDate=((Double)titleAcquisitionData.getAttributeValue("m_ttAcquired")).intValue();
    //Date date=TimeUtils.getDate((int)(acquisitionDate*3.6));
    LOGGER.debug("\tDate: raw="+acquisitionDate);
    int acquisitionType=((Integer)titleAcquisitionData.getAttributeValue("m_eAcquisitionType")).intValue();
    LOGGER.debug("\tAcquisition type: "+_titleAcquisitionType.getString(acquisitionType));
  }

  /**
   * Extract traits.
   * @param traitsRegistry WSL traits registry.
   */
  public void handleTraits(ClassInstance traitsRegistry)
  {
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> traitPools=(Map<Integer,ClassInstance>)traitsRegistry.getAttributeValue("m_arhPools");
    int size=traitPools.size();
    LOGGER.debug("Nb trait pools: "+size);
    for(Map.Entry<Integer,ClassInstance> entry : traitPools.entrySet())
    {
      int key=entry.getKey().intValue();
      ClassInstance persistentTraitPool=entry.getValue();
      handleTraitPool(key,persistentTraitPool);
    }
  }

  @SuppressWarnings("unchecked")
  private void handleTraitPool(int key, ClassInstance traitPool)
  {
    int nature=((Integer)traitPool.getAttributeValue("m_eNature")).intValue();
    String natureLabel=_traitNature.getString(nature);
    LOGGER.debug("Pool: key="+key+", nature="+nature+", name="+natureLabel);
    List<ClassInstance> earnedTraitInfos=(List<ClassInstance>)traitPool.getAttributeValue("m_rlTraitInfo");
    List<Integer> sortedIds2=new ArrayList<Integer>();
    for(ClassInstance earnedTraitInfo : earnedTraitInfos)
    {
      int traitId=((Integer)earnedTraitInfo.getAttributeValue("m_didTrait")).intValue();
      sortedIds2.add(Integer.valueOf(traitId));
      handleTrait(traitId);
      TraitsManager traitsMgr=TraitsManager.getInstance();
      TraitDescription trait=traitsMgr.getTrait(traitId);
      String traitName=(trait!=null)?trait.getName():"???";
      int sourceType=((Integer)earnedTraitInfo.getAttributeValue("m_eSourceType")).intValue();
      String sourceName=_traitAcquisitionType.getString(sourceType);
      LOGGER.debug("\tTrait ID="+traitId+", name: "+traitName+", source: "+sourceName);
    }
    // Attribute 242459635 contains a list of earned trait identifiers
    // We check that is has the same contents as the previous list
    Set<Integer> ids=(Set<Integer>)traitPool.getAttributeValue("242459635");
    List<Integer> sortedIds1=new ArrayList<Integer>(ids);
    Collections.sort(sortedIds1);
    Collections.sort(sortedIds2);
    if (!sortedIds1.equals(sortedIds2))
    {
      LOGGER.warn("Size: "+ids.size()+", "+earnedTraitInfos.size());
    }
    // Slotted traits
    List<List<Integer>> slotted=(List<List<Integer>>)traitPool.getAttributeValue("m_arSlottedTraits");
    // See TraitNature enum (587202647)
    // 1 -> null 
    // 2 -> 3x0
    // 3 -> 7x0
    // 4 -> 5 (Racial)
    // 5 -> 5 (Virtues)
    // 8 -> 5 (Skirmish)
    // 9 -> 4x0
    // 10 -> 3x0
    // 11 -> 4x0
    // 13 -> null
    // 14 -> null
    // 16 -> 7 (War-steed Appearance)
    // 23 -> null (Champion)
    // 29 -> null (Big Battle - engineering?)
    // 28 -> null (1 earned = the one associated with the trait tree, for instance "The Deadly Storm"
    // 30 -> null (Set Bonus)
    if (key==4)
    {
      // Active racial traits
      List<Integer> racialTraitIds=slotted.get(0);
      handleRacialTraits(racialTraitIds);
    }
    if (key==5)
    {
      // Active virtues
      List<Integer> virtueIds=slotted.get(0);
      handleActiveVirtues(virtueIds);
    }
    if (key==8)
    {
      // Skirmish traits
      List<Integer> skirmishTraitIds=slotted.get(0);
      handleSkirmishTraits(skirmishTraitIds);
    }
    if (key==16)
    {
      // War-steed appearance traits
      List<Integer> warsteedAppearanceTraitIds=slotted.get(0);
      handleWarsteedAppearanceTraits(warsteedAppearanceTraitIds);
    }
  }

  private void handleTrait(int traitId)
  {
    StatTomesManager tomesManager=StatTomesManager.getInstance();
    StatTome tome=tomesManager.getStatTomeFromTraitId(traitId);
    if (tome!=null)
    {
      TomesSet tomesSet=_storage.getTomes();
      tomesSet.setTomeRank(tome.getStat(),tome.getRank());
    }
  }

  private void handleRacialTraits(List<Integer> racialTraitIds)
  {
    BuffsManager buffsMgr=_storage.getBuffs();
    BuffRegistry buffsRegistry=BuffRegistry.getInstance();
    TraitsManager traitsMgr=TraitsManager.getInstance();
    for(Integer racialTraitId : racialTraitIds)
    {
      if ((racialTraitId!=null) && (racialTraitId.intValue()!=0))
      {
        TraitDescription trait=traitsMgr.getTrait(racialTraitId.intValue());
        if (trait!=null)
        {
          String traitName=trait.getName();
          LOGGER.debug("Racial trait: "+traitName);
          String key=String.valueOf(trait.getIdentifier());
          BuffInstance buffInstance=buffsRegistry.newBuffInstance(key);
          if (buffInstance!=null)
          {
            buffsMgr.addBuff(buffInstance);
          }
        }
        else
        {
          LOGGER.warn("Racial trait not found: "+racialTraitId);
        }
      }
    }
  }

  private void handleActiveVirtues(List<Integer> virtueIds)
  {
    VirtuesSet virtuesSet=_storage.getVirtues();
    VirtuesManager virtuesMgr=VirtuesManager.getInstance();
    int index=0;
    for(Integer virtueId : virtueIds)
    {
      if (virtueId!=null)
      {
        VirtueDescription virtue=virtuesMgr.getVirtue(virtueId.intValue());
        String virtueName=(virtue!=null)?virtue.getName():"???";
        LOGGER.debug("Virtue: "+virtueName);
        virtuesSet.setSelectedVirtue(virtue,index);
      }
      index++;
    }
  }

  private void handleSkirmishTraits(List<Integer> skirmishTraitIds)
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    for(Integer skirmishTraitId : skirmishTraitIds)
    {
      if (skirmishTraitId!=null)
      {
        TraitDescription trait=traitsMgr.getTrait(skirmishTraitId.intValue());
        String traitName=(trait!=null)?trait.getName():"???";
        LOGGER.debug("Skirmish trait: "+traitName);
      }
    }
  }

  private void handleWarsteedAppearanceTraits(List<Integer> warsteedAppearanceTraitIds)
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    for(Integer warsteedAppearanceTraitId : warsteedAppearanceTraitIds)
    {
      if (warsteedAppearanceTraitId!=null)
      {
        TraitDescription trait=traitsMgr.getTrait(warsteedAppearanceTraitId.intValue());
        String traitName=(trait!=null)?trait.getName():"???";
        LOGGER.debug("War-steed appearance trait: "+traitName);
      }
    }
  }
}
