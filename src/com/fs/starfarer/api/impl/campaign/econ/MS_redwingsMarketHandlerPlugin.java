package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import data.campaign.econ.MS_industries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MS_redwingsMarketHandlerPlugin implements EveryFrameScript {

    private static final List<String> SHIP_SELECT = new ArrayList<>(Arrays.asList(new String[]{
            "ms_ninurta",
            "ms_enlil_redwing",
            "ms_seski_redwing",
            "ms_shamash_redwing",
            "ms_morningstar_redwing",
            "ms_clade_redwing",
            "ms_elysium_redwing",
            "ms_scylla_redwing",
            "ms_charybdis_redwing",
            "ms_mimir_redwing",
            "ms_skadi_redwing",
            "ms_vardr_redwing",
            "ms_carmine_redwing" //remove this one when the actual mission gets set up
    }));

    private static final List<String> SPECIAL_ITEMS = new ArrayList<>(Arrays.asList(new String[]{
            "ms_parallelTooling",
            "ms_militaryLogistics",
            "ms_specializedSystemsFabs",
            "industry_bp"
    }));

    // Wisp: backwards compat.
    private RedwingsMarketListener listener;

    @Override
    public void advance(float amount) {
        SectorAPI sector = Global.getSector();

        if (sector == null) return;

        // Remove old script and clear it.
        if (listener != null) {
            listener.sector = null;
            listener.targetMarkets = null;
            listener = null;
        }

        // Wisp:
        // Remove old, non-transient listener that had a memory leak.
        for (CampaignEventListener listener : sector.getAllListeners()) {
            if (listener instanceof RedwingsMarketListener) {
                ((RedwingsMarketListener) listener).sector = null;
                ((RedwingsMarketListener) listener).targetMarkets = null;
                sector.removeListener(listener);
            }
        }

        // Ensure we have a listener
        boolean hasListener = false;
        for (CampaignEventListener listener : sector.getAllListeners()) {
            if (listener instanceof RedwingsMarketListener2) {
                hasListener = true;
                break;
            }
        }

        if (!hasListener) {
            sector.addTransientListener(new RedwingsMarketListener2(false));
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    // Wisp: Kept to prevent breaking saves, but unused because `sector` as a field variable is a memory leak.
    @Deprecated
    private class RedwingsMarketListener extends BaseCampaignEventListener {
        private List<MarketAPI> targetMarkets = new ArrayList<>();
        private boolean hasRedwings = false;
        // Wisp: This was a memory leak, keeping a ref to Sector in a listener that was added to the Sector.
        SectorAPI sector = null;

        private RedwingsMarketListener() {
            super(false);
        }
    }

    private class RedwingsMarketListener2 extends BaseCampaignEventListener {
        private final List<String> targetMarketIds = new ArrayList<>();
        private boolean hasRedwings = false;

        public RedwingsMarketListener2(boolean permaRegister) {
            super(permaRegister);
        }

        @Override
        public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
            MarketAPI commandMarket = null;
            FactionAPI shadow = Global.getSector().getFaction("shadow_industry");
            FactionAPI red = Global.getSector().getFaction("redwings");

            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m.hasIndustry(MS_industries.REDWINGS)) {
                    commandMarket = m;
                }
            }

            if (commandMarket != null) {
                hasRedwings = true;
            }

            if (!red.knowsShip("ms_carmine_redwing")) red.addKnownShip("ms_carmine_redwing", false);

            if (!targetMarketIds.contains(market.getId())) {
                if (market.getFactionId().contains(shadow.getId())) {
                    // Redwings need their command structure to exist:
                    if (hasRedwings) {
                        if (market.getSubmarket(Submarkets.GENERIC_MILITARY) != null) {
                            CargoAPI cargo = market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
                            if (cargo != null) {
                                int size = market.getSize();
                                for (int i = 0; i < size; i++) {
                                    //run a random against a set float
                                    //on success we add a random ship from the redwings lineup
                                    if (Math.random() > 0.8f) {
                                        Random rand = new Random();
                                        String shipToAdd = SHIP_SELECT.get(rand.nextInt(SHIP_SELECT.size())) + "_Hull";
                                        String name = shadow.pickRandomShipName();
                                        //log.debug("Adding Ship: type = " + shipToAdd + " at " + market.getName());
                                        cargo.addMothballedShip(FleetMemberType.SHIP, shipToAdd, name);
                                    }
                                }

                                cargo.initMothballedShips(shadow.getId());
                                // Wisp: fixed bug where Redwings ships had 0 CR until hovered over.
                                for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
                                    member.getRepairTracker().setCR(0.5f);
                                    cargo.getMothballedShips().addFleetMember(member);
                                }
                            }
                        }

                        if (market.getSubmarket(Submarkets.SUBMARKET_BLACK) != null) {
                            CargoAPI blackM = market.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo();
                            if (blackM != null) {
                                //same odds, but fewer chances
                                for (int i = 0; i < 2; i++) {
                                    if (Math.random() > 0.9f) {
                                        Random rand = new Random();
                                        String shipToAdd = SHIP_SELECT.get(rand.nextInt(SHIP_SELECT.size())) + "_Hull";
                                        String name = shadow.pickRandomShipName();
                                        blackM.addMothballedShip(FleetMemberType.SHIP, shipToAdd, name);
                                    }
                                }

                                blackM.initMothballedShips(shadow.getId());
                                // Wisp: fixed bug where Redwings ships had 0 CR until hovered over.
                                for (FleetMemberAPI member : blackM.getMothballedShips().getMembersListCopy()) {
                                    member.getRepairTracker().setCR(0.5f);
                                    blackM.getMothballedShips().addFleetMember(member);
                                }
                            }
                        }
                    }


                    if (market.getSubmarket(Submarkets.GENERIC_MILITARY) != null) {
                        CargoAPI cargo = market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
                        if (cargo != null) {
                            for (int i = 0; i < 1; i++) {
                                if (Math.random() > 0.8f) {
                                    Random rand = new Random();
                                    String specialItem = SPECIAL_ITEMS.get(rand.nextInt(SPECIAL_ITEMS.size()));
                                    if (specialItem != null) {
                                        if (specialItem.equals("industry_bp"))
                                            cargo.addSpecial(new SpecialItemData(specialItem, "ms_orbitalstation"), 1);
                                        else cargo.addSpecial(new SpecialItemData(specialItem, null), 1);
                                    }
                                }
                            }
                        }
                    }

                    if (market.getSubmarket(Submarkets.SUBMARKET_OPEN) != null) {
                        CargoAPI open = market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo();
                        if (open != null) {
                            for (int i = 0; i < 1; i++) {
                                if (Math.random() > 0.9f) {
                                    Random rand = new Random();
                                    String specialItem = SPECIAL_ITEMS.get(rand.nextInt(SPECIAL_ITEMS.size()));
                                    if (specialItem != null) {
                                        if (specialItem.equals("industry_bp"))
                                            open.addSpecial(new SpecialItemData(specialItem, "ms_orbitalstation"), 1);
                                        else open.addSpecial(new SpecialItemData(specialItem, null), 1);
                                    }
                                }
                            }
                        }
                    }

                    targetMarketIds.add(market.getId());
                }
            }
        }

        @Override
        public void reportEconomyMonthEnd() {
            targetMarketIds.clear();
        }
    }
}
