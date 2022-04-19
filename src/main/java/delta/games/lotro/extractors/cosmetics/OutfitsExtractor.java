package delta.games.lotro.extractors.cosmetics;

import java.util.List;
import java.util.Map;

import delta.games.lotro.character.cosmetics.Outfit;
import delta.games.lotro.character.cosmetics.OutfitElement;
import delta.games.lotro.character.cosmetics.OutfitsConstants;
import delta.games.lotro.character.cosmetics.OutfitsManager;
import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.common.colors.ColorDescription;
import delta.games.lotro.common.colors.ColorsManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Outfits data extractor.
 * @author DAM
 */
public class OutfitsExtractor
{
  /**
   * Extract outfits data.
   * @param playerProps Player properties.
   * @param outfitRegistry Raw data.
   * @return the loaded outfits.
   */
  public OutfitsManager extract(PropertiesSet playerProps, ClassInstance outfitRegistry)
  {
    OutfitsManager ret=new OutfitsManager();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> outfitsMap=(Map<Integer,ClassInstance>)outfitRegistry.getAttributeValue("64160996");
    for(Map.Entry<Integer,ClassInstance> entry : outfitsMap.entrySet())
    {
      int outfitId=entry.getKey().intValue();
      Outfit outfit=extractOutfit(entry.getValue());
      ret.addOutfit(outfitId,outfit);
      // Visibility
      String visibilityProperty=getOutfitVisibilityProperty(outfitId);
      Integer visibilityBits=(Integer)playerProps.getProperty(visibilityProperty);
      updateVisibility(outfit,visibilityBits);
    }
    // Equipment
    {
      Outfit outfit=new Outfit();
      ret.addOutfit(0,outfit);
      // Visibility
      String visibilityProperty=getOutfitVisibilityProperty(0);
      Integer visibilityBits=(Integer)playerProps.getProperty(visibilityProperty);
      updateVisibility(outfit,visibilityBits);
    }
    // Current outfit
    Integer outfitId=(Integer)playerProps.getProperty("Outfit_ActiveOutfitType");
    if (outfitId!=null)
    {
      ret.setCurrentOutfitIndex(outfitId.intValue());
    }
    return ret;
  }

  private void updateVisibility(Outfit outfit, Integer visibilityBits)
  {
    if (visibilityBits==null)
    {
      // All slots are visible
      for(GearSlot slot : OutfitsConstants.OUTFIT_SLOTS)
      {
        outfit.setSlotVisible(slot,true);
      }
    }
    else
    {
      List<GearSlot> slots=DatEnumsUtils.getEquipmentSlots(visibilityBits.intValue());
      for(GearSlot slot : slots)
      {
        outfit.setSlotVisible(slot,true);
      }
    }
  }

  private Outfit extractOutfit(ClassInstance outfitDataMap)
  {
    Outfit ret=new Outfit();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> entries=(Map<Integer,ClassInstance>)outfitDataMap.getAttributeValue("249837057");
    for(Map.Entry<Integer,ClassInstance> entry : entries.entrySet())
    {
      GearSlot slot=DatEnumsUtils.getEquipmentSlot(entry.getKey().intValue());
      ClassInstance outfitData=entry.getValue();
      if ((slot!=null) && (outfitData!=null))
      {
        OutfitElement element=decodeItem(outfitData);
        if (element!=null)
        {
          ret.setSlot(slot,element);
        }
      }
    }
    return ret;
  }

  private OutfitElement decodeItem(ClassInstance outfitData)
  {
    Integer itemId=(Integer)outfitData.getAttributeValue("m_didItem");
    if (itemId==null)
    {
      return null;
    }
    Float colorCode=(Float)outfitData.getAttributeValue("m_fColor");
    if (colorCode==null)
    {
      return null;
    }
    Item item=ItemsManager.getInstance().getItem(itemId.intValue());
    if (item==null)
    {
      return null;
    }
    ColorsManager colorsMgr=ColorsManager.getInstance();
    ColorDescription color=colorsMgr.getColor(colorCode.floatValue());
    if (color==null)
    {
      return null;
    }
    OutfitElement ret=new OutfitElement();
    ret.setItem(item);
    ret.setColor(color);
    return ret;
  }

  private String getOutfitVisibilityProperty(int index)
  {
    if (index==0) return "Outfit_InventorySlotVisibility";
    return "Outfit_Outfit"+index+"SlotVisibility";
  }
}
