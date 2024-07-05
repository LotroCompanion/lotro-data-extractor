package delta.games.lotro.extractors.effects;

import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.buffs.Buff;
import delta.games.lotro.character.stats.buffs.BuffInstance;
import delta.games.lotro.character.stats.buffs.BuffRegistry;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.utils.dat.DatStatUtils;

/**
 * Effect data extractor.
 * @author DAM
 */
public class EffectRecordExtractor
{
  private static final Logger LOGGER=Logger.getLogger(EffectRecordExtractor.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectRecordExtractor(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Extract effect data.
   * @param effectRecord Source data.
   * @return An item effect or <code>null</code>.
   */
  public EffectRecord extract(ClassInstance effectRecord)
  {
    Long itemIid=(Long)effectRecord.getAttributeValue("m_iidFromItem");
    if ((itemIid!=null) && (itemIid.longValue()!=0))
    {
      ItemEffectRecord effect=handleEffectOnItem(itemIid.longValue(),effectRecord);
      return effect;
    }
    Integer effectId=(Integer)effectRecord.getAttributeValue("m_didEffect");
    if ((effectId!=null) && (effectId.intValue()>0))
    {
      DIDEffectRecord effect=handleDIDEffect(effectId.intValue());
      return effect;
    }
    return null;
  }

  private ItemEffectRecord handleEffectOnItem(long itemIid, ClassInstance effectRecord)
  {
    Float spellCraft=(Float)effectRecord.getAttributeValue("m_fSpellcraft");
    LOGGER.debug("Item "+itemIid+", spellCraft="+spellCraft);
    ClassInstance scratchPad=(ClassInstance)effectRecord.getAttributeValue("m_ScratchPad");
    if (scratchPad==null)
    {
      return null;
    }
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> appliedMods=(Map<Integer,ClassInstance>)scratchPad.getAttributeValue("m_arhAppModTable");
    if (appliedMods==null)
    {
      return null;
    }
    BasicStatsSet stats=new BasicStatsSet();
    for(Map.Entry<Integer,ClassInstance> entry : appliedMods.entrySet())
    {
      int propertyId=entry.getKey().intValue();
      PropertyDefinition propDefinition=propsRegistry.getPropertyDef(propertyId);
      if (propDefinition!=null)
      {
        ClassInstance appliedMod=entry.getValue();
        PropertyValue propValue=(PropertyValue)appliedMod.getAttributeValue("m_rModValue");
        StatDescription stat=DatStatUtils.getStatDescription(propertyId,propDefinition.getName());
        Object value=propValue.getValue();
        if (value instanceof Number)
        {
          Number numberValue=(Number)value;
          int modOperation=((Integer)appliedMod.getAttributeValue("m_eModOp")).intValue();
          if ((modOperation==7) && (stat!=null))
          {
            stats.addStat(stat,numberValue);
          }
        }
        else
        {
          LOGGER.warn("Unmanaged value type: "+value+" for property: "+propDefinition.getName());
        }
      }
    }
    ItemEffectRecord effect=new ItemEffectRecord(itemIid,spellCraft.floatValue(),stats);
    return effect;
  }

  private DIDEffectRecord handleDIDEffect(int effectId)
  {
    BuffInstance buffInstance=null;
    BuffRegistry buffsRegistry=BuffRegistry.getInstance();
    String key=String.valueOf(effectId);
    Buff buff=buffsRegistry.getBuffById(key);
    if (buff!=null)
    {
      LOGGER.debug("Found buff: "+buff);
      buffInstance=buffsRegistry.newBuffInstance(key);
    }
    else
    {
      if (LOGGER.isDebugEnabled())
      {
        String name="?";
        PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
        if (effectProps!=null)
        {
          // Name
          name=DatStringUtils.getStringProperty(effectProps,"Effect_Name");
        }
        LOGGER.debug("Unknown buff: "+effectId+": "+name);
      }
    }
    if (buffInstance!=null)
    {
      DIDEffectRecord ret=new DIDEffectRecord(buffInstance);
      return ret;
    }
    return null;
  }
}
