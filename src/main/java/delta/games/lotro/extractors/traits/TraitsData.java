package delta.games.lotro.extractors.traits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TraitNature;

/**
 * Traits-related data, as extracted from memory.
 * @author DAM
 */
public class TraitsData
{
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
}
