package delta.games.lotro.extractors.character;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatType;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Extracts actual character stats from its properties.
 * @author DAM
 */
public class ActualStatsExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ActualStatsExtractor.class);

  private void extractStat(BasicStatsSet stats, StatDescription stat, PropertiesSet props, String name, float factor)
  {
    extractStat(stats,stat,props,name);
    Number value=stats.getStat(stat);
    if (value!=null)
    {
      float newValue=value.floatValue()*factor;
      stats.setStat(stat,Float.valueOf(newValue));
    }
  }

  private void extractStat(BasicStatsSet stats, StatDescription stat, PropertiesSet props, String name)
  {
    StatType type=stat.getType();
    if (type==StatType.FLOAT)
    {
      extractFloatStat(stats,stat,props,name);
    }
    else if (type==StatType.INTEGER)
    {
      extractIntStat(stats,stat,props,name);
    }
    else
    {
      LOGGER.warn("Unmanaged stat type: {}",stat);
    }
  }

  private void extractFloatStat(BasicStatsSet stats, StatDescription stat, PropertiesSet props, String name)
  {
    Object value=props.getProperty(name);
    if (value instanceof Float)
    {
      Float floatValue=(Float)value;
      stats.setStat(stat,floatValue.floatValue());
    }
  }

  private void extractIntStat(BasicStatsSet stats, StatDescription stat, PropertiesSet props, String name)
  {
    Object value=props.getProperty(name);
    if (value instanceof Integer)
    {
      Integer intValue=(Integer)value;
      stats.setStat(stat,intValue.intValue());
    }
  }

  /**
   * Extract stats from the character properties.
   * @param props Properties to use.
   * @return the loaded stats.
   */
  public BasicStatsSet extract(PropertiesSet props)
  {
    BasicStatsSet ret=new BasicStatsSet();
    // Main stats
    // - Morale
    extractStat(ret,WellKnownStat.MORALE,props,"Health_CurrentLevel");
    // - Power
    extractStat(ret,WellKnownStat.POWER,props,"Power_CurrentLevel");
    // - Might
    extractStat(ret,WellKnownStat.MIGHT,props,"Stat_Might");
    // - Agility
    extractStat(ret,WellKnownStat.AGILITY,props,"Stat_Agility");
    // - Will
    extractStat(ret,WellKnownStat.WILL,props,"Stat_Will");
    // - Vitality
    extractStat(ret,WellKnownStat.VITALITY,props,"Stat_Vitality");
    // - Fate
    extractStat(ret,WellKnownStat.FATE,props,"Stat_Fate");
    // - Armour
    extractStat(ret,WellKnownStat.ARMOUR,props,"Combat_Agent_Armor_Value_Float");

    // Vitals (those 4 need x60)
    extractStat(ret,WellKnownStat.OCMR,props,"Vital_HealthPeaceCurrentRegen",60);
    extractStat(ret,WellKnownStat.ICMR,props,"Vital_HealthCombatCurrentRegen",60);
    extractStat(ret,WellKnownStat.OCPR,props,"Vital_PowerPeaceCurrentRegen",60);
    extractStat(ret,WellKnownStat.ICPR,props,"Vital_PowerCombatCurrentRegen",60);

    // Offense
    extractStat(ret,WellKnownStat.PHYSICAL_MASTERY,props,"Combat_Cached_OffensePoints_Melee");
    extractStat(ret,WellKnownStat.TACTICAL_MASTERY,props,"Combat_Cached_OffensePoints_Tactical");
    extractStat(ret,WellKnownStat.FINESSE,props,"Combat_Current_FinessePoints");
    extractStat(ret,WellKnownStat.CRITICAL_RATING,props,"Combat_Current_CriticalPoints_Unified");
    // Could be Combat_Current_CriticalPoints_Melee or Combat_Current_CriticalPoints_Ranged or Combat_Current_CriticalPoints_Tactical

    // Defence/mitigations
    extractStat(ret,WellKnownStat.BLOCK,props,"Combat_Current_BlockPoints"); // or Combat_Stats_BlockPoints
    extractStat(ret,WellKnownStat.PARRY,props,"Combat_Current_ParryPoints"); // or Combat_Stats_ParryPoints
    extractStat(ret,WellKnownStat.EVADE,props,"Combat_Current_EvadePoints");
    // - Resistance
    extractStat(ret,WellKnownStat.RESISTANCE,props,"Resist_Cached_TotalPoints_Resistance_TheOneResistance");
    // Could be Resist_Cached_TotalPoints_Resistance_Corruption or Resist_Cached_TotalPoints_Resistance_Disease
    // or Resist_Cached_TotalPoints_Resistance_Elemental or Resist_Cached_TotalPoints_Resistance_Fear
    // or Resist_Cached_TotalPoints_Resistance_FeignDeath or Resist_Cached_TotalPoints_Resistance_Magic
    // or Resist_Cached_TotalPoints_Resistance_Physical or Resist_Cached_TotalPoints_Resistance_Poison
    // or Resist_Cached_TotalPoints_Resistance_Song or Resist_Cached_TotalPoints_Resistance_Wound
    // or Resist_StatPoints_Resistance_TheOneResistance
    // - Critical Defence
    extractStat(ret,WellKnownStat.CRITICAL_DEFENCE,props,"Combat_Unified_Critical_Defense_Cached");
    // - Physical Mitigation
    extractStat(ret,WellKnownStat.PHYSICAL_MITIGATION,props,"Combat_ArmorDefense_CurrentPoints_Common");
    // - Tactical Mitigation
    extractStat(ret,WellKnownStat.TACTICAL_MITIGATION,props,"Combat_ArmorDefense_CurrentPoints_UnifiedTactical");
    // Could be Combat_ArmorDefense_CurrentPoints_Acid or Combat_ArmorDefense_CurrentPoints_Fire
    // or Combat_ArmorDefense_CurrentPoints_Frost or Combat_ArmorDefense_CurrentPoints_Light
    // or Combat_ArmorDefense_CurrentPoints_Lightning or Combat_ArmorDefense_CurrentPoints_Shadow
    // - Orc-craft/fell-wrought Mitigation
    extractStat(ret,WellKnownStat.OCFW_MITIGATION,props,"Combat_ArmorDefense_CurrentPoints_OrcCraft");
    // Could be Combat_ArmorDefense_CurrentPoints_FellWrought or Combat_ArmorDefense_CurrentPoints_AncientDwarf
    // or Combat_ArmorDefense_CurrentPoints_Beleriand or Combat_ArmorDefense_CurrentPoints_UnifiedPhysical
    // or Combat_ArmorDefense_CurrentPoints_Westernesse

      // Healing
      // - Outgoing healing
    extractStat(ret,WellKnownStat.OUTGOING_HEALING,props,"Combat_Modifier_OutgoingHealing_Points_Cached");
      // - Outgoing healing percentage (needs x100)
    extractStat(ret,WellKnownStat.OUTGOING_HEALING_PERCENTAGE,props,"Combat_OutgoingHeals_EffectMod",100);
      // - Incoming healing
      extractStat(ret,WellKnownStat.INCOMING_HEALING,props,"Combat_IncomingHealing_Points_Cached");

      // Misc
      extractStat(ret,WellKnownStat.LIGHT_OF_EARENDIL,props,"LoE_Light_Value");

      return ret;
  }
}
