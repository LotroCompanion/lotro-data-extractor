package delta.games.lotro.extractors.pvp;

import delta.games.lotro.character.pvp.PVPStatus;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.pvp.Rank;
import delta.games.lotro.lore.pvp.RankScale;
import delta.games.lotro.lore.pvp.RankScaleKeys;
import delta.games.lotro.lore.pvp.RanksManager;

/**
 * PVP status extractor.
 * @author DAM
 */
public class PVPStatusExtractor
{
  /**
   * Extract PVP status.
   * @param props Character properties.
   * @return the extracted data.
   */
  public PVPStatus extract(PropertiesSet props)
  {
    PVPStatus ret=new PVPStatus();

    RanksManager ranksMgr=RanksManager.getInstance();
    // Glory points
    Integer glory=(Integer)props.getProperty("Glory_GloryPoints");
    ret.setGlory((glory!=null)?glory.intValue():0);
    // Rank
    Integer rankCodeInt=(Integer)props.getProperty("Glory_GloryRank");
    int rankCode=(rankCodeInt!=null)?rankCodeInt.intValue():0;
    RankScale renown=ranksMgr.getRankScale(RankScaleKeys.RENOWN);
    Rank rank=renown.getRank(rankCode);
    ret.setRank(rank);
    // Rating
    Float rating=(Float)props.getProperty("Glory_RatingPoints");
    ret.setRating((rating!=null)?rating.floatValue():0);
    // Prestige
    Integer prestigeCodeInt=(Integer)props.getProperty("Glory_Prestige");
    int prestigeCode=(prestigeCodeInt!=null)?prestigeCodeInt.intValue():0;
    RankScale prestigeScale=ranksMgr.getRankScale(RankScaleKeys.PRESTIGE);
    Rank prestige=prestigeScale.getRank(prestigeCode);
    ret.setPrestige(prestige);
    // Deaths
    Integer deaths=(Integer)props.getProperty("Glory_PVPDeaths");
    ret.setDeaths((deaths!=null)?deaths.intValue():0);
    // Kills
    Integer kills=(Integer)props.getProperty("Glory_PVPKills");
    ret.setKills((kills!=null)?kills.intValue():0);
    // Kills above rating
    Integer killsAboveRating=(Integer)props.getProperty("Glory_PVPKillsAboveRating");
    ret.setKillsAboveRating((killsAboveRating!=null)?killsAboveRating.intValue():0);
    // Kills below rating
    Integer killsBelowRating=(Integer)props.getProperty("Glory_PVPKillsBelowRating");
    ret.setKillsBelowRating((killsBelowRating!=null)?killsBelowRating.intValue():0);
    // Kills to deaths ratio
    Float kills2deathsRatio=(Float)props.getProperty("Glory_PVPKillToDeathRatio");
    ret.setKill2deathRatio((kills2deathsRatio!=null)?kills2deathsRatio.floatValue():0);
    // Highest rating killed
    Float highestRatingKilled=(Float)props.getProperty("Glory_PVPHighestRatingKilled");
    ret.setHighestRatingKilled((highestRatingKilled!=null)?highestRatingKilled.floatValue():0);
    // Death blows
    Integer deathBlows=(Integer)props.getProperty("Glory_PVPDeathBlows");
    ret.setDeathBlows((deathBlows!=null)?deathBlows.intValue():0);

    return ret;
  }
}
