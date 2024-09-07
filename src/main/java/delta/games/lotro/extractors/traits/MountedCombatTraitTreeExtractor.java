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
 * Extracts status of the mounted combat trait tree.
 * @author DAM
 */
public class MountedCombatTraitTreeExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MountedCombatTraitTreeExtractor.class);

  /**
   * Use the given properties to load the status of BB trait tree.
   * @param properties Properties to use.
   * @return the trait tree status.
   */
  public TraitTreeStatus extract(PropertiesSet properties)
  {
    Integer traitTreeId=(Integer)properties.getProperty("Trait_CurrentTraitTree_MountedCombat");
    if (traitTreeId==null)
    {
      return null;
    }
    TraitTreesManager ttMgr=TraitTreesManager.getInstance();
    TraitTree traitTree=ttMgr.getTraitTree(traitTreeId.intValue());
    if (traitTree==null)
    {
      return null;
    }
    TraitTreeStatus ret=new TraitTreeStatus(traitTree);
    for(TraitTreeBranch branch : traitTree.getBranches())
    {
      handleBranch(branch,ret,properties);
    }
    // Spent points
    Integer cost=(Integer)properties.getProperty("Trait_MountedCombat_TotalSpentPoints");
    ret.setCost(cost!=null?cost.intValue():0);
    // Total points
    Integer totalPoints=(Integer)properties.getProperty("Trait_MountedCombat_TotalPoints");
    ret.setTotalPoints(totalPoints!=null?totalPoints.intValue():0);
    return ret;
  }

  private int handleBranch(TraitTreeBranch branch, TraitTreeStatus status, PropertiesSet properties)
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
          LOGGER.debug("Trait "+trait.getName()+" => tier "+tier+" (prop="+propertyName+")");
          pointsSpent+=tier.intValue();
          status.setRankForTrait(trait.getIdentifier(),tier.intValue());
        }
      }
    }
    // Handle expertise (mounted combat branches have no progression)
    {
      TraitTreeProgression progression=branch.getProgression();
      int nbSteps=progression.getSteps().size();
      for(int i=0;i<nbSteps;i++)
      {
        Integer stepValue=progression.getSteps().get(i);
        if (pointsSpent>=stepValue.intValue())
        {
          TraitDescription trait=progression.getTraits().get(i);
          LOGGER.debug("Enough points for trait "+trait.getName());
          status.setRankForTrait(trait.getIdentifier(),1);
        }
      }
    }
    return pointsSpent;
  }
}
