package delta.games.lotro.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

/**
 * Traits extractor.
 * @author DAM
 */
public class TraitsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(TraitsExtractor.class);

  private DataFacade _facade;
  private EnumMapper _traitAcquisitionType;
  private EnumMapper _traitNature;
  private CharacterData _storage;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param storage for loaded data. 
   */
  public TraitsExtractor(DataFacade facade, CharacterData storage)
  {
    _facade=facade;
    _traitAcquisitionType=_facade.getEnumsManager().getEnumMapper(587202676);
    _traitNature=_facade.getEnumsManager().getEnumMapper(587202647);
    _storage=storage;
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
      if (persistentTraitPool==null)
      {
        continue;
      }
      handleTraitPool(key,persistentTraitPool);
    }
  }

  @SuppressWarnings("unchecked")
  private void handleTraitPool(int key, ClassInstance traitPool)
  {
    Integer natureCode=(Integer)traitPool.getAttributeValue("m_eNature");
    if (natureCode==null)
    {
      return;
    }
    int nature=natureCode.intValue();
    String natureLabel=_traitNature.getString(nature);
    LOGGER.debug("Pool: key="+key+", nature="+nature+", name="+natureLabel);
    List<ClassInstance> earnedTraitInfos=(List<ClassInstance>)traitPool.getAttributeValue("m_rlTraitInfo");
    List<Integer> sortedIds2=new ArrayList<Integer>();
    for(ClassInstance earnedTraitInfo : earnedTraitInfos)
    {
      if (earnedTraitInfo==null)
      {
        continue;
      }
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
    if (ids==null)
    {
      ids=new HashSet<Integer>();
    }
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
    // 1 -> null Characteristic: Novice, Riding, Conqueror of the Watching-stones, crafting proficiencies
    // 2 -> 3x0 // Legendary => old traits system?
    // 3 -> 7x0 // Class => old traits system?
    // 4 -> 5 Race
    // 5 -> 5 Active Virtues
    // 8-11: Skirmish traits, see https://lotro-wiki.com/index.php/Item%3ASkirmish_Field_Manual_-_Traits
    // 8 -> 5 (Attribute)
    // 9 -> 4x0
    // 10 -> 3x0
    // 11 -> 4x0
    // 13 -> null PvMP
    // 14 -> null Mounted Combat
    // 16 -> 7 (Mount/War-steed Appearance)
    // 23 -> null (Champion) 19-27+31,32
    // 28 -> null Class Specialization: (1 earned = the one associated with the trait tree, for instance "The Deadly Storm")
    // 29 -> null (Big Battles)
    // 30 -> null (Set Bonus)
    if (key==4)
    {
      // Active racial traits
      List<Integer> racialTraitIds=getTraitIDList(slotted);
      handleRacialTraits(racialTraitIds);
    }
    if (key==5)
    {
      // Active virtues
      List<Integer> virtueIds=getTraitIDList(slotted);
      handleActiveVirtues(virtueIds);
    }
    if (key==8)
    {
      // Skirmish traits
      List<Integer> skirmishTraitIds=getTraitIDList(slotted);
      handleSkirmishTraits(skirmishTraitIds);
    }
    if (key==16)
    {
      // War-steed appearance traits
      List<Integer> warsteedAppearanceTraitIds=getTraitIDList(slotted);
      handleWarsteedAppearanceTraits(warsteedAppearanceTraitIds);
    }
  }

  private List<Integer> getTraitIDList(List<List<Integer>> slotted)
  {
    if ((slotted==null) || (slotted.size()==0))
    {
      return new ArrayList<Integer>();
    }
    return slotted.get(0);
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
