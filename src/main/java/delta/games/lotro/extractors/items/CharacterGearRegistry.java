package delta.games.lotro.extractors.items;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.character.gear.GearSlots;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Gear registry for a character.
 * @author DAM
 */
public class CharacterGearRegistry
{
  private static final Logger LOGGER=LoggerFactory.getLogger(CharacterGearRegistry.class);

  private Map<GearSlot,Long> _iidsMap;

  /**
   * Constructor.
   */
  public CharacterGearRegistry()
  {
    _iidsMap=new HashMap<GearSlot,Long>();
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
   * @return <code>true</code> if it is, <code>false</code> otherwise.
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
  public Long getIIDForSlot(GearSlot slot)
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
    GearSlot slot=getSlotFromProperty(property);
    if (slot!=null)
    {
      Long iid=(Long)value;
      _iidsMap.put(slot,iid);
      LOGGER.debug("Registering "+iid+" for slot "+slot);
    }
  }

  private GearSlot getSlotFromProperty(String propertyName)
  {
    if ("Inventory_SlotCache_Eq_Back".equals(propertyName)) return GearSlots.BACK;
    if ("Inventory_SlotCache_Eq_Boots".equals(propertyName)) return GearSlots.FEET;
    if ("Inventory_SlotCache_Eq_Bracelet1".equals(propertyName)) return GearSlots.LEFT_WRIST;
    if ("Inventory_SlotCache_Eq_Bracelet2".equals(propertyName)) return GearSlots.RIGHT_WRIST;
    if ("Inventory_SlotCache_Eq_Chest".equals(propertyName)) return GearSlots.BREAST;
    if ("Inventory_SlotCache_Eq_Class".equals(propertyName)) return GearSlots.CLASS_ITEM;
    if ("Inventory_SlotCache_Eq_CraftTool".equals(propertyName)) return GearSlots.TOOL;
    if ("Inventory_SlotCache_Eq_Earring1".equals(propertyName)) return GearSlots.LEFT_EAR;
    if ("Inventory_SlotCache_Eq_Earring2".equals(propertyName)) return GearSlots.RIGHT_EAR;
    if ("Inventory_SlotCache_Eq_Gloves".equals(propertyName)) return GearSlots.HANDS;
    if ("Inventory_SlotCache_Eq_Head".equals(propertyName)) return GearSlots.HEAD;
    if ("Inventory_SlotCache_Eq_Legs".equals(propertyName)) return GearSlots.LEGS;
    if ("Inventory_SlotCache_Eq_Mounted".equals(propertyName)) return GearSlots.BRIDLE;
    if ("Inventory_SlotCache_Eq_Necklace".equals(propertyName)) return GearSlots.NECK;
    if ("Inventory_SlotCache_Eq_Pocket1".equals(propertyName)) return GearSlots.POCKET;
    if ("Inventory_SlotCache_Eq_Ring1".equals(propertyName)) return GearSlots.LEFT_FINGER;
    if ("Inventory_SlotCache_Eq_Ring2".equals(propertyName)) return GearSlots.RIGHT_FINGER;
    if ("Inventory_SlotCache_Eq_Shoulder".equals(propertyName)) return GearSlots.SHOULDER;
    if ("Inventory_SlotCache_Eq_Weapon_Primary".equals(propertyName)) return GearSlots.MAIN_MELEE;
    if ("Inventory_SlotCache_Eq_Weapon_Ranged".equals(propertyName)) return GearSlots.RANGED;
    if ("Inventory_SlotCache_Eq_Weapon_Secondary".equals(propertyName)) return GearSlots.OTHER_MELEE;
    return null;
  }
}
