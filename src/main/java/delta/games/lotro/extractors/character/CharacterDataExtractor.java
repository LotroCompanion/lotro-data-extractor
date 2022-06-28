package delta.games.lotro.extractors.character;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.CharacterFile;
import delta.games.lotro.character.details.CharacterDetails;
import delta.games.lotro.character.events.CharacterEvent;
import delta.games.lotro.character.events.CharacterEventType;
import delta.games.lotro.character.status.crafting.CraftingLevelStatus;
import delta.games.lotro.character.status.crafting.CraftingLevelTierStatus;
import delta.games.lotro.character.status.crafting.CraftingStatus;
import delta.games.lotro.character.status.crafting.CraftingStatusManager;
import delta.games.lotro.character.status.crafting.GuildStatus;
import delta.games.lotro.character.status.crafting.KnownRecipes;
import delta.games.lotro.character.status.crafting.ProfessionStatus;
import delta.games.lotro.character.status.reputation.FactionStatus;
import delta.games.lotro.character.status.reputation.ReputationStatus;
import delta.games.lotro.character.virtues.SingleVirtueStatus;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.VirtuesManager;
import delta.games.lotro.character.virtues.VirtuesStatus;
import delta.games.lotro.character.virtues.io.VirtuesStatusIO;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.Vocation;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.utils.events.EventsManager;

/**
 * Extractor for character data (details, reputation, crafting, virtues...).
 * @author DAM
 */
public class CharacterDataExtractor
{
  private static final Logger LOGGER=Logger.getLogger(CharacterDataExtractor.class);

  /**
   * Synchronize character details.
   * @param toon Targeted character.
   * @param props Properties to use.
   */
  public void syncDetails(CharacterFile toon, PropertiesSet props)
  {
    CharacterDetails details=toon.getDetails();
    // XP
    Long xp=(Long)props.getProperty("XP_EarnedXP");
    details.setXp(xp!=null?xp.longValue():0);
    // Position
    //Object pos=props.getProperty("Physics_LastValidPosition");
    // In-game time
    Integer inGameTime=(Integer)props.getProperty("Time_TotalInGameTime");
    details.setIngameTime(inGameTime!=null?inGameTime.intValue():0);
    // Title
    Integer titleId=(Integer)props.getProperty("Title_ActiveTitleDID");
    details.setCurrentTitleId(titleId);
    // Vocation
    Integer vocationId=(Integer)props.getProperty("Craft_Vocation");
    details.setCurrentVocationId(vocationId);
    // Area
    Integer areaID=(Integer)props.getProperty("Area_CurrentArea");
    details.setAreaID(areaID);
    // Dungeon
    Integer dungeonID=(Integer)props.getProperty("Dungeon_CurrentDungeon");
    details.setDungeonID(dungeonID);
    // Money
    Integer coppers=(Integer)props.getProperty("Currency_Amount");
    details.getMoney().setRawValue(coppers!=null?coppers.intValue():0);
    // Last logout date
    Integer lastLogout=(Integer)props.getProperty("Time_LastLogoutTimeStamp");
    if (lastLogout!=null)
    {
      Long timeStamp=Long.valueOf(lastLogout.longValue()*1000);
      details.setLastLogoutDate(timeStamp);
    }
    toon.saveDetails(details);
    CharacterEvent event=new CharacterEvent(CharacterEventType.CHARACTER_DETAILS_UPDATED,toon,null);
    EventsManager.invokeEvent(event);
  }

  /**
   * Synchronize reputation status.
   * @param toon Targeted character.
   * @param props Properties to use.
   */
  public void syncReputationStatus(CharacterFile toon, PropertiesSet props)
  {
    ReputationStatus repStatus=toon.getReputation();
    FactionsRegistry factions=FactionsRegistry.getInstance();
    for(Faction faction : factions.getAll())
    {
      FactionStatus factionStatus=repStatus.getOrCreateFactionStat(faction);
      syncFactionStatus(factionStatus,props);
    }
    toon.saveReputation();
  }

  private void syncFactionStatus(FactionStatus factionStatus, PropertiesSet props)
  {
    Faction faction=factionStatus.getFaction();
    // Tier
    String currentTierPropertyName=faction.getCurrentTierPropertyName();
    Integer currentTier=(Integer)props.getProperty(currentTierPropertyName);
    if (currentTier!=null)
    {
      // Level
      FactionLevel currentLevel=faction.getLevelByTier(currentTier.intValue());
      factionStatus.setFactionLevel(currentLevel);
      factionStatus.setReputationFromFactionLevel();
      // Reputation
      String currentReputationPropertyName=faction.getCurrentReputationPropertyName();
      Long currentReputation=(Long)props.getProperty(currentReputationPropertyName);
      if (currentReputation!=null)
      {
        factionStatus.setReputation(Integer.valueOf(currentReputation.intValue()));
      }
    }
    else
    {
      // Undefined
      factionStatus.setFactionLevel(null);
      factionStatus.setReputation(null);
    }
  }

