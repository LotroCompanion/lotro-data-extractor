package delta.games.lotro.extractors.traits;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.status.traits.skirmish.SkirmishTraitsStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.skirmish.SkirmishTraitsManager;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extractor for skirmish traits status.
 * @author DAM
 */
public class SkirmishTraitsStatusExtractor
{
  /**
   * Extract the status of skirmish traits.
   * @param traits Traits data.
   * @param playerProperties Player properties.
   * @return the loaded data.
   */
  public SkirmishTraitsStatus extract(TraitsData traits, PropertiesSet playerProperties)
  {
    SkirmishTraitsStatus ret=new SkirmishTraitsStatus();
    // Trait ranks
    extractRanks(playerProperties,ret);
    // Slotted traits
    SkirmishTraitsManager mgr=SkirmishTraitsManager.getInstance();
    for(TraitNature nature : mgr.getNatures())
    {
      List<Integer> traitIDs=traits.getSlottedTraits(nature);
      extractSlottedTraits(nature,traitIDs,ret);
    }
    return ret;
  }

  /**
   * Use the given properties to load the status of BB skirmish traits.
   * @param properties Properties to use.
   * @param status Storage for extracted data.
   */
  private void extractRanks(PropertiesSet properties, SkirmishTraitsStatus status)
  {
    SkirmishTraitsManager mgr=SkirmishTraitsManager.getInstance();
    for(TraitDescription trait : mgr.getAll())
    {
      String propertyName=trait.getTierPropertyName();
      if (propertyName==null)
      {
        continue;
      }
      Integer rank=(Integer)properties.getProperty(propertyName);
      int rankValue=(rank!=null)?rank.intValue():0;
      status.setTraitRank(trait.getIdentifier(),rankValue);
    }
  }

  /**
   * Extract slotted traits.
   * @param nature Traits nature.
   * @param traitIDs Trait IDs.
   * @param status Storage for extracted data.
   */
  private void extractSlottedTraits(TraitNature nature, List<Integer> traitIDs, SkirmishTraitsStatus status)
  {
    SkirmishTraitsManager mgr=SkirmishTraitsManager.getInstance();
    List<TraitDescription> traits=mgr.getAll(nature);
    if (traits.size()==0)
    {
      // Not skimirsh traits
      return;
    }
    int nbTraitIds=traitIDs.size();
    List<Integer> normalizedTraitIDs=new ArrayList<Integer>(nbTraitIds);
    for(int i=0;i<nbTraitIds;i++)
    {
      Integer traitID=traitIDs.get(i);
      int normalizedTraitID=(traitID!=null)?traitID.intValue():0;
      normalizedTraitIDs.add(Integer.valueOf(normalizedTraitID));
    }
    status.setSlottedTraits(nature,normalizedTraitIDs);
  }
}
