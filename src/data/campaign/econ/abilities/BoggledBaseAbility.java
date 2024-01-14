package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledProjectRequirementsAND;
import data.scripts.BoggledTerraformingProject;
import data.scripts.BoggledTerraformingRequirement;
import data.scripts.BoggledUnderConstructionEveryFrameScript;
import data.scripts.PlayerCargoCalculations.bogglesDefaultCargo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BoggledBaseAbility extends BaseDurationAbility {
    public static abstract class AbilityEffect {
        String id;
        String[] enableSettings;

        protected AbilityEffect(String id, String[] enableSettings) {
            this.id = id;
            this.enableSettings = enableSettings;
        }

        protected abstract void applyEffectImpl(CampaignFleetAPI playerFleet);
        protected abstract void unapplyEffectImpl(CampaignFleetAPI playerFleet);

        public void applyEffect(CampaignFleetAPI playerFleet) {
            if (!boggledTools.optionsAllowThis(enableSettings)) {
                return;
            }
            applyEffectImpl(playerFleet);
        }

        public void unapplyEffect(CampaignFleetAPI playerFleet) {
            if (!boggledTools.optionsAllowThis(enableSettings)) {
                return;
            }
            unapplyEffectImpl(playerFleet);
        }
    }

    public static class RemoveCredits extends AbilityEffect {
        int quantity;

        protected RemoveCredits(String id, String[] enableSettings, int quantity) {
            super(id, enableSettings);
            this.quantity = quantity;
        }

        @Override
        protected void applyEffectImpl(CampaignFleetAPI playerFleet) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(quantity);
        }

        @Override
        protected void unapplyEffectImpl(CampaignFleetAPI playerFleet) {}
    }

    public static class RemoveCommodity extends AbilityEffect {
        String commodityId;
        int quantity;

        protected RemoveCommodity(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings);
            this.commodityId = commodityId;
            this.quantity = quantity;
        }

        @Override
        protected void applyEffectImpl(CampaignFleetAPI playerFleet) {
            bogglesDefaultCargo.active.removeCommodity("a station", commodityId, quantity);
        }

        @Override
        protected void unapplyEffectImpl(CampaignFleetAPI playerFleet) {}
    }

    public static class AddOrbitalStation extends AbilityEffect {
        int maxNumAstro;
        float orbitRadius;
        int numDaysToBuild;

        private int numAstroInOrbit(SectorEntityToken targetPlanet)
        {
            SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

            if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
            {
                return 0;
            }

            List<SectorEntityToken> allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities();

            int numAstropoli = 0;
            for(SectorEntityToken entity : allEntitiesInSystem)
            {
                if (!entity.hasTag("boggled_astropolis")) {
                    continue;
                }
                if (entity.getOrbitFocus() == null) {
                    continue;
                }
                if (!entity.getOrbitFocus().equals(targetPlanet)) {
                    continue;
                }
                numAstropoli++;
            }

            return numAstropoli;
        }

        private String getGreekLetter(int numAstroAlreadyPresent)
        {
            int setting = boggledTools.getIntSetting("boggledAstropolisSpriteToUse");
            if(setting == 1)
            {
                return "alpha";
            }
            else if(setting == 2)
            {
                return "beta";
            }
            else if(setting == 3)
            {
                return "gamma";
            }
            numAstroAlreadyPresent = Math.abs(numAstroAlreadyPresent);
            return getGreekAlphabetList().get(numAstroAlreadyPresent % 3).toLowerCase();
        }

        private String getColonyNameString(int numAstroAlreadyPresent)
        {
            numAstroAlreadyPresent = Math.abs(numAstroAlreadyPresent);
            List<String> greekAlphabetList = getGreekAlphabetList();
            int letterNum = numAstroAlreadyPresent % greekAlphabetList.size();
            int suffixNum = numAstroAlreadyPresent / greekAlphabetList.size();
            String ret = greekAlphabetList.get(letterNum);
            if (suffixNum != 0) {
                ret = ret + "-" + suffixNum;
            }
            return ret;
        }

        @NotNull
        private static List<String> getGreekAlphabetList() {
            List<String> greekAlphabetList = new ArrayList<>();
            greekAlphabetList.add("Alpha");
            greekAlphabetList.add("Beta");
            greekAlphabetList.add("Gamma");
            greekAlphabetList.add("Delta");
            greekAlphabetList.add("Epsilon");
            greekAlphabetList.add("Zeta");
            greekAlphabetList.add("Eta");
            greekAlphabetList.add("Theta");
            greekAlphabetList.add("Kappa");
            greekAlphabetList.add("Lambda");
            greekAlphabetList.add("Mu");
            greekAlphabetList.add("Nu");
            greekAlphabetList.add("Xi");
            greekAlphabetList.add("Omicron");
            greekAlphabetList.add("Pi");
            greekAlphabetList.add("Rho");
            greekAlphabetList.add("Sigma");
            greekAlphabetList.add("Tau");
            greekAlphabetList.add("Upsilon");
            greekAlphabetList.add("Phi");
            greekAlphabetList.add("Chi");
            greekAlphabetList.add("Psi");
            greekAlphabetList.add("Omega");
            return greekAlphabetList;
        }

        private SectorEntityToken compareAndGetLatest(List<String> greekAlphabet, SectorEntityToken o1, SectorEntityToken o2) {
            if (o1 == null) {
                return o2;
            }
            if (o2 == null) {
                return o1;
            }
            int o1NumIndexStart = o1.getName().indexOf('-');
            int o2NumIndexStart = o2.getName().indexOf('-');
            if (o1NumIndexStart == -1 && o2NumIndexStart == -1) {
                // Neither has a number at the end, so we compare according to the index in the alphabet
                int o1GreekIndex = greekAlphabet.indexOf(o1.getName());
                int o2GreekIndex = greekAlphabet.indexOf(o2.getName());
                int comp =  Integer.compare(o1GreekIndex, o2GreekIndex);
                if (comp < 0) {
                    return o1;
                }
                // If they return equal, something else is broken
                if (comp == 0) {
                    Global.getLogger(this.getClass()).error("Sector entity tokens " + o1.getName() + " and " + o2.getName() + " compared equal");
                }
                return o2;
            }
            if (o1NumIndexStart == -1) {
                // o2 has a number at the end, o1 doesn't, therefore o1 comes before o2
                return o2;
            }
            if (o2NumIndexStart == -1) {
                // o1 has a number at the end, o2 doesn't, therefore o1 comes after o2
                return o1;
            }
            // They both have a number at the end, so compare that
            int o1Num = Integer.parseInt(o1.getName().substring(o1NumIndexStart + 1));
            int o2Num = Integer.parseInt(o2.getName().substring(o2NumIndexStart + 1));
            int comp = Integer.compare(o1Num, o2Num);
            if (comp < 0) {
                return o1;
            } else if (comp > 0) {
                return o2;
            }
            // The numbers are the same, so compare based on the index in the alphabet
            int o1GreekIndex = greekAlphabet.indexOf(o1.getName());
            int o2GreekIndex = greekAlphabet.indexOf(o2.getName());
            comp = Integer.compare(o1GreekIndex, o2GreekIndex);
            if (comp < 0) {
                return o1;
            }
            // If they return equal, something else is broken
            if (comp == 0) {
                Global.getLogger(this.getClass()).error("Sector entity tokens " + o1.getName() + " and " + o2.getName() + " compared equal");
            }
            return o2;
        }

        protected AddOrbitalStation(String id, String[] enableSettings, int maxNumAstro, float orbitRadius) {
            super(id, enableSettings);
            this.maxNumAstro = maxNumAstro;
            this.orbitRadius = orbitRadius;
        }

        @Override
        protected void applyEffectImpl(CampaignFleetAPI playerFleet) {
            StarSystemAPI system = playerFleet.getStarSystem();
            SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);

            String playerFactionId = Global.getSector().getPlayerFaction().getId();

            int numAstro = numAstroInOrbit(targetPlanet);

            assert targetPlanet != null;
            SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolis" + numAstro, targetPlanet.getName() + " Astropolis " + getColonyNameString(numAstro), "boggled_astropolis_station_" + getGreekLetter(numAstro) + "_small", playerFactionId);
            SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisLights", targetPlanet.getName() + " Astropolis " + getColonyNameString(numAstro) + " Lights Overlay", "boggled_astropolis_station_" + getGreekLetter(numAstro) + "_small_lights_overlay", playerFactionId);

            if (numAstro == 0) {
                newAstropolis.setCircularOrbitPointingDown(targetPlanet, boggledTools.randomOrbitalAngleFloat(), orbitRadius, orbitRadius / 10f);
            } else {
                final List<String> greekAlphabet = getGreekAlphabetList();
                List<SectorEntityToken> allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities();
                SectorEntityToken latestStation = null;
                for (SectorEntityToken entity : allEntitiesInSystem) {
                    if (!entity.hasTag("boggled_astropolis")) {
                        continue;
                    }
                    if (entity.getOrbitFocus() == null) {
                        continue;
                    }
                    if (!entity.getOrbitFocus().equals(targetPlanet)) {
                        continue;
                    }
                    if (!entity.getCustomEntityType().contains("boggled_astropolis_station")) {
                        continue;
                    }
                    latestStation = compareAndGetLatest(greekAlphabet, latestStation, entity);
                }
                float step = 360f / maxNumAstro;
                assert latestStation != null;
                newAstropolis.setCircularOrbitPointingDown(targetPlanet, (latestStation.getCircularOrbitAngle() + step) % 360f, orbitRadius, orbitRadius / 10f);
            }
            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());

            newAstropolis.addScript(new BoggledUnderConstructionEveryFrameScript(newAstropolis));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0f, 1.0f);
        }

        @Override
        protected void unapplyEffectImpl(CampaignFleetAPI playerFleet) {

        }
    }

    String id;
    String[] enableSettings;
//    List<AbilityEffect> activateEffects;
//    BoggledProjectRequirementsAND requirements;
    BoggledTerraformingProject.ProjectInstance projectInstance;

    public BoggledBaseAbility() {
    }

    public BoggledBaseAbility(String id, String[] enableSettings) {
        this.id = id;
        this.enableSettings = enableSettings;
    }

    public void init(String id, SectorEntityToken entity) {
        super.init(id, entity);
        Global.getLogger(this.getClass()).info("Doing init for ability " + id);
    }

    @Override
    protected void activateImpl() {
//        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//        for (AbilityEffect activateEffect : activateEffects) {
//            activateEffect.applyEffect(playerFleet);
//        }
    }

    @Override
    protected void applyEffect(float amount, float level) {

    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
//        if (!requirements.requirementsMet(new BoggledTerraformingRequirement.RequirementContext(Global.getSector().getPlayerFleet()))) {
//            return false;
//        }
        return !isOnCooldown() && disableFrames <= 0;
    }
}