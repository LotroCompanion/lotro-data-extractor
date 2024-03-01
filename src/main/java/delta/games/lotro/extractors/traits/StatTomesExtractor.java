  package delta.games.lotro.extractors.traits;

import java.util.List;

import delta.games.lotro.character.stats.tomes.StatTome;
import delta.games.lotro.character.stats.tomes.StatTomesManager;
import delta.games.lotro.character.stats.tomes.TomesSet;

/**
 * Extract stat tomes status from acquired traits.
 * @author DAM
 */
public class StatTomesExtractor
{
  private TomesSet _tomesSet;

  /**
   * Constructor.
   * @param tomesSet Storage.
   */
  public StatTomesExtractor(TomesSet tomesSet)
  {
    _tomesSet=tomesSet;
  }

  /**
   * Handle acquired traits.
   * @param traitIDs Acquired traits identifiers.
   */
  public void handleTraits(List<Integer> traitIDs)
  {
    for(Integer traitID : traitIDs)
    {
      handleTrait(traitID.intValue());
    }
  }

  private void handleTrait(int traitId)
  {
    StatTomesManager tomesManager=StatTomesManager.getInstance();
    StatTome tome=tomesManager.getStatTomeFromTraitId(traitId);
    if (tome!=null)
    {
      _tomesSet.setTomeRank(tome.getStat(),tome.getRank());
    }
  }
}
