package delta.games.lotro.extractors.effects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.status.effects.EffectInstance;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.effects.DisplayEffectsUtils;
import delta.games.lotro.utils.dat.DatStatUtils;

/**
 * Effect record extractor.
 * @author DAM
 */
public class EffectRecordExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EffectRecordExtractor.class);

  private static final String M_F_SPELLCRAFT="m_fSpellcraft";
  private static final String M_IID_FROM_ITEM="m_iidFromItem";

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
   * Handle an effect record.
   * @param effects Storage for loaded data.
   * @param effectRecord Record to use.
   */
  public void handleEffectRecord(EffectsData effects, ClassInstance effectRecord)
  {
    if (effectRecord==null)
    {
      return;
    }
    LOGGER.debug("Character effect:");
    if (LOGGER.isDebugEnabled())
    {
      showEffectRecord(effectRecord);
    }
    SingleEffectData effectData=extract(effectRecord);
    if (effectData!=null)
    {
      effects.addEffect(effectData);
    }
  }

  private EffectInstance buildEffectInstance(ClassInstance effectRecord)
  {
    Integer effectDID=(Integer)effectRecord.getAttributeValue("m_didEffect");
    if (effectDID==null)
    {
      return null;
    }
    if (effectDID.intValue()==1879048787)
    {
      // Ignore ModificationEffect
      return null;
    }
    EffectsManager mgr=EffectsManager.getInstance();
    Effect effect=mgr.getEffectById(effectDID.intValue());
    if (effect==null)
    {
      LOGGER.warn("Unknown effect: {}",effectDID);
      return null;
    }
    Integer flags=(Integer)effectRecord.getAttributeValue("m_flags");
    if ((flags!=null) && ((flags.intValue()&0x100)==0))
    {
      LOGGER.info("Removing non-expressed effect: {}", effect);
      return null;
    }

    EffectInstance ret=new EffectInstance(effect);
    // Spellcraft
    Float spellCraft=(Float)effectRecord.getAttributeValue(M_F_SPELLCRAFT);
    ret.setSpellcraft(spellCraft);
    // Cast time
    Integer realTimeCast=(Integer)effectRecord.getAttributeValue("m_uRealTimeCast");
    if (realTimeCast!=null)
    {
      Long castTime=Long.valueOf(realTimeCast.longValue()*1000);
      ret.setCastTime(castTime);
    }
    // Caster
    ClassInstance casterResponsibility=(ClassInstance)effectRecord.getAttributeValue("m_rcCasterResponsibilityInfo");
    if (casterResponsibility!=null)
    {
      Long casterIIDL=(Long)casterResponsibility.getAttributeValue("m_iidOnBehalfOf");
      if (casterIIDL!=null)
      {
        InternalGameId casterIID=new InternalGameId(casterIIDL.longValue());
        ret.setCasterID(casterIID);
      }
    }
    LOGGER.debug("Built effect instance: {}",ret);
    return ret;
  }

  private void showEffectRecord(ClassInstance effectRecord)
  {
    LOGGER.debug("Effect record:");
    Integer effectDID=(Integer)effectRecord.getAttributeValue("m_didEffect");
    LOGGER.debug("Effect DID: {}",effectDID);
    Float spellCraft=(Float)effectRecord.getAttributeValue(M_F_SPELLCRAFT);
    LOGGER.debug("\tSpellcraft: {}",spellCraft);
    EffectsManager mgr=EffectsManager.getInstance();
    Effect effect=mgr.getEffectById(effectDID.intValue());
    if (effect!=null)
    {
      LOGGER.debug("Known effect: {}",effect);
      int level=(spellCraft!=null)?spellCraft.intValue():1;
      List<String> lines=new ArrayList<String>();
      DisplayEffectsUtils.showEffect(lines,effect,level,false);
      if (!lines.isEmpty())
      {
        LOGGER.debug("\tStats:");
        for(String line : lines)
        {
          LOGGER.debug("\t\t{}",line);
        }
      }
    }
    else
    {
      LOGGER.debug("Unknown effect!");
    }
    Long itemIid=(Long)effectRecord.getAttributeValue(M_IID_FROM_ITEM);
    LOGGER.debug("\tItem IID: {}",itemIid);
    Integer flags=(Integer)effectRecord.getAttributeValue("m_flags");
    LOGGER.debug("\tFlags: {}",flags);
    if (flags!=null)
    {
      String meaning=EffectRecordUtils.getEffectRecordFlags(flags.intValue());
      LOGGER.debug(" => {}",meaning);
    }
    Integer casterType=(Integer)effectRecord.getAttributeValue("m_eCasterType");
    LOGGER.debug("\tCaster Type: {}",casterType);
    Integer casterLevel=(Integer)effectRecord.getAttributeValue("m_uiCasterLevel");
    LOGGER.debug("\tCaster Level: {}",casterLevel);
    Double timeCast=(Double)effectRecord.getAttributeValue("m_timeCast");
    Integer realTimeCast=(Integer)effectRecord.getAttributeValue("m_uRealTimeCast");
    Date d=(realTimeCast!=null)?new Date(realTimeCast.longValue()*1000):null;
    LOGGER.debug("\tTime cast: {}, realTimeCast={}, date={}",timeCast,realTimeCast,d);
    Integer effectID=(Integer)effectRecord.getAttributeValue("m_idEffect");
    LOGGER.debug("\tEffect ID: {}",effectID);
    ClassInstance scratchPad=(ClassInstance)effectRecord.getAttributeValue("m_ScratchPad");
    BasicStatsSet stats=extractStats(scratchPad);
    LOGGER.debug("\tStats: {}",stats);
    Object blob=effectRecord.getAttributeValue("m_blob");
    LOGGER.debug("\tBlob: {}",blob);
    ClassInstance casterResponsibility=(ClassInstance)effectRecord.getAttributeValue("m_rcCasterResponsibilityInfo");
    LOGGER.debug("\tCaster responsibility info: {}",casterResponsibility);
    Integer categories=(Integer)effectRecord.getAttributeValue("m_categories");
    LOGGER.debug("\tCategories: {}",categories);
    Integer remainingPulses=(Integer)effectRecord.getAttributeValue("m_uRemainingPulses");
    LOGGER.debug("\tRemaining pulses: {}",remainingPulses);
  }

  /**
   * Extract effect data.
   * @param effectRecord Source data.
   * @return An effect data or <code>null</code>.
   */
  private SingleEffectData extract(ClassInstance effectRecord)
  {
    EffectInstance effectInstance=buildEffectInstance(effectRecord);
    if (effectInstance==null)
    {
      return null;
    }
    Long itemIid=(Long)effectRecord.getAttributeValue(M_IID_FROM_ITEM);
    Float spellCraft=(Float)effectRecord.getAttributeValue(M_F_SPELLCRAFT);
    ClassInstance scratchPad=(ClassInstance)effectRecord.getAttributeValue("m_ScratchPad");
    BasicStatsSet stats=extractStats(scratchPad);
    SingleEffectData ret=new SingleEffectData(itemIid,spellCraft.floatValue(),effectInstance,stats);
    return ret;
  }

  private BasicStatsSet extractStats(ClassInstance scratchPad)
  {
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
        if (propValue==null) continue;
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
          LOGGER.debug("Unmanaged value type: {} for property: {}",value,propDefinition.getName());
        }
      }
    }
    return stats;
  }
}
