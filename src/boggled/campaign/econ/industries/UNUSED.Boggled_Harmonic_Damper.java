package boggled.campaign.econ.industries;

public class Boggled_Harmonic_Damper extends BoggledBaseIndustry {
    public Boggled_Harmonic_Damper() {
        super();
    }

//    @Override
//    public void apply() {
//        super.apply(true);
//        thisIndustry.apply(this, this);
//
////        if(isFunctional())
////        {
////            for (String cid : SUPPRESSED_CONDITIONS)
////            {
////                market.suppressCondition(cid);
////            }
////        }
//
//        if (this.aiCoreId == null)
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_ai_bonus");
//        }
//        else if (this.aiCoreId.equals(Commodities.GAMMA_CORE))
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", GAMMA_DEFENSE_BONUS, "Gamma core (Harmonic damper)");
//        }
//        else if (this.aiCoreId.equals(Commodities.BETA_CORE))
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", BETA_DEFENSE_BONUS, "Beta core (Harmonic damper)");
//        }
//        else if (this.aiCoreId.equals(Commodities.ALPHA_CORE))
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", ALPHA_DEFENSE_BONUS, "Alpha core (Harmonic damper)");
//        }
//    }

//    @Override
//    public void unapply() {
////        for (String cid : SUPPRESSED_CONDITIONS)
////        {
////            market.unsuppressCondition(cid);
////        }
//
//        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_ai_bonus");
//        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_improve_bonus");
//
//        super.unapply();
//        thisIndustry.unapply(this, this);
//
//    }

//    @Override
//    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
//        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
//    }
//
//    @Override
//    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
//        return true;
//    }
//
//    @Override
//    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
//        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
//
//        float opad = 10.0F;
//
//        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED ||!isFunctional())
//        {
//            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
//            int numCondsCountered = 0;
//            for (String id : SUPPRESSED_CONDITIONS)
//            {
//                if(this.market.hasCondition(id))
//                {
//                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
//                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
//                    numCondsCountered++;
//                }
//            }
//
//            if(numCondsCountered == 0)
//            {
//                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
//            }
//        }
//
//        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional())
//        {
//            tooltip.addPara("Countering the effects of:", opad);
//            int numCondsCountered = 0;
//            for (String id : SUPPRESSED_CONDITIONS)
//            {
//                if(this.market.hasCondition(id))
//                {
//                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
//                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
//                    numCondsCountered++;
//                }
//            }
//
//            if(numCondsCountered == 0)
//            {
//                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
//            }
//        }
//    }

//    @Override
//    public float getBasePatherInterest() {
//        return 0;
//    }

//    public static float IMPROVE_DEFENSE_BONUS = 1.25f;
//
//    public static float GAMMA_DEFENSE_BONUS = 1.25f;
//    public static float BETA_DEFENSE_BONUS = 1.50f;
//    public static float ALPHA_DEFENSE_BONUS = 2.00f;

//    @Override
//    public boolean canBeDisrupted() {
//        return true;
//    }

//    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
//    static
//    {
//        SUPPRESSED_CONDITIONS.add(Conditions.TECTONIC_ACTIVITY);
//        SUPPRESSED_CONDITIONS.add(Conditions.EXTREME_TECTONIC_ACTIVITY);
//    }

//    private void addAICoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType, float defenseBonus) {
//        float opad = 10.0F;
//        Color highlight = Misc.getHighlightColor();
//        String pre = coreType + "-level AI core currently assigned. ";
//        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
//            pre = coreType + "-level AI core. ";
//        }
//
//        String str = Strings.X + defenseBonus;
//
//        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
//            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
//            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
//            text.addPara(pre + "Increases ground defenses by %s.", 0.0F, highlight, str);
//            tooltip.addImageWithText(opad);
//        } else {
//            tooltip.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
//        }
//    }
//
//    @Override
//    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
//        addAICoreDescription(tooltip, mode, "Alpha", ALPHA_DEFENSE_BONUS);
//    }
//
//    @Override
//    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
//        addAICoreDescription(tooltip, mode, "Beta", BETA_DEFENSE_BONUS);
//    }
//
//    @Override
//    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
//        addAICoreDescription(tooltip, mode, "Gamma", GAMMA_DEFENSE_BONUS);
//    }
//
//    @Override
//    public boolean canImprove() {
//        return true;
//    }
//
//    @Override
//    protected void applyImproveModifiers()
//    {
//        if (isImproved())
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_improve_bonus", IMPROVE_DEFENSE_BONUS, "Improvements (Harmonic damper)");
//        }
//        else
//        {
//            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_improve_bonus");
//        }
//    }
//
//    @Override
//    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
//        float opad = 10f;
//        Color highlight = Misc.getHighlightColor();
//
//        float a = IMPROVE_DEFENSE_BONUS;
//        String str = Strings.X + (a);
//
//        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
//            info.addPara("Ground defenses increased by %s.", 0f, highlight, str);
//        } else {
//            info.addPara("Increases ground defenses by %s.", 0f, highlight, str);
//        }
//
//        info.addSpacer(opad);
//        super.addImproveDesc(info, mode);
//    }
}

