package delta.games.lotro.extractors.traits;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.stats.virtues.VirtuesSet;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.VirtuesManager;

/**
 * Puts slotted virtues into character data.
 * @author DAM
 */
public class SlottedVirtuesExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SlottedVirtuesExtractor.class);

  /**
   * Handle slotted virtues.
   * @param storage Storage.
   * @param virtueIds Slotted traits IDs for each position (null=not slotted).
   */
  public void handleActiveVirtues(CharacterData storage, List<Integer> virtueIds)
  {
    VirtuesSet virtuesSet=storage.getVirtues();
    VirtuesManager virtuesMgr=VirtuesManager.getInstance();
    int index=0;
    for(Integer virtueId : virtueIds)
    {
      if (virtueId!=null)
      {
        VirtueDescription virtue=virtuesMgr.getVirtue(virtueId.intValue());
        LOGGER.debug("Virtue: {}",virtue);
        virtuesSet.setSelectedVirtue(virtue,index);
      }
      index++;
    }
  }
}
