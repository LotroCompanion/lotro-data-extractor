package delta.games.lotro.extractors.effects;

import delta.games.lotro.character.stats.BasicStatsSet;

/**
 * Base class for effect data.
 * @author DAM
 */
public class ItemEffectData
{
  private Long _itemIid;
  private float _spellCraft;
  private BasicStatsSet _stats;

  /**
   * Constructor.
   * @param itemIid Item instance identifier.
   * @param spellcraft Spellcraft.
   * @param stats Stats.
   */
  public ItemEffectData(Long itemIid, float spellcraft, BasicStatsSet stats)
  {
    _itemIid=itemIid;
    _spellCraft=spellcraft;
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
    return "Item IID="+_itemIid+", spellcraft="+_spellCraft+", stats="+_stats;
  }
}
