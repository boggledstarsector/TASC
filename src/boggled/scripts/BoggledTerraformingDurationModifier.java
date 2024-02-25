package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.combat.MutableStat;

public class BoggledTerraformingDurationModifier {
    public abstract static class TerraformingDurationModifier {
        String id;
        String[] enableSettings;
        protected abstract MutableStat getDurationModifierImpl(BoggledTerraformingRequirement.RequirementContext ctx);

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public TerraformingDurationModifier(String id, String[] enableSettings) {
            this.id = id;
            this.enableSettings = enableSettings;
        }

        public MutableStat getDurationModifier(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!isEnabled()) {
                return new MutableStat(0);
            }
            return getDurationModifierImpl(ctx);
        }
    }

    public static class PlanetSize extends TerraformingDurationModifier {
        public PlanetSize(String id, String[] enableSettings) {
            super(id, enableSettings);
        }

        @Override
        public MutableStat getDurationModifierImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MutableStat ret = new MutableStat(0);
            ret.modifyFlat("PlanetSize", ctx.getPlanet().getRadius());
            return ret;
        }
    }

    public static abstract class SettingModifier extends TerraformingDurationModifier {
        String settingId;
        public SettingModifier(String id, String[] enableSettings, String settingId) {
            super(id, enableSettings);
            this.settingId = settingId;
        }
    }

    public static class DurationSettingModifier extends SettingModifier {
        public DurationSettingModifier(String id, String[] enableSettings, String settingId) {
            super(id, enableSettings, settingId);
        }
        @Override
        public MutableStat getDurationModifierImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledTerraformingProject project = ctx.getProject();
            if (project == null) {
                return new MutableStat(0);
            }
            int requestedDuration = boggledTools.getIntSetting(settingId);
            int baseDuration = project.getBaseProjectDuration();
            MutableStat ret = new MutableStat(0);
            ret.modifyFlat(settingId, requestedDuration - baseDuration);
            return ret;
        }
    }
}
