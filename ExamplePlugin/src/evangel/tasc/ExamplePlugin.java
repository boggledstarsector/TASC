package evangel.tasc;

import com.fs.starfarer.api.BaseModPlugin;
import boggled.campaign.econ.boggledTools;
import boggled.scripts.BoggledTerraformingRequirement;
import boggled.scripts.BoggledTerraformingRequirementFactory;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.json.JSONException;
import org.json.JSONObject;

public final class ExamplePlugin extends BaseModPlugin {
    static class ExampleAOTDResearchRequirement extends BoggledTerraformingRequirement.TerraformingRequirement {
        String researchId;
        protected ExampleAOTDResearchRequirement(String id, String[] enableSettings, boolean invert, String researchId) {
            super(id, enableSettings, invert);
            this.researchId = researchId;
        }

        @Override
        protected boolean checkRequirementImpl(BoggledTerraformingRequirement.RequirementContext requirementContext) {
            return AoTDMainResearchManager.getInstance().isResearchedForPlayer(researchId);
        }
    }

    static class ExampleAOTDResearchRequirementFactory implements BoggledTerraformingRequirementFactory.TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String researchId = jsonData.getString("research_id");
            return new ExampleAOTDResearchRequirement(id, enableSettings, invert, researchId);
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        boggledTools.addTerraformingRequirementFactory("ExampleAOTDResearchRequirement", new ExampleAOTDResearchRequirementFactory());
    }
}
