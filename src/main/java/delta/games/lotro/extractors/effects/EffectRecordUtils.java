package delta.games.lotro.extractors.effects;

/**
 * Utility methods related to effect records.
 * @author DAM
 */
public class EffectRecordUtils
{
  /**
   * Get the meaning of effect record flags.
   * @param flags Flags to use.
   * @return A readable meaning.
   */
  public static String getEffectRecordFlags(int flags)
  {
    StringBuilder sb=new StringBuilder();
    if ((flags&1)!=0) sb.append(" InfiniteTimeoutOverride");
    if ((flags&0x10)!=0) sb.append(" RemoveOnLogout");
    if ((flags&0x80)!=0) sb.append(" Equipper");
    if ((flags&100)!=0) sb.append(" Expressed");
    if ((flags&200)!=0) sb.append(" Threat");
    if ((flags&400)!=0) sb.append(" WasExpressedOnCheckpoint");
    if ((flags&800)!=0) sb.append(" BeforeSuppressionCalled");
    if ((flags&1000)!=0) sb.append(" AfterSuppressionCalled");
    if ((flags&2000)!=0) sb.append(" BeforeExpressionCalled");
    if ((flags&4000)!=0) sb.append(" AfterExpressionCalled");
    if ((flags&8000)!=0) sb.append(" Startup");
    if ((flags&10000)!=0) sb.append(" AlwaysShowModPropInExamination");
    return sb.toString().trim();
  }
}
