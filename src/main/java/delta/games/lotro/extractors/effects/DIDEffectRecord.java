package delta.games.lotro.extractors.effects;

import delta.games.lotro.character.stats.buffs.BuffInstance;

/**
 * DID effect record.
 * @author DAM
 */
public class DIDEffectRecord extends EffectRecord
{
  private BuffInstance _buff;
  /**
   * Constructor.
   * @param buff Managed buff.
   */
  public DIDEffectRecord(BuffInstance buff)
  {
    _buff=buff;
  }

  /**
   * Get the managed buff.
   * @return the managed buff.
   */
  public BuffInstance getBuff()
  {
    return _buff;
  }
}
