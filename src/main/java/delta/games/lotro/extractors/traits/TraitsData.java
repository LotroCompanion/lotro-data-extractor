package delta.games.lotro.extractors.traits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.comparators.NamedComparator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.enums.comparator.LotroEnumEntryCodeComparator;
import delta.games.lotro.common.enums.comparator.LotroEnumEntryNameComparator;

/**
 * Traits-related data, as extracted from memory.
 * @author DAM
 */
public class TraitsData
{
  private static final Logger LOGGER=LoggerFactory.getLogger(TraitsData.class);

  private Map<TraitNature,List<Integer>> _slottedTraitsByNature;
  private Set<Integer> _acquiredTraits;

  /**
   * Constructor.
   */
  public TraitsData()
  {
    _slottedTraitsByNature=new HashMap<TraitNature,List<Integer>>();
    _acquiredTraits=new HashSet<Integer>();
  }

  /**
   * Get the slotted racial traits.
   * @return A list of trait IDs (empty slots are 0).
   */
  public List<Integer> getRacialTraits()
  {
    return getSlottedTraits(4);
  }

  /**
   * Get the slotted mounted appearances.
   * @return A list of trait IDs (empty slots are 0).
   */
  public List<Integer> getMountedAppearances()
  {
    return getSlottedTraits(16);
  }

  /**
   * Get the slotted virtues.
   * @return A list of trait IDs (empty slots are 0).
   */
  public List<Integer> getSlottedVirtues()
  {
    return getSlottedTraits(5);
  }

  /**
   * Get the slotted traits for a given nature.
   * @param code Trait nature code.
   * @return A list of trait IDs (empty slots are 0).
   */
  public List<Integer> getSlottedTraits(int code)
  {
    LotroEnum<TraitNature> traitNatureEnum=LotroEnumsRegistry.getInstance().get(TraitNature.class);
    TraitNature nature=traitNatureEnum.getEntry(code);
    return getSlottedTraits(nature);
  }

  /**
   * Get the slotted traits for a given nature.
   * @param nature Trait nature.
   * @return A list of trait IDs (empty slots are 0).
   */
  public List<Integer> getSlottedTraits(TraitNature nature)
  {
    List<Integer> ret=_slottedTraitsByNature.get(nature);
    if (ret==null)
    {
      ret=new ArrayList<Integer>();
    }
    return ret;
  }

  /**
   * Set the slotted traits for a given nature.
   * @param nature Trait nature.
   * @param slottedTraitIDs Traits to set (empty slots are 0).
   */
  public void setSlottedTraits(TraitNature nature, List<Integer> slottedTraitIDs)
  {
    _slottedTraitsByNature.put(nature,slottedTraitIDs);
  }

  /**
   * Get the known trait natures.
   * @return A list of trait natures, sorted by code.
   */
  public List<TraitNature> getTraitNatures()
  {
    List<TraitNature> ret=new ArrayList<TraitNature>();
    ret.addAll(_slottedTraitsByNature.keySet());
    Collections.sort(ret,new LotroEnumEntryCodeComparator<TraitNature>());
    return ret;
  }

  /**
   * Add an acquired trait.
   * @param traitID Trait identifier.
   */
  public void addAcquiredTrait(int traitID)
  {
    _acquiredTraits.add(Integer.valueOf(traitID));
  }

  /**
   * Get acquired traits.
   * @return A list of trait identifiers.
   */
  public List<Integer> getAcquiredTraits()
  {
    List<Integer> ret=new ArrayList<Integer>(_acquiredTraits);
    Collections.sort(ret);
    return ret;
  }

  /**
   * Show the managed data.
   * @param is Output stream.
   */
  public void show(IndentableStream is)
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    // Slotted traits
    LotroEnum<TraitNature> traitNatureEnum=LotroEnumsRegistry.getInstance().get(TraitNature.class);
    for(TraitNature traitNature : traitNatureEnum.getAll())
    {
      List<Integer> slottedTraits=getSlottedTraits(traitNature);
      if (slottedTraits.isEmpty())
      {
        continue;
      }
      is.println("Trait nature: "+traitNature.getLabel());
      is.incrementIndendationLevel();
      int i=1;
      for(Integer traitIdInt : slottedTraits)
      {
        is.print("#"+i+": ");
        int traitId=traitIdInt.intValue();
        if (traitId>0)
        {
          TraitDescription trait=traitsMgr.getTrait(traitId);
          String traitName=(trait!=null)?trait.toString():"???";
          is.println(traitName);
        }
        else
        {
          is.println("(empty)");
        }
        i++;
      }
      is.decrementIndentationLevel();
    }
    // Acquired traits
    List<TraitDescription> traits=new ArrayList<TraitDescription>();
    for(Integer traitId : getAcquiredTraits())
    {
      TraitDescription trait=traitsMgr.getTrait(traitId.intValue());
      if (trait!=null)
      {
        traits.add(trait);
      }
      else
      {
        LOGGER.warn("Unknown trait ID={}",traitId);
      }
    }
    is.println("Acquired traits:");
    Map<TraitNature,List<TraitDescription>> map=groupByNature(traits);
    is.incrementIndendationLevel();
    List<TraitNature> traitNatures=new ArrayList<TraitNature>(map.keySet());
    Collections.sort(traitNatures,new LotroEnumEntryNameComparator<>());
    for(TraitNature traitNature : traitNatures)
    {
      is.println("Nature: "+traitNature.getLabel());
      is.incrementIndendationLevel();
      List<TraitDescription> traitsForNature=map.get(traitNature);
      Map<SkillCategory,List<TraitDescription>> mapByCategory=groupByCategory(traitsForNature);
      List<SkillCategory> categories=new ArrayList<SkillCategory>(mapByCategory.keySet());
      Collections.sort(categories,new LotroEnumEntryNameComparator<>());
      for(SkillCategory category : categories)
      {
        List<TraitDescription> traitsForNatureAndCategory=mapByCategory.get(category);
        Collections.sort(traitsForNatureAndCategory,new NamedComparator());
        String categoryName=(category!=null)?category.getLabel():"(no category)";
        is.println("Category: "+categoryName);
        is.incrementIndendationLevel();
        for(TraitDescription trait : traitsForNatureAndCategory)
        {
          is.println(trait.toString());
        }
        is.decrementIndentationLevel();
      }
      is.decrementIndentationLevel();
    }
    is.decrementIndentationLevel();
  }

  private Map<TraitNature,List<TraitDescription>> groupByNature(List<TraitDescription> traits)
  {
    Map<TraitNature,List<TraitDescription>> map=new HashMap<TraitNature,List<TraitDescription>>();
    for(TraitDescription trait : traits)
    {
      TraitNature traitNature=trait.getNature();
      List<TraitDescription> list=map.get(traitNature);
      if (list==null)
      {
        list=new ArrayList<TraitDescription>();
        map.put(traitNature,list);
      }
      list.add(trait);
    }
    return map;
  }

  private Map<SkillCategory,List<TraitDescription>> groupByCategory(List<TraitDescription> traits)
  {
    Map<SkillCategory,List<TraitDescription>> map=new HashMap<SkillCategory,List<TraitDescription>>();
    for(TraitDescription trait : traits)
    {
      SkillCategory category=trait.getCategory();
      List<TraitDescription> list=map.get(category);
      if (list==null)
      {
        list=new ArrayList<TraitDescription>();
        map.put(category,list);
      }
      list.add(trait);
    }
    return map;
  }
}
