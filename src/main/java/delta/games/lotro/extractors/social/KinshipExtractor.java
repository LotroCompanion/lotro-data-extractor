package delta.games.lotro.extractors.social;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.extractors.TimeUtils;
import delta.games.lotro.utils.StringUtils;
import delta.games.lotro.utils.dat.DatEnumsUtils;

/**
 * Kinship roster extractor.
 * @author DAM
 */
public class KinshipExtractor
{
  private static final Logger LOGGER=Logger.getLogger(KinshipExtractor.class);

  private DateFormat _dateFormat=new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

  /**
   * Extract kinship data.
   * @param guild Guild WSL description.
   */
  @SuppressWarnings({"unused","unchecked"})
  public void extract(ClassInstance guild)
  {
    Long leaderId=(Long)guild.getAttributeValue("m_iidLeader");
    String name=(String)guild.getAttributeValue("m_name");
    LOGGER.debug("Guild: "+name);
    Map<Integer,ClassInstance> ranks=(Map<Integer,ClassInstance>)guild.getAttributeValue("m_rcRanks");
    for(Map.Entry<Integer,ClassInstance> rankEntry : ranks.entrySet())
    {
      ClassInstance rank=rankEntry.getValue();
      Integer rankId=rankEntry.getKey();
      String rankName=(String)rank.getAttributeValue("m_siName");
      LOGGER.debug("Rank "+rankId+" => "+rankName);
    }
    LOGGER.debug("ID\tName\tRank\tLevel\tRace\tClass\tVocation\tLogout\tJoin\tNote");
    Map<Long,ClassInstance> members=(Map<Long,ClassInstance>)guild.getAttributeValue("m_rcMemberData");
    for(ClassInstance member : members.values())
    {
      if (member==null)
      {
        continue;
      }
      Long charId=(Long)member.getAttributeValue("m_iid");
      String charIdStr=String.format("0x%08x",charId);
      String charName=(String)member.getAttributeValue("m_name");
      boolean male;
      if (charName.contains(" [M")) male=true; 
      else if (charName.contains(" [F")) male=false;
      else
      {
        LOGGER.warn("Unknown sex: "+charName);
        male=true;
      }
      charName=StringUtils.fixName(charName);
      Integer vocation=(Integer)member.getAttributeValue("m_didVocation");
      Integer raceCode=(Integer)member.getAttributeValue("m_species");
      Race race=DatEnumsUtils.getRaceFromRaceId(raceCode.intValue());
      Integer classCode=(Integer)member.getAttributeValue("m_class");
      CharacterClass charClass=DatEnumsUtils.getCharacterClassFromId(classCode.intValue());
      Integer level=(Integer)member.getAttributeValue("m_uiLevel");
      //Integer areaId=(Integer)member.getAttributeValue("m_didArea");
      Integer rankId=(Integer)member.getAttributeValue("m_uiRankID");
      String rankName=(String)ranks.get(rankId).getAttributeValue("m_siName");
      if ((rankName!=null) && (rankName.contains("Kinsman")))
      {
        rankName=male?"Kinsman":"Kinswoman";
      }
      Integer lastLogout=(Integer)member.getAttributeValue("m_ttLastLogout");
      //String durationStr=(lastLogout!=null)?Duration.getDurationString(lastLogout.intValue()):"-";
      Integer joinDateInt=(Integer)member.getAttributeValue("m_ttJoinDate");
      Date joinDate=TimeUtils.getDate(joinDateInt);
      String joinDataStr=(joinDate!=null)?_dateFormat.format(joinDate):"-";
      //String accountName=(String)member.getAttributeValue("m_accountName");
      String note=(String)member.getAttributeValue("212812885");
      if (note==null) note="";
      //String sex=male?"Male":"Female";
      LOGGER.debug(charIdStr+"\t"+charName+/*"\t"+sex+*/"\t"+rankName+"\t"+level+"\t"+race+"\t"+charClass+"\t"+vocation+"\t"+lastLogout+"\t"+joinDataStr+/*"\t"+areaId+"\t"+accountName+*/"\t"+note);
    }
  }
}
