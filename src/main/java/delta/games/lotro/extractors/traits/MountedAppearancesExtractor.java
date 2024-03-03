package delta.games.lotro.extractors.traits;

import java.util.List;

import delta.games.lotro.character.status.traits.shared.SlottedTraitsStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.TraitNature;

/**
 * Extractor for mounted appearances traits status.
 * @author DAM
 */
public class MountedAppearancesExtractor
{
  /**
   * Build the mounted appearances traits status.
   * @param traits Raw traits data.
   * @return A new status.
   */
  public SlottedTraitsStatus buildStatus(TraitsData traits)
  {
    SlottedTraitsStatus status=new SlottedTraitsStatus();
    List<Integer> slottedTraits=traits.getMountedAppearances();
    int nbTraits=slottedTraits.size();
    int[] traitIDs=new int[nbTraits];
    for(int i=0;i<nbTraits;i++)
    {
      traitIDs[i]=slottedTraits.get(i).intValue();
    }
    status.setTraits(traitIDs);
    // Acquired traits
    TraitsManager traitsMgr=TraitsManager.getInstance();
    LotroEnum<TraitNature> traitNatureEnum=LotroEnumsRegistry.getInstance().get(TraitNature.class);
    TraitNature traitNature=traitNatureEnum.getEntry(16);
    List<TraitDescription> possibleTraits=traitsMgr.getTraitsForNature(traitNature);
    for(TraitDescription possibleTrait : possibleTraits)
    {
      Integer traitID=Integer.valueOf(possibleTrait.getIdentifier());
      if (traits.getAcquiredTraits().contains(traitID))
      {
        status.addTraitID(traitID.intValue());
      }
    }
    return status;
  }
}
