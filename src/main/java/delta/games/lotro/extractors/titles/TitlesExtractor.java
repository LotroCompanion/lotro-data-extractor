package delta.games.lotro.extractors.titles;

import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.titles.TitleStatus;
import delta.games.lotro.character.titles.TitlesStatusManager;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;

/**
 * Titles registry extractor.
 * @author DAM
 */
public class TitlesExtractor
{
  private static final Logger LOGGER=Logger.getLogger(TitlesExtractor.class);

  private TitlesStatusManager _titlesStatusMgr;

  /**
   * Constructor.
   * @param titlesStatusMgr Titles status manager.
   */
  public TitlesExtractor(TitlesStatusManager titlesStatusMgr)
  {
    _titlesStatusMgr=titlesStatusMgr;
  }

  /**
   * Extract titles.
   * @param titlesMap Map of acquired titles (Title ID to TitleAcquisitionData).
   */
  public void extract(Map<Integer,ClassInstance> titlesMap)
  {
    _titlesStatusMgr.clear();
    int size=titlesMap.size();
    LOGGER.debug("Nb entries: "+size);
    for(Map.Entry<Integer,ClassInstance> entry : titlesMap.entrySet())
    {
      int titleId=entry.getKey().intValue();
      ClassInstance titleAcquisitionData=entry.getValue();
      handleTitleStatus(titleId,titleAcquisitionData);
    }
  }

  @SuppressWarnings("unused")
  private void handleTitleStatus(int titleId, ClassInstance titleAcquisitionData)
  {
    // titleAcquisitionData is a TitleAcquisitionData
    TitlesManager titlesMgr=TitlesManager.getInstance();
    TitleDescription title=titlesMgr.getTitle(titleId);
    if (title==null)
    {
      LOGGER.warn("Title not found: ID="+titleId);
      return;
    }
    LOGGER.debug("Title name: "+title.getName());
    LOGGER.debug(titleAcquisitionData);
    TitleStatus titleStatus=_titlesStatusMgr.get(title,true);
    // Time of acquisition
    Double ttAcquired=(Double)titleAcquisitionData.getAttributeValue("m_ttAcquired");
    if (ttAcquired!=null)
    {
      titleStatus.setAcquisitionTimeStamp(ttAcquired);
    }
    // Acquisition type
    Integer acquisitionType=(Integer)titleAcquisitionData.getAttributeValue("m_eAcquisitionType");
  }

  void mapAcquisitionType(int acquisitionTypeCode)
  {
    /*
Enum: TitleAcquisitionType, (id=587202681)
0 => Undef
  1 => Accomplishment
  2 => Chargen
  3 => Crafting
  4 => Quest
  5 => Guild
  6 => Admin
  7 => Pedigree
  8 => Hobby
  9 => Account
     */
  }
}
