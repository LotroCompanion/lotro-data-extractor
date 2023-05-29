package delta.games.lotro.extractors.social;

import java.util.Map;

import delta.games.lotro.account.status.friends.Friend;
import delta.games.lotro.account.status.friends.FriendsManager;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.common.id.InternalGameId;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.extractors.TimeUtils;

/**
 * Friends data extractor.
 * @author DAM
 */
public class FriendsExtractor
{
  /**
   * Extract friends data.
   * @param rawData Raw data.
   * @return the loaded friends.
   */
  public FriendsManager extract(Map<Long,PropertiesSet> rawData)
  {
    FriendsManager ret=new FriendsManager();
    for(Map.Entry<Long,PropertiesSet> entry : rawData.entrySet())
    {
      Friend friend=extractFriend(entry.getKey(),entry.getValue());
      ret.addFriend(friend);
    }
    return ret;
  }

  private Friend extractFriend(Long id, PropertiesSet properties)
  {
    Friend ret=new Friend();
    // ID
    Long rawId=(Long)properties.getProperty("Entity_InstanceID");
    InternalGameId friendId=new InternalGameId(rawId.longValue());
    ret.setId(friendId);
    // Name
    String name=(String)properties.getProperty("Name");
    ret.setName(name);
    // Vocation
    Integer vocationID=(Integer)properties.getProperty("Craft_Vocation");
    ret.setVocationID(vocationID);
    // Class
    Integer classCode=(Integer)properties.getProperty("Agent_Class");
    if (classCode!=null)
    {
      ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode.intValue());
      ret.setCharacterClass(characterClass);
    }
    // Level
    Integer level=(Integer)properties.getProperty("Advancement_Level");
    ret.setLevel(level!=null?level.intValue():1);
    // Area ID
    Integer areaId=(Integer)properties.getProperty("Area_DID");
    ret.setAreaID(areaId);
    // Last logout
    Integer lastLogout=(Integer)properties.getProperty("Time_LastLogoutTimeStamp");
    Long lastLogoutTimestamp=TimeUtils.getDateAsMs(lastLogout);
    if (lastLogoutTimestamp!=null)
    {
      ret.setLastLogoutDate(lastLogoutTimestamp);
    }
    // Kinship
    String kinshipName=(String)properties.getProperty("Guild_Name");
    ret.setKinshipName(kinshipName);
    // Notes
    String annotation=(String)properties.getProperty("Annotation_Text");
    ret.setNote(annotation);
    return ret;
  }
}
