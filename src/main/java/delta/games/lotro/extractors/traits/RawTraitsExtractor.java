package delta.games.lotro.extractors.traits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.traits.raw.RawTraitsStatus;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extractor for raw traits.
 * @author DAM
 */
public class RawTraitsExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(RawTraitsExtractor.class);

  /**
   * Load ranks for traits.
   * @param traitsData Storage.
   * @param properties Input character properties.
   * @return the loaded status.
   */
  public RawTraitsStatus extract(TraitsData traitsData, PropertiesSet properties)
  {
    RawTraitsStatus ret=new RawTraitsStatus();
    TraitsManager traitsMgr=TraitsManager.getInstance();
    for(Integer traitID : traitsData.getAcquiredTraits())
    {
      TraitDescription trait=traitsMgr.getTrait(traitID.intValue());
      if (trait!=null)
      {
        String propertyName=trait.getTierPropertyName();
        if (propertyName!=null)
        {
          Integer tier=(Integer)properties.getProperty(propertyName);
          if (tier!=null)
          {
            LOGGER.debug("Trait "+trait.getName()+" => tier "+tier+" (prop="+propertyName+")");
            ret.setTraitRank(traitID.intValue(),tier.intValue());
          }
        }
        else
        {
          LOGGER.debug("Trait "+trait.getName()+" => tier 1");
          ret.setTraitRank(traitID.intValue(),1);
        }
      }
    }
    return ret;
  }
}
