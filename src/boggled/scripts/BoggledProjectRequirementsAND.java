package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BoggledProjectRequirementsAND implements Iterable<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> {
    public static class RequirementWithTooltipOverride {
        private final BoggledProjectRequirementsOR requirements;
        private final BoggledCommonIndustry.TooltipData tooltipOverride;

        public RequirementWithTooltipOverride(BoggledProjectRequirementsOR requirements, BoggledCommonIndustry.TooltipData tooltipOverride) {
            this.requirements = requirements;
            this.tooltipOverride = tooltipOverride;
        }

        public boolean checkRequirement(BoggledTerraformingRequirement.RequirementContext ctx) {
            return requirements.checkRequirement(ctx);
        }

        public BoggledCommonIndustry.TooltipData getTooltip() {
            BoggledCommonIndustry.TooltipData ret;
            if (tooltipOverride.text.isEmpty()) {
                ret = requirements.getTooltip();
            } else {
                ret = tooltipOverride;
            }
            return ret;
        }

        public BoggledCommonIndustry.TooltipData getTooltip(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
            BoggledCommonIndustry.TooltipData ret = getTooltip();
            requirements.addTokenReplacements(ctx, tokenReplacements);
            String newText = boggledTools.doTokenReplacement(ret.text, tokenReplacements);
            List<String> newHighlightText = new ArrayList<>();
            for (String highlight : ret.highlights) {
                newHighlightText.add(boggledTools.doTokenReplacement(highlight, tokenReplacements));
            }
            ret = new BoggledCommonIndustry.TooltipData(newText, ret.highlightColors, newHighlightText);
            return ret;
        }
    }

    private final List<RequirementWithTooltipOverride> requirementsOR;

    public BoggledProjectRequirementsAND(List<RequirementWithTooltipOverride> requirementsOR) {
        this.requirementsOR = requirementsOR;
    }

    @NotNull
    @Override
    public Iterator<RequirementWithTooltipOverride> iterator() {
        return requirementsOR.iterator();
    }

    public boolean add(RequirementWithTooltipOverride req) {
        return requirementsOR.add(req);
    }

    public boolean requirementsMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride req : requirementsOR) {
            if (!req.checkRequirement(ctx)) {
                return false;
            }
        }
        return true;
    }
}
