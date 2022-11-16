package delta.games.lotro.extractors.traits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TraitNature;
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
  private LotroEnum<TraitNature> _traitNatureEnum;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public TraitsExtractor(DataFacade facade)
  {
    _facade=facade;
    _traitAcquisitionType=_facade.getEnumsManager().getEnumMapper(587202676);
    _traitNatureEnum=LotroEnumsRegistry.getInstance().get(TraitNature.class);
  }

  /**
   * Extract data from the traits registry.
   * @param traitsRegistry WSL traits registry.
   * @return the loaded data.
   */
  public TraitsData handleTraits(ClassInstance traitsRegistry)
  {
    TraitsData ret=new TraitsData();
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
      handleTraitPool(key,persistentTraitPool,ret);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  private void handleTraitPool(int key, ClassInstance traitPool, TraitsData storage)
  {
    Integer natureCode=(Integer)traitPool.getAttributeValue("m_eNature");
    if (natureCode==null)
    {
      return;
    }
    TraitNature nature=_traitNatureEnum.getEntry(natureCode.intValue());
    if (nature==null)
    {
      return;
    }
    LOGGER.debug("Pool: key="+key+", code="+natureCode+", nature="+nature);
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
    for(Integer id : ids)
    {
      storage.addAcquiredTrait(id.intValue());
    }
    // Slotted traits
    List<List<Integer>> slotted=(List<List<Integer>>)traitPool.getAttributeValue("m_arSlottedTraits");
    List<Integer> traitIDs=getTraitIDList(slotted);
    // See TraitNature enum (587202647)
    // 1 -> null Characteristic: Novice, Riding, Conqueror of the Watching-stones, crafting proficiencies
    // 2 -> 3x0 // Legendary => old traits system?
    // 3 -> 7x0 // Class => old traits system?
    // 4 -> 5 Race
    // 5 -> 5 Active Virtues
    // 8-11: Skirmish traits, see https://lotro-wiki.com/index.php/Item%3ASkirmish_Field_Manual_-_Traits
    //    8 -> 5 slots (Attribute)
    //    9 -> 4 slots (Skill)
    //   10 -> 3 slots (Personal)
    //   11 -> 4 slots (Training)
    // 13 -> null PvMP
    // 14 -> null Mounted Combat
    // 16 -> 7 (Mount/War-steed Appearance)
    // 23 -> null (Champion) 19-27+31,32
    // 28 -> null Class Specialization: (1 earned = the one associated with the trait tree, for instance "The Deadly Storm")
    // 29 -> null (Big Battles)
    // 30 -> null (Set Bonus)
    storage.setSlottedTraits(nature,traitIDs);
  }

  private List<Integer> getTraitIDList(List<List<Integer>> slotted)
  {
    if ((slotted==null) || (slotted.isEmpty()))
    {
      return new ArrayList<Integer>();
    }
    int size=slotted.size();
    if (size>1)
    {
      LOGGER.warn("More than one traits list: "+slotted);
    }
    return slotted.get(0);
  }
}
