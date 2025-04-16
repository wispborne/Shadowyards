package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import data.campaign.econ.MS_industries;
import org.lwjgl.util.vector.Vector2f;

/**
 * Wisp: This is mostly contributed by WMGreywind (PAGSM), with a few changes to fit SRA better.
 */
public class MS_SDFShadowyards extends SDFBase {

    public MS_SDFShadowyards() {
    }

    @Override
    protected String getFactionId() {
        return "shadow_industry";
    }

    protected SkillPickPreference getCommanderShipSkillPreference() {
        return SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE;
    }

    @Override
    protected MarketAPI getSourceMarket() {
        return Global.getSector().getEconomy().getMarket("euripides");
    }

    @Override
    protected String getDefeatTriggerToUse() {
        return "MS_SDFShadowyardsDefeated";
    }

    @Override
    public boolean canSpawnFleetNow() {
        MarketAPI source = getSourceMarket();
        if (source == null || source.hasCondition(Conditions.DECIVILIZED)) return false;
        // Wisp: Replaced requirement for either Military Base or High Command with a requirement for Redwings Command
        if (!source.hasIndustry(MS_industries.REDWINGS)) return false;
        if (!source.getFactionId().equals(getFactionId())) return false;
        return true;
    }

    @Override
    public CampaignFleetAPI spawnFleet() {

        MarketAPI euripides = getSourceMarket();

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = euripides.getLocationInHyperspace();

        m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_2, "redwings", FleetTypes.PATROL_LARGE, loc);
        // Wisp: Very important! Otherwise the fleet is oops all ventures.
        m.triggerFleetSetShipPickMode(FactionAPI.ShipPickMode.PRIORITY_THEN_ALL);

        m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
        m.triggerSetFleetDoctrineComp(3, 0, 1);
        m.triggerSetFleetCommander(getPerson());

        m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
        m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
        m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
        m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
        m.triggerFleetAddCommanderSkill(Skills.OFFICER_TRAINING, 1);

        m.triggerSetPatrol();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, euripides);
        m.triggerFleetSetNoFactionInName();
        m.triggerFleetSetName("Redwings Reserve Armada");
        m.triggerPatrolAllowTransponderOff();
        //m.triggerFleetSetPatrolActionText("patrolling");
        m.triggerOrderFleetPatrol(euripides.getStarSystem());

        CampaignFleetAPI fleet = m.createFleet();
        // Wisp: Create a Redwings fleet, but set faction to Shadowyards.
        fleet.setFaction("shadow_industry", true);
        fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
        euripides.getContainingLocation().addEntity(fleet);
        fleet.setLocation(euripides.getPlanetEntity().getLocation().x, euripides.getPlanetEntity().getLocation().y);
        fleet.setFacing((float) random.nextFloat() * 360f);


        return fleet;
    }
}




