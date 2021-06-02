package delta.games.lotro.extractors.items;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.character.CharacterEquipment.EQUIMENT_SLOT;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Gear registry for a character.
 * @author DAM
 */
public class CharacterGearRegistry
{
  private Map<EQUIMENT_SLOT,Long> _iidsMap;
  private Map<Long,EQUIMENT_SLOT> _slotsMap;

  /**
   * Constructor.
   */
  public CharacterGearRegistry()
  {
    _iidsMap=new HashMap<EQUIMENT_SLOT,Long>();
    _slotsMap=new HashMap<Long,EQUIMENT_SLOT>();
  }

  /**
   * Use a collection of properties.
   * @param props Properties to use.
   */
  public void useProperties(PropertiesSet props)
  {
    for(String propertyName : props.getPropertyNames())
    {
      useProperty(propertyName,props.getProperty(propertyName));
    }
  }

  /**
   * Indicates if the given IID is known or not.
   * @param iid IID to test.
   * @return <code>true</code> if it is, <code>false</code> ottherwise.
   */
  public boolean hasIID(long iid)
  {
    for(Long knownIID : _iidsMap.values())
    {
      if (InternalGameId.lightMatch(iid,knownIID.longValue()))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the IID for a given slot.
   * @param slot A slot.
   * @return An IID or <code>null</code>.
   */
  public Long getIIDForSlot(EQUIMENT_SLOT slot)
  {
    return _iidsMap.get(slot);
  }

  /**
   * Use a single property.
   * @param property Property to use.
   * @param value Value.
   */
  public void useProperty(String property, Object value)
  {
    EQUIMENT_SLOT slot=getSlotFromProperty(property);
    if (slot!=null)
    {
      Long iid=(Long)value;
      _iidsMap.put(slot,iid);
      _slotsMap.put(iid,slot);
      System.out.println("Registering "+iid+" for slot "+slot);
    }
  }

  private EQUIMENT_SLOT getSlotFromProperty(String propertyName)
  {
    if ("Inventory_SlotCache_Eq_Back".equals(propertyName)) return EQUIMENT_SLOT.BACK;
    if ("Inventory_SlotCache_Eq_Boots".equals(propertyName)) return EQUIMENT_SLOT.FEET;
    if ("Inventory_SlotCache_Eq_Bracelet1".equals(propertyName)) return EQUIMENT_SLOT.LEFT_WRIST;
    if ("Inventory_SlotCache_Eq_Bracelet2".equals(propertyName)) return EQUIMENT_SLOT.RIGHT_WRIST;
    if ("Inventory_SlotCache_Eq_Chest".equals(propertyName)) return EQUIMENT_SLOT.BREAST;
    if ("Inventory_SlotCache_Eq_Class".equals(propertyName)) return EQUIMENT_SLOT.CLASS_ITEM;
    if ("Inventory_SlotCache_Eq_CraftTool".equals(propertyName)) return EQUIMENT_SLOT.TOOL;
    if ("Inventory_SlotCache_Eq_Earring1".equals(propertyName)) return EQUIMENT_SLOT.LEFT_EAR;
    if ("Inventory_SlotCache_Eq_Earring2".equals(propertyName)) return EQUIMENT_SLOT.RIGHT_EAR;
    if ("Inventory_SlotCache_Eq_Gloves".equals(propertyName)) return EQUIMENT_SLOT.HANDS;
    if ("Inventory_SlotCache_Eq_Head".equals(propertyName)) return EQUIMENT_SLOT.HEAD;
    if ("Inventory_SlotCache_Eq_Legs".equals(propertyName)) return EQUIMENT_SLOT.LEGS;
    if ("Inventory_SlotCache_Eq_Mounted".equals(propertyName)) return null; // Unmanaged
    if ("Inventory_SlotCache_Eq_Necklace".equals(propertyName)) return EQUIMENT_SLOT.NECK;
    if ("Inventory_SlotCache_Eq_Pocket1".equals(propertyName)) return EQUIMENT_SLOT.POCKET;
    if ("Inventory_SlotCache_Eq_Ring1".equals(propertyName)) return EQUIMENT_SLOT.LEFT_FINGER;
    if ("Inventory_SlotCache_Eq_Ring2".equals(propertyName)) return EQUIMENT_SLOT.RIGHT_FINGER;
    if ("Inventory_SlotCache_Eq_Shoulder".equals(propertyName)) return EQUIMENT_SLOT.SHOULDER;
    if ("Inventory_SlotCache_Eq_Weapon_Primary".equals(propertyName)) return EQUIMENT_SLOT.MAIN_MELEE;
    if ("Inventory_SlotCache_Eq_Weapon_Ranged".equals(propertyName)) return EQUIMENT_SLOT.RANGED;
    if ("Inventory_SlotCache_Eq_Weapon_Secondary".equals(propertyName)) return EQUIMENT_SLOT.OTHER_MELEE;
    return null;
  }
}
