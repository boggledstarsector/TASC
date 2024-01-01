package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BoggledProjectRequirementsOR {
    private final String requirementId;
    private final String requirementTooltip;
    private final boolean invertAll;
    private final ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements;

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

    public BoggledProjectRequirementsOR(String requirementId, String requirementTooltip, boolean invertAll, ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirements) {
        this.requirementId = requirementId;
        this.requirementTooltip = requirementTooltip;
        this.invertAll = invertAll;
        this.terraformingRequirements = terraformingRequirements;
    }

    public final String getTooltip() {
        return requirementTooltip;
    }

    public final void addTokenReplacements(LinkedHashMap<String, String> tokenReplacements) {
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            terraformingRequirement.addTokenReplacements(tokenReplacements);
        }
    }

    public final String getRequirementId() {
        return requirementId;
    }

    public final boolean checkRequirement(MarketAPI market) {
        boolean requirementsMet = false;
        for (BoggledTerraformingRequirement.TerraformingRequirement terraformingRequirement : terraformingRequirements) {
            requirementsMet = requirementsMet || terraformingRequirement.checkRequirement(market);
        }
        if (invertAll) {
            requirementsMet = !requirementsMet;
        }
        return requirementsMet;
    }
}
