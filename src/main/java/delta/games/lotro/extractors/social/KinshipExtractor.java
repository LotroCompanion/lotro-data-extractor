package delta.games.lotro.extractors.social;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.Genders;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.extractors.TimeUtils;
import delta.games.lotro.kinship.Kinship;
import delta.games.lotro.kinship.KinshipCharacterSummary;
import delta.games.lotro.kinship.KinshipMember;
import delta.games.lotro.kinship.KinshipRank;
import delta.games.lotro.kinship.KinshipRoster;
import delta.games.lotro.kinship.KinshipSummary;
import delta.games.lotro.utils.StringUtils;

/**
 * Kinship roster extractor.
 * @author DAM
 */
public class KinshipExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(KinshipExtractor.class);

  /**
   * Extract kinship data.
   * @param guild Guild WSL description.
   * @param loginTimestamp Login time (seconds since Epoch).
   * @return the loaded kinship.
   */
  @SuppressWarnings("unchecked")
  public Kinship extract(ClassInstance guild, Integer loginTimestamp)
  {
    Kinship kinship=new Kinship();
    // Summary
    KinshipSummary summary=kinship.getSummary();
    // - kinship ID
    Long kinshipId=(Long)guild.getAttributeValue("m_id");
    if (kinshipId!=null)
    {
      summary.setKinshipID(new InternalGameId(kinshipId.longValue()));
    }
    // - leader ID
    Long leaderId=(Long)guild.getAttributeValue("m_iidLeader");
    if (leaderId!=null)
    {
      summary.setLeaderID(new InternalGameId(leaderId.longValue()));
    }
    // - founder ID
    Long founderId=(Long)guild.getAttributeValue("m_iidFounder");
    if (founderId!=null)
    {
      summary.setFounderID(new InternalGameId(founderId.longValue()));
    }
    // - name
    String name=(String)guild.getAttributeValue("m_name");
    summary.setName(name);
    // - Creation date
    Integer creationDateInt=(Integer)guild.getAttributeValue("m_timeCreated");
    Long creationDate=TimeUtils.getDateAsMs(creationDateInt);
    summary.setCreationDate(creationDate);
    // - MOTD
    String motd=(String)guild.getAttributeValue("m_motd");
    summary.setMotd(motd);
    // Roster
    KinshipRoster roster=kinship.getRoster();
    // - ranks
    Map<Integer,ClassInstance> ranks=(Map<Integer,ClassInstance>)guild.getAttributeValue("m_rcRanks");
    for(Map.Entry<Integer,ClassInstance> rankEntry:ranks.entrySet())
    {
      ClassInstance rankInstance=rankEntry.getValue();
      Integer rankId=rankEntry.getKey();
      String rankName=(String)rankInstance.getAttributeValue("m_siName");
      Integer levelInt=(Integer)rankInstance.getAttributeValue("m_uLevel");
      int level=(levelInt!=null)?levelInt.intValue():0;
      LOGGER.debug("Rank {} => level={}, rank={}",rankId,levelInt,rankName);
      if (rankId!=null)
      {
        KinshipRank rank=new KinshipRank(rankId.intValue(),level,rankName);
        roster.addRank(rank);
      }
    }
    // - members
    Map<Long,ClassInstance> members=(Map<Long,ClassInstance>)guild.getAttributeValue("m_rcMemberData");
    for(ClassInstance memberInstance:members.values())
    {
      if (memberInstance==null)
      {
        continue;
      }
      KinshipMember member=extractMember(roster,memberInstance,loginTimestamp);
      roster.addMember(member);
    }
    return kinship;
  }

  private KinshipMember extractMember(KinshipRoster roster, ClassInstance memberInstance, Integer loginTimestamp)
  {
    KinshipMember member=new KinshipMember();
    KinshipCharacterSummary characterSummary=member.getSummary();
    // ID
    Long charId=(Long)memberInstance.getAttributeValue("m_iid");
    if (charId!=null)
    {
      characterSummary.setId(new InternalGameId(charId.longValue()));
    }
    // Name
    String charName=(String)memberInstance.getAttributeValue("m_name");
    if (charName==null)
    {
      charName="?";
      LOGGER.warn("Character name was null!");
    }
    // Sex
    boolean male;
    if (charName.contains(" [M"))
    {
      male=true;
    }
    else if (charName.contains(" [F"))
    {
      male=false;
    }
    else
    {
      LOGGER.warn("Unknown gender: {}",charName);
      male=true;
    }
    characterSummary.setCharacterSex(male?Genders.MALE:Genders.FEMALE);
    charName=StringUtils.fixName(charName);
    characterSummary.setName(charName);
    // Vocation
    Integer vocationID=(Integer)memberInstance.getAttributeValue("m_didVocation");
    characterSummary.setVocationID(vocationID);
    // Race
    Integer raceCode=(Integer)memberInstance.getAttributeValue("m_species");
    RaceDescription race=RacesManager.getInstance().getByCode(raceCode.intValue());
    characterSummary.setRace(race);
    // Class
    int classCode=((Integer)memberInstance.getAttributeValue("m_class")).intValue();
    ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode);
    characterSummary.setCharacterClass(characterClass);
    // Level
    Integer level=(Integer)memberInstance.getAttributeValue("m_uiLevel");
    characterSummary.setLevel(level!=null?level.intValue():1);
    // Area ID
    Integer areaId=(Integer)memberInstance.getAttributeValue("m_didArea");
    characterSummary.setAreaID(areaId);
    // Rank
    Integer rankId=(Integer)memberInstance.getAttributeValue("m_uiRankID");
    if (rankId!=null)
    {
      KinshipRank memberRank=roster.getRankByCode(rankId.intValue());
      member.setRank(memberRank);
    }
    // Last logout
    Long lastLogoutTimestamp=null;
    Integer lastLogout=(Integer)memberInstance.getAttributeValue("m_ttLastLogout");
    Long logoutDuration=TimeUtils.getDateAsMs(lastLogout);
    if (logoutDuration!=null)
    {
      long base=(loginTimestamp!=null)?loginTimestamp.longValue()*1000:System.currentTimeMillis();
      lastLogoutTimestamp=Long.valueOf(base-logoutDuration.longValue());
    }
    characterSummary.setLastLogoutDate(lastLogoutTimestamp);
    // Join date
    Integer joinDateInt=(Integer)memberInstance.getAttributeValue("m_ttJoinDate");
    Long joinTimestamp=TimeUtils.getDateAsMs(joinDateInt);
    member.setJoinDate(joinTimestamp);
    // Notes
    String notes=(String)memberInstance.getAttributeValue("212812885");
    if (notes==null) notes="";
    member.setNotes(notes);
    return member;
  }
}
