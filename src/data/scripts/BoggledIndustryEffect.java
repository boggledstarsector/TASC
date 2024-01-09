package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
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

            String effectSource = Global.getSettings().getCommoditySpec(industry.getAICoreId()).getName() + " (" + industry.getCurrentName() + ")";

            for (IndustryEffect effect : effects) {
                effect.applyEffect(industry, industryInterface, effectSource);
            }
        }

        public void unapplyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            for (Map.Entry<String, List<IndustryEffect>> entry : coreEffects.entrySet()) {
                for (IndustryEffect effect : entry.getValue()) {
                    effect.unapplyEffect(industry, industryInterface);
                }
            }
        }

        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addRightAfterDescriptionSection(industry, mode));
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

        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addPostDemandSection(industry, hasDemand, mode));
            }
            return ret;
        }

        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreId) {
            List<IndustryEffect> effects = coreEffects.get(coreId);
            if (effects == null || !isEnabled()) {
                return new ArrayList<>();
            }

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addAICoreDescription(industry, mode));
            }
            return ret;
        }
    }

    public static abstract class IndustryEffect {
        String id;
        String[] enableSettings;
        List<String> commoditiesDemanded;

        public IndustryEffect(String id, String[] enableSettings, List<String> commoditiesDemanded) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commoditiesDemanded = commoditiesDemanded;
        }

        protected abstract void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource);

        protected abstract void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded);

        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            return new ArrayList<>();
        }

        protected boolean hasPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            return false;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface, String effectSource) {
            if (!isEnabled()) {
                return;
            }
            applyEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]), effectSource);
        }

        public void unapplyEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            unapplyEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addPostDemandSectionImpl(industry, hasDemand, mode, commoditiesDemanded.toArray(new String[0]));
        }

        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addRightAfterDescriptionSectionImpl(industry, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> addImproveDesc(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addImproveDescImpl(industry, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addAICoreDescriptionImpl(industry, mode);
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industryInterface.setFunctional(true);
            if (industry.getMaxDeficit(commoditiesDemanded).two > 0) {
                industryInterface.setFunctional(false);
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {}

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            if (industry.getMaxDeficit(commoditiesDemanded).two <= 0) {
                return new ArrayList<>();
            }
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);
            String text = industry.getCurrentName() + " is inactive due to a shortage of " + shortage;

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getNegativeHighlightColor())), new ArrayList<>(asList(text))));
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            if (industry.isDisrupted()) {
                String text = "Terraforming progress is stalled while the " + industry.getCurrentName() + " is disrupted.";
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
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
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);

            String text = industry.getCurrentName() + " upkeep is increased by " + Strings.X + "%s due to a shortage of " + shortage + ".";
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
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
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (Data datum : data) {
                if (!industry.getMarket().hasCondition(datum.conditionId)) {
                    continue;
                }

                String upkeepMultiplierString = Strings.X + datum.upkeepMultiplier;
                String description = Global.getSettings().getMarketConditionSpec(datum.conditionId).getName();

                String text = industry.getCurrentName() + " upkeep is increased by %s due to condition " + description + " present on " + industry.getMarket().getName() + ".";
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
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
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (Data datum : data) {
                if (!industry.getMarket().hasTag(datum.tag)) {
                    continue;
                }

                String upkeepMultiplierString = Float.toString(datum.upkeepMultiplier);
                String description = datum.description;

                String text = industry.getCurrentName() + " upkeep is increased by %s due to " + industry.getMarket().getName() + " being " + description + ".";
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            if (!industry.isFunctional()) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    return;
                }
                industryTo.getIncome().modifyMult(id, datum.incomeMultiplier, effectSource);
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industry.getMarket().getAccessibilityMod().modifyFlat(id, accessibilityBonus, effectSource);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getAccessibilityMod().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industry.getMarket().getStability().modifyFlat(id, stabilityBonus,effectSource);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getStability().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();

            String stabilityBonusString = String.format("%.0f", stabilityBonus);
            String text = "Increase " + industry.getMarket().getName() + " stability by " + stabilityBonusString + ".";
            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(stabilityBonusString));

            ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));

            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
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
                c.getQuantity().modifyFlat(id, bonus, effectSource);
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            for (MutableCommodityQuantity d : industry.getAllDemand()) {
                d.getQuantity().modifyFlat(id, -demandReduction, effectSource);
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (MutableCommodityQuantity d : industry.getAllDemand()) {
                d.getQuantity().unmodifyFlat(id);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
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
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industry.getUpkeep().modifyMult(id, 1.f - upkeepReduction, effectSource);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String upkeepReductionHighlightString = String.format("%.0f%%", upkeepReduction * 100);
            String upkeepReductionString = upkeepReductionHighlightString + "%";
            String text = "Reduces upkeep by " + upkeepReductionString + ".";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(upkeepReductionHighlightString))));
            return ret;
        }
    }

    public static class EliminatePatherInterest extends IndustryEffect {
        boolean hasPatherInterest = false;
        float prevPatherInterest;
        public EliminatePatherInterest(String id, String[] enableSettings, List<String> commoditiesDemanded) {
            super(id, enableSettings, commoditiesDemanded);
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            if (!hasPatherInterest) {
                // Abundance of caution
                industryInterface.getBasePatherInterest();
                prevPatherInterest = industry.getPatherInterest();
                hasPatherInterest = true;
            }
            if (!industry.isFunctional()) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }
            prevPatherInterest = industry.getPatherInterest();

            float patherInterest = 0f;
            // Now do the calculate pather interest thing
            industryInterface.modifyPatherInterest(id, patherInterest);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industryInterface.unmodifyPatherInterest(id);
        }

        @Override
        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String text = "Pather cells on " + industry.getMarket().getName() + " are eliminated.";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<Color>(), new ArrayList<String>()));
            return ret;
        }
    }

    public static class ConditionToPatherInterest extends IndustryEffect {
        public static class Data {
            String conditionId;
            float patherInterest;
            Data(String conditionId, float patherInterest) {
                this.conditionId = conditionId;
                this.patherInterest = patherInterest;
            }
        }
        List<Data> data;

        public ConditionToPatherInterest(String id, String[] enableSettings, List<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            float accumulatedPatherInterest = 0f;
            for (Data datum : data) {
                if (industry.getMarket().hasCondition(datum.conditionId)) {
                    accumulatedPatherInterest += datum.patherInterest;
                }
            }
            industryInterface.modifyPatherInterest(id, accumulatedPatherInterest);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industryInterface.unmodifyPatherInterest(id);
        }
    }

    public static class IncrementTag extends IndustryEffect {
        String tag;
        int step;
        public IncrementTag(String id, String[] enableSettings, List<String> commoditiesDemanded, String tag, int step) {
            super(id, enableSettings, commoditiesDemanded);
            this.tag = tag;
            this.step = step;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            for (String tag : industry.getMarket().getTags()) {
                if (tag.contains(this.tag)) {
                    int tagValueOld = Integer.parseInt(tag.substring(this.tag.length()));
                    industry.getMarket().removeTag(tag);
                    industry.getMarket().addTag(this.tag + (tagValueOld + step));
                    return;
                }
            }
            industry.getMarket().addTag(this.tag + 1);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {

        }
    }

    public static class RemoveIndustry extends IndustryEffect {
        String industryId;
        public RemoveIndustry(String id, String[] enableSettings, List<String> commoditiesDemanded, String industryId) {
            super(id, enableSettings, commoditiesDemanded);
            this.industryId = industryId;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industry.getMarket().removeIndustry(industryId, null, false);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {

        }
    }

    public static class SuppressConditions extends IndustryEffect {
        List<String> conditionIds;

        public SuppressConditions(String id, String[] enableSettings, List<String> commoditiesDemanded, List<String> conditionIds) {
            super(id, enableSettings, commoditiesDemanded);
            this.conditionIds = conditionIds;
        }


        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE) && industry.getMarket().hasCondition(conditionId)) {
                    // Suppress water surface without actually suppressing it
                    // Actually suppressing it causes aquaculture to produce no food
                    industry.getMarket().getHazard().modifyFlat(id, -0.25f, effectSource);
                } else {
                    industry.getMarket().suppressCondition(conditionId);
                }
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE) && industry.getMarket().hasCondition(conditionId)) {
                    industry.getMarket().getHazard().unmodifyFlat(id);
                } else {
                    industry.getMarket().unsuppressCondition(conditionId);
                }
            }
        }

        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            if (mode == Industry.IndustryTooltipMode.ADD_INDUSTRY || mode == Industry.IndustryTooltipMode.QUEUED) {
                ret.add(new BoggledCommonIndustry.TooltipData("If operational, would counter the effects of:", new ArrayList<Color>(), new ArrayList<String>()));
            } else {
                ret.add(new BoggledCommonIndustry.TooltipData("Countering the effects of:", new ArrayList<Color>(), new ArrayList<String>()));
            }

            int conditionsCountered = 0;
            for (String conditionId : conditionIds) {
                if (!industry.getMarket().hasCondition(conditionId)) {
                    continue;
                }
                String conditionName = Global.getSettings().getMarketConditionSpec(conditionId).getName();
                ret.add(new BoggledCommonIndustry.TooltipData("           " + conditionName, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(conditionName))));
                conditionsCountered++;
            }
            if (conditionsCountered == 0) {
                ret.add(new BoggledCommonIndustry.TooltipData("           (none)", new ArrayList<>(asList(Misc.getGrayColor())), new ArrayList<>(asList("(none"))));
            }
            return ret;
        }
    }

    public static class ImproveGroundDefense extends IndustryEffect {
        float groundDefenseImproveBonus;
        public ImproveGroundDefense(String id, String[] enableSettings, List<String> commoditiesDemanded, float groundDefenseImproveBonus) {
            super(id, enableSettings, commoditiesDemanded);
            this.groundDefenseImproveBonus = groundDefenseImproveBonus;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            industry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, groundDefenseImproveBonus, effectSource);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
        }

        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();

            String text = Strings.X + groundDefenseImproveBonus;
            List<Color> highlights = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlightStrings = new ArrayList<>(asList(text));
            ret.add(new BoggledCommonIndustry.TooltipData("Increases ground defenses by " + text + ".", highlights, highlightStrings));

            return ret;
        }

        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();

            String text = Strings.X + groundDefenseImproveBonus;
            List<Color> highlights = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlightStrings = new ArrayList<>(asList(text));
            if (mode == Industry.ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
                ret.add(new BoggledCommonIndustry.TooltipData("Ground defenses increased by " + text + ".", highlights, highlightStrings));
            } else {
                ret.add(new BoggledCommonIndustry.TooltipData("Increases ground defenses by " + text + ".", highlights, highlightStrings));
            }

            return ret;
        }
    }

    public static class AddCondition extends IndustryEffect {
        String conditionId;
        public AddCondition(String id, String[] enableSettings, List<String> commoditiesDemanded, String conditionId) {
            super(id, enableSettings, commoditiesDemanded);
            this.conditionId = conditionId;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            boggledTools.addCondition(industry.getMarket(), conditionId);
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            boggledTools.removeCondition(industry.getMarket(), conditionId);
        }
    }

    public static class IndustryEffectWithRequirement extends IndustryEffect {
        BoggledProjectRequirementsAND requirements;
        List<IndustryEffect> effects;

        public IndustryEffectWithRequirement(String id, String[] enableSettings, List<String> commoditiesDemanded, BoggledProjectRequirementsAND requirements, List<IndustryEffect> effects) {
            super(id, enableSettings, commoditiesDemanded);
            this.requirements = requirements;
            this.effects = effects;
        }

        @Override
        protected void applyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded, String effectSource) {
            if (!requirements.requirementsMet(industry.getMarket())) {
                unapplyEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }
            for (IndustryEffect effect : effects) {
                effect.applyEffect(industry, industryInterface, effectSource);
            }
        }

        @Override
        protected void unapplyEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (IndustryEffect effect : effects) {
                effect.unapplyEffect(industry, industryInterface);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addPostDemandSectionImpl(industry, hasDemand, mode, commoditiesDemanded));
            }
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addRightAfterDescriptionSectionImpl(industry, mode));
            }
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addImproveDescImpl(BaseIndustry industry, Industry.ImprovementDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addImproveDescImpl(industry, mode));
            }
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addAICoreDescriptionImpl(BaseIndustry industry, Industry.AICoreDescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addAICoreDescriptionImpl(industry, mode));
            }
            return ret;
        }
    }
}
