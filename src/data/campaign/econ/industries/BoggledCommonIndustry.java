package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BoggledCommonIndustry {

    private String industry;
    private String[] requiredOptions;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitable;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitableHidden;

    BoggledCommonIndustry(JSONObject data, String industry) throws JSONException {
        this.industry = industry;
        this.requiredOptions = data.getString("required_options").split(boggledTools.csvOptionSeparator);

        this.requirementsSuitable = boggledTools.getRequirementsSuitable(data, "requirement_suitable", industry);
        this.requirementsSuitableHidden = boggledTools.getRequirementsSuitable(data, "requirement_suitable_hidden", industry);
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public boolean marketSuitable(MarketAPI market, ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirements) {
        for (Pair<boggledTools.TerraformingRequirements, String> terraformingRequirements : requirements) {
            if (!terraformingRequirements.one.checkRequirement(market)) {
                return false;
            }
        }
        return true;
    }

    public boolean marketSuitableBoth(MarketAPI market) {
        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public MarketAPI getFocusMarketOrMarket(MarketAPI market) {
        MarketAPI ret = market.getPrimaryEntity().getOrbitFocus().getMarket();
        if (ret == null) {
            return market;
        }
        return ret;
    }

    /*
    These three are the main reason for this class
    Throw an instance of this on a type and just delegate to it for handling these three BaseIndustry functions
     */
    public boolean isAvailableToBuild(MarketAPI market) {
        if (!boggledTools.optionsAllowThis(requiredOptions)) {
            return false;
        }

        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public boolean showWhenUnavailable(MarketAPI market) {
        if (!boggledTools.optionsAllowThis(requiredOptions)) {
            return false;
        }

        return marketSuitable(market, requirementsSuitableHidden);
    }

    public String getUnavailableReason(MarketAPI market, LinkedHashMap<String, String> tokenReplacements) {
        return boggledTools.getUnavailableReason(requirementsSuitable, requirementsSuitableHidden, industry, market, tokenReplacements, requiredOptions);
    }
}
