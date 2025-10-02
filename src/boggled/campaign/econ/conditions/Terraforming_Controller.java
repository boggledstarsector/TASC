
package boggled.campaign.econ.conditions;

import boggled.terraforming.BoggledBaseTerraformingProject;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class Terraforming_Controller extends BaseMarketConditionPlugin
{
    // This class is used purely to store and retrieve the current ongoing terraforming project at a given market.
    private BoggledBaseTerraformingProject currentProject = null;

    public BoggledBaseTerraformingProject getCurrentProject()
    {
        return this.currentProject;
    }

    public void setCurrentProject(BoggledBaseTerraformingProject project)
    {
        this.currentProject = project;
    }
    @Override
    public boolean isTransient() { return false; }

    @Override
    public boolean showIcon() { return false; }
}
