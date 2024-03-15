package delta.games.lotro.extractors.traits;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.character.stats.buffs.BuffsManager;
import delta.games.lotro.character.status.traits.shared.AvailableTraitsStatus;
import delta.games.lotro.character.status.traits.shared.SlottedTraitsStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;

/**
 * Extractor for racial traits status.
 * @author DAM
 */
public class RacialTraitsExtractor
{
  /**
   * Build the racial traits status.
   * @param traits Raw traits data.
   * @param race Targeted race.
   * @return A new status.
   */
  public SlottedTraitsStatus buildStatus(TraitsData traits, RaceDescription race)
  {
    // Update current racial traits
    SlottedTraitsStatus status=new SlottedTraitsStatus();
    List<Integer> racialTraits=traits.getRacialTraits();
    int nbTraits=racialTraits.size();
    int[] traitIDs=new int[nbTraits];
    for(int i=0;i<nbTraits;i++)
    {
      traitIDs[i]=racialTraits.get(i).intValue();
    }
    status.getSlotsStatus().setTraits(traitIDs);
    // Acquired traits
    if (race!=null)
    {
      AvailableTraitsStatus availableTraits=status.getAvailableTraitsStatus();
      List<TraitDescription> earnableTraits=race.getEarnableTraits();
      for(TraitDescription earnableTrait : earnableTraits)
      {
        Integer traitID=Integer.valueOf(earnableTrait.getIdentifier());
        if (traits.getAcquiredTraits().contains(traitID))
        {
          availableTraits.addTraitID(traitID.intValue());
        }
      }
    }
    return status;
  }
}
