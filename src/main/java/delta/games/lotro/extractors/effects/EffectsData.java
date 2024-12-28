package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.status.effects.EffectInstance;

/**
 * Global effects manager.
 * @author DAM
 */
public class EffectsData
{
  private List<EffectInstance> _effects;
  private ItemEffectsManager _itemEffectsMgr;

  /**
   * Constructor.
   */
  public EffectsData()
  {
    _effects=new ArrayList<EffectInstance>();
    _itemEffectsMgr=new ItemEffectsManager();
  }

  /**
   * Get the item effects manager.
   * @return the item effects manager.
   */
  public ItemEffectsManager getItemEffects()
  {
    return _itemEffectsMgr;
  }

  /**
   * Get the loaded effects.
   * @return the loaded effects.
   */
  public List<EffectInstance> getEffects()
  {
    return _effects;
  }

  /**
   * Add an item effect.
   * @param effect Effect to add.
   */
  public void addItemEffect(ItemEffectData effect)
  {
    Long itemIid=effect.getItemIid();
    if (itemIid!=null)
    {
      _itemEffectsMgr.addEffect(effect);
    }
  }

  /**
   * Add an effect.
   * @param effect Effect to add.
   */
  public void addEffect(EffectInstance effect)
  {
    _effects.add(effect);
  }
}
