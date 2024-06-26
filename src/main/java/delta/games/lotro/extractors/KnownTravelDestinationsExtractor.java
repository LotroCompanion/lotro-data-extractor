package delta.games.lotro.extractors;

import java.util.Set;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;

/**
 * Extractor for known travel destinations.
 * @author DAM
 */
public class KnownTravelDestinationsExtractor
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public KnownTravelDestinationsExtractor(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Extract locations data.
   * @param locationIds Set of location identifiers.
   */
  public void extract(Set<Integer> locationIds)
  {
    for(Integer locationId : locationIds)
    {
      showTravelLocation(locationId.intValue());
    }
  }

  private void showTravelLocation(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    @SuppressWarnings("unused")
    String travelDisplayName=DatStringUtils.getStringProperty(properties,"TravelDisplayName");
  }
}
