package delta.games.lotro.extractors.items;

import org.apache.log4j.Logger;

import delta.games.lotro.character.storage.carryAlls.CarryAllInstance;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.carryalls.CarryAll;

/**
 * Extractor for carry-all data.
 * @author DAM
 */
public class CarryAllDataExtractor
{
  private static final Logger LOGGER=Logger.getLogger(CarryAllDataExtractor.class);

  /**
   * Extract a carry-all from an item (if possible).
   * @param carryAll Carry-all.
   * @param instance Input instance.
   * @param props Entity properties.
   * @return the loaded data.
   */
  public CarryAllInstance extract(CarryAll carryAll, ItemInstance<? extends Item> instance, PropertiesSet props)
  {
    /*
BoS_ItemArray: 
  #1: 
    BoS_ItemDID: 1879132412
    BoS_ItemName: Magnificent Hide[e]
    BoS_Quantity: 1468
     */
    CarryAllInstance carryAllInstance=new CarryAllInstance();
    carryAllInstance.setId(instance.getInstanceId());
    carryAllInstance.setReference(carryAll);
    Object[] entries=(Object[])props.getProperty("BoS_ItemArray");
    if ((entries!=null) && (entries.length>0))
    {
      for(Object entry : entries)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        Integer itemID=(Integer)entryProps.getProperty("BoS_ItemDID");
        if (itemID==null)
        {
          LOGGER.warn("No item ID in BoS entry");
          continue;
        }
        Item entryItem=ItemsManager.getInstance().getItem(itemID.intValue());
        if (entryItem==null)
        {
          LOGGER.warn("Cannot find item: "+itemID);
          continue;
        }
        Integer quantity=(Integer)entryProps.getProperty("BoS_Quantity");
        int count=(quantity!=null)?quantity.intValue():0;
        carryAllInstance.addItem(entryItem,count);
      }
    }
    return carryAllInstance;
  }
}
