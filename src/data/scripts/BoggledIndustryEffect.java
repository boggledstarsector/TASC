package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledCommonIndustry;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.util.Arrays.asList;

public class BoggledIndustryEffect {
//    public static class AICoreEffect {
//        private String id;
//        private String[] enableSettings;
//        private Map<String, List<IndustryEffect>> coreEffects;
//
//        public AICoreEffect(String id, String[] enableSettings, Map<String, List<IndustryEffect>> coreEffects) {
//            this.id = id;
//            this.enableSettings = enableSettings;
//            this.coreEffects = coreEffects;
//        }
//
//        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }
//
//        public void applyEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
//            if (!isEnabled()) {
//                unapplyEffect(ctx);
//            }
//
//            List<IndustryEffect> effects = coreEffects.get(ctx.getIndustry().getAICoreId());
//            if (effects == null) {
//                return;
//            }
//
//            String effectSource = Global.getSettings().getCommoditySpec(ctx.getIndustry().getAICoreId()).getName() + " (" + ctx.getIndustry().getCurrentName() + ")";
//
//            for (IndustryEffect effect : effects) {
//                effect.applyEffect(ctx, effectSource);
//            }
//        }
//
//        public void unapplyEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
//            for (Map.Entry<String, List<IndustryEffect>> entry : coreEffects.entrySet()) {
//                for (IndustryEffect effect : entry.getValue()) {
//                    effect.unapplyEffect(ctx);
//                }
//            }
//        }
//
//        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
//            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
//            if (effects == null || !isEnabled()) {
//                return new ArrayList<>();
//            }
//
//            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
//            for (IndustryEffect effect : effects) {
//                ret.addAll(effect.addRightAfterDescriptionSection(industry, mode));
//            }
//            return ret;
//        }
//
//        public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
//            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
//            if (effects == null || !isEnabled()) {
//                return false;
//            }
//
//            for (IndustryEffect effect : effects ) {
//                if (effect.hasPostDemandSection(industry, hasDemand, mode)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
//            List<IndustryEffect> effects = coreEffects.get(industry.getAICoreId());
//            if (effects == null || !isEnabled()) {
//                return new ArrayList<>();
//            }
//
//            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
//            for (IndustryEffect effect : effects) {
//                List<BoggledCommonIndustry.TooltipData> add = effect.addPostDemandSection(industry, hasDemand, mode);
//                if (add != null) {
//                    ret.addAll(add);
//                }
//            }
//            return ret;
//        }
//
//        public List<BoggledCommonIndustry.TooltipData> addAICoreDescription(BaseIndustry industry, Industry.AICoreDescriptionMode mode, String coreId) {
//            List<IndustryEffect> effects = coreEffects.get(coreId);
//            if (effects == null || !isEnabled()) {
//                return new ArrayList<>();
//            }
//
//            IndustryEffect.DescriptionMode descMode;
//            if (mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
//                descMode = IndustryEffect.DescriptionMode.APPLIED;
//            } else {
//                descMode = IndustryEffect.DescriptionMode.TO_APPLY;
//            }
//
//            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
//            for (IndustryEffect effect : effects) {
//                List<BoggledCommonIndustry.TooltipData> add = effect.getApplyOrAppliedDesc(industry, descMode);
//                if (add != null) {
//                    ret.addAll(add);
//                }
//            }
//            return ret;
//        }
//    }

    public static abstract class IndustryEffect {
        public enum DescriptionMode {
            TO_APPLY,
            APPLIED
        }

        String id;
        String[] enableSettings;

        public IndustryEffect(String id, String[] enableSettings) {
            this.id = id;
            this.enableSettings = enableSettings;
        }

        protected abstract void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource);

