package delta.games.lotro.extractors.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.colors.ColorDescription;
import delta.games.lotro.common.colors.ColorsManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.common.money.Money;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemFactory;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.WeaponInstance;
import delta.games.lotro.lore.items.essences.Essence;
import delta.games.lotro.lore.items.essences.EssencesManager;
import delta.games.lotro.lore.items.essences.EssencesSet;
import delta.games.lotro.lore.items.essences.EssencesSlotsSetup;
import delta.games.lotro.lore.items.legendary.LegendaryInstance;
import delta.games.lotro.lore.items.legendary.LegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegaciesManager;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegendaryInstanceAttrs;
import delta.games.lotro.lore.items.legendary2.Legendary2;
import delta.games.lotro.lore.items.legendary2.LegendaryInstance2;
import delta.games.lotro.lore.items.legendary2.LegendaryInstanceAttrs2;
import delta.games.lotro.lore.items.legendary2.SocketEntryInstance;
import delta.games.lotro.lore.items.legendary2.SocketsSetup;
import delta.games.lotro.lore.items.legendary2.SocketsSetupInstance;
import delta.games.lotro.lore.items.legendary2.TraceriesManager;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.utils.StringUtils;

/**
 * Extracts item instances descriptions from properties.
 * @author DAM
 */
