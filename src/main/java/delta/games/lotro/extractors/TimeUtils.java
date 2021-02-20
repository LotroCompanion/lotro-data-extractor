package delta.games.lotro.extractors;

import java.util.Date;

/**
 * Utility methods related to time.
 * @author DAM
 */
public class TimeUtils
{
  /**
   * Get a date from a timestamp.
   * @param time Timestamp.
   * @return A date or <code>null</code> if no time.
   */
  public static Date getDate(Integer time)
  {
    if (time==null)
    {
      return null;
    }
    if (time.intValue()==0)
    {
      return null;
    }
    long timestamp=time.intValue();
    timestamp=timestamp*1000;
    Date date=new Date(timestamp);
    return date;
  }
}
