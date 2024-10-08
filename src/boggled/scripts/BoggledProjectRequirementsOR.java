package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static boggled.scripts.BoggledTerraformingRequirement.pass;

public class BoggledProjectRequirementsOR {
    private final String requirementId;
    private final BoggledCommonIndustry.TooltipData requirementTooltip;
    private final boolean invertAll;
    private final List<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements;

    public BoggledProjectRequirementsOR(String requirementId, BoggledCommonIndustry.TooltipData requirementTooltip, boolean invertAll, List<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements) {
        this.requirementId = requirementId;
        this.requirementTooltip = requirementTooltip;
        this.invertAll = invertAll;
        this.terraformingRequirements = terraformingRequirements;
    }

    public void addRemoveProjectRequirement(List<BoggledTerraformingRequirement.TerraformingRequirement> add, String[] remove) {
        Logger log = Global.getLogger(BoggledProjectRequirementsOR.class);
        for (String r : remove) {
            for (int i = 0; i < terraformingRequirements.size(); ++i) {
                BoggledTerraformingRequirement.TerraformingRequirement terraformingReq = terraformingRequirements.get(i);
                if (r.equals(terraformingReq.getId())) {
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

    public final BoggledCommonIndustry.TooltipData getTooltip(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        int numRequirementsSkipped = 0;
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            if (!terraformingRequirement.isEnabled()) {
                numRequirementsSkipped++;
            }
        }
        if (numRequirementsSkipped != 0 && numRequirementsSkipped == terraformingRequirements.size()) {
            return new BoggledCommonIndustry.TooltipData("");
        }
        String replaced = boggledTools.doTokenAndFormatReplacement(requirementTooltip.text, tokenReplacements);
        List<String> highlights = new ArrayList<>(requirementTooltip.highlights.size());
        for (String highlight : requirementTooltip.highlights) {
            highlights.add(boggledTools.doTokenReplacement(highlight, tokenReplacements));
        }
        return new BoggledCommonIndustry.TooltipData(replaced, requirementTooltip.highlightColors, highlights);
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
        int numRequirementsSkipped = 0;
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            if (!terraformingRequirement.isEnabled()) {
                // Ignore a requirement that is disabled
                numRequirementsSkipped++;
                continue;
            }
            requirementsMet = requirementsMet || pass(terraformingRequirement.checkRequirement(ctx));
        }
        if (numRequirementsSkipped != 0 && numRequirementsSkipped == terraformingRequirements.size()) {
            // If all the requirements are disabled via settings, ignore this requirement
            return true;
        }
        if (invertAll) {
            requirementsMet = !requirementsMet;
        }
        return requirementsMet;
    }
}
