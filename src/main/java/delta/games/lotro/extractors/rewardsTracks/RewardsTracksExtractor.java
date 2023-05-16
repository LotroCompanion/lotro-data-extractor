package delta.games.lotro.extractors.rewardsTracks;

import java.util.List;

import delta.games.lotro.account.status.rewardsTrack.RewardsTrackStatus;
import delta.games.lotro.account.status.rewardsTrack.RewardsTracksStatusManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.rewardsTrack.RewardsTrack;
import delta.games.lotro.lore.rewardsTrack.RewardsTracksManager;

/**
 * Rewards tracks status extractor.
 * @author DAM
 */
public class RewardsTracksExtractor
{
  private RewardsTracksStatusManager _rewardsTracksStatusMgr;

  /**
   * Constructor.
   * @param rewardsTracksStatusMgr Rewards tracks status manager.
   */
  public RewardsTracksExtractor(RewardsTracksStatusManager rewardsTracksStatusMgr)
  {
    _rewardsTracksStatusMgr=rewardsTracksStatusMgr;
  }

  /**
   * Extract rewards tracks status.
   * @param props Character properties.
   */
  public void extract(PropertiesSet props)
  {
    RewardsTracksManager mgr=RewardsTracksManager.getInstance();
    List<RewardsTrack> rewardTracks=mgr.getAllRewardsTracks();
    for(RewardsTrack rewardsTrack : rewardTracks)
    {
      extractRewardsTrack(rewardsTrack,props);
    }
  }

  private void extractRewardsTrack(RewardsTrack rewardsTrack, PropertiesSet props)
  {
    RewardsTrackStatus status=_rewardsTracksStatusMgr.getStatus(rewardsTrack,true);
    // Claimed milestones
    String claimedMilestonesProperty=rewardsTrack.getClaimedMilestonesProperty();
    Integer claimedMilestonesInt=(Integer)props.getProperty(claimedMilestonesProperty);
    int claimedMilestones=(claimedMilestonesInt!=null)?claimedMilestonesInt.intValue():0;
    // Current milestone
    String currentMilestoneProperty=rewardsTrack.getCurrentMilestoneProperty();
    Integer currentMilestoneInt=(Integer)props.getProperty(currentMilestoneProperty);
    int currentMilestone=(currentMilestoneInt!=null)?currentMilestoneInt.intValue():0;
    // Last XP goal
    String lastExperienceGoalProperty=rewardsTrack.getLastExperienceGoalProperty();
    Integer lastExperienceGoalInt=(Integer)props.getProperty(lastExperienceGoalProperty);
    int lastExperienceGoal=(lastExperienceGoalInt!=null)?lastExperienceGoalInt.intValue():0;
    // Current XP
    String currentExperienceProperty=rewardsTrack.getCurrentExperienceProperty();
    Integer currentExperienceInt=(Integer)props.getProperty(currentExperienceProperty);
    int currentExperience=(currentExperienceInt!=null)?currentExperienceInt.intValue():0;
    // Next XP goal
    String nextExperienceGoalProperty=rewardsTrack.getNextExperienceGoalProperty();
    Integer nextExperienceGoalInt=(Integer)props.getProperty(nextExperienceGoalProperty);
    int nextExperienceGoal=(nextExperienceGoalInt!=null)?nextExperienceGoalInt.intValue():0;

    if ((claimedMilestones!=0) && (currentMilestone!=0) && (lastExperienceGoal!=0) && (currentExperience!=0) && (nextExperienceGoal!=0))
    {
      status.setClaimedMilestones(claimedMilestones);
      status.setCurrentMilestone(currentMilestone);
      status.setLastExperienceGoal(lastExperienceGoal);
      status.setCurrentExperience(currentExperience);
      status.setNextExperienceGoal(nextExperienceGoal);
    }
  }
}
