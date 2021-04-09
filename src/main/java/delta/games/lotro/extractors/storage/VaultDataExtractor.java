package delta.games.lotro.extractors.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.storage.vaults.Chest;
import delta.games.lotro.character.storage.vaults.Vault;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.bank.VaultDescriptor;
import delta.games.lotro.dat.data.bank.VaultItemDescriptor;
import delta.games.lotro.extractors.items.ItemInstancesExtractor;
import delta.games.lotro.lore.items.CountedItemInstance;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Extractor for vault data.
 * @author DAM
 */
public class VaultDataExtractor
{
  private static final Logger LOGGER=Logger.getLogger(VaultDataExtractor.class);

  private VaultDescriptor _vault;
  private List<VaultItemDescriptor> _items;
  private ItemInstancesExtractor _itemInstancesExtractor;

  /**
   * Constructor.
   */
  public VaultDataExtractor()
  {
    _itemInstancesExtractor=new ItemInstancesExtractor();
    _items=new ArrayList<VaultItemDescriptor>();
  }

  /**
   * Set the vault descriptor.
   * @param vault VAult descriptor to set.
   */
  public void setVault(VaultDescriptor vault)
  {
    _vault=vault;
  }

  /**
   * Register a vault item.
   * @param vaultItem Vault item to add.
   */
  public void addVaultItem(VaultItemDescriptor vaultItem)
  {
    _items.add(vaultItem);
  }

  /**
   * Build the result Vault.
   * @return a Vault or <code>null</code> if not enough data.
   */
  public Vault buildVault()
  {
    if (_vault==null)
    {
      LOGGER.warn("No vault descriptor!");
      return null;
    }
    Vault ret=new Vault();
    // Chests
    List<Integer> chestIds=_vault.getChestIds();
    for(Integer chestId : chestIds)
    {
      Chest chest=new Chest(chestId.intValue());
      String chestName=_vault.getChestName(chestId.intValue());
      chest.setName(chestName);
      ret.addChest(chest);
    }
    // Items
    for(VaultItemDescriptor vaultItem : _items)
    {
      CountedItemInstance countedItemInstance=decodeVaultItem(vaultItem);
      if (countedItemInstance==null)
      {
        continue;
      }
      PropertiesSet vaultProps=vaultItem.getProps();
      Integer chestId=(Integer)vaultProps.getProperty("Bank_Repository_ChestType");
      int chestIdInt=(chestId!=null)?chestId.intValue():0;
      Chest chest=ret.getChest(chestIdInt);
      if (chest==null)
      {
        chest=new Chest(chestIdInt);
        ret.addChest(chest);
      }
      chest.addItem(countedItemInstance);
    }
    return ret;
  }

  private CountedItemInstance decodeVaultItem(VaultItemDescriptor vaultItem)
  {
    PropertiesSet vaultProps=vaultItem.getProps();
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Vault item props: "+vaultProps.dump());
    }
    // Find item
    Integer itemId=(Integer)vaultProps.getProperty("Bank_Repository_ItemDataID");
    ItemsManager itemsMgr=ItemsManager.getInstance();
    Item item=itemsMgr.getItem(itemId.intValue());
    if (item==null)
    {
      LOGGER.warn("Item not found: ID="+itemId);
      return null;
    }
    //Integer chestId=(Integer)vaultProps.getProperty("Bank_Repository_ChestType");
    //Integer bankType=(Integer)vaultProps.getProperty("BankRepository_BankType");
    //System.out.println("\tBank type: "+bankType);
    //System.out.println("\tChest ID: "+chestId);

    PropertiesSet tooltipProps=getTooltipProps(vaultItem);
    ItemInstance<? extends Item> itemInstance=_itemInstancesExtractor.buildItemInstanceFromProps(tooltipProps,item);
    if (itemInstance==null)
    {
      LOGGER.warn("Item instance could not be build!'");
      return null;
    }
    long itemIID=vaultItem.getItemIID();
    itemInstance.setInstanceId(new InternalGameId(itemIID));

    // Quantity
    // Vault quantity is always present, while inventory quantity is sometimes missing (mean 1), and sometimes 1.
    Integer vaultQuantity=(Integer)vaultProps.getProperty("Bank_Repository_Quantity");
    int vaultQuantityValue=(vaultQuantity!=null)?vaultQuantity.intValue():1;
    Integer quantityValue=(Integer)tooltipProps.getProperty("Inventory_Quantity");
    int quantity=(quantityValue!=null)?quantityValue.intValue():1;
    if (quantity!=vaultQuantityValue)
    {
      LOGGER.warn("Quantities differ: vault="+vaultQuantityValue+", inventory="+quantity+" for IID="+itemIID);
    }

    CountedItemInstance countedItemInstance=new CountedItemInstance(itemInstance,vaultQuantityValue);
    Integer slotCode=(Integer)vaultProps.getProperty("Container_Slot");
    if (slotCode!=null)
    {
      int index=slotCode.intValue()&0xFFFF;
      LOGGER.debug("Index: "+index+" => "+countedItemInstance);
    }

    // Binding data
    // BoundToIID ignored: same data in instance props
    //Long boundToIID=(Long)props.getProperty("Bank_Repository_BoundToIID");
    // Manager IID
    // Should be the IID of the current character
    //Long managerIID=(Long)vaultProps.getProperty("Bank_Repository_ItemManagerIID");
    return countedItemInstance;
  }

  /**
   * Get tooltip properties (from the tooltip helper).
   * @param vaultItem Source item.
   * @return the tooltip properties.
   */
  public PropertiesSet getTooltipProps(VaultItemDescriptor vaultItem)
  {
    PropertyValue tooltipHelper=vaultItem.getTooltipHelper();
    PropertiesSet tooltipProps=(PropertiesSet)tooltipHelper.getValue();
    ArrayPropertyValue arrayHelperProps=(ArrayPropertyValue)tooltipProps.getPropertyValueByName("UI_Examination_Tooltip_ArrayHelper");
    PropertiesSet props=new PropertiesSet();
    for(PropertyValue value : arrayHelperProps.getValues())
    {
      props.setProperty(value);
    }
    return props;
  }
}