package data.scripts;

import data.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BoggledProjectRequirementsAND implements Iterable<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> {
    public static class RequirementWithTooltipOverride {
        private final BoggledProjectRequirementsOR requirements;
        private final String tooltipOverride;

        public RequirementWithTooltipOverride(BoggledProjectRequirementsOR requirements, String tooltipOverride) {
            this.requirements = requirements;
            this.tooltipOverride = tooltipOverride;
        }

        public boolean checkRequirement(BoggledTerraformingRequirement.RequirementContext ctx) {
            return requirements.checkRequirement(ctx);
        }

        public String getTooltip() {
            String ret = tooltipOverride;
            if (tooltipOverride.isEmpty()) {
                ret = requirements.getTooltip();
            }
            return ret;
        }
        public String getTooltip(Map<String, String> tokenReplacements) {
            String ret = getTooltip();
            requirements.addTokenReplacements(tokenReplacements);
            ret = boggledTools.doTokenReplacement(ret, tokenReplacements);
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
