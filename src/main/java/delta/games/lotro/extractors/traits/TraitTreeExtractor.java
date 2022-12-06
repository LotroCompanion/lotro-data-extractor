package delta.games.lotro.extractors.traits;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.BasicCharacterAttributes;
import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.traitTree.TraitTree;
import delta.games.lotro.character.classes.traitTree.TraitTreeBranch;
import delta.games.lotro.character.classes.traitTree.TraitTreeProgression;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.character.stats.buffs.BuffsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extracts buffs that come from trait trees.
 * @author DAM
 */
public class TraitTreeExtractor
{
  private static final Logger LOGGER=Logger.getLogger(TraitTreeExtractor.class);

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
    CharacterClass characterClass=attrs.getCharacterClass();
    ClassesManager classesMgr=ClassesManager.getInstance();
    ClassDescription classDescription=classesMgr.getClassDescription(characterClass);
    TraitTree traitTree=classDescription.getTraitTree();
    fetchTiersForTraitTree(traitTree,properties);
    // TODO Spent points/total points
    /*
    Integer cost=(Integer)properties.getProperty("Trait_TraitTreeUI_TotalSpentPoints");
    ret.setCost(cost!=null?cost.intValue():0);
    // Total points
    Integer totalPoints=(Integer)properties.getProperty("Trait_TraitTreeUI_TotalPoints");
    ret.setTotalPoints(totalPoints!=null?totalPoints.intValue():0);
    */
  }

  private void fetchTiersForTraitTree(TraitTree traitTree, PropertiesSet properties)
  {
    Integer selectedBranch=(Integer)properties.getProperty("Trait_TraitTree_Class_SpecializationBranch");

    BuffsManager buffsMgr=_storage.getBuffs();
    BuffRegistry buffsRegistry=BuffRegistry.getInstance();
    Map<Integer,Integer> ranksMap=new HashMap<Integer,Integer>();
    for(TraitTreeBranch branch : traitTree.getBranches())
    {
      Map<Integer,Integer> foundTraits=handleBranch(branch,selectedBranch,properties);
      ranksMap.putAll(foundTraits);
    }
    for(TraitDescription trait : traitTree.getAllTraits())
    {
      int traitID=trait.getIdentifier();
      Integer rank=ranksMap.get(Integer.valueOf(traitID));
      if (rank!=null)
      {
        BuffInstance buffInstance=buffsRegistry.newBuffInstance(String.valueOf(traitID));
        if (buffInstance!=null)
        {
          buffInstance.setTier(rank);
          buffsMgr.addBuff(buffInstance);
        }
      }
    }
  }

  private Map<Integer,Integer> handleBranch(TraitTreeBranch branch, Integer selectedBranch, PropertiesSet properties)
  {
    Map<Integer,Integer> ret=new HashMap<Integer,Integer>();
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
          Integer key=Integer.valueOf(trait.getIdentifier());
          ret.put(key,tier);
        }
      }
    }
    if ((selectedBranch!=null) && (selectedBranch.intValue()==branch.getCode()))
    {
      // Main trait
      TraitDescription mainTrait=branch.getMainTrait();
      if (mainTrait!=null)
      {
        Integer key=Integer.valueOf(mainTrait.getIdentifier());
        ret.put(key,Integer.valueOf(1));
      }
      // Progression
      TraitTreeProgression progression=branch.getProgression();
      int nbSteps=progression.getSteps().size();
      for(int i=0;i<nbSteps;i++)
      {
        Integer stepValue=progression.getSteps().get(i);
        if (pointsSpent>=stepValue.intValue())
        {
          TraitDescription trait=progression.getTraits().get(i);
          LOGGER.debug("Enough points for trait "+trait.getName());
          Integer key=Integer.valueOf(trait.getIdentifier());
          ret.put(key,Integer.valueOf(1));
        }
      }
    }
    return ret;
  }
}
