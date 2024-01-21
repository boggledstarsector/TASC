package boggled.scripts;

import com.fs.starfarer.api.combat.MutableStat;

public class BoggledTerraformingDurationModifier {
    public interface TerraformingDurationModifier {
        MutableStat getDurationModifier(BoggledTerraformingRequirement.RequirementContext ctx);
    }

    public static class PlanetSize implements TerraformingDurationModifier {
        @Override
        public MutableStat getDurationModifier(BoggledTerraformingRequirement.RequirementContext ctx) {
            MutableStat ret = new MutableStat(0);
            ret.modifyFlat("PlanetSize", ctx.getPlanet().getRadius());
            return ret;
        }
    }
}
