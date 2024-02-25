package boggled.campaign.econ.abilities;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import boggled.scripts.*;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import boggled.campaign.econ.boggledTools;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class BoggledBaseAbility extends BaseDurationAbility {
    String projectId;
    String[] enableSettings;
    BoggledTerraformingProject project;

    BoggledTerraformingRequirement.RequirementContext ctx;

    private void setFromThat(BoggledBaseAbility that) {
        this.projectId = that.projectId;
        this.enableSettings = that.enableSettings;
        this.project = that.project;
    }

    public BoggledBaseAbility() {
        super();
    }

    public BoggledBaseAbility(String id, String[] enableSettings, BoggledTerraformingProject project) {
        super();
        this.projectId = project.getId();
        this.enableSettings = enableSettings;
        this.project = project;
    }

    public void init(String id, SectorEntityToken entity) {
        super.init(id, entity);

        if (entity instanceof CampaignFleetAPI) {
            this.ctx = new BoggledTerraformingRequirement.RequirementContext((CampaignFleetAPI) entity);
        }
        BoggledBaseAbility that = boggledTools.getAbility(id);
        assert that != null;
        setFromThat(that);
    }

    public Object readResolve() {
        super.readResolve();
        BoggledTascPlugin.loadSettingsFromJSON();
        this.project = boggledTools.getProject(this.projectId);
        return this;
    }

    @Override
    protected void activateImpl() {
        project.finishProject(ctx, project.getProjectTooltip());
    }

    @Override
    protected void applyEffect(float amount, float level) {

    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
        ctx.updatePlanet();
        if (!project.requirementsMet(ctx)) {
            return false;
        }

        return !isOnCooldown() && disableFrames <= 0;
    }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltip(tooltip, expanded);
        float pad = 4f;
        float space = 10f;

        ctx.updatePlanet();
        String projectTooltip = project.getProjectTooltip();

        tooltip.addTitle(getSpec().getName());
        tooltip.addPara(projectTooltip, pad);

        Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara = project.getEffectTooltipInfo(ctx);
        for (Map.Entry<String, BoggledTerraformingProjectEffect.EffectTooltipPara> entry : effectTypeToPara.entrySet()) {
            String infixAnd = Misc.getAndJoined(entry.getValue().infix.toArray(new String[0]));
            tooltip.addPara(entry.getValue().prefix + infixAnd + entry.getValue().suffix, pad, entry.getValue().highlightColors.toArray(new Color[0]), entry.getValue().highlights.toArray(new String[0]));
        }

        Map<String, String> tokenReplacements = boggledTools.getTokenReplacements(ctx);
        boolean first = true;
        for (BoggledProjectRequirementsAND.RequirementAndThen req : project.getRequirements()) {
            if (req.checkRequirement(ctx)) {
                continue;
            }

            List<BoggledCommonIndustry.TooltipData> tooltips = req.getTooltipFailedRequirements(ctx, tokenReplacements);
            if (tooltips.isEmpty()) {
                continue;
            }

            if (first) {
                tooltip.addSpacer(space);
                first = false;
            }
            for (BoggledCommonIndustry.TooltipData tt : tooltips) {
                if (tt.text.isEmpty()) {
                    continue;
                }
                tooltip.addPara(tt.text, Misc.getNegativeHighlightColor(), pad);
            }
        }
    }
}
