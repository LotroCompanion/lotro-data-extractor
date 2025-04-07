package delta.games.lotro.extractors.traits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.traitTree.TraitTree;
import delta.games.lotro.character.classes.traitTree.TraitTreeBranch;
import delta.games.lotro.character.classes.traitTree.TraitTreeProgression;
import delta.games.lotro.character.classes.traitTree.TraitTreesManager;
import delta.games.lotro.character.status.traitTree.TraitTreeStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extracts status of the big battles trait tree.
 * @author DAM
 */
public class BBTraitTreeExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(BBTraitTreeExtractor.class);

  /**
   * Use the given properties to load the status of BB trait tree.
   * @param properties Properties to use.
   * @return the trait tree status.
   */
  public TraitTreeStatus extract(PropertiesSet properties)
  {
    TraitTreesManager ttMgr=TraitTreesManager.getInstance();
    TraitTree traitTree=ttMgr.getTraitTreeByCode(44);
    TraitTreeStatus ret=new TraitTreeStatus(traitTree);
    for(TraitTreeBranch branch : traitTree.getBranches())
    {
      handleBranch(branch,ret,properties);
    }
    // Spent points
    Integer spentPoints=(Integer)properties.getProperty("BigBattle_TraitTree_TotalSpentPoints");
    ret.setCost(spentPoints!=null?spentPoints.intValue():0);
    // Total points
    Integer totalPoints=(Integer)properties.getProperty("BigBattle_TraitTree_TotalPoints");
    ret.setTotalPoints(totalPoints!=null?totalPoints.intValue():0);
    return ret;
  }

  private void handleBranch(TraitTreeBranch branch, TraitTreeStatus status, PropertiesSet properties)
  {
    // Fetch tiers
    int pointsSpent=0;
    for(TraitDescription trait : branch.getTraits())
    {
      String propertyName=trait.getTierPropertyName();
      if (propertyName!=null)
      {
        Integer tier=(Integer)properties.getProperty(propertyName);
        if (tier!=null)
        {
          LOGGER.debug("Trait {} => tier {} (prop={})",trait,tier,propertyName);
          pointsSpent+=tier.intValue();
          status.setRankForTrait(trait.getIdentifier(),tier.intValue());
        }
      }
    }
    // Handle expertise
    {
      TraitTreeProgression progression=branch.getProgression();
      int nbSteps=progression.getSteps().size();
      for(int i=0;i<nbSteps;i++)
      {
        Integer stepValue=progression.getSteps().get(i);
        if (pointsSpent>=stepValue.intValue())
        {
          TraitDescription trait=progression.getTraits().get(i);
          LOGGER.debug("Enough points for trait {}",trait);
          status.setRankForTrait(trait.getIdentifier(),1);
        }
      }
    }
  }
}
