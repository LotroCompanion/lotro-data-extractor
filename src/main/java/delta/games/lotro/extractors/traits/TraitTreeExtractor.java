package delta.games.lotro.extractors.traits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.BasicCharacterAttributes;
import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.traitTree.TraitTree;
import delta.games.lotro.character.classes.traitTree.TraitTreeBranch;
import delta.games.lotro.character.status.traitTree.TraitTreeStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extracts buffs that come from trait trees.
 * @author DAM
 */
public class TraitTreeExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(TraitTreeExtractor.class);

  private CharacterData _storage;

  /**
   * Constructor.
   * @param storage for loaded data. 
   */
  public TraitTreeExtractor(CharacterData storage)
  {
    _storage=storage;
  }

  /**
   * Use the given properties to setup buffs from trait trees.
   * @param properties Properties to use.
   */
  public void useProperties(PropertiesSet properties)
  {
    BasicCharacterAttributes attrs=_storage.getSummary();
    ClassDescription characterClass=attrs.getCharacterClass();
    TraitTree traitTree=characterClass.getTraitTree();
    TraitTreeStatus status=extractTraitTree(traitTree,properties);
    if (status!=null)
    {
      // Cost
      Integer costValue=(Integer)properties.getProperty("Trait_TraitTreeUI_TotalSpentPoints");
      int expectedCost=(costValue!=null?costValue.intValue():0);
      int actualCost=status.getCost();
      if (actualCost!=expectedCost)
      {
        LOGGER.warn("Actual cost difference: expected="+expectedCost+", actual="+actualCost);
      }
      // Total points
      Integer totalPointsValue=(Integer)properties.getProperty("Trait_TraitTreeUI_TotalPoints");
      int totalPoints=(totalPointsValue!=null?totalPointsValue.intValue():0);
      status.setTotalPoints(totalPoints);
    }
    // Set trait tree status
    _storage.getTraits().setTraitTreeStatus(status);
  }

  private TraitTreeStatus extractTraitTree(TraitTree traitTree, PropertiesSet properties)
  {
    TraitTreeStatus ret=new TraitTreeStatus(traitTree);
    Integer selectedBranchCode=(Integer)properties.getProperty("Trait_TraitTree_Class_SpecializationBranch");
    if (selectedBranchCode==null)
    {
      return ret;
    }
    TraitTreeBranch selectedBranch=traitTree.getBranchByCode(selectedBranchCode.intValue());
    ret.setSelectedBranch(selectedBranch);

    for(TraitTreeBranch branch : traitTree.getBranches())
    {
      for(TraitDescription trait : branch.getTraits())
      {
        String propertyName=trait.getTierPropertyName();
        if (propertyName!=null)
        {
          Integer tier=(Integer)properties.getProperty(propertyName);
          if (tier!=null)
          {
            LOGGER.debug("Trait "+trait.getName()+" => tier "+tier+" (prop="+propertyName+")");
            int traitID=trait.getIdentifier();
            ret.setRankForTrait(traitID,tier.intValue());
          }
        }
      }
    }
    int cost=ret.computeCost();
    ret.setCost(cost);
    return ret;
  }
}
