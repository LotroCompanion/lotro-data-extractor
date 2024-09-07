package delta.games.lotro.extractors.emotes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.status.emotes.EmoteStatus;
import delta.games.lotro.character.status.emotes.EmotesStatusManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;

/**
 * Emotes status extractor.
 * @author DAM
 */
public class EmotesExtractor
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EmotesExtractor.class);

  private EmotesStatusManager _emotesStatusMgr;

  /**
   * Constructor.
   * @param emotesStatusMgr Emotes status manager.
   */
  public EmotesExtractor(EmotesStatusManager emotesStatusMgr)
  {
    _emotesStatusMgr=emotesStatusMgr;
  }

  /**
   * Extract emotes status.
   * @param props Character properties.
   */
  public void extract(PropertiesSet props)
  {
    _emotesStatusMgr.clear();
    EmotesManager mgr=EmotesManager.getInstance();
    Object[] emotesArray=(Object[])props.getProperty("Emote_GrantedList");
    if (emotesArray!=null)
    {
      for(Object emoteObj : emotesArray)
      {
        Integer emoteID=(Integer)emoteObj;
        if (emoteID==null)
        {
          continue;
        }
        EmoteDescription emote=mgr.getEmote(emoteID.intValue());
        if (emote==null)
        {
          LOGGER.warn("Unknown emote: "+emoteID);
          continue;
        }
        EmoteStatus status=_emotesStatusMgr.get(emote,true);
        status.setAvailable(true);
      }
    }
  }
}
