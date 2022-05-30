package delta.games.lotro.extractors.effects;

/**
 * Global effects manager.
 * @author DAM
 */
public class EffectsManager
{
  private ItemEffectsManager _itemEffectsMgr;
  private DIDEffectsManager _didEffectsMgr;

  /**
   * Constructor.
   */
  public EffectsManager()
  {
    _itemEffectsMgr=new ItemEffectsManager();
    _didEffectsMgr=new DIDEffectsManager();
  }

  /**
   * Add a new effect.
   * @param effectRecord Effect to add.
   */
  public void addEffect(EffectRecord effectRecord)
  {
    if (effectRecord instanceof ItemEffectRecord)
    {
      _itemEffectsMgr.addEffect((ItemEffectRecord)effectRecord);
    }
    else if (effectRecord instanceof DIDEffectRecord)
    {
      DIDEffectRecord didEffect=(DIDEffectRecord)effectRecord;
      _didEffectsMgr.addEffect(didEffect);
    }
  }

  /**
   * Get the item effects manager.
   * @return the item effects manager.
   */
  public ItemEffectsManager getItemEffectsMgr()
  {
    return _itemEffectsMgr;
  }

  /**
   * Get the DID effects manager.
   * @return the DID effects manager.
   */
  public DIDEffectsManager getDidEffectsMgr()
  {
    return _didEffectsMgr;
  }
}