public class ItemInstancesExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ItemInstancesExtractor.class);

  /**
   * Build an item instance from the given properties.
   * @param props Properties to use.
   * @param item Item model to use.
   * @return An instance or <code>null</code> if an error occurs.
   */
  public ItemInstance<? extends Item> buildItemInstanceFromProps(PropertiesSet props, Item item)
  {
    LOGGER.debug(props.dump());
    ItemInstance<? extends Item> instance=ItemFactory.buildInstance(item);
    if (instance==null)
    {
      return null;
    }
    decodeShared(props,instance);
    instance.updateAutoStats();
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Found item: "+instance.dump());
    }
    return instance;
  }

  private void decodeShared(PropertiesSet props, ItemInstance<? extends Item> itemInstance)
  {
    Item ref=itemInstance.getReference();
    // Crafter name
    String crafterName=(String)props.getProperty("CrafterName");
    if (crafterName!=null)
    {
      crafterName=StringUtils.fixName(crafterName).trim();
    }
    itemInstance.setCrafterName(crafterName);
    // Item inscription
    String itemInscription=(String)props.getProperty("Craft_ItemInscription");
    itemInstance.setBirthName(itemInscription);
    // Min usage level
    Integer usageMinLevel=(Integer)props.getProperty("Usage_MinLevel");
    if (usageMinLevel!=null)
    {
      LOGGER.debug("Usage min level: "+usageMinLevel);
      itemInstance.setMinLevel(usageMinLevel);
    }
    // Item level
    Integer itemLevel=(Integer)props.getProperty("Item_Level");
    if (itemLevel!=null)
    {
      LOGGER.debug("Item level: "+itemLevel);
      itemInstance.setItemLevel(itemLevel);
    }

    // Binding data
    // Bound to
    Long boundTo=(Long)props.getProperty("Inventory_BoundToID");
    if (boundTo!=null)
    {
      InternalGameId id=new InternalGameId(boundTo.longValue());
      LOGGER.debug("Bound to: "+id);
      itemInstance.setBoundTo(id);
    }
    // Bind on acquire
    Integer bindOnAcquire=(Integer)props.getProperty("Inventory_BindOnAcquire");
    if (bindOnAcquire!=null)
    {
      LOGGER.debug("Bind on acquire: "+bindOnAcquire);
    }
    // Bind to account
    Integer bindToAccount=(Integer)props.getProperty("Inventory_BindToAccount");
    if (bindToAccount!=null)
    {
      LOGGER.debug("Bind to account: "+bindToAccount);
    }

    // Current item durability
    Integer durability=(Integer)props.getProperty("Item_CurStructurePoints");
    if (durability!=null)
    {
      LOGGER.debug("Durability: "+durability);
    }
    itemInstance.setDurability(durability);
    // Item value
    Integer itemValue=(Integer)props.getProperty("Item_Value");
    if (itemValue!=null)
    {
      LOGGER.debug("Item value: "+itemValue);
      Money money=parseItemValue(itemValue.intValue());
      itemInstance.setValue(money);
    }

    // Sockets (essences and traceries)
    Object[] socketEntries=(Object[])props.getProperty("Item_Socket_Gem_Array");
    decodeSockets(ref,itemInstance,socketEntries);

    // Clothing color
    Float colorCode=(Float)props.getProperty("Item_ClothingColor");
    if (colorCode!=null)
    {
      LOGGER.debug("Dye: "+colorCode);
      ColorsManager colorsMgr=ColorsManager.getInstance();
      ColorDescription color=colorsMgr.getColor(colorCode.floatValue());
      itemInstance.setColor(color);
    }

    // Legendary specifics
    decodeLegendaryData(props,itemInstance);
    // Weapon specifics
    if (itemInstance instanceof WeaponInstance)
    {
      WeaponInstance<?> weaponInstance=(WeaponInstance<?>)itemInstance;
      decodeWeaponSpecifics(props,weaponInstance);
    }
  }

  private void decodeLegendaryData(PropertiesSet props, ItemInstance<? extends Item> itemInstance)
  {
    decodeLegendaryData1(props,itemInstance);
    decodeLegendaryData2(props,itemInstance);
  }

  private void decodeLegendaryData1(PropertiesSet props, ItemInstance<? extends Item> itemInstance)
  {
    if (!(itemInstance instanceof LegendaryInstance))
    {
      return;
    }
    LegendaryInstance legendary=(LegendaryInstance)itemInstance;
    LegendaryInstanceAttrs attrs=legendary.getLegendaryAttributes();
    // Name
    String name=(String)props.getProperty("Name");
 attrs.setLegendaryName(name);
    // Imbued?
    Integer imbuedValue=(Integer)props.getProperty("ItemAdvancement_Imbued");
    LOGGER.debug("Imbued: "+imbuedValue);
    attrs.setImbued((imbuedValue!=null)&&(imbuedValue.intValue()>0));
    NonImbuedLegendaryInstanceAttrs nonImbuedAttrs=attrs.getNonImbuedAttrs();
    // Item upgrades (crystals)
    Integer itemUpgrades=(Integer)props.getProperty("Item_LevelUpgradeTier");
    if (itemUpgrades!=null)
    {
      LOGGER.debug("Item upgrades: "+itemUpgrades);
      nonImbuedAttrs.setNbUpgrades(itemUpgrades.intValue());
    }
    // Default legacy rank
    Integer defaultLegacyRank=(Integer)props.getProperty("ItemAdvancement_CombatPropertyModLevel");
    LOGGER.debug("Default legacy rank: "+defaultLegacyRank);
    // Item advancement level
    Integer liLevel=(Integer)props.getProperty("ItemAdvancement_Level");
    if (liLevel!=null)
    {
      LOGGER.debug("LI level: "+liLevel);
      nonImbuedAttrs.setLegendaryItemLevel(liLevel.intValue());
    }
    // Max LI level: ItemAdvancement_MaxLevel_Override: 70
    // XP of non-imbued item: ItemAdvancement_EarnedXP - not supported yet
    // Leveling enabled: ItemAdvancement_ItemLevelingEnabled
    // Delving applied or not: ItemAdvancement_TotalLegacyAddedThroughNonReforge: 1
    // Default legacy for non-imbued items:
    Integer combatPropertyModDID=(Integer)props.getProperty("ItemAdvancement_CombatPropertyModDID");
    Integer combatPropertyModLevel=(Integer)props.getProperty("ItemAdvancement_CombatPropertyModLevel");
    if ((combatPropertyModDID!=null) && (combatPropertyModLevel!=null))
    {
      DefaultNonImbuedLegacyInstance defaultLegacyInstance=nonImbuedAttrs.getDefaultLegacy();
      DefaultNonImbuedLegacy defaultLegacy=NonImbuedLegaciesManager.getInstance().getDefaultLegacy(combatPropertyModDID.intValue());
      if (defaultLegacy!=null)
      {
        defaultLegacyInstance.setLegacy(defaultLegacy);
        defaultLegacyInstance.setRank(combatPropertyModLevel.intValue());
      }
    }
    // - ItemAdvancement_CombatPropertyModDID: 1879325215
    // - ItemAdvancement_CombatPropertyModLevel: 39
    Integer legendaryPointsAvailable=(Integer)props.getProperty("ItemAdvancement_LegendaryPoints");
    Integer legendaryPointsSpent=(Integer)props.getProperty("ItemAdvancement_LegendaryPoints_Spent");
    if ((legendaryPointsAvailable!=null) && (legendaryPointsSpent!=null))
    {
      nonImbuedAttrs.setPointsLeft(legendaryPointsAvailable.intValue());
      nonImbuedAttrs.setPointsSpent(legendaryPointsSpent.intValue());
    }
  }

  private void decodeLegendaryData2(PropertiesSet props, ItemInstance<? extends Item> itemInstance)
  {
    if (!(itemInstance instanceof LegendaryInstance2))
    {
      return;
    }
    LegendaryInstanceAttrs2 attrs=null;
    LegendaryInstance2 legendary=(LegendaryInstance2)itemInstance;
    attrs=legendary.getLegendaryAttributes();
    // Name
    String name=(String)props.getProperty("Name");
    attrs.setLegendaryName(name);
  }

  private void decodeWeaponSpecifics(PropertiesSet props, WeaponInstance<?> weaponInstance)
  {
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Weapon props: "+props.dump());
    }
    // Max damage
    Float maxDamage=(Float)props.getProperty("Combat_Damage");
    if (maxDamage!=null)
    {
      weaponInstance.setMaxDamage(maxDamage);
    }
    // Damage variance
    // Not found on weapons. Found on mob/player entities.
    Float damageVariance=(Float)props.getProperty("Combat_DamageVariance");
    if (damageVariance!=null)
    {
      LOGGER.warn("Found a damage variance: "+damageVariance);
    }
    // Damage type
    Integer damageTypeCode=(Integer)props.getProperty("Combat_DamageType");
    if (damageTypeCode!=null)
    {
      LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
      DamageType damageType=damageTypeEnum.getEntry(damageTypeCode.intValue());
      weaponInstance.setDamageType(damageType);
    }
    // Base DPS
    Float baseDPS=(Float)props.getProperty("Combat_BaseDPS");
    if (baseDPS!=null)
    {
      weaponInstance.setDPS(baseDPS);
    }
    // Slayer modifier
    // Really? Seems to be on item templates, but not on weapon instances
    // May be on old LIs with a title applied?
    Float slayerValue=(Float)props.getProperty("Combat_WeaponSlayerAddMod");
    if (slayerValue!=null)
    {
      LOGGER.warn("Found a weapon slayer modifier: "+slayerValue);
    }
  }

  private Money parseItemValue(int itemValue)
  {
    int copper=itemValue%100;
    itemValue=itemValue/100;
    int silver=itemValue%1000;
    int gold=itemValue/1000;
    return new Money(gold,silver,copper);
  }

  private void decodeSockets(Item item, ItemInstance<? extends Item> itemInstance, Object[] sockets)
  {
    if (sockets==null)
    {
      return;
    }
    if (item instanceof Legendary2)
    {
      decodeTraceries((Legendary2)item,itemInstance,sockets);
    }
    else
    {
      EssencesSet essences=decodeEssences(item,sockets);
      itemInstance.setEssences(essences);
    }
  }

  private void decodeTraceries(Legendary2 item, ItemInstance<? extends Item> itemInstance, Object[] sockets)
  {
    int nbTraceries=sockets.length;
    SocketsSetup setupTemplate=item.getLegendaryAttrs().getSockets();
    int expectedSocketsCount=setupTemplate.getSocketsCount();
    int nb=Math.min(nbTraceries,expectedSocketsCount);
    LegendaryInstance2 legInstance2=(LegendaryInstance2)itemInstance;
    LegendaryInstanceAttrs2 legAttrs=legInstance2.getLegendaryAttributes();
    SocketsSetupInstance socketsSetup=legAttrs.getSocketsSetup();
    for(int i=0;i<nb;i++)
    {
      PropertiesSet socketProps=(PropertiesSet)sockets[i];
      if (socketProps==null)
      {
        continue;
      }
      Integer gemID=(Integer)socketProps.getProperty("Item_Socket_GemDID");
      Integer gemLevel=(Integer)socketProps.getProperty("Item_Socket_GemLevel");
      if ((gemID!=null) && (gemLevel!=null))
      {
        Tracery tracery=TraceriesManager.getInstance().getTracery(gemID.intValue());
        if (tracery!=null)
        {
          SocketEntryInstance entryInstance=socketsSetup.getEntry(i);
          entryInstance.setTracery(tracery);
          entryInstance.setItemLevel(gemLevel.intValue());
        }
      }
    }
  }

  private EssencesSet decodeEssences(Item item, Object[] essences)
  {
    EssencesManager essencesMgr=EssencesManager.getInstance();
    int nbEssencesMax=item.getEssenceSlots();
    if (nbEssencesMax==0)
    {
      return null;
    }
    EssencesSlotsSetup setup=item.getEssenceSlotsSetup();
    EssencesSet ret=new EssencesSet(setup);
    int nbEssences=0;
    if ((essences!=null) && (essences.length>0))
    {
      nbEssences=essences.length;
    }
    LOGGER.debug("Nb essences: "+nbEssences);
    for(int i=0;i<nbEssences;i++)
    {
      PropertiesSet essenceProps=(PropertiesSet)essences[i];
      if (essenceProps==null)
      {
        continue;
      }
      Integer essenceId=(Integer)essenceProps.getProperty("Item_Socket_GemDID");
      Integer essenceLevel=(Integer)essenceProps.getProperty("Item_Socket_GemLevel");
      if ((essenceId!=null) && (essenceLevel!=null))
      {
        Essence essence=essencesMgr.getEssence(essenceId.intValue());
        ret.setEssence(i,essence);
      }
    }
    return ret;
  }
}
