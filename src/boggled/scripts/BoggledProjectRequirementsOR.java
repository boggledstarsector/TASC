package boggled.scripts;

import com.fs.starfarer.api.Global;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

public class BoggledProjectRequirementsOR {
    private final String requirementId;
    private final BoggledCommonIndustry.TooltipData requirementTooltip;
    private final boolean invertAll;
    private final ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements;

    public BoggledProjectRequirementsOR(String requirementId, BoggledCommonIndustry.TooltipData requirementTooltip, boolean invertAll, ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements) {
        this.requirementId = requirementId;
        this.requirementTooltip = requirementTooltip;
        this.invertAll = invertAll;
        this.terraformingRequirements = terraformingRequirements;
    }

    public void addRemoveProjectRequirement(ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> add, String[] remove) {
        Logger log = Global.getLogger(BoggledProjectRequirementsOR.class);
        for (String r : remove) {
            for (int i = 0; i < terraformingRequirements.size(); ++i) {
                BoggledTerraformingRequirement.TerraformingRequirement terraformingReq = terraformingRequirements.get(i);
                if (r.equals(terraformingReq.getRequirementId())) {
                    log.info("Terraforming requirements " + requirementId + " removing requirement " + r);
                    terraformingRequirements.remove(i);
                    break;
                }
            }
        }

        terraformingRequirements.addAll(add);
    }

    public final BoggledCommonIndustry.TooltipData getTooltip() {
        return requirementTooltip;
    }

    public final void addTokenReplacements(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            terraformingRequirement.addTokenReplacements(ctx, tokenReplacements);
        }
    }

    public final String getRequirementId() {
        return requirementId;
    }

    public final boolean checkRequirement(BoggledTerraformingRequirement.RequirementContext ctx) {
        boolean requirementsMet = false;
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            requirementsMet = requirementsMet || terraformingRequirement.checkRequirement(ctx);
        }
        if (invertAll) {
            requirementsMet = !requirementsMet;
        }
        return requirementsMet;
    }
}
