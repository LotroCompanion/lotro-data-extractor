package delta.games.lotro.extractors.effects;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.status.effects.EffectInstance;

/**
 * Single effect data.
 * @author DAM
 */
public class SingleEffectData
{
  private Long _itemIid;
  private float _spellCraft;
  private EffectInstance _effectInstance;
  private BasicStatsSet _stats;

  /**
   * Constructor.
   * @param itemIid Item instance identifier.
   * @param spellcraft Spellcraft.
   * @param effectInstance Effect instance.
   * @param stats Stats.
   */
  public SingleEffectData(Long itemIid, float spellcraft, EffectInstance effectInstance, BasicStatsSet stats)
  {
    _itemIid=itemIid;
    _spellCraft=spellcraft;
    _effectInstance=effectInstance;
    _stats=stats;
  }

  /**
   * Get the identifier of the source/targeted item instance
   * @return an item instance identifier.
   */
  public Long getItemIid()
  {
    return _itemIid;
  }

  /**
   * Get the managed effect instance.
   * @return the managed effect instance.
   */
  public EffectInstance getEffectInstance()
  {
    return _effectInstance;
  }

  /**
   * Get the spellcraft.
   * @return the spellcraft.
   */
  public float getSpellCraft()
  {
    return _spellCraft;
  }

  /**
   * Get the provided stats.
   * @return the provided stats.
   */
  public BasicStatsSet getStats()
  {
    return _stats;
  }

  @Override
  public String toString()
  {
    return "Effect: Item IID="+_itemIid+", spellcraft="+", stats="+_stats+", effect="+_effectInstance;
  }
}
