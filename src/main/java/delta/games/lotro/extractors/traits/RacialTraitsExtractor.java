package delta.games.lotro.extractors.traits;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.character.stats.buffs.BuffsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;

/**
 * Puts racial traits into character data.
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
}
