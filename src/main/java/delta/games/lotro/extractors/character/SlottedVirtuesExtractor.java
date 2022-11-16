package delta.games.lotro.extractors.character;

import java.util.List;

import org.apache.log4j.Logger;

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
  private static final Logger LOGGER=Logger.getLogger(SlottedVirtuesExtractor.class);

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
        String virtueName=(virtue!=null)?virtue.getName():"???";
        LOGGER.debug("Virtue: "+virtueName);
        virtuesSet.setSelectedVirtue(virtue,index);
      }
      index++;
    }
  }
}