        protected abstract void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx);

        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            return new ArrayList<>();
        }

        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            return new ArrayList<>();
        }

        protected boolean hasPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            return false;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyEffect(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!isEnabled()) {
                return;
            }
            applyEffectImpl(ctx, effectSource);
        }

        public void unapplyEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!isEnabled()) {
                return;
            }
            unapplyEffectImpl(ctx);
        }

        public List<BoggledCommonIndustry.TooltipData> addPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addPostDemandSectionImpl(industry, hasDemand, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSection(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }

            return addRightAfterDescriptionSectionImpl(industry, mode);
        }

        public List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDesc(BaseIndustry industry, DescriptionMode mode) {
            if (!isEnabled()) {
                return new ArrayList<>();
            }
            return getApplyOrAppliedDescImpl(industry, mode);
        }

        public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return false;
            }
            return hasPostDemandSectionImpl(industry, hasDemand, mode);
        }
    }

    public static class DeficitToInactive extends IndustryEffect {
        List<String> commoditiesDemanded;
        public DeficitToInactive(String id, String[] enableSettings, List<String> commoditiesDemanded) {
            super(id, enableSettings);
            this.commoditiesDemanded = commoditiesDemanded;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getIndustryInterface().setFunctional(true);
            if (ctx.getIndustry().getMaxDeficit(commoditiesDemanded.toArray(new String[0])).two > 0) {
                ctx.getIndustryInterface().setFunctional(false);
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {}

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            String[] commDem = commoditiesDemanded.toArray(new String[0]);
            if (industry.getMaxDeficit(commDem).two <= 0) {
                return new ArrayList<>();
            }
            String shortage = boggledTools.buildCommodityList(industry, commDem);
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
        List<String> commoditiesDemanded;
        List<String> commoditiesDeficited;

        public DeficitToCommodity(String id, String[] enableSettings, List<String> commoditiesDemanded, List<String> commoditiesDeficited) {
            super(id, enableSettings);
            this.commoditiesDemanded = commoditiesDemanded;
            this.commoditiesDeficited = commoditiesDeficited;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            Pair<String, Integer> deficit = ctx.getIndustry().getMaxDeficit(commoditiesDemanded.toArray(new String[0]));
            ctx.getIndustryInterface().applyDeficitToProduction(id + "_DeficitToCommodity", deficit, commoditiesDeficited.toArray(new String[0]));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {}
    }

    public static class DeficitMultiplierToUpkeep extends IndustryEffect {
        List<String> commoditiesDemanded;
        float upkeepMultiplier;

        public DeficitMultiplierToUpkeep(String id, String[] enableSettings, List<String> commoditiesDemanded, float upkeepMultiplier) {
            super(id, enableSettings);
            this.commoditiesDemanded = commoditiesDemanded;
            this.upkeepMultiplier = upkeepMultiplier;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            List<Pair<String, Integer>> deficits = ctx.getIndustry().getAllDeficit(commoditiesDemanded.toArray(new String[0]));
            if (deficits.isEmpty()) {
                unapplyEffectImpl(ctx);
            } else {
                ctx.getIndustry().getUpkeep().modifyMult(id + "_DeficitMultiplierToUpkeep", upkeepMultiplier, "Commodity shortage");
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustry().getUpkeep().unmodifyMult(id + "_DeficitMultiplierToUpkeep");
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded.toArray(new String[0]));
            if (shortage.isEmpty()) {
                return new ArrayList<>();
            }

            String upkeepHighlight = Strings.X + String.format("%.0f", upkeepMultiplier);
            String text = industry.getCurrentName() + " upkeep is increased by " + upkeepHighlight + " due to a shortage of " + shortage + ".";
            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(upkeepHighlight));

            return new ArrayList<>(asList(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights)));
        }
    }

    public static class EffectToIndustry extends IndustryEffect {
        String industryId;
        IndustryEffect effect;

        public EffectToIndustry(String id, String[] enableSettings, String industryId, IndustryEffect effect) {
            super(id, enableSettings);
            this.industryId = industryId;
            this.effect = effect;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            Industry industryTo = ctx.getIndustry().getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }
            if (ctx.getIndustryInterface() != null) {
                Global.getLogger(this.getClass()).warn("IndustryEffect '" + id + "' is applying '" + effect.id + "' to non-BoggledIndustryInterface industry '" + industryTo.getId() + "', this may crash the game");
            }
            effect.applyEffect(ctx, effectSource);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            Industry industryTo = ctx.getIndustry().getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }
            if (ctx.getIndustryInterface() == null) {
                Global.getLogger(this.getClass()).warn("IndustryEffect '" + id + "' is applying '" + effect.id + "' to non-BoggledIndustryInterface industry '" + industryTo.getId() + "', this may crash the game");
            }
            effect.unapplyEffect(ctx);
        }
    }

    private static abstract class Modifier extends IndustryEffect {
        private enum StatModType {
            FLAT,
            MULT,
            PERCENT,
            MARKET_SIZE
        }
        private StatModType modType;
        private final float value;

        protected Modifier(String id, String[] enableSettings, String typeString, float value) {
            super(id, enableSettings);
            switch (typeString) {
                case "flat": this.modType = StatModType.FLAT; break;
                case "mult": this.modType = StatModType.MULT; break;
                case "percent": this.modType = StatModType.PERCENT; break;
                case "market_size": this.modType = StatModType.MARKET_SIZE; break;
            }
            if (modType == null) {
                Global.getLogger(this.getClass()).error("Industry effect " + id + " has invalid mod type string " + typeString);
            }
            this.value = value;
        }

        protected MutableStat createModifier(MarketAPI market, String effectSource) {
            MutableStat mod = new MutableStat(0);
            switch (modType) {
                case MARKET_SIZE: mod.modifyFlat(id, market.getSize() + value, effectSource); break;
                case FLAT: mod.modifyFlat(id, value, effectSource); break;
                case MULT: mod.modifyMult(id, value, effectSource); break;
                case PERCENT: mod.modifyPercent(id, value, effectSource); break;
            }
            return mod;
        }

        protected BoggledCommonIndustry.TooltipData createTooltipData(MarketAPI market, String effect, String suffix, DescriptionMode mode) {
            ModifierStrings modStrings = new ModifierStrings(market, modType, value);
            String text;
            // Result should be 'Reduces demand by 1 unit'
            // increasesOrReduces supplyDemand by value suffix
            if (mode == DescriptionMode.APPLIED) {
                text = Misc.ucFirst(effect) + " " + modStrings.increasedOrReduced + " by " + modStrings.bonusString;
            } else {
                text = modStrings.IncreasesOrReduces + " " + Misc.lcFirst(effect) + " by " + modStrings.bonusString;
            }

            if (!suffix.isEmpty()) {
                text += " " + suffix;
            }
            text += ".";

            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(modStrings.highlightString));

            return new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights);
        }

        protected BoggledCommonIndustry.TooltipData createPostDemandSection(MarketAPI market, String effect) {
            ModifierStrings modStrings = new ModifierStrings(market, modType, value);

            String text = modStrings.bonusString + " " + effect + " " + modStrings.suffix;
            List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
            List<String> highlights = new ArrayList<>(asList(modStrings.highlightString));

            return new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights);
        }

        public static class ModifierStrings {
            String increasesOrReduces;
            String IncreasesOrReduces;
            String increasedOrReduced;
            String IncreasedOrReduced;

            String bonusString;
            String highlightString;

            String suffix;

            private void setToIncrease() {
                this.increasesOrReduces = "increases";
                this.IncreasesOrReduces = "Increases";
                this.increasedOrReduced = "increased";
                this.IncreasedOrReduced = "Increased";
            }

            private void setToReduce() {
                this.increasesOrReduces = "reduces";
                this.IncreasesOrReduces = "Reduces";
                this.increasedOrReduced = "reduced";
                this.IncreasedOrReduced = "Reduced";
            }

            private String formatBonusString(float value) {
//                if (value % 1.0 != 0) {
//                    return String.format("%s", value);
//                }
                return String.format("%.0f", value);
            }

            ModifierStrings(MarketAPI market, StatModType modType, float baseValue) {
                suffix = "";
                float value = baseValue;
                switch (modType) {
                    case MARKET_SIZE:
                        suffix = "(based on colony size)";
                        value += market.getSize();
                    case FLAT: {
                        if (value < 0) {
                            setToReduce();
                            bonusString = formatBonusString(Math.abs(value));
                        } else {
                            setToIncrease();
                            bonusString = formatBonusString(value);
                        }
                        highlightString = bonusString;
                        break;
                    }
                    case MULT: {
                        if (value < 1) {
                            setToReduce();
                            highlightString = formatBonusString((1 - value) * 100) + "%";
                        } else {
                            setToIncrease();
                            highlightString = formatBonusString((value - 1) * 100) + "%";
                        }
                        bonusString = highlightString + "%";
                        break;
                    }
                    case PERCENT: {
                        if (value < 100) {
                            setToReduce();
                            highlightString = formatBonusString(100 - value) + "%";
                        } else {
                            setToIncrease();
                            highlightString = formatBonusString(value - 100) + "%";
                        }
                        bonusString = highlightString + "%";
                        break;
                    }
                }
            }
        }
    }

    public static class ModifyIncome extends Modifier {
        public ModifyIncome(String id, String[] enableSettings, String typeString, float value) {
            super(id, enableSettings, typeString, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getIndustry().getIncome().applyMods(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustry().getIncome().unmodify(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "Income", "", mode));
            return ret;
        }
    }

    public static class ModifyAccessibility extends Modifier {
        public ModifyAccessibility(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getMarket().getAccessibilityMod().applyMods(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().getAccessibilityMod().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "accessibility", "", mode));
            return ret;
        }
    }

    public static class ModifyStability extends Modifier {
        public ModifyStability(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getMarket().getStability().applyMods(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().getStability().unmodifyFlat(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "stability", "", mode));
            return ret;
        }
    }

    public static class SupplyBonusToIndustryWithDeficit extends IndustryEffect {
        List<String> commoditiesDemanded;
        String industryId;
        int supplyBonus;
        public SupplyBonusToIndustryWithDeficit(String id, String[] enableSettings, List<String> commoditiesDemanded, String industryId, int supplyBonus) {
            super(id, enableSettings);
            this.commoditiesDemanded = commoditiesDemanded;
            this.industryId = industryId;
            this.supplyBonus = supplyBonus;
        }

        private int getBonusAmountWithDeficit(BaseIndustry industry, String[] commoditiesDemanded) {
            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded);
            return Math.max(0, supplyBonus - deficit.two);
        }
        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!ctx.getIndustry().isFunctional()) {
                unapplyEffectImpl(ctx);
                return;
            }

            Industry industryTo = ctx.getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }
            int bonus = getBonusAmountWithDeficit(ctx.getIndustry(), commoditiesDemanded.toArray(new String[0]));
            for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                c.getQuantity().modifyFlat(id, bonus, effectSource);
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            Industry industryTo = ctx.getMarket().getIndustry(industryId);
            if (industryTo == null) {
                return;
            }

            for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                c.getQuantity().unmodifyFlat(id);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            String industryToName = Global.getSettings().getIndustrySpec(industryId).getName();

            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String demandReductionHighlightString = String.format("%d", Math.abs(supplyBonus));
            String demandReductionString = demandReductionHighlightString;
            String increasesOrReduces;
            String increasedOrReduced;
            if (supplyBonus < 0) {
                increasesOrReduces = "Reduces";
                increasedOrReduced = "reduced";
            } else {
                increasesOrReduces = "Increases";
                increasedOrReduced = "increased";
            }

            String text;
            if (mode == DescriptionMode.APPLIED) {
                text = industryToName + " supply " + increasedOrReduced + " by " + demandReductionString + ".";
            } else {
                text = increasesOrReduces + " " + industryToName + " supply by " + demandReductionString + ".";

            }

            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(demandReductionHighlightString))));
            return ret;
        }
    }

    public static class ModifyAllDemand extends Modifier {
        public ModifyAllDemand(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (MutableCommodityQuantity d : ctx.getIndustry().getAllDemand()) {
                d.getQuantity().applyMods(createModifier(ctx.getMarket(), effectSource));
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (MutableCommodityQuantity d : ctx.getIndustry().getAllDemand()) {
                d.getQuantity().unmodify(id);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "Demand", "unit", mode));
            return ret;
        }
    }

    public static class ModifyUpkeep extends Modifier {
        public ModifyUpkeep(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getIndustry().getUpkeep().applyMods(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustry().getUpkeep().unmodify(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "Upkeep cost", "", mode));
            return ret;
        }
    }

    public static class EliminatePatherInterest extends IndustryEffect {
        public EliminatePatherInterest(String id, String[] enableSettings) {
            super(id, enableSettings);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!ctx.getIndustry().isFunctional()) {
                unapplyEffectImpl(ctx);
                return;
            }

            // Now do the calculate pather interest thing
            // It's simpler than you might think
            // Start with basePatherInterest and don't add this industry's pather interest later
            // Otherwise chattering
            float patherInterest = ctx.getIndustryInterface().getBasePatherInterest();

            if (ctx.getMarket().getAdmin().getAICoreId() != null) {
                patherInterest += 10;
            }

            for (Industry otherIndustry : ctx.getMarket().getIndustries()) {
                if (otherIndustry.isHidden()) {
                    continue;
                }
                if (otherIndustry.getId().equals(ctx.getIndustry().getId())) {
                    continue;
                }
                patherInterest += otherIndustry.getPatherInterest();
            }

            MutableStat modifier = new MutableStat(0f);
            modifier.modifyFlat(id, -patherInterest, effectSource);
            ctx.getIndustryInterface().modifyPatherInterest(modifier);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustryInterface().unmodifyPatherInterest(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String text = "Pather cells on " + industry.getMarket().getName() + " are eliminated.";
            ret.add(new BoggledCommonIndustry.TooltipData(text, new ArrayList<>(asList(Misc.getHighlightColor())), new ArrayList<>(asList(industry.getMarket().getName()))));
            return ret;
        }
    }

    public static class ModifyPatherInterest extends Modifier {
        public ModifyPatherInterest(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getIndustryInterface().modifyPatherInterest(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustryInterface().unmodifyPatherInterest(id);
        }
    }

    public static class ModifyColonyGrowthRate extends Modifier {
        public ModifyColonyGrowthRate(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getIndustryInterface().modifyImmigration(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustryInterface().unmodifyImmigration(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "Colony growth rate", "", mode));
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createPostDemandSection(industry.getMarket(), "population growth"));
            return ret;
        }
    }

    public static class IncrementTag extends IndustryEffect {
        String tag;
        int step;
        public IncrementTag(String id, String[] enableSettings, String tag, int step) {
            super(id, enableSettings);
            this.tag = tag;
            this.step = step;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (String tag : ctx.getMarket().getTags()) {
                if (tag.contains(this.tag)) {
                    int tagValueOld = Integer.parseInt(tag.substring(this.tag.length()));
                    ctx.getMarket().removeTag(tag);
                    ctx.getMarket().addTag(this.tag + (tagValueOld + step));
                    return;
                }
            }
            ctx.getMarket().addTag(this.tag + 1);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class RemoveIndustry extends IndustryEffect {
        String industryId;
        public RemoveIndustry(String id, String[] enableSettings, String industryId) {
            super(id, enableSettings);
            this.industryId = industryId;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            CargoAPI cargo = ctx.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
            if (cargo != null) {
                if (ctx.getIndustry().getAICoreId() != null) {
                    cargo.addCommodity(ctx.getIndustry().getAICoreId(), 1);
                }

                SpecialItemData specialItem = ctx.getIndustry().getSpecialItem();
                if (specialItem != null) {
                    cargo.addSpecial(specialItem, 1);
                }

                for (InstallableIndustryItemPlugin installableItem : ctx.getIndustry().getInstallableItems()) {
                    cargo.addSpecial(installableItem.getCurrentlyInstalledItemData(), 1);
                }
            }
            ctx.getMarket().removeIndustry(industryId, null, false);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class SuppressConditions extends IndustryEffect {
        List<String> conditionIds;

        public SuppressConditions(String id, String[] enableSettings, List<String> conditionIds) {
            super(id, enableSettings);
            this.conditionIds = conditionIds;
        }


        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE) && ctx.getMarket().hasCondition(conditionId)) {
                    // Suppress water surface without actually suppressing it
                    // Actually suppressing it causes aquaculture to produce no food
                    ctx.getMarket().getHazard().modifyFlat(id, -0.25f, effectSource);
                } else {
                    ctx.getMarket().suppressCondition(conditionId);
                }
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE) && ctx.getMarket().hasCondition(conditionId)) {
                    ctx.getMarket().getHazard().unmodifyFlat(id);
                } else {
                    ctx.getMarket().unsuppressCondition(conditionId);
                }
            }
        }

        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
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
                ret.add(new BoggledCommonIndustry.TooltipData("           (none)", new ArrayList<>(asList(Misc.getGrayColor())), new ArrayList<>(asList("(none)"))));
            }
            return ret;
        }
    }

    public static class ModifyGroundDefense extends Modifier {
        public ModifyGroundDefense(String id, String[] enableSettings, String modType, float value) {
            super(id, enableSettings, modType, value);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).applyMods(createModifier(ctx.getMarket(), effectSource));
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            ret.add(createTooltipData(industry.getMarket(), "Ground defenses", "",mode));
            return ret;
        }
    }

    public static class AddCondition extends IndustryEffect {
        String conditionId;
        public AddCondition(String id, String[] enableSettings, String conditionId) {
            super(id, enableSettings);
            this.conditionId = conditionId;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            boggledTools.addCondition(ctx.getMarket(), conditionId);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            boggledTools.removeCondition(ctx.getMarket(), conditionId);
        }
    }

    public static class IndustryEffectWithRequirement extends IndustryEffect {
        BoggledProjectRequirementsAND requirements;
        List<IndustryEffect> effects;

        public IndustryEffectWithRequirement(String id, String[] enableSettings, BoggledProjectRequirementsAND requirements, List<IndustryEffect> effects) {
            super(id, enableSettings);
            this.requirements = requirements;
            this.effects = effects;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!requirements.requirementsMet(ctx)) {
                unapplyEffectImpl(ctx);
                return;
            }
            List<String> reqList = new ArrayList<>();
            for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride req : requirements) {
                reqList.add(req.getTooltip(new HashMap<String, String>()));
            }
            String reqs = Misc.getAndJoined(reqList);
            for (IndustryEffect effect : effects) {
                effect.applyEffect(ctx, effectSource + " (" + reqs + ")");
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (IndustryEffect effect : effects) {
                effect.unapplyEffect(ctx);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addPostDemandSectionImpl(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.addPostDemandSectionImpl(industry, hasDemand, mode));
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
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            for (IndustryEffect effect : effects) {
                ret.addAll(effect.getApplyOrAppliedDesc(industry, mode));
            }
            return ret;
        }
    }

    public static class AddStellarReflectorsToMarket extends IndustryEffect {
        void createMirrorsOrShades(MarketAPI market) {
            if(boggledTools.numReflectorsInOrbit(market) >= 3)
            {
                return;
            }

            boggledTools.clearReflectorsInOrbit(market);

            //True is mirrors, false is shades
            boolean mirrorsOrShades = boggledTools.getCreateMirrorsOrShades(market);
            StarSystemAPI system = market.getStarSystem();

            ArrayList<Pair<String, String>> mirrorIdNamePairs = new ArrayList<>(Arrays.asList(
                    new Pair<>("stellar_mirror_alpha", "Stellar Mirror Alpha"),
                    new Pair<>("stellar_mirror_beta", "Stellar Mirror Beta"),
                    new Pair<>("stellar_mirror_gamma", "Stellar Mirror Gamma")
            ));

            ArrayList<Pair<String, String>> shadeIdNamePairs = new ArrayList<>(Arrays.asList(
                    new Pair<>("stellar_shade_alpha", "Stellar Shade Alpha"),
                    new Pair<>("stellar_shade_beta", "Stellar Shade Beta"),
                    new Pair<>("stellar_shade_gamma", "Stellar Shade Gamma")
            ));

            float baseAngle = market.getPrimaryEntity().getCircularOrbitAngle();
            ArrayList<Float> mirrorAnglesOrbitingStar = new ArrayList<>(Arrays.asList(
                    baseAngle - 30,
                    baseAngle,
                    baseAngle + 30
            ));

            ArrayList<Float> shadeAnglesOrbitingStar = new ArrayList<>(Arrays.asList(
                    baseAngle + 154,
                    baseAngle + 180,
                    baseAngle + 206
            ));

            ArrayList<Float> mirrorAndShadeAnglesOrbitingNotStar = new ArrayList<>(Arrays.asList(
                    0f,
                    120f,
                    240f
            ));

            float orbitRadius = market.getPrimaryEntity().getRadius() + 80f;
            float orbitDays = market.getPrimaryEntity().getCircularOrbitPeriod();
            float orbitDaysNotStar = market.getPrimaryEntity().getCircularOrbitPeriod() / 10;

            SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();

            ArrayList<Pair<String, String>> idNamePairs = mirrorsOrShades ? mirrorIdNamePairs : shadeIdNamePairs;
            String entityType = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";
            String customDescriptionId = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";
            ArrayList<Float> orbitAngles = mirrorsOrShades ? mirrorAnglesOrbitingStar : shadeAnglesOrbitingStar;
            float orbitPeriod = orbitDays;
            if (!(orbitFocus != null && orbitFocus.isStar())) {
                orbitAngles = mirrorAndShadeAnglesOrbitingNotStar;
                orbitPeriod = orbitDaysNotStar;
            }

            for (int i = 0; i < 3; ++i) {
                SectorEntityToken reflector = system.addCustomEntity(idNamePairs.get(i).one, idNamePairs.get(i).two, entityType, market.getFactionId());
                reflector.setCircularOrbitPointingDown(market.getPrimaryEntity(), orbitAngles.get(i), orbitRadius, orbitPeriod);
                reflector.setCustomDescriptionId(customDescriptionId);
            }
        }

        public AddStellarReflectorsToMarket(String id, String[] enableSettings) {
            super(id, enableSettings);
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            createMirrorsOrShades(ctx.getMarket());
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            boggledTools.clearReflectorsInOrbit(ctx.getMarket());
        }
    }

    public static class TagSubstringPowerModifyBuildCost extends IndustryEffect {
        MutableStat modifier;
        String tagSubstring;
        int tagCountDefault;
        public TagSubstringPowerModifyBuildCost(String id, String[] enableSettings, String tagSubstring, int tagCountDefault) {
            super(id, enableSettings);
            this.modifier = new MutableStat(0);
            this.tagSubstring = tagSubstring;
            this.tagCountDefault = tagCountDefault;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            int tagCount = tagCountDefault;
            for (String tag : ctx.getMarket().getTags()) {
                if (tag.contains(tagSubstring)) {
                    tagCount = Integer.parseInt(tag.substring(tagSubstring.length()));
                    break;
                }
            }
            modifier.modifyMult(id, (float) Math.pow(2, tagCount));
            ctx.getIndustryInterface().modifyBuildCost(modifier);
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getIndustryInterface().unmodifyBuildCost(id);
        }
    }

    public static class MonthlyItemProductionChance extends IndustryEffect {
        protected List<BoggledCommonIndustry.ProductionData> data;

        public MonthlyItemProductionChance(String id, String[] enableSettings, List<BoggledCommonIndustry.ProductionData> data) {
            super(id, enableSettings);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (BoggledCommonIndustry.ProductionData datum : data) {
                ctx.getIndustryInterface().addProductionData(datum);
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (BoggledCommonIndustry.ProductionData datum : data) {
                ctx.getIndustryInterface().removeProductionData(datum);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> addRightAfterDescriptionSectionImpl(BaseIndustry industry, Industry.IndustryTooltipMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            String chanceOrChances = data.size() == 1 ? "chance" : "chances";
            String text = "Base " + chanceOrChances + " of producing items:";
            ret.add(new BoggledCommonIndustry.TooltipData(text));

            for (BoggledCommonIndustry.ProductionData datum : data) {
                String chance = datum.chance.getModifiedInt() + "%";
                List<Color> highlightColors = new ArrayList<>(asList(Misc.getHighlightColor()));
                List<String> highlights = new ArrayList<>(asList(chance));
                chance += "%";

                String itemText = Global.getSettings().getCommoditySpec(datum.commodityId).getName() + ": " + chance;

                for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride tooltip : datum.requirements) {
                    String tooltipText = Misc.lcFirst(tooltip.getTooltip());
                    itemText += ", " + tooltipText;
                    highlightColors.add(Misc.getNegativeHighlightColor());
                    highlights.add(tooltipText);
                }

                ret.add(new BoggledCommonIndustry.TooltipData(itemText, highlightColors, highlights));
            }
            return ret;
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            List<Color> highlightColors = new ArrayList<>();
            List<String> highlights = new ArrayList<>();
            String text = "Improves chances of producing items: ";
            List<String> commodityAndChance = new ArrayList<>();
            for (BoggledCommonIndustry.ProductionData datum : data) {
                commodityAndChance.add(Global.getSettings().getCommoditySpec(datum.commodityId).getName() + " by " + datum.chance.getModifiedInt() + "%%");
                highlightColors.add(Misc.getHighlightColor());
                highlights.add(datum.chance.getModifiedInt() + "%");
            }
            text += Misc.getAndJoined(commodityAndChance);
            ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));
            return ret;
        }
    }

    public static class MonthlyItemProductionChanceModifier extends IndustryEffect {
        List<Pair<String, Integer>> data;
        public MonthlyItemProductionChanceModifier(String id, String[] enableSettings, List<Pair<String, Integer>> data) {
            super(id, enableSettings);
            this.data = data;
        }

        @Override
        protected void applyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (Pair<String, Integer> datum : data) {
                ctx.getIndustryInterface().modifyProductionChance(datum.one, id, datum.two);
            }
        }

        @Override
        protected void unapplyEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (Pair<String, Integer> datum : data) {
                ctx.getIndustryInterface().unmodifyProductionChance(datum.one, id);
            }
        }

        @Override
        protected List<BoggledCommonIndustry.TooltipData> getApplyOrAppliedDescImpl(BaseIndustry industry, DescriptionMode mode) {
            List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
            List<Color> highlightColors = new ArrayList<>();
            List<String> highlights = new ArrayList<>();
            String text = "Improves chances of producing items: ";
            List<String> commodityAndChance = new ArrayList<>();
            for (Pair<String, Integer> datum : data) {
                commodityAndChance.add(Global.getSettings().getCommoditySpec(datum.one).getName() + " by " + datum.two + "%%");
                highlightColors.add(Misc.getHighlightColor());
                highlights.add(datum.two + "%");
            }
            text += Misc.getAndJoined(commodityAndChance);
            ret.add(new BoggledCommonIndustry.TooltipData(text, highlightColors, highlights));
            return ret;
        }
    }
}