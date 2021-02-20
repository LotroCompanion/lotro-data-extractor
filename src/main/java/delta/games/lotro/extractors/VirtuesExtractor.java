package delta.games.lotro.extractors;

import java.util.List;

import delta.games.lotro.character.CharacterData;
import delta.games.lotro.character.stats.virtues.VirtuesSet;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.VirtuesManager;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extracts virtues state.
 * @author DAM
 */
public class VirtuesExtractor
{
  private CharacterData _storage;

  /**
   * Constructor.
   * @param storage for loaded data. 
   */
  public VirtuesExtractor(CharacterData storage)
  {
    _storage=storage;
  }

  /**
   * Use the given properties to get virtues status.
   * @param properties Properties to use.
   */
  public void useProperties(PropertiesSet properties)
  {
    VirtuesSet virtuesSet=_storage.getVirtues();
    List<VirtueDescription> virtues=VirtuesManager.getInstance().getAll();
    for(VirtueDescription virtue : virtues)
    {
      String xpProperty=virtue.getXpPropertyName();
      Integer xp=(Integer)properties.getProperty(xpProperty);
      int xpValue=(xp!=null)?xp.intValue():0;
      int tier=virtue.getTierForXp(xpValue);
      virtuesSet.setVirtueValue(virtue,tier);
    }
  }
}
