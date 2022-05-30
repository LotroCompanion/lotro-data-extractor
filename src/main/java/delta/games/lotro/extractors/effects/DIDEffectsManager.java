package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.List;

/**
 * DID effects manager.
 * @author DAM
 */
public class DIDEffectsManager
{
  private List<DIDEffectRecord> _effects;

  /**
   * Constructor.
   */
  public DIDEffectsManager()
  {
    _effects=new ArrayList<DIDEffectRecord>();
  }

  /**
   * Add an effect.
   * @param effect Effect to add.
   */
  public void addEffect(DIDEffectRecord effect)
  {
    _effects.add(effect);
  }

  /**
   * Get the managed effect.
   * @return a list of effect.
   */
  public List<DIDEffectRecord> getEffects()
  {
    return new ArrayList<DIDEffectRecord>(_effects);
  }
}
