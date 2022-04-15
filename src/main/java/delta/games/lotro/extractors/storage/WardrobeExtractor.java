package delta.games.lotro.extractors.storage;

import delta.games.lotro.character.storage.wardrobe.Wardrobe;
import delta.games.lotro.character.storage.wardrobe.WardrobeItem;
import delta.games.lotro.common.colors.ColorDescription;
import delta.games.lotro.common.colors.ColorsManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Wardrobe extractor.
 * @author DAM
 */
public class WardrobeExtractor
{
  /**
   * Extract wardrobe data.
   * @param playerProps Player properties.
   * @return the loaded wardrobe or <code>null</code> if not found.
   */
  public Wardrobe extract(PropertiesSet playerProps)
  {
    Object cache=playerProps.getProperty("Wardrobe_MergedItemCache");
    if (cache==null)
    {
      return null;
    }
    Wardrobe ret=new Wardrobe();
    for(Object entry : (Object[])cache)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      WardrobeItem wardrobeItem=extractWardrobeItem(entryProps);
      if (wardrobeItem!=null)
      {
        ret.addItem(wardrobeItem);
      }
    }
    return ret;
  }

  private WardrobeItem extractWardrobeItem(PropertiesSet entry)
  {
    // Item
    Integer itemId=(Integer)entry.getProperty("Wardrobe_ItemDID");
    if (itemId==null)
    {
      return null;
    }
    Item item=ItemsManager.getInstance().getItem(itemId.intValue());
    if (item==null)
    {
      return null;
    }
    // Color
    ColorDescription color=null;
    Long colorBit=(Long)entry.getProperty("Wardrobe_ItemClothingColorsExt");
    if (colorBit!=null)
    {
      int colorCode=Long.numberOfTrailingZeros(colorBit.intValue())+1;
      color=ColorsManager.getInstance().getColor(colorCode);
    }
    WardrobeItem ret=new WardrobeItem(item,color);
    return ret;
  }
}
