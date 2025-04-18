package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.effects.EffectInstance;

/**
 * Global effects manager.
 * @author DAM
 */
public class EffectsData
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EffectsData.class);

  private Map<Long,List<SingleEffectData>> _mapItemIIDToEffects;

  /**
   * Constructor.
   */
  public EffectsData()
  {
    _mapItemIIDToEffects=new HashMap<Long,List<SingleEffectData>>();
  }

  /**
   * Add an effect.
   * @param effect Effect to add.
   */
  public void addEffect(SingleEffectData effect)
  {
    Long itemIid=effect.getItemIid();
    List<SingleEffectData> effects=_mapItemIIDToEffects.get(itemIid);
    if (effects==null)
    {
      effects=new ArrayList<SingleEffectData>();
      _mapItemIIDToEffects.put(itemIid,effects);
    }
    effects.add(effect);
  }

  /**
   * Get the identifiers of the known items.
   * @return A list of item identifiers.
   */
  public List<Long> getKnownItems()
  {
    List<Long> ret=new ArrayList<Long>(_mapItemIIDToEffects.keySet());
    ret.remove(null);
    return ret;
  }

  /**
   * Get the effects for an item.
   * @param itemIid Item instance identifier.
   * @return A list of effects.
   */
  public List<SingleEffectData> getEffectsForItem(long itemIid)
  {
    return _mapItemIIDToEffects.get(Long.valueOf(itemIid));
  }

  /**
   * Consume the effects for the given item identifier.
   * @param itemIid Item instance identifier.
   */
  public void consumeItemEffects(Long itemIid)
  {
    LOGGER.debug("Consuming effects for item: {}",itemIid);
    _mapItemIIDToEffects.remove(itemIid);
  }

  /**
   * Get the remaining effects.
   * @return A list of effect instances.
   */
  public List<EffectInstance> getCharacterEffects()
  {
    List<EffectInstance> ret=new ArrayList<EffectInstance>();
    for(SingleEffectData effectData : getEffects())
    {
      LOGGER.debug("Got character effect: {}",effectData);
      EffectInstance effect=effectData.getEffectInstance();
      if (effect!=null)
      {
        ret.add(effect);
      }
    }
    return ret;
  }

  private List<SingleEffectData> getEffects()
  {
    List<SingleEffectData> ret=new ArrayList<SingleEffectData>();
    for(Map.Entry<Long,List<SingleEffectData>> entry : _mapItemIIDToEffects.entrySet())
    {
      ret.addAll(entry.getValue());
    }
    return ret;
  }
}
