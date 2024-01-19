package boggled.scripts;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class BoggledProjectRequirementsAND implements Iterable<BoggledProjectRequirementsAND.RequirementAndThen> {
    public static class RequirementAndThen {
        BoggledProjectRequirementsOR requirement;
        BoggledProjectRequirementsAND andThen;

        public RequirementAndThen(BoggledProjectRequirementsOR requirement, BoggledProjectRequirementsAND andThen) {
            this.requirement = requirement;
            this.andThen = andThen;
        }

        public boolean checkRequirementDontCascade(BoggledTerraformingRequirement.RequirementContext ctx) {
            return requirement.checkRequirement(ctx);
        }

        public boolean checkRequirement(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!checkRequirementDontCascade(ctx)) {
                return false;
            }
            if (andThen != null) {
                return andThen.requirementsMet(ctx);
            }
            return true;
        }

        public List<BoggledCommonIndustry.TooltipData> getTooltip(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
            requirement.addTokenReplacements(ctx, tokenReplacements);
            if (checkRequirementDontCascade(ctx) && andThen != null) {
                return andThen.getTooltip(ctx, tokenReplacements);
            }
            return new ArrayList<>(asList(requirement.getTooltip(ctx, tokenReplacements)));
        }
    }
    private final List<RequirementAndThen> requirements;

    public BoggledProjectRequirementsAND() {
        this.requirements = new ArrayList<>();
    }

    public BoggledProjectRequirementsAND(List<RequirementAndThen> requirements) {
        this.requirements = requirements;
    }

    public int size() { return requirements.size(); }

    @NotNull
    @Override
    public Iterator<RequirementAndThen> iterator() {
        return requirements.iterator();
    }

    public List<BoggledCommonIndustry.TooltipData> getTooltip(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        List<BoggledCommonIndustry.TooltipData> ret = new ArrayList<>();
        for (RequirementAndThen req : requirements) {
            ret.addAll(req.getTooltip(ctx, tokenReplacements));
        }
        return ret;
    }

    public boolean requirementsMetDontCascade(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (RequirementAndThen req : requirements) {
            if (!req.requirement.checkRequirement(ctx)) {
                return false;
            }
        }
        return true;
    }

    public boolean requirementsMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (RequirementAndThen req : requirements) {
            if (!req.requirement.checkRequirement(ctx)) {
                return false;
            }
            if (req.andThen != null && !req.andThen.requirementsMet(ctx)) {
                return false;
            }
        }
        return true;
    }
}
