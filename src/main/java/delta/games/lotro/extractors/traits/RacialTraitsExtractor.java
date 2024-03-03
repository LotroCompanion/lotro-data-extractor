package delta.games.lotro.extractors.traits;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.character.stats.buffs.BuffsManager;
import delta.games.lotro.character.status.traits.shared.SlottedTraitsStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;

/**
 * Extractor for racial traits status.
 * @author DAM
 */
public class RacialTraitsExtractor
{
  private static final Logger LOGGER=Logger.getLogger(RacialTraitsExtractor.class);

  /**
   * Handle slotted racial traits.
   * @param storage Storage.
   * @param racialTraitIds Slotted traits IDs for each position (0=not slotted).
   */
  public void handleRacialTraits(CharacterData storage, List<Integer> racialTraitIds)
  {
    BuffsManager buffsMgr=storage.getBuffs();
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
    status.setTraits(traitIDs);
    // Acquired traits
    if (race!=null)
    {
      List<TraitDescription> earnableTraits=race.getEarnableTraits();
      for(TraitDescription earnableTrait : earnableTraits)
      {
        Integer traitID=Integer.valueOf(earnableTrait.getIdentifier());
        if (traits.getAcquiredTraits().contains(traitID))
        {
          status.addTraitID(traitID.intValue());
        }
      }
    }
    return status;
  }
}
