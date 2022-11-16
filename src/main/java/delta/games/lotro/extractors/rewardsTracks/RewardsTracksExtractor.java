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
    if (rewardTracks.size()>0)
    {
      RewardsTrack rewardsTrack=rewardTracks.get(rewardTracks.size()-1);
      extractRewardsTrack(rewardsTrack,props);
    }
  }

  private void extractRewardsTrack(RewardsTrack rewardsTrack, PropertiesSet props)
  {
    RewardsTrackStatus status=_rewardsTracksStatusMgr.getStatus(rewardsTrack,true);
    // Claimed milestones
    String claimedMilestonesProperty=rewardsTrack.getClaimedMilestonesProperty();
    Integer claimedMilestones=(Integer)props.getProperty(claimedMilestonesProperty);
    status.setClaimedMilestones((claimedMilestones!=null)?claimedMilestones.intValue():0);
    // Current milestone
    String currentMilestoneProperty=rewardsTrack.getCurrentMilestoneProperty();
    Integer currentMilestone=(Integer)props.getProperty(currentMilestoneProperty);
    status.setCurrentMilestone((currentMilestone!=null)?currentMilestone.intValue():0);
    // Last XP goal
    String lastExperienceGoalProperty=rewardsTrack.getLastExperienceGoalProperty();
    Integer lastExperienceGoal=(Integer)props.getProperty(lastExperienceGoalProperty);
    status.setLastExperienceGoal((lastExperienceGoal!=null)?lastExperienceGoal.intValue():0);
    // Current XP
    String currentExperienceProperty=rewardsTrack.getCurrentExperienceProperty();
    Integer currentExperience=(Integer)props.getProperty(currentExperienceProperty);
    status.setCurrentExperience((currentExperience!=null)?currentExperience.intValue():0);
    // Next XP goal
    String nextExperienceGoalProperty=rewardsTrack.getNextExperienceGoalProperty();
    Integer nextExperienceGoal=(Integer)props.getProperty(nextExperienceGoalProperty);
    status.setNextExperienceGoal((nextExperienceGoal!=null)?nextExperienceGoal.intValue():0);
  }
}
