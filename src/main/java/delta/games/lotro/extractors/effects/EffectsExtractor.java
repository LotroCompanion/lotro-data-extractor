package delta.games.lotro.extractors.effects;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Effects extractor;
 * @author DAM
 */
public class EffectsExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EffectsExtractor.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectsExtractor(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load effects data.
   * @param localPlayer
   * @return the loaded data.
   */
  public EffectsData doIt(ClassInstance localPlayer)
  {
    EffectsData store=new EffectsData();
    EffectRecordExtractor extractor=new EffectRecordExtractor(_facade);
    ClassInstance effectRegistry=(ClassInstance)localPlayer.getAttributeValue("m_regEffect");
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> effectsMap=(Map<Integer,ClassInstance>)effectRegistry.getAttributeValue("m_effectInfo");
    for(ClassInstance rawEffectRecord : effectsMap.values())
    {
      try
      {
        extractor.handleEffectRecord(store,rawEffectRecord);
      }
      catch(Exception e)
      {
        LOGGER.warn("Error during effects extraction!",e);
      }
    }
    return store;
  }
}
