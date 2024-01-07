package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledCommonIndustry;
import data.campaign.econ.industries.BoggledIndustryInterface;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class BoggledIndustryEffect {
    public static class AICoreEffect {
        private String id;
        private String[] enableSettings;
        private Map<String, List<IndustryEffect>> coreEffects;

        public AICoreEffect(String id, String[] enableSettings, Map<String, List<IndustryEffect>> coreEffects) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.coreEffects = coreEffects;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                unapplyEffect(industry, industryInterface);
            }

            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null) {
                return;
            }
            for (IndustryEffect effect : effects) {
                effect.applyEffect(industry, industryInterface);
            }
        }

        public void unapplyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            for (Map.Entry<String, List<IndustryEffect>> entry : coreEffects.entrySet()) {
                for (IndustryEffect effect : entry.getValue()) {
                    effect.unapplyEffect(industry, industryInterface);
                }
            }
        }

        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addRightAfterDescriptionSection(industryTooltip, industry, mode));
            }
            return ret;
        }

        public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null || !isEnabled()) {
                return false;
            }

            for (IndustryEffect effect : effects ) {
                if (effect.hasPostDemandSection(industry, hasDemand, mode)) {
                    return true;
                }
            }
            return false;
        }

        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addPostDemandSection(industryTooltip, industry, hasDemand, mode));
            }
            return ret;
        }

        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreId) {
            List<IndustryEffect> effects = coreEffects.get(coreId);
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addAICoreDescription(industryTooltip, industry, mode));
            }
            return ret;
        }
    }
//    public static abstract class AICoreEffect {
//        String id;
//        String[] enableSettings;
//
//        public AICoreEffect(String id, String[] enableSettings) {
//            this.id = id;
//            this.enableSettings = enableSettings;
//        }
//
//        protected abstract void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface);
//        protected abstract void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface);
//        protected abstract List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreType, String coreId);
//        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
//            return new ArrayList<>();
//        }
//        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
//            return new ArrayList<>();
//        }
//
//        protected boolean hasPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
//            return false;
//        }
//
//        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }
//
//        public void applyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//            if (!isEnabled()) {
//                unapplyEffectImpl(industry, industryInterface);
//                return;
//            }
//
//            applyEffectImpl(industry, industryInterface);
//        }
//
//        public void unapplyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//            unapplyEffectImpl(industry, industryInterface);
//        }
//
//        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
//            if (!isEnabled()) {
//                return new ArrayList<>();
//            }
//
//            return addAICoreDescriptionImpl(industryTooltip, industry, mode, coreType, coreId);
//        }
//

//