  /**
   * Synchronize crafting status.
   * @param toon Targeted character.
   * @param props Properties to use.
   */
  public void syncCraftingStatus(CharacterFile toon, PropertiesSet props)
  {
    // Note that this updates the current reputation for all factions and guilds,
    // But it does not update the history of status changes
    CraftingStatusManager craftingMgr=toon.getCraftingMgr();
    CraftingStatus craftingStatus=craftingMgr.getCraftingStatus();
    CraftingData craftingData=CraftingSystem.getInstance().getData();

    // Vocation
    Integer vocationId=(Integer)props.getProperty("Craft_Vocation");
    if (vocationId!=null)
    {
      Vocation vocation=craftingData.getVocationsRegistry().getVocationById(vocationId.intValue());
      craftingStatus.setVocation(vocation);
    }

    Professions professions=craftingData.getProfessionsRegistry();
    for(Profession profession : professions.getAll())
    {
      String enabledProperty=profession.getEnabledPropertyName();
      Integer enabled=(Integer)props.getProperty(enabledProperty);
      if ((enabled==null) || (enabled.intValue()==0))
      {
        continue;
      }
      LOGGER.debug("\tProfession: "+profession.getName());
      ProfessionStatus professionStatus=craftingStatus.getProfessionStatus(profession,true);
      // Mastery
      String masteryLevelProperty=profession.getMasteryLevelPropertyName();
      Integer masteryLevel=(Integer)props.getProperty(masteryLevelProperty);
      String masteryXpProperty=profession.getMasteryXpPropertyName();
      Integer masteryXp=(Integer)props.getProperty(masteryXpProperty);
      LOGGER.debug("\t\tMastery: level="+masteryLevel+", XP="+masteryXp);
      updateProfession(professionStatus,masteryLevel,masteryXp,true);
      // Proficiency
      String proficiencyLevelProperty=profession.getProficiencyLevelPropertyName();
      Integer proficiencyLevel=(Integer)props.getProperty(proficiencyLevelProperty);
      String proficiencyXpProperty=profession.getProficiencyXpPropertyName();
      Integer proficiencyXp=(Integer)props.getProperty(proficiencyXpProperty);
      LOGGER.debug("\t\tProficiency: level="+proficiencyLevel+", XP="+proficiencyXp);
      updateProfession(professionStatus,proficiencyLevel,proficiencyXp,false);
      // Extra recipes
      KnownRecipes knownRecipes=professionStatus.getKnownRecipes();
      fetchKnownRecipes(profession,props,knownRecipes);

      // Sync guild status if needed
      Faction guildFaction=profession.getGuildFaction();
      if (guildFaction!=null)
      {
        GuildStatus guildStatus=craftingStatus.getGuildStatus(profession,true);
        syncFactionStatus(guildStatus.getFactionStatus(),props);
      }
    }
    craftingMgr.saveCrafting();
  }

  private void fetchKnownRecipes(Profession profession, PropertiesSet props, KnownRecipes knownRecipes)
  {
    knownRecipes.clear();
    String extraRecipesProperty=profession.getExtraRecipesPropertyName();
    Object[] recipeIdsArray=(Object[])props.getProperty(extraRecipesProperty);
    if (recipeIdsArray!=null)
    {
      for(Object recipeIdObj : recipeIdsArray)
      {
        int recipeId=((Integer)recipeIdObj).intValue();
        knownRecipes.addRecipe(recipeId);
      }
    }
  }

  private void updateProfession(ProfessionStatus professionStatus, Integer tier, Integer xp, boolean mastery)
  {
    Profession profession=professionStatus.getProfession();
    if (tier!=null)
    {
      CraftingLevel masteryCraftingLevel=profession.getByTier(tier.intValue());
      professionStatus.setCompletionStatus(masteryCraftingLevel,mastery,true);
    }
    else
    {
      CraftingLevel beginnerLevel=profession.getBeginnerLevel();
      professionStatus.setCompletionStatus(beginnerLevel,mastery,true);
    }
    if ((xp!=null) && (xp.intValue()>0))
    {
      CraftingLevel craftingLevel;
      if (tier!=null)
      {
        craftingLevel=profession.getByTier(tier.intValue()+1);
      }
      else
      {
        craftingLevel=profession.getByTier(1);
      }
      if (craftingLevel!=null)
      {
        professionStatus.setCompletionStatus(craftingLevel,mastery,false);
        CraftingLevelStatus toUpdate=professionStatus.getLevelStatus(craftingLevel);
        CraftingLevelTierStatus tierStatus=mastery?toUpdate.getMastery():toUpdate.getProficiency();
        tierStatus.setAcquiredXP(xp.intValue());
      }
    }
  }

  /**
   * Synchronize virtues status.
   * @param toon Targeted character.
   * @param props Properties to use.
   */
  public void syncVirtuesStatus(CharacterFile toon, PropertiesSet props)
  {
    VirtuesStatus status=new VirtuesStatus();
    List<VirtueDescription> virtues=VirtuesManager.getInstance().getAll();
    for(VirtueDescription virtue : virtues)
    {
      String xpProperty=virtue.getXpPropertyName();
      Integer xp=(Integer)props.getProperty(xpProperty);
      int xpValue=(xp!=null)?xp.intValue():0;
      SingleVirtueStatus virtueStatus=status.getVirtueStatus(virtue);
      virtueStatus.setXp(xpValue);
      int tier=virtue.getTierForXp(xpValue);
      virtueStatus.setTier(tier);
    }
    VirtuesStatusIO.save(toon,status);
  }
}