//
//    public static class SupplyBonusWithDeficitToIndustry extends AICoreEffect {
//        public static class Data {
//            String industryId;
//            String description;
//            int flatBonus;
//            public Data(String industryId, String description, int flatBonus) {
//                this.industryId = industryId;
//                this.description = description;
//                this.flatBonus = flatBonus;
//            }
//        }
//
//        List<Data> data;
//        Map<String, Integer> aiCoreBonus;
//        List<String> commoditiesDemanded;
//
//        String aiCoreDescription;
//        String afterDescriptionSection;
//        String afterDescriptionSectionShortage;
//
//        public SupplyBonusWithDeficitToIndustry(String id, String[] enableSettings, List<Data> data, Map<String, Integer> aiCoreBonus, List<String> commoditiesDemanded, String aiCoreDescription, String afterDescriptionSection, String afterDescriptionSectionShortage) {
//            super(id, enableSettings);
//            this.data = data;
//            this.aiCoreBonus = aiCoreBonus;
//            this.commoditiesDemanded = commoditiesDemanded;
//            this.aiCoreDescription = aiCoreDescription;
//            this.afterDescriptionSection = afterDescriptionSection;
//            this.afterDescriptionSectionShortage = afterDescriptionSectionShortage;
//        }
//
//        @Override
//        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//            if (!industry.isFunctional()) {
//                unapplyEffectImpl(industry, industryInterface);
//                return;
//            }
//
//            int aiCoreBonus = getBonusAmountWithDeficit(industry, industry.getAICoreId());
//
//            for (Data datum : data) {
//                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
//                if (industryTo == null) {
//                    continue;
//                }
//
//                int bonus = aiCoreBonus + datum.flatBonus;
//                for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
//                    industryTo.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, bonus, datum.description);
//                }
//            }
//        }
//
//        @Override
//        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//            for (Data datum : data) {
//                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
//                if (industryTo == null) {
//                    continue;
//                }
//
//                for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
//                    industryTo.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
//                }
//            }
//        }
//
//        @Override
//        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String prefix, String coreId) {
//
//            Map<String, String> tokenReplacements = getTokenReplacements(industry, coreId);
//            String replaced = boggledTools.doTokenReplacement(aiCoreDescription, tokenReplacements);
//            String highlights = boggledTools.doTokenReplacement("$aiCoreBonusWithoutDeficit", tokenReplacements);
//
//            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
//            ret.add(new BoggledCommonIndustry.TooltipData(replaced, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(highlights))));
//            return ret;
//        }
//
//        @Override
//        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
//
//            Map<String, String> tokenReplacements = getTokenReplacements(industry, industry.getAICoreId());
//            String replaced = boggledTools.doTokenReplacement(afterDescriptionSection, tokenReplacements);
//            String highlights = boggledTools.doTokenReplacement("$aiCoreBonusWithDeficit", tokenReplacements);
//
//            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
//            ret.add(new BoggledCommonIndustry.TooltipData(replaced, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(highlights))));
//
//            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded.toArray(new String[0]));
//            if (deficit.two > 0) {
//                String replaced2 = boggledTools.doTokenReplacement(afterDescriptionSectionShortage, tokenReplacements);
//                String highlights2 = boggledTools.doTokenReplacement("$commodityShortageAmount", tokenReplacements);
//                ret.add(new BoggledCommonIndustry.TooltipData(replaced2, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(highlights2))));
//            }
//            return ret;
//        }
//
//        private int getBonusAmount(String aiCoreId) {
//            Integer bonus = aiCoreBonus.get(aiCoreId);
//            if (bonus == null) {
//                bonus = 0;
//            }
//            return bonus;
//        }
//
//        private int getBonusAmountWithDeficit(BaseIndustry industry, String aiCoreId) {
//            int bonus = getBonusAmount(aiCoreId);
//            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded.toArray(new String[0]));
//            if (deficit.two > 0) {
//                bonus -= deficit.two;
//            }
//            return Math.max(bonus, 0);
//        }
//
//        private Map<String, String> getTokenReplacements(BaseIndustry industry, String aiCoreId) {
//            Map<String, String> ret = boggledTools.getTokenReplacements(industry.getMarket());
//            int aiCoreBonusWithoutDeficit = getBonusAmount(aiCoreId);
//            int aiCoreBonusWithDeficit = getBonusAmountWithDeficit(industry, aiCoreId);
//            ret.put("$aiCoreBonusWithoutDeficit", Integer.toString(aiCoreBonusWithoutDeficit));
//            ret.put("$aiCoreBonusWithDeficit", Integer.toString(aiCoreBonusWithDeficit));
//            ret.put("$unitOrUnits", aiCoreBonusWithoutDeficit != 1 ? "units" : "unit");
//            List<String> industries = new ArrayList<>();
//            for (Data datum : data) {
//                industries.add(Global.getSettings().getIndustrySpec(datum.industryId).getName().toLowerCase());
//            }
//            ret.put("$industryTo", Misc.getAndJoined(industries.toArray(new String[0])));
//
//            List<Pair<String, Integer>> deficits = industry.getAllDeficit(commoditiesDemanded.toArray(new String[0]));
//            if (!deficits.isEmpty()) {
//                List<String> deficitStrings = new ArrayList<>();
//                int maxDeficit = 0;
//                for (Pair<String, Integer> deficit : deficits) {
//                    deficitStrings.add(deficit.one);
//                    maxDeficit = Math.max(maxDeficit, deficit.two);
//                }
//                String deficit = Misc.getAndJoined(deficitStrings);
//                ret.put("$commodityShortageName", deficit);
//                ret.put("$commodityShortageAmount", Integer.toString(maxDeficit));
//            }
//
//            return ret;
//        }
//    }
//
//    public static class EliminatePatherInterest extends AICoreEffect {
//        EliminatePatherInterest(String id, String[] enableSettings) {
//            super(id, enableSettings);
//        }
//
//        @Override
//        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
//            String text = "Pather cells on " + industry.getMarket().getName() + " are eliminated.";
//            List<Color> highlightColors = new ArrayList<>();
//            List<String> highlights = new ArrayList<>();
//            return new ArrayList<>(asList(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights)));
//        }
//    }
//
//    public static class ReduceDemand extends AICoreEffect {
//        ReduceDemand(String id, String[] enableSettings) {
//            super(id, enableSettings);
//        }
//
//        @Override
//        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
//            return new ArrayList<>();
//        }
//    }
//
//    public static class ReduceUpkeep extends AICoreEffect {
//        ReduceUpkeep(String id, String[] enableSettings) {
//            super(id, enableSettings);
//        }
//
//        @Override
//        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
//
//        }
//
//        @Override
//        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
//            return new ArrayList<>();
//        }
//    }

    public static abstract class IndustryEffect {
        String id;
        String[] enableSettings;
        List<String> commoditiesDemanded;

        public IndustryEffect(String id, String[] enableSettings, List<String> commoditiesDemanded) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commoditiesDemanded = commoditiesDemanded;
        }

        protected abstract void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded);

        protected abstract void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded);

        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(String industryTooltip, BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            return new ArrayList<>();
        }

        protected boolean hasPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            return false;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            applyEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public void unapplyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            unapplyEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addPostDemandSectionImpl(industryTooltip, industry, hasDemand, mode, commoditiesDemanded.toArray(new String[0]));
        }

        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addRightAfterDescriptionSectionImpl(industryTooltip, industry, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> addImproveDesc(String industryTooltip, BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addImproveDescImpl(industryTooltip, industry, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addAICoreDescriptionImpl(industryTooltip, industry, mode);
        }

        public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return false;
            }
            return hasPostDemandSectionImpl(industry, hasDemand, mode);
        }
    }

    public static class DeficitToInactive extends IndustryEffect {
        public DeficitToInactive(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded) {
            super(id, enableSettings, commoditiesDemanded);
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industryInterface.setFunctional(true);
            if (industry.getMaxDeficit(commoditiesDemanded).two > 0) {
                industryInterface.setFunctional(false);
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {}

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            if (industry.getMaxDeficit(commoditiesDemanded).two <= 0) {
                return new ArrayList<>();
            }
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);
            String text = industryTooltip + " is inactive due to a shortage of " + shortage;

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getNegativeHighlightColor())), new ArrayList<>(asList(text))));
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(String industryTooltip, BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            if (industry.isDisrupted()) {
                String text = "Terraforming progress is stalled while the " + industryTooltip + " is disrupted.";
                List<Color> highlightColors = new ArrayList<>(asList(Misc.getNegativeHighlightColor()));
                List<String> highlights = new ArrayList<>(asList(text));
                return new ArrayList<>(asList(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights)));
            }
            return new ArrayList<>();
        }
    }

    public static class DeficitToCommodity extends IndustryEffect {
        ArrayList<String> commoditiesDeficited;

        public DeficitToCommodity(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, ArrayList<String> commoditiesDeficited) {
            super(id, enableSettings, commoditiesDemanded);
            this.commoditiesDeficited = commoditiesDeficited;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded);
            industryInterface.applyDeficitToProduction(id + "_DeficitToCommodity", deficit, commoditiesDeficited.toArray(new String[0]));
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {}
    }

    public static class DeficitMultiplierToUpkeep extends IndustryEffect {
        float upkeepMultiplier;

        public DeficitMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, float upkeepMultiplier) {
            super(id, enableSettings, commoditiesDemanded);
            this.upkeepMultiplier = upkeepMultiplier;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            List<Pair<String, Integer>> deficits = industry.getAllDeficit(commoditiesDemanded);
            if (deficits.isEmpty()) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
            } else {
                industry.getUpkeep().modifyMult(id + "_DeficitMultiplierToUpkeep", upkeepMultiplier, "Commodity shortage");
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().unmodifyMult(id + "_DeficitMultiplierToUpkeep");
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);

            String text = industryTooltip + " upkeep is increased by %s due to a shortage of " + shortage + ".";
            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(Float.toString(upkeepMultiplier)));

            return new ArrayList<>(asList(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights)));
        }
    }

    public static class ConditionMultiplierToUpkeep extends IndustryEffect {
        public static class Data {
            String conditionId;
            float upkeepMultiplier;
            public Data(String conditionId, float upkeepMultiplier) {
                this.conditionId = conditionId;
                this.upkeepMultiplier = upkeepMultiplier;
            }
        }
        List<Data> data;

        public ConditionMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                if (industry.getMarket().hasCondition(datum.conditionId)) {
                    industry.getUpkeep().modifyMult(id + "_ConditionMultiplierToUpkeep", datum.upkeepMultiplier, Global.getSettings().getMarketConditionSpec(datum.conditionId).getName());
                    return;
                }
            }
            unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().unmodifyMult(id + "_ConditionMultiplierToUpkeep");
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (Data datum : data) {
                if (!industry.getMarket().hasCondition(datum.conditionId)) {
                    continue;
                }

                String upkeepMultiplierString = "x" + datum.upkeepMultiplier;
                String description = Global.getSettings().getMarketConditionSpec(datum.conditionId).getName();

                String text = industryTooltip + " upkeep is increased by %s due to condition " + description + " present on " + industry.getMarket().getName() + ".";
                List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
                List<String> highlights = new ArrayList<>(asList(upkeepMultiplierString));
                ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));
            }
            return ret;
        }
    }

    public static class TagMultiplierToUpkeep extends IndustryEffect {
        public static class Data {
            String tag;
            String description;
            float upkeepMultiplier;

            public Data(String tag, String description, float upkeepMultiplier) {
                this.tag = tag;
                this.description = description;
                this.upkeepMultiplier = upkeepMultiplier;
            }
        }
        List<Data> data;

        public TagMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                if (industry.getMarket().hasTag(datum.tag)) {
                    industry.getUpkeep().modifyMult(id + "_TagMultiplierToUpkeep", datum.upkeepMultiplier, datum.description);
                    return;
                }
            }
            unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().unmodifyMult(id + "_TagMultiplierToUpkeep");
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (Data datum : data) {
                if (!industry.getMarket().hasTag(datum.tag)) {
                    continue;
                }

                String upkeepMultiplierString = Float.toString(datum.upkeepMultiplier);
                String description = datum.description;

                String text = industryTooltip + " upkeep is increased by %s due to " + industry.getMarket().getName() + " being " + description + ".";
                List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
                List<String> highlights = new ArrayList<>(asList(upkeepMultiplierString));

                ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));
            }
            return ret;
        }
    }

    public static class IncomeBonusToIndustry extends IndustryEffect {
        public static class Data {
            String industryId;
            float incomeMultiplier;
            public Data(String industryId, float incomeMultiplier) {
                this.industryId = industryId;
                this.incomeMultiplier = incomeMultiplier;
            }
        }
        List<Data> data;

        public IncomeBonusToIndustry(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            if (!industry.isFunctional()) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    return;
                }
                industryTo.getIncome().modifyMult(id, datum.incomeMultiplier, industry.getCurrentName());
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    return;
                }
                industryTo.getIncome().unmodifyMult(id);
            }
        }
    }

    public static class BonusToAccessibility extends IndustryEffect {
        private final float accessibilityBonus;
        public BonusToAccessibility(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, float accessibilityBonus) {
            super(id, enableSettings, commoditiesDemanded);
            this.accessibilityBonus = accessibilityBonus;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getAccessibilityMod().modifyFlat(id, accessibilityBonus, industry.getCurrentName());
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getAccessibilityMod().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(String industryTooltip, BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            String text;
            String accessibilityHighlightString = String.format("%.0f", accessibilityBonus * 100) + "%";
            String accessibilityBonusString = accessibilityHighlightString + "%";
            if (mode == Industry.ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
                text = industry.getMarket().getName() + " accessibility increased by " + accessibilityBonusString + ".";
            } else {
                text = "Increases " + industry.getMarket().getName() + " accessibility by " + accessibilityBonusString + ".";
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();

            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(accessibilityHighlightString));

            ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));

            return ret;
        }
    }

    public static class BonusToStability extends IndustryEffect {
        private final float stabilityBonus;

        public BonusToStability(String id, String[] enableSettings, List<String> commoditiesDemanded, float stabilityBonus) {
            super(id, enableSettings, commoditiesDemanded);
            this.stabilityBonus = stabilityBonus;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getStability().modifyFlat(id, stabilityBonus,industry.getCurrentName());
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getStability().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(String industryTooltip, BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            String text;
            String stabilityBonusString = String.format("%.0f", stabilityBonus);

            if (mode == Industry.ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
                text = industry.getMarket().getName() + " stability increased by " + stabilityBonusString + ".";
            } else {
                text = "Increases " + industry.getMarket().getName() + " stability by " + stabilityBonusString + ".";
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();

            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(stabilityBonusString));

            ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));

            return ret;
        }
    }

    public static class SupplyBonusToIndustryWithDeficit extends IndustryEffect {
        String industryId;
        int supplyBonus;
        public SupplyBonusToIndustryWithDeficit(String id, String[] enableSettings, List<String> commoditiesDemanded, String industryId, int supplyBonus) {
            super(id, enableSettings, commoditiesDemanded);
            this.industryId = industryId;
            this.supplyBonus = supplyBonus;
        }

        private int getBonusAmountWithDeficit(BaseIndustry industry, String[] commoditiesDemanded) {
            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded);
            return Math.max(0, supplyBonus - deficit.two);
        }
        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            if (!industry.isFunctional()) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }

            Industry industryTo = industry.getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }
            int bonus = getBonusAmountWithDeficit(industry, commoditiesDemanded);
            for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                c.getQuantity().modifyFlat(id, bonus, industry.getCurrentName());
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            Industry industryTo = industry.getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }

            for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                c.getQuantity().unmodifyFlat(id);
            }
        }
    }

    public static class ReduceAllDemand extends IndustryEffect {
        int demandReduction;

        public ReduceAllDemand(String id, String[] enableSettings, List<String> commoditiesDemanded, int demandReduction) {
            super(id, enableSettings, commoditiesDemanded);
            this.demandReduction = demandReduction;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (MutableCommodityQuantity d : industry.getAllDemand()) {
                d.getQuantity().modifyFlat(id, -demandReduction, industry.getCurrentName());
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (MutableCommodityQuantity d : industry.getAllDemand()) {
                d.getQuantity().unmodifyFlat(id);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String demandReductionHighlightString = String.format("%d", demandReduction);
            String demandReductionString = demandReductionHighlightString;
            String text = "Reduces demand by " + demandReductionString + ".";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(demandReductionHighlightString))));
            return ret;
        }
    }

    public static class ReduceUpkeep extends IndustryEffect {
        float upkeepReduction;

        public ReduceUpkeep(String id, String[] enableSettings, List<String> commoditiesDemanded, float upkeepReduction) {
            super(id, enableSettings, commoditiesDemanded);
            this.upkeepReduction = upkeepReduction;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().modifyMult(id, 1.f - upkeepReduction, industry.getCurrentName());
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String upkeepReductionHighlightString = String.format("%.0f%%", upkeepReduction * 100);
            String upkeepReductionString = upkeepReductionHighlightString + "%";
            String text = "Reduces upkeep by " + upkeepReductionString + ".";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(upkeepReductionHighlightString))));
            return ret;
        }
    }

    public static class EliminatePatherInterest extends IndustryEffect {
        public EliminatePatherInterest(String id, String[] enableSettings, List<String> commoditiesDemanded) {
            super(id, enableSettings, commoditiesDemanded);
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {

        }

        @Override
        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(String industryTooltip, BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String text = "Pather cells on " + industry.getMarket().getName() + " are eliminated.";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<Color>(), new ArrayList<String>()));
            return ret;
        }
    }
}
